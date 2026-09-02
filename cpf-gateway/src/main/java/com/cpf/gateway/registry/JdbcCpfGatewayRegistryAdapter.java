package com.cpf.gateway.registry;

import com.cpf.gateway.api.*;
import com.cpf.platform.operations.api.runtime.CpfRuntimePolicyDistributionPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import java.sql.Timestamp;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Gateway Control Plane의 JDBC Adapter입니다.
 *
 * <p>Vendor 전용 UPSERT/RETURNING/LIMIT를 사용하지 않고 CAS 기반 Select→Insert/Update 흐름으로
 * Oracle·PostgreSQL·MariaDB에서 동일한 동작을 제공합니다.</p>
 */
// @ConditionalOnBean 은 auto-configuration 에서만 신뢰할 수 있다. component scan 으로
// 등록되는 이 Adapter 는 auto-configuration 이 대상 Bean 을 정의하기 전에 조건이 평가되어
// 항상 false 가 된다. Gateway 는 CPF Platform DB 를 소유하는 DataSource Owner 이고 이
// Adapter 는 해당 Port 의 유일한 구현체이므로, 조건부가 아니라 항상 등록되어야 한다.
// 의존 Bean 이 없으면 조용히 사라지는 대신 기동이 명확한 원인으로 실패해야 한다.
@Repository
public class JdbcCpfGatewayRegistryAdapter implements CpfGatewayRegistryPort {
    private final JdbcTemplate jdbc;
    private final CpfRuntimePolicyDistributionPort distribution;

    /** JdbcCpfGatewayRegistryAdapter 작업을 CPF 표준 계약에 따라 수행한다. */
    public JdbcCpfGatewayRegistryAdapter(DataSource dataSource) {
        this(dataSource, (CpfRuntimePolicyDistributionPort) null);
    }

    // Gateway Runtime 에는 cpfCommonDataSource 와 cpfPlatformDataSource 가 함께 존재한다.
    // 타입만으로 주입하면 후보가 둘이라 기동이 실패한다. Gateway 는 CPF Platform DB Role
    // Owner 이므로 Role 을 명시해 해석한다.
    @Autowired
    public JdbcCpfGatewayRegistryAdapter(
            CpfDataSourceRegistry dataSources, ObjectProvider<CpfRuntimePolicyDistributionPort> distributionProvider) {
        this(dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB), distributionProvider == null ? null : distributionProvider.getIfAvailable());
    }

    private JdbcCpfGatewayRegistryAdapter(
            DataSource dataSource, CpfRuntimePolicyDistributionPort distribution) {
        this.jdbc = new JdbcTemplate(Objects.requireNonNull(dataSource, "dataSource"));
        this.distribution = distribution;
    }

    @Override
    public List<ServerGroup> findServerGroups(String environmentCode, String serviceId, String status, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT g.server_group_id,g.group_name,g.environment_code,g.service_id,g.endpoint_code,
                       g.target_protocol,g.load_balance_policy,g.hash_key_source,g.health_policy_id,
                       g.failover_group_id,g.group_status,g.direct_allowed_yn,g.row_version,g.updated_at,
                       (SELECT COUNT(*) FROM GW_SERVER_GROUP_MEMBER m
                         WHERE m.server_group_id=g.server_group_id AND m.enabled_yn='Y') member_count
                  FROM GW_SERVER_GROUP g WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        append(sql,args," AND g.environment_code=?",environmentCode);
        append(sql,args," AND g.service_id=?",serviceId);
        append(sql,args," AND g.group_status=?",status);
        sql.append(" ORDER BY g.environment_code,g.service_id,g.server_group_id");
        return queryLimited(sql.toString(), args, limit, (rs,n) -> new ServerGroup(
                rs.getString("server_group_id"),rs.getString("group_name"),rs.getString("environment_code"),
                rs.getString("service_id"),rs.getString("endpoint_code"),protocol(rs.getString("target_protocol")),
                loadBalance(rs.getString("load_balance_policy")),rs.getString("hash_key_source"),
                rs.getString("health_policy_id"),rs.getString("failover_group_id"),rs.getString("group_status"),
                yes(rs.getString("direct_allowed_yn")),rs.getInt("member_count"),rs.getLong("row_version"),
                offset(rs.getTimestamp("updated_at"))));
    }

    @Override
    public List<GroupMember> findMembers(String serverGroupId) {
        return jdbc.query("""
                SELECT m.server_group_id,m.instance_id,m.weight,m.priority_no,m.canary_percent,m.enabled_yn,m.effective_status,
                       m.fencing_token,m.updated_at
                  FROM GW_SERVER_GROUP_MEMBER m
                 WHERE m.server_group_id=? ORDER BY m.priority_no,m.instance_id
                """, (rs,n) -> new GroupMember(rs.getString("server_group_id"),rs.getString("instance_id"),
                rs.getInt("weight"),rs.getInt("priority_no"),rs.getInt("canary_percent"),yes(rs.getString("enabled_yn")),
                health(rs.getString("effective_status")),rs.getLong("fencing_token"),offset(rs.getTimestamp("updated_at"))),
                serverGroupId);
    }

    @Override
    public List<GatewayBinding> findBindings(String environmentCode, String routeId, String status, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT binding_id,route_id,environment_code,host_pattern,path_pattern,target_path,http_method,api_version,
                       ingress_protocol,target_protocol,service_id,server_group_id,route_version,tls_policy_id,
                       authentication_policy_id,authorization_policy_id,header_policy_id,rate_limit_policy_id,
                       health_policy_id,connect_timeout_ms,response_timeout_ms,overall_timeout_ms,max_retry_count,
                       idempotent_yn,failover_group_id,binding_status,gateway_allowed_yn,direct_allowed_yn,approval_id,
                       effective_from,effective_to,binding_checksum,row_version,updated_at
                  FROM GW_BINDING WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        append(sql,args," AND environment_code=?",environmentCode);
        append(sql,args," AND route_id=?",routeId);
        append(sql,args," AND binding_status=?",status);
        sql.append(" ORDER BY updated_at DESC,binding_id");
        return queryLimited(sql.toString(),args,limit,(rs,n)->new GatewayBinding(
                rs.getString("binding_id"),rs.getString("route_id"),rs.getString("environment_code"),
                rs.getString("host_pattern"),rs.getString("path_pattern"),rs.getString("target_path"),rs.getString("http_method"),rs.getString("api_version"),
                protocol(rs.getString("ingress_protocol")),protocol(rs.getString("target_protocol")),rs.getString("service_id"),
                rs.getString("server_group_id"),rs.getString("route_version"),rs.getString("tls_policy_id"),
                rs.getString("authentication_policy_id"),rs.getString("authorization_policy_id"),rs.getString("header_policy_id"),
                rs.getString("rate_limit_policy_id"),rs.getString("health_policy_id"),rs.getInt("connect_timeout_ms"),
                rs.getInt("response_timeout_ms"),rs.getInt("overall_timeout_ms"),rs.getInt("max_retry_count"),
                yes(rs.getString("idempotent_yn")),rs.getString("failover_group_id"),rs.getString("binding_status"),
                yes(rs.getString("gateway_allowed_yn")),yes(rs.getString("direct_allowed_yn")),rs.getString("approval_id"),
                offset(rs.getTimestamp("effective_from")),offset(rs.getTimestamp("effective_to")),rs.getString("binding_checksum"),
                rs.getLong("row_version"),offset(rs.getTimestamp("updated_at"))));
    }

    @Override
    public List<ApplyStatus> findApplyStatuses(String bindingId, int limit) {
        return queryLimited("""
                SELECT binding_id,gateway_instance_id,expected_version,applied_version,apply_status,error_code,
                       error_message,acknowledged_at,last_seen_at
                  FROM GW_APPLY_STATUS WHERE binding_id=? ORDER BY last_seen_at DESC
                """,List.of(bindingId),limit,(rs,n)->new ApplyStatus(rs.getString("binding_id"),
                rs.getString("gateway_instance_id"),rs.getString("expected_version"),rs.getString("applied_version"),
                rs.getString("apply_status"),rs.getString("error_code"),rs.getString("error_message"),
                offset(rs.getTimestamp("acknowledged_at")),offset(rs.getTimestamp("last_seen_at"))));
    }

    @Override
    public List<ConnectionTestResult> findConnectionTests(String bindingId, int limit) {
        return queryLimited("""
                SELECT test_id,binding_id,gateway_instance_id,instance_id,test_type,test_status,failure_stage,
                       duration_ms,trace_id,operation_id,tested_at,tested_by
                  FROM GW_CONNECTION_TEST WHERE binding_id=? ORDER BY tested_at DESC
                """,List.of(bindingId),limit,(rs,n)->new ConnectionTestResult(rs.getString("test_id"),
                rs.getString("binding_id"),rs.getString("gateway_instance_id"),rs.getString("instance_id"),
                rs.getString("test_type"),rs.getString("test_status"),rs.getString("failure_stage"),rs.getLong("duration_ms"),
                rs.getString("trace_id"),rs.getString("operation_id"),offset(rs.getTimestamp("tested_at")),rs.getString("tested_by")));
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public MutationResult saveServerGroup(ServerGroupCommand c) {
        validateGroup(c);
        Long current = version("GW_SERVER_GROUP","server_group_id",c.serverGroupId());
        long next;
        if (current == null) {
            requireNew(c.expectedVersion());
            jdbc.update("""
                    INSERT INTO GW_SERVER_GROUP
                    (server_group_id,group_name,environment_code,service_id,endpoint_code,target_protocol,
                     load_balance_policy,hash_key_source,health_policy_id,failover_group_id,group_status,
                     direct_allowed_yn,created_by,created_at,updated_by,updated_at,row_version)
                    VALUES (?,?,?,?,?,?,?,?,?,?,'DRAFT',?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,1)
                    """,c.serverGroupId(),c.groupName(),c.environmentCode(),c.serviceId(),c.endpointCode(),
                    c.targetProtocol().name(),c.loadBalancePolicy().name(),clean(c.hashKeySource()),clean(c.healthPolicyId()),
                    clean(c.failoverGroupId()),yn(c.directAllowed()),c.requestedBy(),c.requestedBy());
            next=1;
        } else {
            requireExpected(current,c.expectedVersion());
            int updated=jdbc.update("""
                    UPDATE GW_SERVER_GROUP SET group_name=?,environment_code=?,service_id=?,endpoint_code=?,
                           target_protocol=?,load_balance_policy=?,hash_key_source=?,health_policy_id=?,failover_group_id=?,
                           direct_allowed_yn=?,updated_by=?,updated_at=CURRENT_TIMESTAMP,row_version=row_version+1
                     WHERE server_group_id=? AND row_version=?
                    """,c.groupName(),c.environmentCode(),c.serviceId(),c.endpointCode(),c.targetProtocol().name(),
                    c.loadBalancePolicy().name(),clean(c.hashKeySource()),clean(c.healthPolicyId()),clean(c.failoverGroupId()),
                    yn(c.directAllowed()),c.requestedBy(),c.serverGroupId(),current);
            requireUpdated(updated,"Server Group version conflict"); next=current+1;
        }
        Map<String, GroupMember> existingMembers = findMembers(c.serverGroupId()).stream()
                .collect(java.util.stream.Collectors.toMap(value -> value.instanceId(), member -> member));
        Set<String> seen=new HashSet<>();
        List<MemberCommand> members = c.members() == null ? List.of() : c.members();
        for(MemberCommand member:members) {
            required(member.instanceId(),"member.instanceId");
            if(!seen.add(member.instanceId())) throw new IllegalArgumentException("Duplicate instance in group: "+member.instanceId());
            if (existingMembers.containsKey(member.instanceId())) {
                requireUpdated(jdbc.update("""
                        UPDATE GW_SERVER_GROUP_MEMBER
                           SET weight=?,priority_no=?,canary_percent=?,enabled_yn=?,updated_by=?,updated_at=CURRENT_TIMESTAMP
                         WHERE server_group_id=? AND instance_id=?
                        """,Math.max(1,member.weight()),Math.max(0,member.priority()),boundedPercent(member.canaryPercent()),yn(member.enabled()),
                        c.requestedBy(),c.serverGroupId(),member.instanceId()),
                        "Gateway member update failed: "+member.instanceId());
            } else {
                jdbc.update("""
                        INSERT INTO GW_SERVER_GROUP_MEMBER
                        (server_group_id,instance_id,weight,priority_no,canary_percent,enabled_yn,effective_status,fencing_token,
                         created_by,created_at,updated_by,updated_at)
                        VALUES (?,?,?,?,?,?,'UNKNOWN',0,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)
                        """,c.serverGroupId(),member.instanceId(),Math.max(1,member.weight()),Math.max(0,member.priority()),
                        boundedPercent(member.canaryPercent()),yn(member.enabled()),c.requestedBy(),c.requestedBy());
            }
        }
        for (GroupMember existing : existingMembers.values()) {
            if (!seen.contains(existing.instanceId()) && existing.enabled()) {
                requireUpdated(jdbc.update("""
                        UPDATE GW_SERVER_GROUP_MEMBER
                           SET enabled_yn='N',updated_by=?,updated_at=CURRENT_TIMESTAMP
                         WHERE server_group_id=? AND instance_id=?
                        """,c.requestedBy(),c.serverGroupId(),existing.instanceId()),
                        "Gateway member retire failed: "+existing.instanceId());
            }
        }
        recordEvent("GWY-GROUP-"+c.operationId(),"SERVER_GROUP_CHANGED","SERVER_GROUP",c.serverGroupId(),"SAVED",
                json("version",Long.toString(next),"memberCount",Integer.toString(members.size())));
        return mutation("SERVER_GROUP",c.serverGroupId(),"SAVED",next);
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public MutationResult saveBinding(GatewayBindingCommand c) {
        validateBinding(c);
        Long current=version("GW_BINDING","binding_id",c.bindingId());
        long next;
        CpfGatewayRoute r=c.route();
        String bindingKeyHash=bindingKeyHash(r);
        String bindingChecksum=bindingChecksum(c);
        if(current==null) {
            requireNew(c.expectedVersion());
            jdbc.update("""
                    INSERT INTO GW_BINDING
                    (binding_id,route_id,environment_code,host_pattern,path_pattern,target_path,http_method,api_version,
                     ingress_protocol,target_protocol,service_id,server_group_id,route_version,binding_key_hash,tls_policy_id,
                     authentication_policy_id,authorization_policy_id,header_policy_id,rate_limit_policy_id,
                     health_policy_id,connect_timeout_ms,response_timeout_ms,overall_timeout_ms,max_retry_count,
                     idempotent_yn,failover_group_id,gateway_allowed_yn,direct_allowed_yn,binding_status,approval_id,
                     effective_from,effective_to,binding_checksum,created_by,created_at,updated_by,updated_at,row_version)
                     VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'DRAFT',?,?,?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,1)
                    """,c.bindingId(),r.routeId(),r.environmentCode(),r.hostPattern(),r.pathPattern(),r.targetPath(),r.httpMethod(),r.apiVersion(),
                     r.ingressProtocol().name(),r.targetProtocol().name(),r.serviceId(),c.serverGroupId(),r.routeVersion(),bindingKeyHash,
                    clean(r.tlsPolicyId()),clean(r.authenticationPolicyId()),clean(r.authorizationPolicyId()),clean(r.headerPolicyId()),
                    clean(r.rateLimitPolicyId()),clean(r.healthPolicyId()),r.connectTimeoutMs(),r.responseTimeoutMs(),r.overallTimeoutMs(),
                    r.maxRetryCount(),yn(r.idempotent()),clean(r.failoverGroupId()),yn(c.gatewayAllowed()),yn(c.directAllowed()),
                    clean(c.approvalId()),timestamp(c.effectiveFrom()),timestamp(c.effectiveTo()),bindingChecksum,c.requestedBy(),c.requestedBy());
            next=1;
        } else {
            requireExpected(current,c.expectedVersion());
            int updated=jdbc.update("""
                    UPDATE GW_BINDING SET route_id=?,environment_code=?,host_pattern=?,path_pattern=?,target_path=?,http_method=?,
                           api_version=?,ingress_protocol=?,target_protocol=?,service_id=?,server_group_id=?,route_version=?,binding_key_hash=?,
                           tls_policy_id=?,authentication_policy_id=?,authorization_policy_id=?,header_policy_id=?,
                           rate_limit_policy_id=?,health_policy_id=?,connect_timeout_ms=?,response_timeout_ms=?,overall_timeout_ms=?,
                           max_retry_count=?,idempotent_yn=?,failover_group_id=?,gateway_allowed_yn=?,direct_allowed_yn=?,approval_id=?,
                           effective_from=?,effective_to=?,binding_checksum=?,updated_by=?,updated_at=CURRENT_TIMESTAMP,row_version=row_version+1
                     WHERE binding_id=? AND row_version=?
                    """,r.routeId(),r.environmentCode(),r.hostPattern(),r.pathPattern(),r.targetPath(),r.httpMethod(),r.apiVersion(),
                     r.ingressProtocol().name(),r.targetProtocol().name(),r.serviceId(),c.serverGroupId(),r.routeVersion(),bindingKeyHash,
                    clean(r.tlsPolicyId()),clean(r.authenticationPolicyId()),clean(r.authorizationPolicyId()),clean(r.headerPolicyId()),
                    clean(r.rateLimitPolicyId()),clean(r.healthPolicyId()),r.connectTimeoutMs(),r.responseTimeoutMs(),r.overallTimeoutMs(),
                    r.maxRetryCount(),yn(r.idempotent()),clean(r.failoverGroupId()),yn(c.gatewayAllowed()),yn(c.directAllowed()),
                    clean(c.approvalId()),timestamp(c.effectiveFrom()),timestamp(c.effectiveTo()),bindingChecksum,c.requestedBy(),c.bindingId(),current);
            requireUpdated(updated,"Gateway Binding version conflict"); next=current+1;
        }
        recordEvent("GWY-BINDING-"+c.operationId(),"BINDING_CHANGED","GATEWAY_BINDING",c.bindingId(),"SAVED",
                json("version",Long.toString(next),"routeVersion",r.routeVersion()));
        return mutation("GATEWAY_BINDING",c.bindingId(),"SAVED",next);
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public MutationResult changeBindingState(BindingStateCommand c) {
        String target=normalizeState(c.targetState());
        List<Map<String,Object>> rows=jdbc.queryForList("""
                SELECT binding_status,row_version,gateway_allowed_yn,tls_policy_id,authentication_policy_id,
                       authorization_policy_id,header_policy_id,rate_limit_policy_id,server_group_id,
                       effective_from,effective_to,binding_checksum,route_version
                  FROM GW_BINDING WHERE binding_id=?
                """,c.bindingId());
        if(rows.isEmpty()) throw new IllegalArgumentException("Gateway Binding not found: "+c.bindingId());
        Map<String,Object> row=rows.getFirst();
        String source=Objects.toString(row.get("binding_status"),"").toUpperCase(Locale.ROOT);
        long current=((Number)row.get("row_version")).longValue();
        requireExpected(current,c.expectedVersion());
        requireTransition(source,target);
        if(Set.of("APPROVAL_PENDING","APPROVED","ACTIVE","BLOCKED","RETIRED").contains(target))
            requireReason(c.reason());
        if("ACTIVE".equals(target)) {
            if(clean(c.approvalId()).isBlank()) throw new IllegalArgumentException("ACTIVE requires approvalId");
            if (!yes(Objects.toString(row.get("gateway_allowed_yn"),"N")))
                throw new IllegalStateException("ACTIVE requires gatewayAllowed");
            requireConfigured(row,"authentication_policy_id","ACTIVE requires authenticationPolicyId");
            requireConfigured(row,"authorization_policy_id","ACTIVE requires authorizationPolicyId");
            requireConfigured(row,"header_policy_id","ACTIVE requires headerPolicyId");
            requireConfigured(row,"rate_limit_policy_id","ACTIVE requires rateLimitPolicyId");
            OffsetDateTime from=offset((Timestamp)row.get("effective_from"));
            OffsetDateTime to=offset((Timestamp)row.get("effective_to"));
            OffsetDateTime now=OffsetDateTime.now(ZoneOffset.UTC);
            if(from!=null && now.isBefore(from)) throw new IllegalStateException("Binding is not effective yet");
            if(to!=null && !now.isBefore(to)) throw new IllegalStateException("Binding effective period has expired");
        }
        if (runtimeImpact(target) && distribution == null)
            throw new IllegalStateException("Gateway Runtime Policy Distribution Port가 없어 상태 전환을 중단합니다.");
        int updated=jdbc.update("""
                UPDATE GW_BINDING SET binding_status=?,approval_id=?,updated_by=?,updated_at=CURRENT_TIMESTAMP,
                       row_version=row_version+1 WHERE binding_id=? AND row_version=?
                """,target,clean(c.approvalId()),c.requestedBy(),c.bindingId(),current);
        requireUpdated(updated,"Gateway Binding state version conflict");
        if (runtimeImpact(target)) {
            publishBindingEvent(c.operationId(),c.bindingId(),current+1,target,
                    Objects.toString(row.get("binding_checksum"),""),Objects.toString(row.get("route_version"),""),
                    c.reason(),c.requestedBy());
        }
        recordEvent("GWY-STATE-"+c.operationId(),"BINDING_STATE_CHANGED","GATEWAY_BINDING",c.bindingId(),target,
                json("version",Long.toString(current+1),"routeVersion",Objects.toString(row.get("route_version"),"")));
        return mutation("GATEWAY_BINDING",c.bindingId(),target,current+1);
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public void deleteServerGroup(String id, DeleteCommand c) {
        requireReason(c.reason());
        long current=versionRequired("GW_SERVER_GROUP","server_group_id",id);
        requireExpected(current,c.expectedVersion());
        Integer bindings=jdbc.queryForObject(
                "SELECT COUNT(*) FROM GW_BINDING WHERE server_group_id=? AND binding_status<>'RETIRED'",
                Integer.class,id);
        if(bindings!=null&&bindings>0) throw new IllegalStateException("Active Gateway Binding references Server Group");
        requireUpdated(jdbc.update("""
                UPDATE GW_SERVER_GROUP
                   SET group_status='RETIRED',updated_by=?,updated_at=CURRENT_TIMESTAMP,row_version=row_version+1
                 WHERE server_group_id=? AND row_version=?
                """,c.requestedBy(),id,current),"Server Group version conflict");
        jdbc.update("""
                UPDATE GW_SERVER_GROUP_MEMBER
                   SET enabled_yn='N',updated_by=?,updated_at=CURRENT_TIMESTAMP
                 WHERE server_group_id=?
                """,c.requestedBy(),id);
        recordEvent("GWY-GROUP-RETIRE-"+c.operationId(),"SERVER_GROUP_RETIRED","SERVER_GROUP",id,"RETIRED",
                json("version",Long.toString(current+1)));
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public void deleteBinding(String id, DeleteCommand c) {
        required(c.operationId(),"operationId");
        requireReason(c.reason());
        required(c.requestedBy(),"requestedBy");
        List<Map<String,Object>> rows=jdbc.queryForList(
                "SELECT binding_status,row_version FROM GW_BINDING WHERE binding_id=?",id);
        if(rows.isEmpty()) throw new IllegalArgumentException("Gateway Binding not found: "+id);
        String state=Objects.toString(rows.getFirst().get("binding_status"),"").toUpperCase(Locale.ROOT);
        long current=((Number)rows.getFirst().get("row_version")).longValue();
        requireExpected(current,c.expectedVersion());
        if(Set.of("APPROVAL_PENDING","APPROVED","ACTIVE","PARTIAL","BLOCKED").contains(state))
            throw new IllegalStateException("운영 Binding 폐기는 Approval Owner의 RETIRED 상태 전환을 사용해야 합니다.");
        requireUpdated(jdbc.update("""
                UPDATE GW_BINDING
                   SET binding_status='RETIRED',gateway_allowed_yn='N',direct_allowed_yn='N',
                       effective_to=COALESCE(effective_to,CURRENT_TIMESTAMP),updated_by=?,
                       updated_at=CURRENT_TIMESTAMP,row_version=row_version+1
                 WHERE binding_id=? AND row_version=?
                """,c.requestedBy(),id,current),"Gateway Binding version conflict");
        recordEvent("GWY-BINDING-RETIRE-"+c.operationId(),"BINDING_RETIRED","GATEWAY_BINDING",id,"RETIRED",
                json("version",Long.toString(current+1)));
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public ApplyStatus acknowledge(ApplyAckCommand c) {
        int updated=jdbc.update("""
                UPDATE GW_APPLY_STATUS SET expected_version=?,applied_version=?,apply_status=?,error_code=?,
                       error_message=?,acknowledged_at=?,last_seen_at=CURRENT_TIMESTAMP
                 WHERE binding_id=? AND gateway_instance_id=?
                """,c.expectedVersion(),c.appliedVersion(),c.status(),clean(c.errorCode()),clean(c.errorMessage()),
                timestamp(c.acknowledgedAt()),c.bindingId(),c.gatewayInstanceId());
        if(updated==0) jdbc.update("""
                INSERT INTO GW_APPLY_STATUS
                (binding_id,gateway_instance_id,expected_version,applied_version,apply_status,error_code,error_message,
                 acknowledged_at,last_seen_at) VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)
                """,c.bindingId(),c.gatewayInstanceId(),c.expectedVersion(),c.appliedVersion(),c.status(),
                clean(c.errorCode()),clean(c.errorMessage()),timestamp(c.acknowledgedAt()));
        recordEvent(null,"APPLY_ACK","GATEWAY_BINDING",c.bindingId(),c.status(),
                json("gatewayInstanceId",c.gatewayInstanceId(),"expectedVersion",clean(c.expectedVersion()),
                        "appliedVersion",clean(c.appliedVersion()),"errorCode",clean(c.errorCode())));
        return new ApplyStatus(c.bindingId(),c.gatewayInstanceId(),c.expectedVersion(),c.appliedVersion(),c.status(),
                clean(c.errorCode()),clean(c.errorMessage()),c.acknowledgedAt(),OffsetDateTime.now(ZoneOffset.UTC));
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public ConnectionTestResult recordConnectionTest(ConnectionTestCommand c) {
        jdbc.update("""
                INSERT INTO GW_CONNECTION_TEST
                (test_id,binding_id,gateway_instance_id,instance_id,test_type,test_status,failure_stage,duration_ms,
                 trace_id,operation_id,tested_at,tested_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """,c.testId(),c.bindingId(),c.gatewayInstanceId(),c.instanceId(),c.testType(),c.status(),
                clean(c.failureStage()),Math.max(0,c.durationMs()),clean(c.traceId()),clean(c.operationId()),
                timestamp(c.testedAt()),c.testedBy());
        recordEvent("GWY-TEST-RESULT-"+c.testId(),"CONNECTION_TEST_RESULT","GATEWAY_BINDING",c.bindingId(),c.status(),
                json("testId",c.testId(),"operationId",clean(c.operationId()),"instanceId",clean(c.instanceId()),
                        "failureStage",clean(c.failureStage())));
        return new ConnectionTestResult(c.testId(),c.bindingId(),c.gatewayInstanceId(),c.instanceId(),c.testType(),
                c.status(),clean(c.failureStage()),Math.max(0,c.durationMs()),clean(c.traceId()),clean(c.operationId()),
                c.testedAt(),c.testedBy());
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public ConnectionTestOperation requestConnectionTest(ConnectionTestRequest c) {
        required(c.operationId(),"operationId"); required(c.bindingId(),"bindingId");
        required(c.testType(),"testType"); requireReason(c.reason()); required(c.payloadHash(),"payloadHash");
        required(c.requestedBy(),"requestedBy");
        if(c.expiresAt()==null || !c.expiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC)))
            throw new IllegalArgumentException("Connection Test expiry must be in the future");
        Integer bindingCount=jdbc.queryForObject("SELECT COUNT(*) FROM GW_BINDING WHERE binding_id=?",Integer.class,c.bindingId());
        if(bindingCount==null||bindingCount!=1) throw new IllegalArgumentException("Gateway Binding not found: "+c.bindingId());
        try {
            jdbc.update("""
                    INSERT INTO GW_CONNECTION_TEST_OPERATION(
                      operation_id,binding_id,test_type,operation_status,requested_by,request_reason,
                      request_payload_hash,expires_at,cancel_requested_yn,created_at,row_version)
                    VALUES (?,?,?,'REQUESTED',?,?,?,?, 'N',CURRENT_TIMESTAMP,1)
                    """,c.operationId(),c.bindingId(),c.testType(),c.requestedBy(),c.reason(),c.payloadHash(),timestamp(c.expiresAt()));
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch(org.springframework.dao.DuplicateKeyException duplicate) {
            List<Map<String,Object>> rows=jdbc.queryForList("""
                    SELECT binding_id,test_type,request_payload_hash FROM GW_CONNECTION_TEST_OPERATION
                     WHERE operation_id=?
                    """,c.operationId());
            if(rows.isEmpty() || !Objects.equals(rows.getFirst().get("binding_id"),c.bindingId())
                    || !Objects.equals(rows.getFirst().get("test_type"),c.testType())
                    || !Objects.equals(rows.getFirst().get("request_payload_hash"),c.payloadHash()))
                throw new IllegalStateException("operationId payload conflict: "+c.operationId(),duplicate);
        }
        ConnectionTestOperation created=operation(c.operationId());
        recordEvent("GWY-TEST-REQUEST-"+c.operationId(),"CONNECTION_TEST_REQUESTED","CONNECTION_TEST",c.operationId(),created.status(),
                json("bindingId",c.bindingId(),"testType",c.testType(),"expiresAt",String.valueOf(c.expiresAt())));
        return created;
    }

    @Override
    public ConnectionTestOperation findConnectionTestOperation(String operationId) {
        return operation(required(operationId,"operationId"));
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public ConnectionTestOperation cancelConnectionTest(ConnectionTestCancel c) {
        required(c.operationId(),"operationId");
        requireReason(c.reason());
        required(c.requestedBy(),"requestedBy");
        ConnectionTestOperation current=operation(c.operationId());
        requireExpected(current.version(),c.expectedVersion());
        if ("CANCELLED".equals(current.status())) return current;
        if (Set.of("SUCCESS","FAILED","PARTIAL","STALE").contains(current.status()))
            throw new IllegalStateException("Terminal Connection Test cannot be cancelled: "+current.status());
        int changed;
        if ("REQUESTED".equals(current.status())) {
            changed=jdbc.update("""
                    UPDATE GW_CONNECTION_TEST_OPERATION
                       SET operation_status='CANCELLED',cancel_requested_yn='Y',result_summary=?,
                           completed_at=CURRENT_TIMESTAMP,row_version=row_version+1
                     WHERE operation_id=? AND row_version=? AND operation_status='REQUESTED'
                    """,clean(c.reason()),c.operationId(),current.version());
        } else {
            changed=jdbc.update("""
                    UPDATE GW_CONNECTION_TEST_OPERATION
                       SET cancel_requested_yn='Y',result_summary=?,row_version=row_version+1
                     WHERE operation_id=? AND row_version=? AND operation_status='RUNNING'
                    """,clean(c.reason()),c.operationId(),current.version());
        }
        requireUpdated(changed,"Connection Test cancellation version/state conflict");
        ConnectionTestOperation cancelled=operation(c.operationId());
        recordEvent("GWY-TEST-CANCEL-"+c.operationId()+"-"+cancelled.version(),"CONNECTION_TEST_CANCELLED",
                "CONNECTION_TEST",c.operationId(),cancelled.status(),json("reason",c.reason()));
        return cancelled;
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public ConnectionTestOperation revalidateConnectionTest(ConnectionTestRevalidation c) {
        required(c.sourceOperationId(),"sourceOperationId");
        required(c.newOperationId(),"newOperationId");
        required(c.payloadHash(),"payloadHash");
        requireReason(c.reason());
        required(c.requestedBy(),"requestedBy");
        ConnectionTestOperation source=operation(c.sourceOperationId());
        if (!Set.of("SUCCESS","FAILED","PARTIAL","CANCELLED","STALE").contains(source.status()))
            throw new IllegalStateException("Running Connection Test cannot be revalidated: "+source.status());
        return requestConnectionTest(new ConnectionTestRequest(
                c.newOperationId(),source.bindingId(),source.testType(),c.reason(),c.payloadHash(),c.expiresAt(),c.requestedBy()));
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public List<ConnectionTestOperation> claimConnectionTests(String gatewayInstanceId,int limit) {
        required(gatewayInstanceId,"gatewayInstanceId");
        jdbc.update("""
                UPDATE GW_CONNECTION_TEST_OPERATION
                   SET operation_status='STALE',result_summary='Request expired before claim',
                       completed_at=CURRENT_TIMESTAMP,row_version=row_version+1
                 WHERE operation_status='REQUESTED' AND expires_at<=CURRENT_TIMESTAMP
                """);
        List<String> ids=queryLimited("""
                SELECT operation_id FROM GW_CONNECTION_TEST_OPERATION
                 WHERE operation_status='REQUESTED' AND cancel_requested_yn='N' AND expires_at>CURRENT_TIMESTAMP
                 ORDER BY created_at,operation_id
                """,List.of(),limit,(rs,n)->rs.getString(1));
        List<ConnectionTestOperation> claimed=new ArrayList<>();
        for(String id:ids) {
            int changed=jdbc.update("""
                    UPDATE GW_CONNECTION_TEST_OPERATION
                       SET operation_status='RUNNING',started_at=CURRENT_TIMESTAMP,row_version=row_version+1
                     WHERE operation_id=? AND operation_status='REQUESTED' AND cancel_requested_yn='N'
                    """,id);
            if(changed==1) claimed.add(operation(id));
        }
        return List.copyOf(claimed);
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public ConnectionTestOperation completeConnectionTest(ConnectionTestCompletion c) {
        String status=required(c.status(),"status").toUpperCase(Locale.ROOT);
        if(!Set.of("PARTIAL","SUCCESS","FAILED","CANCELLED","STALE").contains(status))
            throw new IllegalArgumentException("Unsupported connection test completion status: "+status);
        ConnectionTestOperation current=operation(c.operationId());
        requireExpected(current.version(),c.expectedVersion());
        requireUpdated(jdbc.update("""
                UPDATE GW_CONNECTION_TEST_OPERATION
                   SET operation_status=?,result_summary=?,completed_at=CURRENT_TIMESTAMP,row_version=row_version+1
                 WHERE operation_id=? AND row_version=? AND operation_status='RUNNING'
                """,status,clean(c.resultSummary()),c.operationId(),current.version()),
                "Connection Test operation version/state conflict");
        ConnectionTestOperation completed=operation(c.operationId());
        recordEvent("GWY-TEST-COMPLETE-"+c.operationId()+"-"+completed.version(),"CONNECTION_TEST_COMPLETED",
                "CONNECTION_TEST",c.operationId(),completed.status(),json("summary",clean(c.resultSummary())));
        return completed;
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public List<HealthProbeTarget> claimHealthProbes(String gatewayInstanceId,int limit,long leaseSeconds) {
        required(gatewayInstanceId,"gatewayInstanceId");
        int bounded=Math.max(1,Math.min(limit,1_000));
        long lease=Math.max(5,Math.min(leaseSeconds,300));
        List<Map<String,Object>> candidates=queryLimited("""
                SELECT m.server_group_id,m.instance_id,m.fencing_token,i.host_name,i.port_no,
                       g.target_protocol,e.context_path,
                       (SELECT MAX(b.response_timeout_ms) FROM GW_BINDING b
                          WHERE b.service_id=g.service_id
                            AND b.environment_code=g.environment_code
                            AND b.binding_status<>'RETIRED') AS response_timeout_ms
                  FROM GW_SERVER_GROUP_MEMBER m
                  JOIN GW_SERVER_GROUP g ON g.server_group_id=m.server_group_id
                  JOIN OPS_SERVICE_INSTANCE i ON i.instance_id=m.instance_id
                  JOIN OPS_SERVICE_ENDPOINT e ON e.endpoint_code=g.endpoint_code
                 WHERE m.enabled_yn='Y' AND g.group_status<>'RETIRED'
                   AND (m.probe_lease_until IS NULL OR m.probe_lease_until<CURRENT_TIMESTAMP)
                 ORDER BY COALESCE(m.last_probe_at,m.created_at),m.server_group_id,m.instance_id
                """,List.of(),bounded,(rs,n)->{
                    Map<String,Object> row=new LinkedHashMap<>();
                    row.put("server_group_id",rs.getString("server_group_id"));row.put("instance_id",rs.getString("instance_id"));
                    row.put("fencing_token",rs.getLong("fencing_token"));row.put("host_name",rs.getString("host_name"));
                    row.put("port_no",rs.getInt("port_no"));row.put("target_protocol",rs.getString("target_protocol"));
                    row.put("context_path",rs.getString("context_path"));row.put("response_timeout_ms",rs.getInt("response_timeout_ms"));return row;});
        List<HealthProbeTarget> result=new ArrayList<>();
        for(Map<String,Object> row:candidates) {
            String group=Objects.toString(row.get("server_group_id"));String instance=Objects.toString(row.get("instance_id"));
            long token=((Number)row.get("fencing_token")).longValue();
            int changed=jdbc.update("""
                    UPDATE GW_SERVER_GROUP_MEMBER
                       SET probe_owner_id=?,probe_lease_until=?,fencing_token=fencing_token+1,updated_at=CURRENT_TIMESTAMP
                     WHERE server_group_id=? AND instance_id=? AND fencing_token=?
                       AND (probe_lease_until IS NULL OR probe_lease_until<CURRENT_TIMESTAMP)
                    """,gatewayInstanceId,timestamp(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(lease)),group,instance,token);
            if(changed==1) result.add(new HealthProbeTarget(group,instance,gatewayInstanceId,token+1,
                    Objects.toString(row.get("host_name")),((Number)row.get("port_no")).intValue(),
                    protocol(Objects.toString(row.get("target_protocol"))),clean(Objects.toString(row.get("context_path"))),
                    Math.max(250,((Number)row.get("response_timeout_ms")).intValue())));
        }
        return List.copyOf(result);
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public HealthProbeTarget claimHealthProbe(String serverGroupId,String instanceId,String gatewayInstanceId,long leaseSeconds) {
        required(serverGroupId,"serverGroupId");required(instanceId,"instanceId");required(gatewayInstanceId,"gatewayInstanceId");
        List<Map<String,Object>> rows=jdbc.queryForList("""
                SELECT m.fencing_token,i.host_name,i.port_no,g.target_protocol,e.context_path,
                       (SELECT MAX(b.response_timeout_ms) FROM GW_BINDING b
                          WHERE b.service_id=g.service_id
                            AND b.environment_code=g.environment_code
                            AND b.binding_status<>'RETIRED') AS response_timeout_ms
                  FROM GW_SERVER_GROUP_MEMBER m
                  JOIN GW_SERVER_GROUP g ON g.server_group_id=m.server_group_id
                  JOIN OPS_SERVICE_INSTANCE i ON i.instance_id=m.instance_id
                  JOIN OPS_SERVICE_ENDPOINT e ON e.endpoint_code=g.endpoint_code
                 WHERE m.server_group_id=? AND m.instance_id=? AND m.enabled_yn='Y'
                   AND (m.probe_lease_until IS NULL OR m.probe_lease_until<CURRENT_TIMESTAMP)
                """,serverGroupId,instanceId);
        if(rows.isEmpty()) return null;
        Map<String,Object> row=rows.getFirst();long token=((Number)row.get("fencing_token")).longValue();
        long lease=Math.max(5,Math.min(leaseSeconds,300));
        int changed=jdbc.update("""
                UPDATE GW_SERVER_GROUP_MEMBER
                   SET probe_owner_id=?,probe_lease_until=?,fencing_token=fencing_token+1,updated_at=CURRENT_TIMESTAMP
                 WHERE server_group_id=? AND instance_id=? AND fencing_token=?
                   AND (probe_lease_until IS NULL OR probe_lease_until<CURRENT_TIMESTAMP)
                """,gatewayInstanceId,timestamp(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(lease)),serverGroupId,instanceId,token);
        if(changed!=1) return null;
        return new HealthProbeTarget(serverGroupId,instanceId,gatewayInstanceId,token+1,
                Objects.toString(row.get("host_name")),((Number)row.get("port_no")).intValue(),
                protocol(Objects.toString(row.get("target_protocol"))),clean(Objects.toString(row.get("context_path"))),
                probeTimeoutMs(row.get("response_timeout_ms")));
    }

    /** Server Group 에 활성 Binding 이 없으면 서브쿼리가 NULL 이므로 하한값으로 Probe 한다. */
    private static int probeTimeoutMs(Object configured) {
        return configured instanceof Number number ? Math.max(250, number.intValue()) : 250;
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    @Override @Transactional
    public void reportHealth(HealthProbeResult c) {
        int changed=jdbc.update("""
                UPDATE GW_SERVER_GROUP_MEMBER
                   SET effective_status=?,last_probe_at=?,last_probe_code=?,
                       consecutive_successes=CASE WHEN ?='UP' THEN consecutive_successes+1 ELSE 0 END,
                       consecutive_failures=CASE WHEN ?='UP' THEN 0 ELSE consecutive_failures+1 END,
                       ewma_latency_ms=(ewma_latency_ms*0.8)+(?*0.2),probe_owner_id=NULL,probe_lease_until=NULL,
                       updated_at=CURRENT_TIMESTAMP
                 WHERE server_group_id=? AND instance_id=? AND probe_owner_id=? AND fencing_token=?
                """,c.overallStatus().name(),timestamp(c.observedAt()),clean(c.resultCode()),c.overallStatus().name(),
                c.overallStatus().name(),Math.max(0,c.durationMs()),c.serverGroupId(),c.instanceId(),c.gatewayInstanceId(),c.fencingToken());
        requireUpdated(changed,"Stale or non-owner Gateway health result rejected");
        jdbc.update("""
                INSERT INTO GW_HEALTH_HISTORY(
                  health_history_id,server_group_id,instance_id,gateway_instance_id,fencing_token,
                  network_status,tcp_status,tls_status,application_status,overall_status,result_code,
                  duration_ms,observed_at,recorded_at)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)
                """,c.healthHistoryId(),c.serverGroupId(),c.instanceId(),c.gatewayInstanceId(),c.fencingToken(),
                c.networkStatus(),c.tcpStatus(),c.tlsStatus(),c.applicationStatus(),c.overallStatus().name(),
                clean(c.resultCode()),Math.max(0,c.durationMs()),timestamp(c.observedAt()));
        upsertCertificateInventory(c);
        recordEvent("GWY-HEALTH-"+c.healthHistoryId(),"HEALTH_PROBE_RECORDED","SERVER_GROUP_MEMBER",
                c.serverGroupId()+":"+c.instanceId(),c.overallStatus().name(),
                json("gatewayInstanceId",c.gatewayInstanceId(),"resultCode",clean(c.resultCode()),
                        "durationMs",Long.toString(Math.max(0,c.durationMs()))));
    }

    private ConnectionTestOperation operation(String id) {
        List<ConnectionTestOperation> rows=jdbc.query("""
                SELECT operation_id,binding_id,test_type,operation_status,requested_by,request_reason,
                       request_payload_hash,expires_at,cancel_requested_yn,result_summary,created_at,started_at,
                       completed_at,row_version
                  FROM GW_CONNECTION_TEST_OPERATION WHERE operation_id=?
                """,(rs,n)->new ConnectionTestOperation(rs.getString("operation_id"),rs.getString("binding_id"),
                rs.getString("test_type"),rs.getString("operation_status"),rs.getString("requested_by"),
                rs.getString("request_reason"),rs.getString("request_payload_hash"),offset(rs.getTimestamp("expires_at")),
                yes(rs.getString("cancel_requested_yn")),rs.getString("result_summary"),offset(rs.getTimestamp("created_at")),
                offset(rs.getTimestamp("started_at")),offset(rs.getTimestamp("completed_at")),rs.getLong("row_version")),id);
        if(rows.isEmpty()) throw new IllegalArgumentException("Connection Test operation not found: "+id);
        return rows.getFirst();
    }

    private void upsertCertificateInventory(HealthProbeResult c) {
        if (c.certificateNotAfter() == null || c.certificateFingerprintSha256() == null
                || c.certificateFingerprintSha256().isBlank()) return;
        String status = c.certificateNotAfter().isBefore(c.observedAt()) ? "EXPIRED" : "ACTIVE";
        Object[] args = {c.certificateFingerprintSha256(),status,timestamp(c.certificateNotAfter()),
                timestamp(c.observedAt()),c.serverGroupId(),c.instanceId()};
        int changed=jdbc.update("""
                UPDATE GW_CERTIFICATE_INVENTORY
                   SET certificate_fingerprint_sha256=?,certificate_status=?,not_after=?,observed_at=?,updated_at=CURRENT_TIMESTAMP
                 WHERE server_group_id=? AND instance_id=?
                """,args);
        if(changed==0){
            try{jdbc.update("""
                    INSERT INTO GW_CERTIFICATE_INVENTORY(
                      server_group_id,instance_id,certificate_fingerprint_sha256,certificate_status,not_after,observed_at,updated_at)
                    VALUES (?,?,?,?,?,?,CURRENT_TIMESTAMP)
                    """,c.serverGroupId(),c.instanceId(),c.certificateFingerprintSha256(),status,
                    timestamp(c.certificateNotAfter()),timestamp(c.observedAt()));}
            // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
            catch(org.springframework.dao.DuplicateKeyException race){
                jdbc.update("""
                        UPDATE GW_CERTIFICATE_INVENTORY
                           SET certificate_fingerprint_sha256=?,certificate_status=?,not_after=?,observed_at=?,updated_at=CURRENT_TIMESTAMP
                         WHERE server_group_id=? AND instance_id=?
                        """,args);
            }
        }
    }

    @Override
    public OperationsSnapshot operationsSnapshot() {
        OffsetDateTime now=OffsetDateTime.now(ZoneOffset.UTC);
        Timestamp since=Timestamp.from(now.minusMinutes(1).toInstant());
        List<String> warnings=new ArrayList<>();
        List<TrafficSample> samples=querySafe("""
                SELECT result_status,total_duration_ms FROM GW_TRANSACTION
                 WHERE created_at>=?
                """,List.of(since),(rs,n)->new TrafficSample(rs.getString(1),rs.getLong(2)),warnings,"TRANSACTION_READ_FAILED");
        long total=samples.size();
        long success=samples.stream().filter(v->"SUCCESS".equalsIgnoreCase(v.status())).count();
        long unknown=samples.stream().filter(v->"UNKNOWN_RESULT".equalsIgnoreCase(v.status())||"UNKNOWN".equalsIgnoreCase(v.status())).count();
        long failed=Math.max(0,total-success-unknown);
        List<Long> durations=samples.stream().map(value -> value.durationMs()).sorted().toList();
        long openCircuit=countSafe("SELECT COUNT(*) FROM OPS_SERVICE_CIRCUIT_STATE WHERE circuit_state='OPEN'",List.of(),warnings,"CIRCUIT_READ_FAILED");
        long expiring=countSafe("SELECT COUNT(*) FROM GW_CERTIFICATE_INVENTORY WHERE certificate_status='ACTIVE' AND not_after<=?",
                List.of(Timestamp.from(now.plusDays(30).toInstant())),warnings,"CERTIFICATE_READ_FAILED");
        long backlog=countSafe("SELECT COALESCE(SUM(backlog_count),0) FROM GW_SPOOL_CHECKPOINT",List.of(),warnings,"SPOOL_READ_FAILED");
        long backlogBytes=countSafe("SELECT COALESCE(SUM(backlog_bytes),0) FROM GW_SPOOL_CHECKPOINT",List.of(),warnings,"SPOOL_READ_FAILED");
        long drift=countSafe("""
                SELECT COUNT(*) FROM GW_APPLY_STATUS
                 WHERE apply_status NOT IN ('APPLIED','REMOVED')
                    OR COALESCE(expected_version,'?')<>COALESCE(applied_version,'!')
                """,List.of(),warnings,"DRIFT_READ_FAILED");
        long failedTests=countSafe("""
                SELECT COUNT(*) FROM GW_CONNECTION_TEST
                 WHERE tested_at>=? AND test_status NOT IN ('PASS','SUCCESS')
                """,List.of(Timestamp.from(now.minusDays(1).toInstant())),warnings,"CONNECTION_TEST_READ_FAILED");
        String lastEvent=lastEventId(warnings);
        double successRate=total==0?0d:(success*100d/total);
        double errorRate=total==0?0d:((failed+unknown)*100d/total);
        return new OperationsSnapshot(warnings.isEmpty()?"AVAILABLE":"PARTIAL",now,
                com.cpf.foundation.runtime.CpfInstanceIdentity.current().instanceId(),60,total,success,failed,unknown,
                total/60d,round(successRate),round(errorRate),percentile(durations,0.95d),percentile(durations,0.99d),
                openCircuit,expiring,backlog,backlogBytes,drift,failedTests,lastEvent,List.copyOf(warnings));
    }

    @Override
    public List<OperationsEvent> operationsEvents(String afterEventId,int limit) {
        StringBuilder sql=new StringBuilder("""
                SELECT event_id,event_type,aggregate_type,aggregate_id,event_status,source_instance_id,payload_json,occurred_at
                  FROM GW_OPERATIONS_EVENT WHERE 1=1
                """);
        List<Object> args=new ArrayList<>();
        if(afterEventId!=null&&!afterEventId.isBlank()){sql.append(" AND event_id>?");args.add(afterEventId.trim());}
        sql.append(" ORDER BY event_id");
        return queryLimited(sql.toString(),args,limit,(rs,n)->new OperationsEvent(rs.getString("event_id"),
                rs.getString("event_type"),rs.getString("aggregate_type"),rs.getString("aggregate_id"),
                rs.getString("event_status"),rs.getString("source_instance_id"),rs.getString("payload_json"),
                offset(rs.getTimestamp("occurred_at"))));
    }

    private String lastEventId(List<String> warnings){
        try {List<String> ids=jdbc.query(connection->{
                    java.sql.PreparedStatement statement=connection.prepareStatement(
                            "SELECT event_id FROM GW_OPERATIONS_EVENT ORDER BY event_id DESC");
                    statement.setMaxRows(1);
                    return statement;
                },(rs,n)->rs.getString(1));
            return ids.isEmpty()?"":ids.getFirst();}
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        catch(DataAccessException ex){warnings.add("EVENT_READ_FAILED");return "";}
    }
    private long countSafe(String sql,List<Object> args,List<String> warnings,String code){
        try {Long value=jdbc.queryForObject(sql,Long.class,args.toArray());return value==null?0:value;}
        catch(DataAccessException ex){if(!warnings.contains(code))warnings.add(code);return 0;}
    }
    private <T> List<T> querySafe(String sql,List<Object> args,org.springframework.jdbc.core.RowMapper<T> mapper,
            List<String> warnings,String code){
        try{return queryLimited(sql,args,100000,mapper);}catch(DataAccessException ex){warnings.add(code);return List.of();}
    }
    private static long percentile(List<Long> values,double percentile){
        if(values.isEmpty())return 0;int index=(int)Math.ceil(values.size()*percentile)-1;
        return values.get(Math.max(0,Math.min(index,values.size()-1)));
    }
    private static double round(double value){return Math.round(value*100d)/100d;}
    /** TrafficSample 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    private record TrafficSample(String status,long durationMs){}

    private void recordEvent(String preferredId,String type,String aggregateType,String aggregateId,String status,String payloadJson){
        String id=preferredId==null||preferredId.isBlank()?eventId():preferredId;
        try{jdbc.update("""
                INSERT INTO GW_OPERATIONS_EVENT
                (event_id,event_type,aggregate_type,aggregate_id,event_status,source_instance_id,payload_json,occurred_at)
                VALUES (?,?,?,?,?,?,?,CURRENT_TIMESTAMP)
                """,id,type,aggregateType,aggregateId,status,
                com.cpf.foundation.runtime.CpfInstanceIdentity.current().instanceId(),payloadJson);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        }catch(org.springframework.dao.DuplicateKeyException duplicate){
            // operationId 기반 Event는 동일 Command 재실행 시 멱등 처리합니다.
        }
    }
    private static String eventId(){
        java.time.Instant now=java.time.Instant.now();
        long nanos=Math.addExact(Math.multiplyExact(now.getEpochSecond(),1_000_000_000L),now.getNano());
        return String.format(java.util.Locale.ROOT,"%019d-%s",nanos,java.util.UUID.randomUUID());
    }
    private static String json(String... pairs){
        StringBuilder out=new StringBuilder("{");
        for(int i=0;i<pairs.length;i+=2){if(i>0)out.append(',');out.append('"').append(jsonEscape(pairs[i])).append("\":\"")
                .append(jsonEscape(i+1<pairs.length?pairs[i+1]:"")).append('"');}
        return out.append('}').toString();
    }
    private static String jsonEscape(String value) {
        return clean(value).replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", " ")
                .replace("\n", " ");
    }


    private void validateGroup(ServerGroupCommand c) {
        required(c.operationId(),"operationId");required(c.serverGroupId(),"serverGroupId");required(c.groupName(),"groupName");
        required(c.environmentCode(),"environmentCode");required(c.serviceId(),"serviceId");required(c.endpointCode(),"endpointCode");
        Objects.requireNonNull(c.targetProtocol(),"targetProtocol");Objects.requireNonNull(c.loadBalancePolicy(),"loadBalancePolicy");
        requireReason(c.reason());required(c.requestedBy(),"requestedBy");
        List<MemberCommand> members=Objects.requireNonNullElse(c.members(),List.of());
        if(members.isEmpty()) throw new IllegalArgumentException("Server Group requires at least one member");
        if(c.loadBalancePolicy()==CpfGatewayLoadBalancePolicy.RENDEZVOUS_HASH && clean(c.hashKeySource()).isBlank())
            throw new IllegalArgumentException("RENDEZVOUS_HASH requires hashKeySource");
    }
    private void validateBinding(GatewayBindingCommand c) {
        required(c.operationId(),"operationId");required(c.bindingId(),"bindingId");Objects.requireNonNull(c.route(),"route");
        required(c.serverGroupId(),"serverGroupId");requireReason(c.reason());required(c.requestedBy(),"requestedBy");
        if(c.gatewayAllowed() && !c.serverGroupId().equals(c.route().serverGroupId()))
            throw new IllegalArgumentException("Route and Binding serverGroupId must match");
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM GW_SERVER_GROUP WHERE server_group_id=?",Integer.class,c.serverGroupId());
        if(count==null||count==0) throw new IllegalArgumentException("Server Group not found: "+c.serverGroupId());
    }

    private Long version(String table,String key,String id) {
        List<Long> rows=jdbc.query("SELECT row_version FROM "+table+" WHERE "+key+"=?",(rs,n)->rs.getLong(1),id);
        return rows.isEmpty()?null:rows.getFirst();
    }
    private long versionRequired(String table,String key,String id) {
        Long value=version(table,key,id); if(value==null) throw new IllegalArgumentException("Resource not found: "+id); return value;
    }
    private static void requireNew(Long expected){if(expected!=null&&expected!=0)throw new IllegalStateException("New resource expectedVersion must be 0");}
    private static void requireExpected(long current,Long expected){if(expected==null||current!=expected)throw new IllegalStateException("Version conflict: expected="+expected+", actual="+current);}
    private static void requireUpdated(int count,String message){if(count!=1)throw new IllegalStateException(message);}
    private static void requireReason(String reason){if(clean(reason).length()<5)throw new IllegalArgumentException("Reason must be at least 5 characters");}
    private static String required(String value,String field){if(value==null||value.isBlank())throw new IllegalArgumentException(field+" is required");return value.trim();}
    private static String normalizeState(String state){String value=required(state,"targetState").toUpperCase(Locale.ROOT);if(!Set.of("DRAFT","VALIDATED","APPROVAL_PENDING","APPROVED","ACTIVE","PARTIAL","BLOCKED","RETIRED").contains(value))throw new IllegalArgumentException("Unsupported binding state: "+value);return value;}
    private static void requireTransition(String source,String target) {
        Map<String,Set<String>> allowed=Map.of(
                "DRAFT",Set.of("VALIDATED","RETIRED"),
                "VALIDATED",Set.of("DRAFT","APPROVAL_PENDING","RETIRED"),
                "APPROVAL_PENDING",Set.of("DRAFT","APPROVED","RETIRED"),
                "APPROVED",Set.of("ACTIVE","DRAFT","RETIRED"),
                "ACTIVE",Set.of("BLOCKED","RETIRED"),
                "PARTIAL",Set.of("ACTIVE","BLOCKED","RETIRED"),
                "BLOCKED",Set.of("ACTIVE","RETIRED"),
                "RETIRED",Set.of());
        if(!allowed.getOrDefault(source,Set.of()).contains(target))
            throw new IllegalStateException("Invalid Gateway Binding transition: "+source+" -> "+target);
    }
    private static void requireConfigured(Map<String,Object> row,String column,String message) {
        if(clean(Objects.toString(row.get(column),"")).isBlank()) throw new IllegalStateException(message);
    }
    private static int boundedPercent(int value){if(value<0||value>100)throw new IllegalArgumentException("canaryPercent must be between 0 and 100");return value;}
    private static String clean(String value){return value==null?"":value.trim();}
    private static String yn(boolean value){return value?"Y":"N";}
    private static boolean yes(String value){return "Y".equalsIgnoreCase(value)||"TRUE".equalsIgnoreCase(value);}
    private static <E extends Enum<E>> E enumValue(Class<E> type,String value,String field) {
        String normalized=required(value,field).toUpperCase(Locale.ROOT);
        try{return Enum.valueOf(type,normalized);}
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        catch(IllegalArgumentException ex){throw new IllegalStateException("Unsupported "+field+" code: "+value,ex);}
    }
    private static CpfGatewayProtocol protocol(String value){return enumValue(CpfGatewayProtocol.class,value,"gateway protocol");}
    private static CpfGatewayLoadBalancePolicy loadBalance(String value){return enumValue(CpfGatewayLoadBalancePolicy.class,value,"load-balance policy");}
    private static CpfGatewayHealthStatus health(String value){return enumValue(CpfGatewayHealthStatus.class,value,"health status");}
    private static String bindingChecksum(GatewayBindingCommand c) {
        CpfGatewayRoute r=c.route();
        String canonical=String.join("|",
                c.bindingId(),r.routeId(),r.environmentCode(),r.hostPattern(),r.pathPattern(),r.targetPath(),
                clean(r.httpMethod()),r.apiVersion(),r.ingressProtocol().name(),r.targetProtocol().name(),
                r.serviceId(),c.serverGroupId(),r.routeVersion(),clean(r.tlsPolicyId()),
                clean(r.authenticationPolicyId()),clean(r.authorizationPolicyId()),clean(r.headerPolicyId()),
                clean(r.rateLimitPolicyId()),clean(r.healthPolicyId()),String.valueOf(r.connectTimeoutMs()),
                String.valueOf(r.responseTimeoutMs()),String.valueOf(r.overallTimeoutMs()),
                String.valueOf(r.maxRetryCount()),yn(r.idempotent()),clean(r.failoverGroupId()),
                yn(c.gatewayAllowed()),yn(c.directAllowed()),Objects.toString(c.effectiveFrom(),""),
                Objects.toString(c.effectiveTo(),""));
        return sha256Hex(canonical);
    }

    /**
     * 긴 Route Match Key 전체의 동등성을 Vendor 공통 64자 키로 보존합니다.
     * 각 성분을 먼저 SHA-256으로 프레이밍하므로 구분자 충돌과 Vendor 문자열 길이 제한이 없습니다.
     */
    static String bindingKeyHash(CpfGatewayRoute route) {
        Objects.requireNonNull(route, "route");
        StringBuilder framed=new StringBuilder(64*6);
        for(String component:List.of(route.environmentCode(),route.hostPattern(),route.pathPattern(),
                route.httpMethod(),route.apiVersion(),route.routeVersion())) {
            framed.append(sha256Hex(component));
        }
        return sha256Hex(framed.toString());
    }

    private static String sha256Hex(String value) {
        try { return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (java.security.NoSuchAlgorithmException ex) { throw new IllegalStateException("SHA-256 unavailable",ex); }
    }

    private static boolean runtimeImpact(String target) {
        return Set.of("ACTIVE","BLOCKED","RETIRED","DRAFT").contains(target);
    }

    private void publishBindingEvent(
            String eventId,String bindingId,long version,String action,String checksum,String routeVersion,
            String reason,String requestedBy) {
        required(eventId,"operationId");
        distribution.publish(new CpfRuntimePolicyDistributionPort.PublishCommand(
                eventId,"GATEWAY_ROUTE","GATEWAY_BINDING",bindingId,version,action,clean(checksum),
                Map.of("bindingId",bindingId,"targetState",action,"routeVersion",clean(routeVersion)),
                reason,requestedBy,OffsetDateTime.now(ZoneOffset.UTC)));
    }

    private static Timestamp timestamp(OffsetDateTime value){return value==null?null:Timestamp.from(value.toInstant());}
    private static OffsetDateTime offset(Timestamp value){return value==null?null:value.toInstant().atOffset(ZoneOffset.UTC);}
    private static MutationResult mutation(String type,String id,String status,long version){return new MutationResult(type,id,status,version,OffsetDateTime.now(ZoneOffset.UTC));}
    private static void append(StringBuilder sql,List<Object> args,String clause,String value){if(value!=null&&!value.isBlank()){sql.append(clause);args.add(value);}}
    private <T> List<T> queryLimited(String sql,List<Object> args,int limit,org.springframework.jdbc.core.RowMapper<T> mapper) {
        int max=limit<=0?100:Math.min(limit,1000);
        return jdbc.query(connection->{var ps=connection.prepareStatement(sql);ps.setMaxRows(max);for(int i=0;i<args.size();i++)ps.setObject(i+1,args.get(i));return ps;},mapper);
    }
}

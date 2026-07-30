package com.cpf.gateway.registry;

import com.cpf.core.api.gateway.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Gateway Control Plane의 JDBC Adapter입니다.
 *
 * <p>Vendor 전용 UPSERT/RETURNING/LIMIT를 사용하지 않고 CAS 기반 Select→Insert/Update 흐름으로
 * Oracle·PostgreSQL·MariaDB에서 동일한 동작을 제공합니다.</p>
 */
@Repository
@ConditionalOnBean(DataSource.class)
public class JdbcCpfGatewayRegistryAdapter implements CpfGatewayRegistryPort {
    private final JdbcTemplate jdbc;

    public JdbcCpfGatewayRegistryAdapter(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(Objects.requireNonNull(dataSource, "dataSource"));
    }

    @Override
    public List<ServerGroup> findServerGroups(String environmentCode, String serviceId, String status, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT g.server_group_id,g.group_name,g.environment_code,g.service_id,g.endpoint_code,
                       g.target_protocol,g.load_balance_policy,g.hash_key_source,g.health_policy_id,
                       g.failover_group_id,g.group_status,g.direct_allowed_yn,g.row_version,g.updated_at,
                       (SELECT COUNT(*) FROM cpf_gateway_server_group_member m
                         WHERE m.server_group_id=g.server_group_id AND m.enabled_yn='Y') member_count
                  FROM cpf_gateway_server_group g WHERE 1=1
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
                SELECT m.server_group_id,m.instance_id,m.weight,m.priority_no,m.enabled_yn,m.effective_status,
                       m.fencing_token,m.updated_at
                  FROM cpf_gateway_server_group_member m
                 WHERE m.server_group_id=? ORDER BY m.priority_no,m.instance_id
                """, (rs,n) -> new GroupMember(rs.getString("server_group_id"),rs.getString("instance_id"),
                rs.getInt("weight"),rs.getInt("priority_no"),yes(rs.getString("enabled_yn")),
                health(rs.getString("effective_status")),rs.getLong("fencing_token"),offset(rs.getTimestamp("updated_at"))),
                serverGroupId);
    }

    @Override
    public List<GatewayBinding> findBindings(String environmentCode, String routeId, String status, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT binding_id,route_id,environment_code,host_pattern,path_pattern,http_method,api_version,
                       ingress_protocol,target_protocol,service_id,server_group_id,route_version,tls_policy_id,
                       authentication_policy_id,authorization_policy_id,header_policy_id,rate_limit_policy_id,
                       health_policy_id,connect_timeout_ms,response_timeout_ms,overall_timeout_ms,max_retry_count,
                       idempotent_yn,failover_group_id,binding_status,gateway_allowed_yn,direct_allowed_yn,approval_id,
                       effective_from,effective_to,row_version,updated_at
                  FROM cpf_gateway_binding WHERE 1=1
                """);
        List<Object> args = new ArrayList<>();
        append(sql,args," AND environment_code=?",environmentCode);
        append(sql,args," AND route_id=?",routeId);
        append(sql,args," AND binding_status=?",status);
        sql.append(" ORDER BY updated_at DESC,binding_id");
        return queryLimited(sql.toString(),args,limit,(rs,n)->new GatewayBinding(
                rs.getString("binding_id"),rs.getString("route_id"),rs.getString("environment_code"),
                rs.getString("host_pattern"),rs.getString("path_pattern"),rs.getString("http_method"),rs.getString("api_version"),
                protocol(rs.getString("ingress_protocol")),protocol(rs.getString("target_protocol")),rs.getString("service_id"),
                rs.getString("server_group_id"),rs.getString("route_version"),rs.getString("tls_policy_id"),
                rs.getString("authentication_policy_id"),rs.getString("authorization_policy_id"),rs.getString("header_policy_id"),
                rs.getString("rate_limit_policy_id"),rs.getString("health_policy_id"),rs.getInt("connect_timeout_ms"),
                rs.getInt("response_timeout_ms"),rs.getInt("overall_timeout_ms"),rs.getInt("max_retry_count"),
                yes(rs.getString("idempotent_yn")),rs.getString("failover_group_id"),rs.getString("binding_status"),
                yes(rs.getString("gateway_allowed_yn")),yes(rs.getString("direct_allowed_yn")),rs.getString("approval_id"),
                offset(rs.getTimestamp("effective_from")),offset(rs.getTimestamp("effective_to")),
                rs.getLong("row_version"),offset(rs.getTimestamp("updated_at"))));
    }

    @Override
    public List<ApplyStatus> findApplyStatuses(String bindingId, int limit) {
        return queryLimited("""
                SELECT binding_id,gateway_instance_id,expected_version,applied_version,apply_status,error_code,
                       error_message,acknowledged_at,last_seen_at
                  FROM cpf_gateway_apply_status WHERE binding_id=? ORDER BY last_seen_at DESC
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
                  FROM cpf_gateway_connection_test WHERE binding_id=? ORDER BY tested_at DESC
                """,List.of(bindingId),limit,(rs,n)->new ConnectionTestResult(rs.getString("test_id"),
                rs.getString("binding_id"),rs.getString("gateway_instance_id"),rs.getString("instance_id"),
                rs.getString("test_type"),rs.getString("test_status"),rs.getString("failure_stage"),rs.getLong("duration_ms"),
                rs.getString("trace_id"),rs.getString("operation_id"),offset(rs.getTimestamp("tested_at")),rs.getString("tested_by")));
    }

    @Override @Transactional
    public MutationResult saveServerGroup(ServerGroupCommand c) {
        validateGroup(c);
        Long current = version("cpf_gateway_server_group","server_group_id",c.serverGroupId());
        long next;
        if (current == null) {
            requireNew(c.expectedVersion());
            jdbc.update("""
                    INSERT INTO cpf_gateway_server_group
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
                    UPDATE cpf_gateway_server_group SET group_name=?,environment_code=?,service_id=?,endpoint_code=?,
                           target_protocol=?,load_balance_policy=?,hash_key_source=?,health_policy_id=?,failover_group_id=?,
                           direct_allowed_yn=?,updated_by=?,updated_at=CURRENT_TIMESTAMP,row_version=row_version+1
                     WHERE server_group_id=? AND row_version=?
                    """,c.groupName(),c.environmentCode(),c.serviceId(),c.endpointCode(),c.targetProtocol().name(),
                    c.loadBalancePolicy().name(),clean(c.hashKeySource()),clean(c.healthPolicyId()),clean(c.failoverGroupId()),
                    yn(c.directAllowed()),c.requestedBy(),c.serverGroupId(),current);
            requireUpdated(updated,"Server Group version conflict"); next=current+1;
        }
        jdbc.update("DELETE FROM cpf_gateway_server_group_member WHERE server_group_id=?",c.serverGroupId());
        Set<String> seen=new HashSet<>();
        List<MemberCommand> members = c.members() == null ? List.of() : c.members();
        for(MemberCommand member:members) {
            if(!seen.add(member.instanceId())) throw new IllegalArgumentException("Duplicate instance in group: "+member.instanceId());
            jdbc.update("""
                    INSERT INTO cpf_gateway_server_group_member
                    (server_group_id,instance_id,weight,priority_no,enabled_yn,effective_status,fencing_token,
                     created_by,created_at,updated_by,updated_at)
                    VALUES (?,?,?,?,?,'UNKNOWN',0,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP)
                    """,c.serverGroupId(),member.instanceId(),Math.max(1,member.weight()),Math.max(0,member.priority()),
                    yn(member.enabled()),c.requestedBy(),c.requestedBy());
        }
        return mutation("SERVER_GROUP",c.serverGroupId(),"SAVED",next);
    }

    @Override @Transactional
    public MutationResult saveBinding(GatewayBindingCommand c) {
        validateBinding(c);
        Long current=version("cpf_gateway_binding","binding_id",c.bindingId());
        long next;
        CpfGatewayRoute r=c.route();
        if(current==null) {
            requireNew(c.expectedVersion());
            jdbc.update("""
                    INSERT INTO cpf_gateway_binding
                    (binding_id,route_id,environment_code,host_pattern,path_pattern,http_method,api_version,
                     ingress_protocol,target_protocol,service_id,server_group_id,route_version,tls_policy_id,
                     authentication_policy_id,authorization_policy_id,header_policy_id,rate_limit_policy_id,
                     health_policy_id,connect_timeout_ms,response_timeout_ms,overall_timeout_ms,max_retry_count,
                     idempotent_yn,failover_group_id,gateway_allowed_yn,direct_allowed_yn,binding_status,approval_id,
                     effective_from,effective_to,created_by,created_at,updated_by,updated_at,row_version)
                    VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'DRAFT',?,?,?,?,CURRENT_TIMESTAMP,?,CURRENT_TIMESTAMP,1)
                    """,c.bindingId(),r.routeId(),r.environmentCode(),r.hostPattern(),r.pathPattern(),r.httpMethod(),r.apiVersion(),
                    r.ingressProtocol().name(),r.targetProtocol().name(),r.serviceId(),c.serverGroupId(),r.routeVersion(),
                    clean(r.tlsPolicyId()),clean(r.authenticationPolicyId()),clean(r.authorizationPolicyId()),clean(r.headerPolicyId()),
                    clean(r.rateLimitPolicyId()),clean(r.healthPolicyId()),r.connectTimeoutMs(),r.responseTimeoutMs(),r.overallTimeoutMs(),
                    r.maxRetryCount(),yn(r.idempotent()),clean(r.failoverGroupId()),yn(c.gatewayAllowed()),yn(c.directAllowed()),
                    clean(c.approvalId()),timestamp(c.effectiveFrom()),timestamp(c.effectiveTo()),c.requestedBy(),c.requestedBy());
            next=1;
        } else {
            requireExpected(current,c.expectedVersion());
            int updated=jdbc.update("""
                    UPDATE cpf_gateway_binding SET route_id=?,environment_code=?,host_pattern=?,path_pattern=?,http_method=?,
                           api_version=?,ingress_protocol=?,target_protocol=?,service_id=?,server_group_id=?,route_version=?,
                           tls_policy_id=?,authentication_policy_id=?,authorization_policy_id=?,header_policy_id=?,
                           rate_limit_policy_id=?,health_policy_id=?,connect_timeout_ms=?,response_timeout_ms=?,overall_timeout_ms=?,
                           max_retry_count=?,idempotent_yn=?,failover_group_id=?,gateway_allowed_yn=?,direct_allowed_yn=?,approval_id=?,
                           effective_from=?,effective_to=?,updated_by=?,updated_at=CURRENT_TIMESTAMP,row_version=row_version+1
                     WHERE binding_id=? AND row_version=?
                    """,r.routeId(),r.environmentCode(),r.hostPattern(),r.pathPattern(),r.httpMethod(),r.apiVersion(),
                    r.ingressProtocol().name(),r.targetProtocol().name(),r.serviceId(),c.serverGroupId(),r.routeVersion(),
                    clean(r.tlsPolicyId()),clean(r.authenticationPolicyId()),clean(r.authorizationPolicyId()),clean(r.headerPolicyId()),
                    clean(r.rateLimitPolicyId()),clean(r.healthPolicyId()),r.connectTimeoutMs(),r.responseTimeoutMs(),r.overallTimeoutMs(),
                    r.maxRetryCount(),yn(r.idempotent()),clean(r.failoverGroupId()),yn(c.gatewayAllowed()),yn(c.directAllowed()),
                    clean(c.approvalId()),timestamp(c.effectiveFrom()),timestamp(c.effectiveTo()),c.requestedBy(),c.bindingId(),current);
            requireUpdated(updated,"Gateway Binding version conflict"); next=current+1;
        }
        return mutation("GATEWAY_BINDING",c.bindingId(),"SAVED",next);
    }

    @Override @Transactional
    public MutationResult changeBindingState(BindingStateCommand c) {
        String target=normalizeState(c.targetState());
        Long current=version("cpf_gateway_binding","binding_id",c.bindingId());
        if(current==null) throw new IllegalArgumentException("Gateway Binding not found: "+c.bindingId());
        requireExpected(current,c.expectedVersion());
        if(Set.of("APPROVED","ACTIVE","BLOCKED","RETIRED").contains(target) && clean(c.reason()).length()<5)
            throw new IllegalArgumentException("Risk action reason must be at least 5 characters");
        if("ACTIVE".equals(target) && clean(c.approvalId()).isBlank()) throw new IllegalArgumentException("ACTIVE requires approvalId");
        int updated=jdbc.update("""
                UPDATE cpf_gateway_binding SET binding_status=?,approval_id=?,updated_by=?,updated_at=CURRENT_TIMESTAMP,
                       row_version=row_version+1 WHERE binding_id=? AND row_version=?
                """,target,clean(c.approvalId()),c.requestedBy(),c.bindingId(),current);
        requireUpdated(updated,"Gateway Binding state version conflict");
        return mutation("GATEWAY_BINDING",c.bindingId(),target,current+1);
    }

    @Override @Transactional
    public void deleteServerGroup(String id, DeleteCommand c) {
        requireReason(c.reason()); requireExpected(versionRequired("cpf_gateway_server_group","server_group_id",id),c.expectedVersion());
        Integer bindings=jdbc.queryForObject("SELECT COUNT(*) FROM cpf_gateway_binding WHERE server_group_id=? AND binding_status<>'RETIRED'",Integer.class,id);
        if(bindings!=null&&bindings>0) throw new IllegalStateException("Active Gateway Binding references Server Group");
        jdbc.update("DELETE FROM cpf_gateway_server_group_member WHERE server_group_id=?",id);
        requireUpdated(jdbc.update("DELETE FROM cpf_gateway_server_group WHERE server_group_id=? AND row_version=?",id,c.expectedVersion()),"Server Group version conflict");
    }

    @Override @Transactional
    public void deleteBinding(String id, DeleteCommand c) {
        requireReason(c.reason()); requireExpected(versionRequired("cpf_gateway_binding","binding_id",id),c.expectedVersion());
        jdbc.update("DELETE FROM cpf_gateway_apply_status WHERE binding_id=?",id);
        jdbc.update("DELETE FROM cpf_gateway_connection_test WHERE binding_id=?",id);
        requireUpdated(jdbc.update("DELETE FROM cpf_gateway_binding WHERE binding_id=? AND row_version=?",id,c.expectedVersion()),"Gateway Binding version conflict");
    }

    @Override @Transactional
    public ApplyStatus acknowledge(ApplyAckCommand c) {
        int updated=jdbc.update("""
                UPDATE cpf_gateway_apply_status SET expected_version=?,applied_version=?,apply_status=?,error_code=?,
                       error_message=?,acknowledged_at=?,last_seen_at=CURRENT_TIMESTAMP
                 WHERE binding_id=? AND gateway_instance_id=?
                """,c.expectedVersion(),c.appliedVersion(),c.status(),clean(c.errorCode()),clean(c.errorMessage()),
                timestamp(c.acknowledgedAt()),c.bindingId(),c.gatewayInstanceId());
        if(updated==0) jdbc.update("""
                INSERT INTO cpf_gateway_apply_status
                (binding_id,gateway_instance_id,expected_version,applied_version,apply_status,error_code,error_message,
                 acknowledged_at,last_seen_at) VALUES (?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)
                """,c.bindingId(),c.gatewayInstanceId(),c.expectedVersion(),c.appliedVersion(),c.status(),
                clean(c.errorCode()),clean(c.errorMessage()),timestamp(c.acknowledgedAt()));
        return new ApplyStatus(c.bindingId(),c.gatewayInstanceId(),c.expectedVersion(),c.appliedVersion(),c.status(),
                clean(c.errorCode()),clean(c.errorMessage()),c.acknowledgedAt(),OffsetDateTime.now(ZoneOffset.UTC));
    }

    @Override @Transactional
    public ConnectionTestResult recordConnectionTest(ConnectionTestCommand c) {
        jdbc.update("""
                INSERT INTO cpf_gateway_connection_test
                (test_id,binding_id,gateway_instance_id,instance_id,test_type,test_status,failure_stage,duration_ms,
                 trace_id,operation_id,tested_at,tested_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """,c.testId(),c.bindingId(),c.gatewayInstanceId(),c.instanceId(),c.testType(),c.status(),
                clean(c.failureStage()),Math.max(0,c.durationMs()),clean(c.traceId()),clean(c.operationId()),
                timestamp(c.testedAt()),c.testedBy());
        return new ConnectionTestResult(c.testId(),c.bindingId(),c.gatewayInstanceId(),c.instanceId(),c.testType(),
                c.status(),clean(c.failureStage()),Math.max(0,c.durationMs()),clean(c.traceId()),clean(c.operationId()),
                c.testedAt(),c.testedBy());
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
        Integer count=jdbc.queryForObject("SELECT COUNT(*) FROM cpf_gateway_server_group WHERE server_group_id=?",Integer.class,c.serverGroupId());
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
    private static String clean(String value){return value==null?"":value.trim();}
    private static String yn(boolean value){return value?"Y":"N";}
    private static boolean yes(String value){return "Y".equalsIgnoreCase(value)||"TRUE".equalsIgnoreCase(value);}
    private static CpfGatewayProtocol protocol(String value){try{return CpfGatewayProtocol.valueOf(value);}catch(Exception e){return CpfGatewayProtocol.HTTP;}}
    private static CpfGatewayLoadBalancePolicy loadBalance(String value){try{return CpfGatewayLoadBalancePolicy.valueOf(value);}catch(Exception e){return CpfGatewayLoadBalancePolicy.ROUND_ROBIN;}}
    private static CpfGatewayHealthStatus health(String value){try{return CpfGatewayHealthStatus.valueOf(value);}catch(Exception e){return CpfGatewayHealthStatus.UNKNOWN;}}
    private static Timestamp timestamp(OffsetDateTime value){return value==null?null:Timestamp.from(value.toInstant());}
    private static OffsetDateTime offset(Timestamp value){return value==null?null:value.toInstant().atOffset(ZoneOffset.UTC);}
    private static MutationResult mutation(String type,String id,String status,long version){return new MutationResult(type,id,status,version,OffsetDateTime.now(ZoneOffset.UTC));}
    private static void append(StringBuilder sql,List<Object> args,String clause,String value){if(value!=null&&!value.isBlank()){sql.append(clause);args.add(value);}}
    private <T> List<T> queryLimited(String sql,List<Object> args,int limit,org.springframework.jdbc.core.RowMapper<T> mapper) {
        int max=limit<=0?100:Math.min(limit,1000);
        return jdbc.query(connection->{var ps=connection.prepareStatement(sql);ps.setMaxRows(max);for(int i=0;i<args.size();i++)ps.setObject(i+1,args.get(i));return ps;},mapper);
    }
}

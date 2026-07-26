package com.cpf.bizadmin.directory.repository;

import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/** BZA 조직/인사/Role 이력 정본 Repository. */
@Repository
public class BzaDirectoryRepository {
 private final ObjectProvider<NamedParameterJdbcTemplate> provider;
 public BzaDirectoryRepository(@Qualifier("bzaJdbcTemplate")ObjectProvider<NamedParameterJdbcTemplate> provider){this.provider=provider;}

 public List<Map<String,Object>> findPositions(){return positionPage(new CpfPageRequest(0,200)).content();}
 public CpfPage<Map<String,Object>> positionPage(CpfPageRequest p){return page("""
   SELECT position_code AS positionCode,position_name AS positionName,rank_order AS rankOrder,use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt
   FROM bza_position ORDER BY rank_order,position_code LIMIT :limit OFFSET :offset
   ""","SELECT COUNT(*) FROM bza_position",Map.of(),p);}
 public Optional<Map<String,Object>> findPosition(String code){return one("SELECT position_code AS positionCode,version_no AS versionNo FROM bza_position WHERE position_code=:code",new MapSqlParameterSource("code",code));}
 public int insertPosition(Map<String,?>v){return jdbc().update("INSERT INTO bza_position(position_code,position_name,rank_order,use_yn,version_no,created_by,updated_by) VALUES(:positionCode,:positionName,:rankOrder,:useYn,0,:operatorId,:operatorId)",v);}
 public int updatePosition(Map<String,?>v){return jdbc().update("UPDATE bza_position SET position_name=:positionName,rank_order=:rankOrder,use_yn=:useYn,version_no=version_no+1,updated_by=:operatorId,updated_at=CURRENT_TIMESTAMP(3) WHERE position_code=:positionCode AND version_no=:expectedVersion",v);}

 public List<Map<String,Object>> findJobTitles(){return jobTitlePage(new CpfPageRequest(0,200)).content();}
 public CpfPage<Map<String,Object>> jobTitlePage(CpfPageRequest p){return page("""
  SELECT job_title_code AS jobTitleCode,job_title_name AS jobTitleName,manager_yn AS managerYn,use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt
  FROM bza_job_title ORDER BY job_title_code LIMIT :limit OFFSET :offset
  ""","SELECT COUNT(*) FROM bza_job_title",Map.of(),p);}
 public Optional<Map<String,Object>> findJobTitle(String code){return one("SELECT job_title_code AS jobTitleCode,version_no AS versionNo FROM bza_job_title WHERE job_title_code=:code",new MapSqlParameterSource("code",code));}
 public int insertJobTitle(Map<String,?>v){return jdbc().update("INSERT INTO bza_job_title(job_title_code,job_title_name,manager_yn,use_yn,version_no,created_by,updated_by) VALUES(:jobTitleCode,:jobTitleName,:managerYn,:useYn,0,:operatorId,:operatorId)",v);}
 public int updateJobTitle(Map<String,?>v){return jdbc().update("UPDATE bza_job_title SET job_title_name=:jobTitleName,manager_yn=:managerYn,use_yn=:useYn,version_no=version_no+1,updated_by=:operatorId,updated_at=CURRENT_TIMESTAMP(3) WHERE job_title_code=:jobTitleCode AND version_no=:expectedVersion",v);}

 public List<Map<String,Object>> findAssignments(String employeeNo,String organizationCode,Instant at){return assignmentPage(employeeNo,organizationCode,at,new CpfPageRequest(0,200)).content();}
 public CpfPage<Map<String,Object>> assignmentPage(String employeeNo,String org,Instant at,CpfPageRequest p){Map<String,Object>m=new LinkedHashMap<>();m.put("employeeNo",empty(employeeNo));m.put("organizationCode",empty(org));m.put("effectiveAt",Timestamp.from(at));return page("""
  SELECT assignment_id AS assignmentId,employee_no AS employeeNo,organization_code AS organizationCode,position_code AS positionCode,job_title_code AS jobTitleCode,
         assignment_type AS assignmentType,primary_yn AS primaryYn,effective_from AS effectiveFrom,effective_to AS effectiveTo,version_no AS versionNo,updated_at AS updatedAt
    FROM bza_employee_assignment WHERE (:employeeNo IS NULL OR employee_no=:employeeNo) AND (:organizationCode IS NULL OR organization_code=:organizationCode)
     AND effective_from<=:effectiveAt AND (effective_to IS NULL OR effective_to>:effectiveAt)
   ORDER BY employee_no,primary_yn DESC,effective_from DESC,assignment_id DESC LIMIT :limit OFFSET :offset
  ""","SELECT COUNT(*) FROM bza_employee_assignment WHERE (:employeeNo IS NULL OR employee_no=:employeeNo) AND (:organizationCode IS NULL OR organization_code=:organizationCode) AND effective_from<=:effectiveAt AND (effective_to IS NULL OR effective_to>:effectiveAt)",m,p);}
 public void lockEmployee(String employeeNo){jdbc().queryForList("SELECT employee_id FROM bza_employee WHERE employee_no=:employeeNo FOR UPDATE",new MapSqlParameterSource("employeeNo",employeeNo));}
 public long countOverlappingPrimaryAssignment(String employeeNo,Instant from,Instant to,Long exclude){Long c=jdbc().queryForObject("""
   SELECT COUNT(*) FROM bza_employee_assignment WHERE employee_no=:employeeNo AND primary_yn='Y' AND (:exclude IS NULL OR assignment_id<>:exclude)
    AND (effective_to IS NULL OR effective_to>:from) AND (:to IS NULL OR effective_from<:to)
  """,new MapSqlParameterSource().addValue("employeeNo",employeeNo).addValue("from",Timestamp.from(from)).addValue("to",to==null?null:Timestamp.from(to)).addValue("exclude",exclude),Long.class);return c==null?0:c;}
 public int saveAssignment(Map<String,?>v){if(v.get("assignmentId")==null)return jdbc().update("""
   INSERT INTO bza_employee_assignment(employee_no,organization_code,position_code,job_title_code,assignment_type,primary_yn,effective_from,effective_to,version_no,created_by,updated_by)
   VALUES(:employeeNo,:organizationCode,:positionCode,:jobTitleCode,:assignmentType,:primaryYn,:effectiveFrom,:effectiveTo,0,:operatorId,:operatorId)
   """,v);return jdbc().update("""
   UPDATE bza_employee_assignment SET employee_no=:employeeNo,organization_code=:organizationCode,position_code=:positionCode,job_title_code=:jobTitleCode,assignment_type=:assignmentType,
      primary_yn=:primaryYn,effective_from=:effectiveFrom,effective_to=:effectiveTo,version_no=version_no+1,updated_by=:operatorId,updated_at=CURRENT_TIMESTAMP(3)
    WHERE assignment_id=:assignmentId AND version_no=:expectedVersion
   """,v);}

 public List<Map<String,Object>> findResponsibilities(String org,Instant at){return responsibilityPage(org,at,new CpfPageRequest(0,200)).content();}
 public CpfPage<Map<String,Object>> responsibilityPage(String org,Instant at,CpfPageRequest p){Map<String,Object>m=new LinkedHashMap<>();m.put("organizationCode",empty(org));m.put("effectiveAt",Timestamp.from(at));return page("""
  SELECT responsibility_id AS responsibilityId,organization_code AS organizationCode,responsibility_type AS responsibilityType,employee_no AS employeeNo,
         effective_from AS effectiveFrom,effective_to AS effectiveTo,priority_no AS priorityNo,use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt
    FROM bza_organization_responsibility WHERE (:organizationCode IS NULL OR organization_code=:organizationCode) AND effective_from<=:effectiveAt AND (effective_to IS NULL OR effective_to>:effectiveAt)
   ORDER BY organization_code,responsibility_type,priority_no,effective_from DESC LIMIT :limit OFFSET :offset
  ""","SELECT COUNT(*) FROM bza_organization_responsibility WHERE (:organizationCode IS NULL OR organization_code=:organizationCode) AND effective_from<=:effectiveAt AND (effective_to IS NULL OR effective_to>:effectiveAt)",m,p);}
 public int saveResponsibility(Map<String,?>v){if(v.get("responsibilityId")==null)return jdbc().update("""
   INSERT INTO bza_organization_responsibility(organization_code,responsibility_type,employee_no,effective_from,effective_to,priority_no,use_yn,version_no,created_by,updated_by)
   VALUES(:organizationCode,:responsibilityType,:employeeNo,:effectiveFrom,:effectiveTo,:priorityNo,:useYn,0,:operatorId,:operatorId)
  """,v);return jdbc().update("""
   UPDATE bza_organization_responsibility SET organization_code=:organizationCode,responsibility_type=:responsibilityType,employee_no=:employeeNo,effective_from=:effectiveFrom,effective_to=:effectiveTo,
     priority_no=:priorityNo,use_yn=:useYn,version_no=version_no+1,updated_by=:operatorId,updated_at=CURRENT_TIMESTAMP(3)
   WHERE responsibility_id=:responsibilityId AND version_no=:expectedVersion
  """,v);}

 public List<Map<String,Object>> findUserRoles(String loginId,Instant at){return userRolePage(loginId,at,new CpfPageRequest(0,200)).content();}
 public CpfPage<Map<String,Object>> userRolePage(String loginId,Instant at,CpfPageRequest p){Map<String,Object>m=new LinkedHashMap<>();m.put("loginId",empty(loginId));m.put("effectiveAt",Timestamp.from(at));return page("""
  SELECT ur.user_role_id AS userRoleId,u.admin_login_id AS loginId,ur.admin_user_id AS adminUserId,ur.role_code AS roleCode,r.role_name AS roleName,
         ur.valid_from AS validFrom,ur.valid_to AS validTo,ur.primary_yn AS primaryYn,ur.grant_reason AS grantReason,ur.operation_id AS operationId,ur.version_no AS versionNo
    FROM bza_user_role ur JOIN bza_admin_user u ON u.admin_user_id=ur.admin_user_id JOIN bza_role r ON r.role_code=ur.role_code
   WHERE (:loginId IS NULL OR u.admin_login_id=:loginId) AND (ur.valid_from IS NULL OR ur.valid_from<=:effectiveAt) AND (ur.valid_to IS NULL OR ur.valid_to>:effectiveAt)
   ORDER BY u.admin_login_id,ur.primary_yn DESC,ur.created_at DESC LIMIT :limit OFFSET :offset
  ""","SELECT COUNT(*) FROM bza_user_role ur JOIN bza_admin_user u ON u.admin_user_id=ur.admin_user_id WHERE (:loginId IS NULL OR u.admin_login_id=:loginId) AND (ur.valid_from IS NULL OR ur.valid_from<=:effectiveAt) AND (ur.valid_to IS NULL OR ur.valid_to>:effectiveAt)",m,p);}
 public Optional<Map<String,Object>> findUserRoleByOperationId(String operationId){if(operationId==null)return Optional.empty();return one("""
   SELECT ur.user_role_id AS userRoleId,u.admin_login_id AS loginId,ur.role_code AS roleCode,ur.operation_id AS operationId,ur.primary_yn AS primaryYn,ur.valid_from AS validFrom,ur.valid_to AS validTo
   FROM bza_user_role ur JOIN bza_admin_user u ON u.admin_user_id=ur.admin_user_id WHERE ur.operation_id=:operationId
  """,new MapSqlParameterSource("operationId",operationId));}
 public long lockUserAndId(String loginId){List<Map<String,Object>>r=jdbc().queryForList("SELECT admin_user_id AS adminUserId FROM bza_admin_user WHERE admin_login_id=:loginId FOR UPDATE",new MapSqlParameterSource("loginId",loginId));if(r.isEmpty())return 0;return ((Number)r.get(0).get("adminUserId")).longValue();}
 public void clearPrimaryRoles(long adminUserId,Instant from,Instant to,String actor){jdbc().update("""
   UPDATE bza_user_role SET primary_yn='N',version_no=version_no+1,updated_by=:actor,updated_at=CURRENT_TIMESTAMP(3)
    WHERE admin_user_id=:id AND primary_yn='Y' AND (valid_to IS NULL OR valid_to>:from) AND (:to IS NULL OR valid_from IS NULL OR valid_from<:to)
  """,new MapSqlParameterSource().addValue("id",adminUserId).addValue("from",Timestamp.from(from)).addValue("to",to==null?null:Timestamp.from(to)).addValue("actor",actor));}
 public int insertUserRole(long adminUserId,Map<String,?>v){MapSqlParameterSource p=new MapSqlParameterSource(v).addValue("adminUserId",adminUserId);return jdbc().update("""
   INSERT INTO bza_user_role(admin_user_id,role_code,valid_from,valid_to,primary_yn,grant_reason,operation_id,version_no,created_by,updated_by)
   VALUES(:adminUserId,:roleCode,:validFrom,:validTo,:primaryYn,:grantReason,:operationId,0,:operatorId,:operatorId)
  """,p);}
 public void syncLegacyPrimaryRole(long id,String role,String actor){jdbc().update("UPDATE bza_admin_user SET role_code=:role,updated_by=:actor,updated_at=CURRENT_TIMESTAMP WHERE admin_user_id=:id",new MapSqlParameterSource().addValue("role",role).addValue("actor",actor).addValue("id",id));}

 private Optional<Map<String,Object>> one(String sql,MapSqlParameterSource p){return jdbc().queryForList(sql,p).stream().findFirst();}
 private CpfPage<Map<String,Object>> page(String sql,String countSql,Map<String,?>m,CpfPageRequest p){MapSqlParameterSource q=new MapSqlParameterSource(m).addValue("limit",p.size()).addValue("offset",p.offset());Long total=jdbc().queryForObject(countSql,new MapSqlParameterSource(m),Long.class);return new CpfPage<>(jdbc().queryForList(sql,q),total==null?0:total,p.page(),p.size());}
 private NamedParameterJdbcTemplate jdbc(){NamedParameterJdbcTemplate j=provider.getIfAvailable();if(j==null)throw new IllegalStateException("BZA datasource가 구성되지 않았습니다.");return j;}
 private static String empty(String v){return v==null||v.isBlank()?null:v.trim();}
}

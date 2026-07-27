package com.cpf.bizadmin.directory.repository;

import com.cpf.core.api.database.CpfVendorSqlCatalog;
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
 private final CpfVendorSqlCatalog sql;
 public BzaDirectoryRepository(
         @Qualifier("bzaJdbcTemplate")ObjectProvider<NamedParameterJdbcTemplate> provider,
         com.cpf.core.api.database.CpfVendorSqlCatalogProvider sqlCatalogProvider){
   this.provider=provider;
   this.sql=sqlCatalogProvider.forModule("bza");
 }

 public List<Map<String,Object>> findPositions(){return positionPage(new CpfPageRequest(0,200)).content();}
 public CpfPage<Map<String,Object>> positionPage(CpfPageRequest p){return page(sql.required("directory-repository-position-page-01"),sql.required("directory-repository-position-page-02"),Map.of(),p);}
 public Optional<Map<String,Object>> findPosition(String code){return one(sql.required("directory-repository-find-position-01"),new MapSqlParameterSource("code",code));}
 public int insertPosition(Map<String,?>v){return jdbc().update(sql.required("directory-repository-insert-position-01"),v);}
 public int updatePosition(Map<String,?>v){return jdbc().update(sql.required("directory-repository-update-position-01"),v);}

 public List<Map<String,Object>> findJobTitles(){return jobTitlePage(new CpfPageRequest(0,200)).content();}
 public CpfPage<Map<String,Object>> jobTitlePage(CpfPageRequest p){return page(sql.required("directory-repository-job-title-page-01"),sql.required("directory-repository-job-title-page-02"),Map.of(),p);}
 public Optional<Map<String,Object>> findJobTitle(String code){return one(sql.required("directory-repository-find-job-title-01"),new MapSqlParameterSource("code",code));}
 public int insertJobTitle(Map<String,?>v){return jdbc().update(sql.required("directory-repository-insert-job-title-01"),v);}
 public int updateJobTitle(Map<String,?>v){return jdbc().update(sql.required("directory-repository-update-job-title-01"),v);}

 public List<Map<String,Object>> findAssignments(String employeeNo,String organizationCode,Instant at){return assignmentPage(employeeNo,organizationCode,at,new CpfPageRequest(0,200)).content();}
 public CpfPage<Map<String,Object>> assignmentPage(String employeeNo,String org,Instant at,CpfPageRequest p){Map<String,Object>m=new LinkedHashMap<>();m.put("employeeNo",empty(employeeNo));m.put("organizationCode",empty(org));m.put("effectiveAt",Timestamp.from(at));return page(sql.required("directory-repository-assignment-page-01"),sql.required("directory-repository-assignment-page-02"),m,p);}
 public void lockEmployee(String employeeNo){jdbc().queryForList(sql.required("directory-repository-lock-employee-01"),new MapSqlParameterSource("employeeNo",employeeNo));}
 public long countOverlappingPrimaryAssignment(String employeeNo,Instant from,Instant to,Long exclude){Long c=jdbc().queryForObject(sql.required("directory-repository-count-overlapping-primary-assignment-01"),new MapSqlParameterSource().addValue("employeeNo",employeeNo).addValue("from",Timestamp.from(from)).addValue("to",to==null?null:Timestamp.from(to)).addValue("exclude",exclude),Long.class);return c==null?0:c;}
 public int saveAssignment(Map<String,?>v){if(v.get("assignmentId")==null)return jdbc().update(sql.required("directory-repository-save-assignment-01"),v);return jdbc().update(sql.required("directory-repository-save-assignment-02"),v);}

 public List<Map<String,Object>> findResponsibilities(String org,Instant at){return responsibilityPage(org,at,new CpfPageRequest(0,200)).content();}
 public CpfPage<Map<String,Object>> responsibilityPage(String org,Instant at,CpfPageRequest p){Map<String,Object>m=new LinkedHashMap<>();m.put("organizationCode",empty(org));m.put("effectiveAt",Timestamp.from(at));return page(sql.required("directory-repository-responsibility-page-01"),sql.required("directory-repository-responsibility-page-02"),m,p);}
 public int saveResponsibility(Map<String,?>v){if(v.get("responsibilityId")==null)return jdbc().update(sql.required("directory-repository-save-responsibility-01"),v);return jdbc().update(sql.required("directory-repository-save-responsibility-02"),v);}

 public List<Map<String,Object>> findUserRoles(String loginId,Instant at){return userRolePage(loginId,at,new CpfPageRequest(0,200)).content();}
 public CpfPage<Map<String,Object>> userRolePage(String loginId,Instant at,CpfPageRequest p){Map<String,Object>m=new LinkedHashMap<>();m.put("loginId",empty(loginId));m.put("effectiveAt",Timestamp.from(at));return page(sql.required("directory-repository-user-role-page-01"),sql.required("directory-repository-user-role-page-02"),m,p);}
 public Optional<Map<String,Object>> findUserRoleByOperationId(String operationId){if(operationId==null)return Optional.empty();return one(sql.required("directory-repository-find-user-role-by-operation-id-01"),new MapSqlParameterSource("operationId",operationId));}
 public long lockUserAndId(String loginId){List<Map<String,Object>>r=jdbc().queryForList(sql.required("directory-repository-lock-user-and-id-01"),new MapSqlParameterSource("loginId",loginId));if(r.isEmpty())return 0;return ((Number)r.get(0).get("adminUserId")).longValue();}
 public void clearPrimaryRoles(long adminUserId,Instant from,Instant to,String actor){jdbc().update(sql.required("directory-repository-clear-primary-roles-01"),new MapSqlParameterSource().addValue("id",adminUserId).addValue("from",Timestamp.from(from)).addValue("to",to==null?null:Timestamp.from(to)).addValue("actor",actor));}
 public int insertUserRole(long adminUserId,Map<String,?>v){MapSqlParameterSource p=new MapSqlParameterSource(v).addValue("adminUserId",adminUserId);return jdbc().update(sql.required("directory-repository-insert-user-role-01"),p);}
 public void syncLegacyPrimaryRole(long id,String role,String actor){jdbc().update(sql.required("directory-repository-sync-legacy-primary-role-01"),new MapSqlParameterSource().addValue("role",role).addValue("actor",actor).addValue("id",id));}

 private Optional<Map<String,Object>> one(String sql,MapSqlParameterSource p){return jdbc().queryForList(sql,p).stream().findFirst();}
 private CpfPage<Map<String,Object>> page(String sql,String countSql,Map<String,?>m,CpfPageRequest p){MapSqlParameterSource q=new MapSqlParameterSource(m).addValue("limit",p.size()).addValue("offset",p.offset());Long total=jdbc().queryForObject(countSql,new MapSqlParameterSource(m),Long.class);return new CpfPage<>(jdbc().queryForList(sql,q),total==null?0:total,p.page(),p.size());}
 private NamedParameterJdbcTemplate jdbc(){NamedParameterJdbcTemplate j=provider.getIfAvailable();if(j==null)throw new IllegalStateException("BZA datasource가 구성되지 않았습니다.");return j;}
 private static String empty(String v){return v==null||v.isBlank()?null:v.trim();}
}

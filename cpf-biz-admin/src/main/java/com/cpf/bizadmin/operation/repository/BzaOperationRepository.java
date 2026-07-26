package com.cpf.bizadmin.operation.repository;

import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** BZA 운영 정본 Repository. 조회는 서버 Paging, 변경은 version_no CAS를 사용합니다. */
@Repository
public class BzaOperationRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;
    public BzaOperationRepository(@Qualifier("bzaJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    public List<Map<String,Object>> findAdminUsers(){ return adminUserPage(new CpfPageRequest(0,200)).content(); }
    public CpfPage<Map<String,Object>> adminUserPage(CpfPageRequest p){ return page("""
        SELECT admin_user_id AS adminUserId,admin_login_id AS adminLoginId,admin_name AS adminName,
               role_code AS roleCode,use_yn AS useYn,lock_yn AS lockYn,password_change_required_yn AS passwordChangeRequiredYn,
               last_login_at AS lastLoginAt,created_at AS createdAt,updated_at AS updatedAt
          FROM bza_admin_user ORDER BY admin_user_id DESC LIMIT :limit OFFSET :offset
        """, "SELECT COUNT(*) FROM bza_admin_user", Map.of(), p); }
    public Optional<Map<String,Object>> findAdminUser(String loginId){ return jdbc().queryForList("""
        SELECT admin_user_id AS adminUserId,admin_login_id AS adminLoginId,admin_name AS adminName,role_code AS roleCode,
               use_yn AS useYn,lock_yn AS lockYn,password_change_required_yn AS passwordChangeRequiredYn
          FROM bza_admin_user WHERE admin_login_id=:loginId
        """, new MapSqlParameterSource("loginId",loginId)).stream().findFirst(); }
    public void saveAdminUser(Map<String,?> v){ jdbc().update("""
        INSERT INTO bza_admin_user(admin_login_id,admin_name,password_hash,role_code,use_yn,lock_yn,password_change_required_yn,created_by,updated_by)
        VALUES(:loginId,:adminName,:passwordHash,:roleCode,:useYn,:lockYn,:passwordChangeRequiredYn,:requestUser,:requestUser)
        ON DUPLICATE KEY UPDATE admin_name=VALUES(admin_name),password_hash=COALESCE(VALUES(password_hash),password_hash),
          use_yn=VALUES(use_yn),lock_yn=VALUES(lock_yn),password_change_required_yn=VALUES(password_change_required_yn),
          updated_by=VALUES(updated_by),updated_at=CURRENT_TIMESTAMP
        """,v); }
    public void ensureInitialUserRole(String loginId,String roleCode,String actor){
        jdbc().update("""
          INSERT INTO bza_user_role(admin_user_id,role_code,valid_from,primary_yn,grant_reason,operation_id,version_no,created_by,updated_by)
          SELECT admin_user_id,:roleCode,CURRENT_TIMESTAMP(3),'Y','INITIAL_ROLE',CONCAT('INIT:',admin_user_id,':',:roleCode),0,:actor,:actor
            FROM bza_admin_user WHERE admin_login_id=:loginId
          ON DUPLICATE KEY UPDATE updated_by=VALUES(updated_by)
          """,new MapSqlParameterSource().addValue("loginId",loginId).addValue("roleCode",roleCode).addValue("actor",actor));
        syncLegacyPrimaryRole(loginId,roleCode,actor);
    }
    public void syncLegacyPrimaryRole(String loginId,String roleCode,String actor){ jdbc().update("""
        UPDATE bza_admin_user SET role_code=:roleCode,updated_by=:actor,updated_at=CURRENT_TIMESTAMP WHERE admin_login_id=:loginId
        """,new MapSqlParameterSource().addValue("loginId",loginId).addValue("roleCode",roleCode).addValue("actor",actor)); }

    public List<Map<String,Object>> findMenus(){ return menuPage(new CpfPageRequest(0,200)).content(); }
    public CpfPage<Map<String,Object>> menuPage(CpfPageRequest p){ return page("""
        SELECT menu_id AS menuId,menu_code AS menuCode,menu_name AS menuName,parent_menu_code AS parentMenuCode,module_code AS moduleCode,
               route_path AS routePath,icon_code AS iconCode,environment_code AS environmentCode,api_path AS apiPath,sort_order AS sortOrder,
               use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt
          FROM bza_menu ORDER BY module_code,sort_order,menu_code LIMIT :limit OFFSET :offset
        ""","SELECT COUNT(*) FROM bza_menu",Map.of(),p); }
    public Optional<Map<String,Object>> findMenu(String code){return jdbc().queryForList("""
        SELECT menu_code AS menuCode,menu_name AS menuName,parent_menu_code AS parentMenuCode,module_code AS moduleCode,
               route_path AS routePath,icon_code AS iconCode,environment_code AS environmentCode,api_path AS apiPath,sort_order AS sortOrder,
               use_yn AS useYn,version_no AS versionNo FROM bza_menu WHERE menu_code=:code
        """,new MapSqlParameterSource("code",code)).stream().findFirst();}
    public int insertMenu(Map<String,?> v){return jdbc().update("""
        INSERT INTO bza_menu(menu_code,menu_name,parent_menu_code,module_code,route_path,icon_code,environment_code,api_path,sort_order,use_yn,version_no,created_by,updated_by)
        VALUES(:menuCode,:menuName,:parentMenuCode,:moduleCode,:routePath,:iconCode,:environmentCode,:apiPath,:sortOrder,:useYn,0,:requestUser,:requestUser)
        """,v);}
    public int updateMenu(Map<String,?> v){return jdbc().update("""
        UPDATE bza_menu SET menu_name=:menuName,parent_menu_code=:parentMenuCode,module_code=:moduleCode,route_path=:routePath,icon_code=:iconCode,
               environment_code=:environmentCode,api_path=:apiPath,sort_order=:sortOrder,use_yn=:useYn,version_no=version_no+1,
               updated_by=:requestUser,updated_at=CURRENT_TIMESTAMP
         WHERE menu_code=:menuCode AND version_no=:expectedVersion
        """,v);}

    public List<Map<String,Object>> findRoles(){ return rolePage(new CpfPageRequest(0,200)).content(); }
    public CpfPage<Map<String,Object>> rolePage(CpfPageRequest p){return page("""
        SELECT role_id AS roleId,role_code AS roleCode,role_name AS roleName,write_allowed_yn AS writeAllowedYn,data_scope AS dataScope,
               use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt FROM bza_role ORDER BY role_code LIMIT :limit OFFSET :offset
        ""","SELECT COUNT(*) FROM bza_role",Map.of(),p);}
    public Optional<Map<String,Object>> findRole(String code){return jdbc().queryForList("""
        SELECT role_code AS roleCode,role_name AS roleName,write_allowed_yn AS writeAllowedYn,data_scope AS dataScope,use_yn AS useYn,version_no AS versionNo
        FROM bza_role WHERE role_code=:code
        """,new MapSqlParameterSource("code",code)).stream().findFirst();}
    public int insertRole(Map<String,?> v){return jdbc().update("""
        INSERT INTO bza_role(role_code,role_name,write_allowed_yn,data_scope,use_yn,version_no,created_by,updated_by)
        VALUES(:roleCode,:roleName,:writeAllowedYn,:dataScope,:useYn,0,:requestUser,:requestUser)
        """,v);}
    public int updateRole(Map<String,?> v){return jdbc().update("""
        UPDATE bza_role SET role_name=:roleName,write_allowed_yn=:writeAllowedYn,data_scope=:dataScope,use_yn=:useYn,
               version_no=version_no+1,updated_by=:requestUser,updated_at=CURRENT_TIMESTAMP
         WHERE role_code=:roleCode AND version_no=:expectedVersion
        """,v);}

    public List<Map<String,Object>> findPermissions(){ return permissionPage(new CpfPageRequest(0,200)).content(); }
    public CpfPage<Map<String,Object>> permissionPage(CpfPageRequest p){return page("""
        SELECT permission_id AS permissionId,role_code AS roleCode,menu_code AS menuCode,button_code AS buttonCode,
               permission_type AS permissionType,http_method AS httpMethod,api_pattern AS apiPattern,domain_code AS domainCode,
               environment_code AS environmentCode,data_scope AS dataScope,allow_yn AS allowYn,use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt
          FROM bza_permission ORDER BY role_code,menu_code,button_code,permission_id LIMIT :limit OFFSET :offset
        ""","SELECT COUNT(*) FROM bza_permission",Map.of(),p);}
    public Optional<Map<String,Object>> findPermission(Long id){ if(id==null)return Optional.empty(); return jdbc().queryForList("""
        SELECT permission_id AS permissionId,role_code AS roleCode,menu_code AS menuCode,button_code AS buttonCode,permission_type AS permissionType,
               http_method AS httpMethod,api_pattern AS apiPattern,domain_code AS domainCode,environment_code AS environmentCode,data_scope AS dataScope,
               allow_yn AS allowYn,use_yn AS useYn,version_no AS versionNo FROM bza_permission WHERE permission_id=:id
        """,new MapSqlParameterSource("id",id)).stream().findFirst();}
    public int insertPermission(Map<String,?> v){return jdbc().update("""
        INSERT INTO bza_permission(role_code,menu_code,button_code,permission_type,http_method,api_pattern,domain_code,environment_code,data_scope,allow_yn,use_yn,version_no,created_by,updated_by)
        VALUES(:roleCode,:menuCode,:buttonCode,:permissionType,:httpMethod,:apiPattern,:domainCode,:environmentCode,:dataScope,:allowYn,:useYn,0,:requestUser,:requestUser)
        """,v);}
    public int updatePermission(Map<String,?> v){return jdbc().update("""
        UPDATE bza_permission SET role_code=:roleCode,menu_code=:menuCode,button_code=:buttonCode,permission_type=:permissionType,http_method=:httpMethod,
               api_pattern=:apiPattern,domain_code=:domainCode,environment_code=:environmentCode,data_scope=:dataScope,allow_yn=:allowYn,use_yn=:useYn,
               version_no=version_no+1,updated_by=:requestUser,updated_at=CURRENT_TIMESTAMP
         WHERE permission_id=:permissionId AND version_no=:expectedVersion
        """,v);}

    public List<Map<String,Object>> findSettings(){return jdbc().queryForList("SELECT setting_id AS settingId,setting_key AS settingKey,setting_value AS settingValue,description,use_yn AS useYn,updated_at AS updatedAt FROM bza_project_setting ORDER BY setting_key",Map.of());}
    public List<Map<String,Object>> findDownloadPolicies(){return jdbc().queryForList("SELECT setting_key AS policyKey,setting_value AS policyValue,description,use_yn AS useYn,updated_at AS updatedAt FROM bza_project_setting WHERE setting_key LIKE 'DOWNLOAD.%' ORDER BY setting_key",Map.of());}

    public void insertBusinessAudit(Map<String,?> v){jdbc().update("""
        INSERT INTO bza_business_audit(transaction_id,actor_id,action_type,target_type,target_id,reason,before_data,after_data,created_by,updated_by)
        VALUES(:transactionId,:actorId,:actionType,:targetType,:targetId,:reason,:beforeData,:afterData,:actorId,:actorId)
        """,v);}

    private CpfPage<Map<String,Object>> page(String sql,String countSql,Map<String,?> params,CpfPageRequest p){
        MapSqlParameterSource q=new MapSqlParameterSource(params).addValue("limit",p.size()).addValue("offset",p.offset());
        Long total=jdbc().queryForObject(countSql,new MapSqlParameterSource(params),Long.class);
        return new CpfPage<>(jdbc().queryForList(sql,q),total==null?0:total,p.page(),p.size());
    }
    private NamedParameterJdbcTemplate jdbc(){NamedParameterJdbcTemplate j=jdbcTemplateProvider.getIfAvailable(); if(j==null)throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"BZA DB datasource가 비활성화되어 운영 저장소를 사용할 수 없습니다."); return j;}
}

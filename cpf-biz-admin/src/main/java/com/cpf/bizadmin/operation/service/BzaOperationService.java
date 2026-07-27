package com.cpf.bizadmin.operation.service;

import com.cpf.bizadmin.operation.repository.BzaOperationRepository;
import com.cpf.bizadmin.audit.service.BzaBusinessAuditService;
import com.cpf.bizadmin.common.model.BzaAdminAccountStatus;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.util.CpfStrings;
import com.cpf.core.api.security.password.CpfPasswordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/** BZA 사용자/메뉴/Role/Permission 운영 서비스. */
@Service
public class BzaOperationService extends com.cpf.bizadmin.common.base.BzaBaseService {
 private static final Set<String> HTTP_METHODS=Set.of("GET","POST","PUT","PATCH","DELETE","ALL");
 private final BzaOperationRepository repository; private final CpfPasswordService passwordHashingPort; private final BzaBusinessAuditService auditService;
 public BzaOperationService(BzaOperationRepository repository,CpfPasswordService passwordHashingPort,BzaBusinessAuditService auditService){this.repository=repository;this.passwordHashingPort=passwordHashingPort;this.auditService=auditService;}
 public List<Map<String,Object>> findAdminUsers(){return repository.findAdminUsers();}
 public CpfPage<Map<String,Object>> findAdminUsersPage(Integer page,Integer size){return repository.adminUserPage(CpfPageRequest.of(page,size));}
 public List<Map<String,Object>> findMenus(){return repository.findMenus();}
 public CpfPage<Map<String,Object>> findMenusPage(Integer page,Integer size){return repository.menuPage(CpfPageRequest.of(page,size));}
 public List<Map<String,Object>> findRoles(){return repository.findRoles();}
 public CpfPage<Map<String,Object>> findRolesPage(Integer page,Integer size){return repository.rolePage(CpfPageRequest.of(page,size));}
 public List<Map<String,Object>> findPermissions(){return repository.findPermissions();}
 public CpfPage<Map<String,Object>> findPermissionsPage(Integer page,Integer size){return repository.permissionPage(CpfPageRequest.of(page,size));}
 public List<Map<String,Object>> findSettings(){return repository.findSettings();} public List<Map<String,Object>> findDownloadPolicies(){return repository.findDownloadPolicies();}

 @Transactional(transactionManager="bzaTransactionManager")
 public Map<String,Object> saveAdminUser(AdminUserRequest r,String operatorId){
   String login=required(r.loginId(),"loginId"), actor=required(operatorId,"operatorId");
   Map<String,Object> before=repository.findAdminUser(login).orElse(null);
   boolean create=before==null;
   if(create && r.roleCode()!=null && !r.roleCode().isBlank()) {
     throw new CpfValidationException("신규 관리자는 Role을 자동 부여하지 않습니다. 생성 후 사용자 Role 이력에서 명시적으로 부여하십시오.");
   }
   if(!create && r.expectedVersion()==null) throw new CpfValidationException("관리자 수정에는 expectedVersion이 필요합니다.");
   String accountStatus=BzaAdminAccountStatus.parse(
           create?"PENDING_ACTIVATION":defaultText(r.accountStatus(),String.valueOf(before.get("accountStatus")))).name();
   if("ACTIVE".equals(accountStatus) && repository.countEffectiveRoles(login)==0)
     throw new CpfValidationException("Role이 없는 관리자는 ACTIVE로 전환할 수 없습니다.");
   Map<String,Object> v=new LinkedHashMap<>();
   v.put("loginId",login); v.put("adminName",required(r.adminName(),"adminName"));
   v.put("passwordHash",hashPassword(r.rawPassword(),create));
   v.put("accountStatus",accountStatus);
   String useYn = "DISABLED".equals(accountStatus) ? "N" : yn(r.useYn(),"Y");
   v.put("useYn",useYn);
   v.put("lockYn","LOCKED".equals(accountStatus)?"Y":"N");
   v.put("passwordChangeRequiredYn",yn(r.passwordChangeRequiredYn(),create?"Y":"N"));
   v.put("expectedVersion",r.expectedVersion()); v.put("requestUser",actor);
   int changed=create?repository.insertAdminUser(v):repository.updateAdminUser(v);
   if(changed!=1) throw new CpfValidationException("관리자 정보가 다른 관리자에 의해 변경되었습니다. 다시 조회하십시오.");
   audit(actor,"ADMIN_USER_SAVE","bza_admin_user",login,required(r.reason(),"reason"),before,withoutSecret(v));
   return withoutSecret(v);
 }

 @Transactional(transactionManager="bzaTransactionManager") public Map<String,Object> saveMenu(MenuRequest r,String operatorId){
   String key=code(r.menuCode(),"menuCode"), actor=required(operatorId,"operatorId"); Map<String,Object> before=repository.findMenu(key).orElse(null); Map<String,Object> v=new LinkedHashMap<>();
   v.put("menuCode",key);v.put("menuName",required(r.menuName(),"menuName"));v.put("parentMenuCode",blank(r.parentMenuCode()));v.put("moduleCode",code(defaultText(r.moduleCode(),"BZA"),"moduleCode"));v.put("routePath",blank(r.routePath()));v.put("iconCode",blank(r.iconCode()));v.put("environmentCode",code(defaultText(r.environmentCode(),"ALL"),"environmentCode"));v.put("apiPath",blank(r.apiPath()));v.put("sortOrder",r.sortOrder()==null?0:r.sortOrder());v.put("useYn",yn(r.useYn(),"Y"));v.put("requestUser",actor);v.put("expectedVersion",r.expectedVersion());
   int changed=before==null?repository.insertMenu(v):repository.updateMenu(requireVersion(v,r.expectedVersion(),"menu")); if(changed!=1)throw new CpfValidationException("메뉴가 다른 관리자에 의해 변경되었습니다. 다시 조회하십시오."); audit(actor,"MENU_SAVE","bza_menu",key,required(r.reason(),"reason"),before,v); return v;
 }

 @Transactional(transactionManager="bzaTransactionManager") public Map<String,Object> saveRole(RoleRequest r,String operatorId){
   String key=code(r.roleCode(),"roleCode"),actor=required(operatorId,"operatorId");Map<String,Object> before=repository.findRole(key).orElse(null);Map<String,Object> v=new LinkedHashMap<>();v.put("roleCode",key);v.put("roleName",required(r.roleName(),"roleName"));v.put("writeAllowedYn",yn(r.writeAllowedYn(),"N"));v.put("dataScope",code(defaultText(r.dataScope(),"OWN"),"dataScope"));v.put("useYn",yn(r.useYn(),"Y"));v.put("requestUser",actor);v.put("expectedVersion",r.expectedVersion());int changed=before==null?repository.insertRole(v):repository.updateRole(requireVersion(v,r.expectedVersion(),"role"));if(changed!=1)throw new CpfValidationException("Role이 다른 관리자에 의해 변경되었습니다. 다시 조회하십시오.");audit(actor,"ROLE_SAVE","bza_role",key,required(r.reason(),"reason"),before,v);return v;
 }

 @Transactional(transactionManager="bzaTransactionManager") public Map<String,Object> savePermission(PermissionRequest r,String operatorId){
   String actor=required(operatorId,"operatorId"),method=blank(r.httpMethod());if(method!=null){method=method.toUpperCase(Locale.ROOT);if(!HTTP_METHODS.contains(method))throw new CpfValidationException("허용되지 않은 HTTP 메서드입니다.");}
   Map<String,Object> before=repository.findPermission(r.permissionId()).orElse(null);Map<String,Object> v=new LinkedHashMap<>();v.put("permissionId",r.permissionId());v.put("roleCode",code(r.roleCode(),"roleCode"));v.put("menuCode",code(r.menuCode(),"menuCode"));v.put("buttonCode",code(r.buttonCode(),"buttonCode"));v.put("permissionType",code(defaultText(r.permissionType(),"BUTTON"),"permissionType"));v.put("httpMethod",method);v.put("apiPattern",blank(r.apiPattern()));v.put("domainCode",blank(r.domainCode()));v.put("environmentCode",code(defaultText(r.environmentCode(),"ALL"),"environmentCode"));v.put("dataScope",code(defaultText(r.dataScope(),"ROLE"),"dataScope"));v.put("allowYn",yn(r.allowYn(),"N"));v.put("useYn",yn(r.useYn(),"Y"));v.put("requestUser",actor);v.put("expectedVersion",r.expectedVersion());int changed=before==null?repository.insertPermission(v):repository.updatePermission(requireVersion(v,r.expectedVersion(),"permission"));if(changed!=1)throw new CpfValidationException("Permission이 다른 관리자에 의해 변경되었습니다. 다시 조회하십시오.");audit(actor,"PERMISSION_SAVE","bza_permission",String.valueOf(r.permissionId()),required(r.reason(),"reason"),before,v);return v;
 }

 private Map<String,Object> requireVersion(Map<String,Object> v,Long version,String name){if(version==null)throw new CpfValidationException(name+" 수정에는 expectedVersion이 필요합니다.");return v;}
 private String hashPassword(String raw,boolean create){if(raw==null||raw.isBlank()){if(create)throw new CpfValidationException("신규 사용자의 rawPassword는 필수입니다.");return null;}if(raw.length()<12)throw new CpfValidationException("비밀번호는 12자 이상이어야 합니다.");char[] c=raw.toCharArray();try{return passwordHashingPort.hash(c);}finally{Arrays.fill(c,'\0');}}
 private void audit(String actor,String action,String type,String id,String reason,Object before,Object after){auditService.record(actor,action,type,id,reason,before,after);}
 private Map<String,Object> withoutSecret(Map<String,Object> v){Map<String,Object>r=new LinkedHashMap<>(v);r.remove("passwordHash");return r;}
 private String required(String v,String f){return CpfStrings.requireText(v,f);} private String code(String v,String f){return required(v,f).toUpperCase(Locale.ROOT);} private String defaultText(String v,String d){return v==null||v.isBlank()?d:v.trim();} private String blank(String v){return v==null||v.isBlank()?null:v.trim();} private String yn(String v,String d){String x=defaultText(v,d).toUpperCase(Locale.ROOT);if(!Set.of("Y","N").contains(x))throw new CpfValidationException("Y/N 값이 올바르지 않습니다.");return x;}
 public record AdminUserRequest(
         String loginId,String adminName,String roleCode,String rawPassword,
         String accountStatus,String useYn,String lockYn,String passwordChangeRequiredYn,
         Long expectedVersion,String requestUser,String reason){
   public AdminUserRequest(String loginId,String adminName,String roleCode,String rawPassword,
                           String useYn,String lockYn,String passwordChangeRequiredYn,
                           String requestUser,String reason){
     this(loginId,adminName,roleCode,rawPassword,null,useYn,lockYn,passwordChangeRequiredYn,null,requestUser,reason);
   }
 }
 public record MenuRequest(String menuCode,String menuName,String parentMenuCode,String moduleCode,String routePath,String iconCode,String environmentCode,String apiPath,Integer sortOrder,String useYn,Long expectedVersion,String requestUser,String reason){}
 public record RoleRequest(String roleCode,String roleName,String writeAllowedYn,String dataScope,String useYn,Long expectedVersion,String requestUser,String reason){}
 public record PermissionRequest(Long permissionId,String roleCode,String menuCode,String buttonCode,String permissionType,String httpMethod,String apiPattern,String domainCode,String environmentCode,String dataScope,String allowYn,String useYn,Long expectedVersion,String requestUser,String reason){}
}

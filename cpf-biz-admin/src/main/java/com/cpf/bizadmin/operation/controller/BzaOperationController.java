package com.cpf.bizadmin.operation.controller;
import com.cpf.bizadmin.operation.service.BzaOperationService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.page.CpfPage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** BZA 운영 API. 목록은 기존 호환 API와 서버 Paging API를 함께 제공합니다. */
@RestController @RequestMapping("/api/bza")
public class BzaOperationController extends com.cpf.bizadmin.common.base.BzaBaseController {
 private final BzaOperationService s; public BzaOperationController(BzaOperationService s){this.s=s;}
 @GetMapping("/admin-users") public ResponseEntity<List<Map<String,Object>>> users(){return ResponseEntity.ok(s.findAdminUsers());}
 @GetMapping("/admin-users/page") @CpfOnlineTransaction(id="OBZAAD1101",name="BzaAdminUserPage") public ResponseEntity<CpfPage<Map<String,Object>>> usersPage(@RequestParam(required=false)Integer page,@RequestParam(required=false)Integer size){return ResponseEntity.ok(s.findAdminUsersPage(page,size));}
 @PostMapping("/admin-users") public ResponseEntity<Map<String,Object>> saveUser(@RequestBody BzaOperationService.AdminUserRequest r,@RequestAttribute("bza.operatorId")String op){return ResponseEntity.ok(s.saveAdminUser(r,op));}
 @GetMapping("/menus") public ResponseEntity<List<Map<String,Object>>> menus(){return ResponseEntity.ok(s.findMenus());}
 @GetMapping("/menus/page") @CpfOnlineTransaction(id="OBZAMN1101",name="BzaMenuPage") public ResponseEntity<CpfPage<Map<String,Object>>> menusPage(@RequestParam(required=false)Integer page,@RequestParam(required=false)Integer size){return ResponseEntity.ok(s.findMenusPage(page,size));}
 @PostMapping("/menus") public ResponseEntity<Map<String,Object>> saveMenu(@RequestBody BzaOperationService.MenuRequest r,@RequestAttribute("bza.operatorId")String op){return ResponseEntity.ok(s.saveMenu(r,op));}
 @GetMapping("/roles") public ResponseEntity<List<Map<String,Object>>> roles(){return ResponseEntity.ok(s.findRoles());}
 @GetMapping("/roles/page") @CpfOnlineTransaction(id="OBZARO1101",name="BzaRolePage") public ResponseEntity<CpfPage<Map<String,Object>>> rolesPage(@RequestParam(required=false)Integer page,@RequestParam(required=false)Integer size){return ResponseEntity.ok(s.findRolesPage(page,size));}
 @PostMapping("/roles") public ResponseEntity<Map<String,Object>> saveRole(@RequestBody BzaOperationService.RoleRequest r,@RequestAttribute("bza.operatorId")String op){return ResponseEntity.ok(s.saveRole(r,op));}
 @GetMapping("/permissions") public ResponseEntity<List<Map<String,Object>>> perms(){return ResponseEntity.ok(s.findPermissions());}
 @GetMapping("/permissions/page") @CpfOnlineTransaction(id="OBZAPE1101",name="BzaPermissionPage") public ResponseEntity<CpfPage<Map<String,Object>>> permsPage(@RequestParam(required=false)Integer page,@RequestParam(required=false)Integer size){return ResponseEntity.ok(s.findPermissionsPage(page,size));}
 @PostMapping("/permissions") public ResponseEntity<Map<String,Object>> savePerm(@RequestBody BzaOperationService.PermissionRequest r,@RequestAttribute("bza.operatorId")String op){return ResponseEntity.ok(s.savePermission(r,op));}
 @GetMapping("/settings") public ResponseEntity<List<Map<String,Object>>> settings(){return ResponseEntity.ok(s.findSettings());}
 @GetMapping("/downloads") public ResponseEntity<List<Map<String,Object>>> downloads(){return ResponseEntity.ok(s.findDownloadPolicies());}
}

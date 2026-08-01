package com.cpf.bizadmin.operation.controller;

import com.cpf.bizadmin.operation.service.BzaOperationService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.page.CpfPage;
import java.util.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** BZA 운영 API. 목록은 기존 호환 API와 서버 Paging API를 함께 제공합니다. */
@RestController
@RequestMapping("/api/bza")
public class BzaOperationController extends com.cpf.bizadmin.common.base.BzaBaseController {
  private final BzaOperationService s;

  public BzaOperationController(BzaOperationService s) {
    this.s = s;
  }

  @GetMapping("/admin-users")
  @Operation(operationId = "bzaOperationFindAdminUsers", summary = "업무 관리자 목록 조회")
  public ResponseEntity<List<Map<String, Object>>> users() {
    return ResponseEntity.ok(s.findAdminUsers());
  }

  @GetMapping("/admin-users/page")
  @CpfOnlineTransaction(id = "OBZAAD1101", name = "BzaAdminUserPage")
  @Operation(operationId = "bzaOperationFindAdminUsersPage", summary = "업무 관리자 서버 Paging 조회")
  public ResponseEntity<CpfPage<Map<String, Object>>> usersPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.findAdminUsersPage(page, size));
  }

  @PostMapping("/admin-users")
  @Operation(operationId = "bzaOperationSaveAdminUser", summary = "업무 관리자 등록·수정")
  public ResponseEntity<Map<String, Object>> saveUser(
      @RequestBody BzaOperationService.AdminUserRequest r,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.saveAdminUser(r, op));
  }

  @GetMapping("/menus")
  @Operation(operationId = "bzaOperationFindMenus", summary = "메뉴 목록 조회")
  public ResponseEntity<List<Map<String, Object>>> menus() {
    return ResponseEntity.ok(s.findMenus());
  }

  @GetMapping("/menus/page")
  @CpfOnlineTransaction(id = "OBZAMN1101", name = "BzaMenuPage")
  @Operation(operationId = "bzaOperationFindMenusPage", summary = "메뉴 서버 Paging 조회")
  public ResponseEntity<CpfPage<Map<String, Object>>> menusPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.findMenusPage(page, size));
  }

  @PostMapping("/menus")
  @Operation(operationId = "bzaOperationSaveMenu", summary = "메뉴 등록·수정")
  public ResponseEntity<Map<String, Object>> saveMenu(
      @RequestBody BzaOperationService.MenuRequest r,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.saveMenu(r, op));
  }

  @GetMapping("/menus/{menuCode}/impact")
  @CpfOnlineTransaction(id = "OBZAMN1102", name = "BzaMenuImpact")
  @Operation(operationId = "bzaOperationFindMenuImpact", summary = "메뉴 영향도 조회")
  public ResponseEntity<BzaOperationService.MenuImpact> menuImpact(
      @PathVariable String menuCode) {
    return ResponseEntity.ok(s.findMenuImpact(menuCode));
  }

  @DeleteMapping("/menus/{menuCode}")
  @CpfOnlineTransaction(id = "OBZAMN1201", name = "BzaMenuDelete")
  @Operation(operationId = "bzaOperationDeleteMenu", summary = "메뉴 삭제")
  public ResponseEntity<BzaOperationService.MenuDeleteResult> deleteMenu(
      @PathVariable String menuCode,
      @RequestBody BzaOperationService.MenuDeleteRequest r,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.deleteMenu(menuCode, r, op));
  }

  @GetMapping("/roles")
  @Operation(operationId = "bzaOperationFindRoles", summary = "역할 목록 조회")
  public ResponseEntity<List<Map<String, Object>>> roles() {
    return ResponseEntity.ok(s.findRoles());
  }

  @GetMapping("/roles/page")
  @CpfOnlineTransaction(id = "OBZARO1101", name = "BzaRolePage")
  @Operation(operationId = "bzaOperationFindRolesPage", summary = "역할 서버 Paging 조회")
  public ResponseEntity<CpfPage<Map<String, Object>>> rolesPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.findRolesPage(page, size));
  }

  @PostMapping("/roles")
  @Operation(operationId = "bzaOperationSaveRole", summary = "역할 등록·수정")
  public ResponseEntity<Map<String, Object>> saveRole(
      @RequestBody BzaOperationService.RoleRequest r,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.saveRole(r, op));
  }

  @GetMapping("/permissions")
  @Operation(operationId = "bzaOperationFindPermissions", summary = "권한 목록 조회")
  public ResponseEntity<List<Map<String, Object>>> perms() {
    return ResponseEntity.ok(s.findPermissions());
  }

  @GetMapping("/permissions/page")
  @CpfOnlineTransaction(id = "OBZAPE1101", name = "BzaPermissionPage")
  @Operation(operationId = "bzaOperationFindPermissionsPage", summary = "권한 서버 Paging 조회")
  public ResponseEntity<CpfPage<Map<String, Object>>> permsPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.findPermissionsPage(page, size));
  }

  @PostMapping("/permissions")
  @Operation(operationId = "bzaOperationSavePermission", summary = "권한 등록·수정")
  public ResponseEntity<Map<String, Object>> savePerm(
      @RequestBody BzaOperationService.PermissionRequest r,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.savePermission(r, op));
  }

  @GetMapping("/settings")
  @Operation(operationId = "bzaOperationFindSettings", summary = "업무 설정 조회")
  public ResponseEntity<List<Map<String, Object>>> settings() {
    return ResponseEntity.ok(s.findSettings());
  }

  @GetMapping("/downloads")
  @Operation(operationId = "bzaOperationFindDownloadPolicies", summary = "다운로드 정책 조회")
  public ResponseEntity<List<Map<String, Object>>> downloads() {
    return ResponseEntity.ok(s.findDownloadPolicies());
  }
}

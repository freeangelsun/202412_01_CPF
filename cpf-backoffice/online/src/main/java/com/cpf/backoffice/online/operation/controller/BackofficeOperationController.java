package com.cpf.backoffice.online.operation.controller;

import com.cpf.web.api.CpfRestController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;


import com.cpf.backoffice.online.operation.service.BackofficeOperationService;
import com.cpf.foundation.api.page.CpfPage;
import java.util.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** MBW 운영 API. 목록은 기존 호환 API와 서버 Paging API를 함께 제공합니다. */
@CpfRestController
@RequestMapping("/api/v1/backoffice")
public class BackofficeOperationController extends com.cpf.backoffice.online.base.BackofficeBaseController {
  private final BackofficeOperationService s;

  public BackofficeOperationController(BackofficeOperationService s) {
    this.s = s;
  }

  @GetMapping("/admin-users")
  @Operation(operationId = "MBW_OPERATION_FIND_ADMIN_USERS", summary = "업무 관리자 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_FIND_ADMIN_USERS", name = "업무 관리자 목록 조회", description = "업무 관리자 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** users 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<List<Map<String, Object>>> users() {
    return ResponseEntity.ok(s.findAdminUsers());
  }

  @GetMapping("/admin-users/page")  @Operation(operationId = "MBW_OPERATION_FIND_ADMIN_USERS_PAGE", summary = "업무 관리자 서버 Paging 조회")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_FIND_ADMIN_USERS_PAGE", name = "업무 관리자 서버 Paging 조회", description = "업무 관리자 서버 Paging 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** usersPage 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<CpfPage<Map<String, Object>>> usersPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.findAdminUsersPage(page, size));
  }

  @PostMapping("/admin-users")
  @Operation(operationId = "MBW_OPERATION_SAVE_ADMIN_USER", summary = "업무 관리자 등록·수정")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_SAVE_ADMIN_USER", name = "업무 관리자 등록·수정", description = "업무 관리자 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** saveUser 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<Map<String, Object>> saveUser(
      @RequestBody BackofficeOperationService.AdminUserRequest r,
      @RequestAttribute("backoffice.operatorId") String op) {
    return ResponseEntity.ok(s.saveAdminUser(r, op));
  }

  @GetMapping("/menus")
  @Operation(operationId = "MBW_OPERATION_FIND_MENUS", summary = "메뉴 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_FIND_MENUS", name = "메뉴 목록 조회", description = "메뉴 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** menus 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<List<Map<String, Object>>> menus() {
    return ResponseEntity.ok(s.findMenus());
  }

  @GetMapping("/menus/page")  @Operation(operationId = "MBW_OPERATION_FIND_MENUS_PAGE", summary = "메뉴 서버 Paging 조회")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_FIND_MENUS_PAGE", name = "메뉴 서버 Paging 조회", description = "메뉴 서버 Paging 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** menusPage 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<CpfPage<Map<String, Object>>> menusPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.findMenusPage(page, size));
  }

  @PostMapping("/menus")
  @Operation(operationId = "MBW_OPERATION_SAVE_MENU", summary = "메뉴 등록·수정")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_SAVE_MENU", name = "메뉴 등록·수정", description = "메뉴 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** saveMenu 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<Map<String, Object>> saveMenu(
      @RequestBody BackofficeOperationService.MenuRequest r,
      @RequestAttribute("backoffice.operatorId") String op) {
    return ResponseEntity.ok(s.saveMenu(r, op));
  }

  @GetMapping("/menus/{menuCode}/impact")  @Operation(operationId = "MBW_OPERATION_FIND_MENU_IMPACT", summary = "메뉴 영향도 조회")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_FIND_MENU_IMPACT", name = "메뉴 영향도 조회", description = "메뉴 영향도 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** menuImpact 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<BackofficeOperationService.MenuImpact> menuImpact(
      @PathVariable String menuCode) {
    return ResponseEntity.ok(s.findMenuImpact(menuCode));
  }

  @DeleteMapping("/menus/{menuCode}")  @Operation(operationId = "MBW_OPERATION_DELETE_MENU", summary = "메뉴 삭제")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_DELETE_MENU", name = "메뉴 삭제", description = "메뉴 삭제 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** deleteMenu 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<BackofficeOperationService.MenuDeleteResult> deleteMenu(
      @PathVariable String menuCode,
      @RequestBody BackofficeOperationService.MenuDeleteRequest r,
      @RequestAttribute("backoffice.operatorId") String op) {
    return ResponseEntity.ok(s.deleteMenu(menuCode, r, op));
  }

  @GetMapping("/roles")
  @Operation(operationId = "MBW_OPERATION_FIND_ROLES", summary = "역할 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_FIND_ROLES", name = "역할 목록 조회", description = "역할 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** roles 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<List<Map<String, Object>>> roles() {
    return ResponseEntity.ok(s.findRoles());
  }

  @GetMapping("/roles/page")  @Operation(operationId = "MBW_OPERATION_FIND_ROLES_PAGE", summary = "역할 서버 Paging 조회")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_FIND_ROLES_PAGE", name = "역할 서버 Paging 조회", description = "역할 서버 Paging 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** rolesPage 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<CpfPage<Map<String, Object>>> rolesPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.findRolesPage(page, size));
  }

  @PostMapping("/roles")
  @Operation(operationId = "MBW_OPERATION_SAVE_ROLE", summary = "역할 등록·수정")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_SAVE_ROLE", name = "역할 등록·수정", description = "역할 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** saveRole 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<Map<String, Object>> saveRole(
      @RequestBody BackofficeOperationService.RoleRequest r,
      @RequestAttribute("backoffice.operatorId") String op) {
    return ResponseEntity.ok(s.saveRole(r, op));
  }

  @GetMapping("/permissions")
  @Operation(operationId = "MBW_OPERATION_FIND_PERMISSIONS", summary = "권한 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_FIND_PERMISSIONS", name = "권한 목록 조회", description = "권한 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** perms 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<List<Map<String, Object>>> perms() {
    return ResponseEntity.ok(s.findPermissions());
  }

  @GetMapping("/permissions/page")  @Operation(operationId = "MBW_OPERATION_FIND_PERMISSIONS_PAGE", summary = "권한 서버 Paging 조회")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_FIND_PERMISSIONS_PAGE", name = "권한 서버 Paging 조회", description = "권한 서버 Paging 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** permsPage 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<CpfPage<Map<String, Object>>> permsPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.findPermissionsPage(page, size));
  }

  @PostMapping("/permissions")
  @Operation(operationId = "MBW_OPERATION_SAVE_PERMISSION", summary = "권한 등록·수정")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_SAVE_PERMISSION", name = "권한 등록·수정", description = "권한 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** savePerm 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<Map<String, Object>> savePerm(
      @RequestBody BackofficeOperationService.PermissionRequest r,
      @RequestAttribute("backoffice.operatorId") String op) {
    return ResponseEntity.ok(s.savePermission(r, op));
  }

  @GetMapping("/settings")
  @Operation(operationId = "MBW_OPERATION_FIND_SETTINGS", summary = "업무 설정 조회")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_FIND_SETTINGS", name = "업무 설정 조회", description = "업무 설정 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<List<Map<String, Object>>> settings() {
    return ResponseEntity.ok(s.findSettings());
  }

  @GetMapping("/downloads")
  @Operation(operationId = "MBW_OPERATION_FIND_DOWNLOAD_POLICIES", summary = "다운로드 정책 조회")
    @CpfOnlineTransaction(operationId = "MBW_OPERATION_FIND_DOWNLOAD_POLICIES", name = "다운로드 정책 조회", description = "다운로드 정책 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  /** downloads 작업을 CPF 표준 계약에 따라 수행한다. */
  public ResponseEntity<List<Map<String, Object>>> downloads() {
    return ResponseEntity.ok(s.findDownloadPolicies());
  }
}

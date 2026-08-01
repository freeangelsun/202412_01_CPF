package com.cpf.bizadmin.directory.controller;

import com.cpf.bizadmin.directory.service.BzaDirectoryService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.page.CpfPage;
import java.time.Instant;
import java.util.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 업무 관리자용 조직·직원·직급·직책 Directory 조회·변경 API를 제공합니다. */
@RestController
@RequestMapping("/api/bza/directory")
public class BzaDirectoryController extends com.cpf.bizadmin.common.base.BzaBaseController {
  private final BzaDirectoryService s;

  public BzaDirectoryController(BzaDirectoryService s) {
    this.s = s;
  }

  @GetMapping("/positions")
  @Operation(operationId = "bzaDirectoryFindPositions", summary = "직위 목록 조회")
  public ResponseEntity<List<Map<String, Object>>> positions() {
    return ResponseEntity.ok(s.findPositions());
  }

  @GetMapping("/positions/page")
  @CpfOnlineTransaction(id = "OBZADR1101", name = "BzaPositionPage")
  @Operation(operationId = "bzaDirectoryFindPositionsPage", summary = "직위 서버 Paging 조회")
  public ResponseEntity<CpfPage<Map<String, Object>>> positionsPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.positionsPage(page, size));
  }

  @PostMapping("/positions")
  @Operation(operationId = "bzaDirectorySavePosition", summary = "직위 등록·수정")
  public ResponseEntity<Map<String, Object>> savePosition(
      @RequestBody BzaDirectoryService.PositionRequest q,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.savePosition(q, op));
  }

  @GetMapping("/job-titles")
  @Operation(operationId = "bzaDirectoryFindJobTitles", summary = "직책 목록 조회")
  public ResponseEntity<List<Map<String, Object>>> jobs() {
    return ResponseEntity.ok(s.findJobTitles());
  }

  @GetMapping("/job-titles/page")
  @CpfOnlineTransaction(id = "OBZADR1102", name = "BzaJobTitlePage")
  @Operation(operationId = "bzaDirectoryFindJobTitlesPage", summary = "직책 서버 Paging 조회")
  public ResponseEntity<CpfPage<Map<String, Object>>> jobsPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.jobTitlesPage(page, size));
  }

  @PostMapping("/job-titles")
  @Operation(operationId = "bzaDirectorySaveJobTitle", summary = "직책 등록·수정")
  public ResponseEntity<Map<String, Object>> saveJob(
      @RequestBody BzaDirectoryService.JobTitleRequest q,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.saveJobTitle(q, op));
  }

  @GetMapping("/assignments")
  @Operation(operationId = "bzaDirectoryFindAssignments", summary = "직원 배치 목록 조회")
  public ResponseEntity<List<Map<String, Object>>> assign(
      @RequestParam(required = false) String employeeNo,
      @RequestParam(required = false) String organizationCode,
      @RequestParam(required = false) Instant effectiveAt) {
    return ResponseEntity.ok(s.findAssignments(employeeNo, organizationCode, effectiveAt));
  }

  @GetMapping("/assignments/page")
  @CpfOnlineTransaction(id = "OBZADR1103", name = "BzaAssignmentPage")
  @Operation(operationId = "bzaDirectoryFindAssignmentsPage", summary = "직원 배치 서버 Paging 조회")
  public ResponseEntity<CpfPage<Map<String, Object>>> assignPage(
      @RequestParam(required = false) String employeeNo,
      @RequestParam(required = false) String organizationCode,
      @RequestParam(required = false) Instant effectiveAt,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(
        s.assignmentsPage(employeeNo, organizationCode, effectiveAt, page, size));
  }

  @PostMapping("/assignments")
  @Operation(operationId = "bzaDirectorySaveAssignment", summary = "직원 배치 등록·수정")
  public ResponseEntity<Map<String, Object>> saveAssign(
      @RequestBody BzaDirectoryService.AssignmentRequest q,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.saveAssignment(q, op));
  }

  @GetMapping("/responsibilities")
  @Operation(operationId = "bzaDirectoryFindResponsibilities", summary = "책임 목록 조회")
  public ResponseEntity<List<Map<String, Object>>> resp(
      @RequestParam(required = false) String organizationCode,
      @RequestParam(required = false) Instant effectiveAt) {
    return ResponseEntity.ok(s.findResponsibilities(organizationCode, effectiveAt));
  }

  @GetMapping("/responsibilities/page")
  @CpfOnlineTransaction(id = "OBZADR1104", name = "BzaResponsibilityPage")
  @Operation(operationId = "bzaDirectoryFindResponsibilitiesPage", summary = "책임 서버 Paging 조회")
  public ResponseEntity<CpfPage<Map<String, Object>>> respPage(
      @RequestParam(required = false) String organizationCode,
      @RequestParam(required = false) Instant effectiveAt,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.responsibilitiesPage(organizationCode, effectiveAt, page, size));
  }

  @PostMapping("/responsibilities")
  @Operation(operationId = "bzaDirectorySaveResponsibility", summary = "책임 등록·수정")
  public ResponseEntity<Map<String, Object>> saveResp(
      @RequestBody BzaDirectoryService.ResponsibilityRequest q,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.saveResponsibility(q, op));
  }

  @GetMapping("/user-roles")
  @Operation(operationId = "bzaDirectoryFindUserRoles", summary = "사용자 역할 목록 조회")
  public ResponseEntity<List<Map<String, Object>>> roles(
      @RequestParam(required = false) String loginId,
      @RequestParam(required = false) Instant effectiveAt) {
    return ResponseEntity.ok(s.findUserRoles(loginId, effectiveAt));
  }

  @GetMapping("/user-roles/page")
  @CpfOnlineTransaction(id = "OBZADR1105", name = "BzaUserRolePage")
  @Operation(operationId = "bzaDirectoryFindUserRolesPage", summary = "사용자 역할 서버 Paging 조회")
  public ResponseEntity<CpfPage<Map<String, Object>>> rolesPage(
      @RequestParam(required = false) String loginId,
      @RequestParam(required = false) Instant effectiveAt,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.userRolesPage(loginId, effectiveAt, page, size));
  }

  @PostMapping("/user-roles")
  @Operation(operationId = "bzaDirectorySaveUserRole", summary = "사용자 역할 등록·수정")
  public ResponseEntity<Map<String, Object>> saveRole(
      @RequestBody BzaDirectoryService.UserRoleRequest q,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.saveUserRole(q, op));
  }
}

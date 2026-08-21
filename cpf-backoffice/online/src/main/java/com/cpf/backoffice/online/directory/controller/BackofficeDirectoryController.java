package com.cpf.backoffice.online.directory.controller;

import com.cpf.web.api.CpfController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;

import com.cpf.backoffice.online.directory.service.BackofficeDirectoryService;
import com.cpf.foundation.api.page.CpfPage;
import java.time.Instant;
import java.util.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** 업무 관리자용 조직·직원·직급·직책 Directory 조회·변경 API를 제공합니다. */
@CpfController
@RequestMapping("/api/v1/backoffice/directory")
public class BackofficeDirectoryController extends com.cpf.backoffice.online.base.BackofficeBaseController {
  private final BackofficeDirectoryService s;

  public BackofficeDirectoryController(BackofficeDirectoryService s) {
    this.s = s;
  }

  @GetMapping("/positions")
  @Operation(operationId = "MBW_DIRECTORY_FIND_POSITIONS", summary = "직위 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_FIND_POSITIONS", name = "직위 목록 조회", description = "직위 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<List<Map<String, Object>>> positions() {
    return ResponseEntity.ok(s.findPositions());
  }

  @GetMapping("/positions/page")  @Operation(operationId = "MBW_DIRECTORY_FIND_POSITIONS_PAGE", summary = "직위 서버 Paging 조회")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_FIND_POSITIONS_PAGE", name = "직위 서버 Paging 조회", description = "직위 서버 Paging 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<CpfPage<Map<String, Object>>> positionsPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.positionsPage(page, size));
  }

  @PostMapping("/positions")
  @Operation(operationId = "MBW_DIRECTORY_SAVE_POSITION", summary = "직위 등록·수정")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_SAVE_POSITION", name = "직위 등록·수정", description = "직위 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<Map<String, Object>> savePosition(
      @RequestBody BackofficeDirectoryService.PositionRequest q,
      @RequestAttribute("backoffice.operatorId") String op) {
    return ResponseEntity.ok(s.savePosition(q, op));
  }

  @GetMapping("/job-titles")
  @Operation(operationId = "MBW_DIRECTORY_FIND_JOB_TITLES", summary = "직책 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_FIND_JOB_TITLES", name = "직책 목록 조회", description = "직책 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<List<Map<String, Object>>> jobs() {
    return ResponseEntity.ok(s.findJobTitles());
  }

  @GetMapping("/job-titles/page")  @Operation(operationId = "MBW_DIRECTORY_FIND_JOB_TITLES_PAGE", summary = "직책 서버 Paging 조회")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_FIND_JOB_TITLES_PAGE", name = "직책 서버 Paging 조회", description = "직책 서버 Paging 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<CpfPage<Map<String, Object>>> jobsPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.jobTitlesPage(page, size));
  }

  @PostMapping("/job-titles")
  @Operation(operationId = "MBW_DIRECTORY_SAVE_JOB_TITLE", summary = "직책 등록·수정")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_SAVE_JOB_TITLE", name = "직책 등록·수정", description = "직책 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<Map<String, Object>> saveJob(
      @RequestBody BackofficeDirectoryService.JobTitleRequest q,
      @RequestAttribute("backoffice.operatorId") String op) {
    return ResponseEntity.ok(s.saveJobTitle(q, op));
  }

  @GetMapping("/assignments")
  @Operation(operationId = "MBW_DIRECTORY_FIND_ASSIGNMENTS", summary = "직원 배치 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_FIND_ASSIGNMENTS", name = "직원 배치 목록 조회", description = "직원 배치 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<List<Map<String, Object>>> assign(
      @RequestParam(required = false) String employeeNo,
      @RequestParam(required = false) String organizationCode,
      @RequestParam(required = false) Instant effectiveAt) {
    return ResponseEntity.ok(s.findAssignments(employeeNo, organizationCode, effectiveAt));
  }

  @GetMapping("/assignments/page")  @Operation(operationId = "MBW_DIRECTORY_FIND_ASSIGNMENTS_PAGE", summary = "직원 배치 서버 Paging 조회")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_FIND_ASSIGNMENTS_PAGE", name = "직원 배치 서버 Paging 조회", description = "직원 배치 서버 Paging 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
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
  @Operation(operationId = "MBW_DIRECTORY_SAVE_ASSIGNMENT", summary = "직원 배치 등록·수정")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_SAVE_ASSIGNMENT", name = "직원 배치 등록·수정", description = "직원 배치 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<Map<String, Object>> saveAssign(
      @RequestBody BackofficeDirectoryService.AssignmentRequest q,
      @RequestAttribute("backoffice.operatorId") String op) {
    return ResponseEntity.ok(s.saveAssignment(q, op));
  }

  @GetMapping("/responsibilities")
  @Operation(operationId = "MBW_DIRECTORY_FIND_RESPONSIBILITIES", summary = "책임 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_FIND_RESPONSIBILITIES", name = "책임 목록 조회", description = "책임 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<List<Map<String, Object>>> resp(
      @RequestParam(required = false) String organizationCode,
      @RequestParam(required = false) Instant effectiveAt) {
    return ResponseEntity.ok(s.findResponsibilities(organizationCode, effectiveAt));
  }

  @GetMapping("/responsibilities/page")  @Operation(operationId = "MBW_DIRECTORY_FIND_RESPONSIBILITIES_PAGE", summary = "책임 서버 Paging 조회")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_FIND_RESPONSIBILITIES_PAGE", name = "책임 서버 Paging 조회", description = "책임 서버 Paging 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<CpfPage<Map<String, Object>>> respPage(
      @RequestParam(required = false) String organizationCode,
      @RequestParam(required = false) Instant effectiveAt,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.responsibilitiesPage(organizationCode, effectiveAt, page, size));
  }

  @PostMapping("/responsibilities")
  @Operation(operationId = "MBW_DIRECTORY_SAVE_RESPONSIBILITY", summary = "책임 등록·수정")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_SAVE_RESPONSIBILITY", name = "책임 등록·수정", description = "책임 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<Map<String, Object>> saveResp(
      @RequestBody BackofficeDirectoryService.ResponsibilityRequest q,
      @RequestAttribute("backoffice.operatorId") String op) {
    return ResponseEntity.ok(s.saveResponsibility(q, op));
  }

  @GetMapping("/user-roles")
  @Operation(operationId = "MBW_DIRECTORY_FIND_USER_ROLES", summary = "사용자 역할 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_FIND_USER_ROLES", name = "사용자 역할 목록 조회", description = "사용자 역할 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<List<Map<String, Object>>> roles(
      @RequestParam(required = false) String loginId,
      @RequestParam(required = false) Instant effectiveAt) {
    return ResponseEntity.ok(s.findUserRoles(loginId, effectiveAt));
  }

  @GetMapping("/user-roles/page")  @Operation(operationId = "MBW_DIRECTORY_FIND_USER_ROLES_PAGE", summary = "사용자 역할 서버 Paging 조회")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_FIND_USER_ROLES_PAGE", name = "사용자 역할 서버 Paging 조회", description = "사용자 역할 서버 Paging 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<CpfPage<Map<String, Object>>> rolesPage(
      @RequestParam(required = false) String loginId,
      @RequestParam(required = false) Instant effectiveAt,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.userRolesPage(loginId, effectiveAt, page, size));
  }

  @PostMapping("/user-roles")
  @Operation(operationId = "MBW_DIRECTORY_SAVE_USER_ROLE", summary = "사용자 역할 등록·수정")
    @CpfOnlineTransaction(operationId = "MBW_DIRECTORY_SAVE_USER_ROLE", name = "사용자 역할 등록·수정", description = "사용자 역할 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
  public ResponseEntity<Map<String, Object>> saveRole(
      @RequestBody BackofficeDirectoryService.UserRoleRequest q,
      @RequestAttribute("backoffice.operatorId") String op) {
    return ResponseEntity.ok(s.saveUserRole(q, op));
  }
}

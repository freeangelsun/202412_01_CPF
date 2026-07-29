package com.cpf.bizadmin.directory.controller;

import com.cpf.bizadmin.directory.service.BzaDirectoryService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.page.CpfPage;
import java.time.Instant;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bza/directory")
public class BzaDirectoryController extends com.cpf.bizadmin.common.base.BzaBaseController {
  private final BzaDirectoryService s;

  public BzaDirectoryController(BzaDirectoryService s) {
    this.s = s;
  }

  @GetMapping("/positions")
  public ResponseEntity<List<Map<String, Object>>> positions() {
    return ResponseEntity.ok(s.findPositions());
  }

  @GetMapping("/positions/page")
  @CpfOnlineTransaction(id = "OBZADR1101", name = "BzaPositionPage")
  public ResponseEntity<CpfPage<Map<String, Object>>> positionsPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.positionsPage(page, size));
  }

  @PostMapping("/positions")
  public ResponseEntity<Map<String, Object>> savePosition(
      @RequestBody BzaDirectoryService.PositionRequest q,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.savePosition(q, op));
  }

  @GetMapping("/job-titles")
  public ResponseEntity<List<Map<String, Object>>> jobs() {
    return ResponseEntity.ok(s.findJobTitles());
  }

  @GetMapping("/job-titles/page")
  @CpfOnlineTransaction(id = "OBZADR1102", name = "BzaJobTitlePage")
  public ResponseEntity<CpfPage<Map<String, Object>>> jobsPage(
      @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.jobTitlesPage(page, size));
  }

  @PostMapping("/job-titles")
  public ResponseEntity<Map<String, Object>> saveJob(
      @RequestBody BzaDirectoryService.JobTitleRequest q,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.saveJobTitle(q, op));
  }

  @GetMapping("/assignments")
  public ResponseEntity<List<Map<String, Object>>> assign(
      @RequestParam(required = false) String employeeNo,
      @RequestParam(required = false) String organizationCode,
      @RequestParam(required = false) Instant effectiveAt) {
    return ResponseEntity.ok(s.findAssignments(employeeNo, organizationCode, effectiveAt));
  }

  @GetMapping("/assignments/page")
  @CpfOnlineTransaction(id = "OBZADR1103", name = "BzaAssignmentPage")
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
  public ResponseEntity<Map<String, Object>> saveAssign(
      @RequestBody BzaDirectoryService.AssignmentRequest q,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.saveAssignment(q, op));
  }

  @GetMapping("/responsibilities")
  public ResponseEntity<List<Map<String, Object>>> resp(
      @RequestParam(required = false) String organizationCode,
      @RequestParam(required = false) Instant effectiveAt) {
    return ResponseEntity.ok(s.findResponsibilities(organizationCode, effectiveAt));
  }

  @GetMapping("/responsibilities/page")
  @CpfOnlineTransaction(id = "OBZADR1104", name = "BzaResponsibilityPage")
  public ResponseEntity<CpfPage<Map<String, Object>>> respPage(
      @RequestParam(required = false) String organizationCode,
      @RequestParam(required = false) Instant effectiveAt,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.responsibilitiesPage(organizationCode, effectiveAt, page, size));
  }

  @PostMapping("/responsibilities")
  public ResponseEntity<Map<String, Object>> saveResp(
      @RequestBody BzaDirectoryService.ResponsibilityRequest q,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.saveResponsibility(q, op));
  }

  @GetMapping("/user-roles")
  public ResponseEntity<List<Map<String, Object>>> roles(
      @RequestParam(required = false) String loginId,
      @RequestParam(required = false) Instant effectiveAt) {
    return ResponseEntity.ok(s.findUserRoles(loginId, effectiveAt));
  }

  @GetMapping("/user-roles/page")
  @CpfOnlineTransaction(id = "OBZADR1105", name = "BzaUserRolePage")
  public ResponseEntity<CpfPage<Map<String, Object>>> rolesPage(
      @RequestParam(required = false) String loginId,
      @RequestParam(required = false) Instant effectiveAt,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer size) {
    return ResponseEntity.ok(s.userRolesPage(loginId, effectiveAt, page, size));
  }

  @PostMapping("/user-roles")
  public ResponseEntity<Map<String, Object>> saveRole(
      @RequestBody BzaDirectoryService.UserRoleRequest q,
      @RequestAttribute("bza.operatorId") String op) {
    return ResponseEntity.ok(s.saveUserRole(q, op));
  }
}

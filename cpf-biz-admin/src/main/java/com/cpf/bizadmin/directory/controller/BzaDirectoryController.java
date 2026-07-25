package com.cpf.bizadmin.directory.controller;

import com.cpf.bizadmin.directory.service.BzaDirectoryService;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** BZA 다중 조직/직급/직책/발령/Role 정본 관리 API. */
@RestController
@RequestMapping("/api/bza/directory")
@Tag(name = "BZA-Directory", description = "BZA 조직·인사·다중 Role 유효기간 정본 API")
public class BzaDirectoryController extends com.cpf.bizadmin.common.base.BzaBaseController {
    private final BzaDirectoryService service;

    public BzaDirectoryController(BzaDirectoryService service) { this.service = service; }

    @GetMapping("/positions")
    @CpfOnlineTransaction(id = "OBZADR0001", name = "BzaPositionList")
    @Operation(operationId = "bzaDirectoryPositions", summary = "직급 목록")
    public ResponseEntity<List<Map<String,Object>>> positions() { return ResponseEntity.ok(service.findPositions()); }

    @PostMapping("/positions")
    @CpfOnlineTransaction(id = "OBZADR0002", name = "BzaPositionSave")
    @Operation(operationId = "bzaDirectorySavePosition", summary = "직급 등록·수정")
    public ResponseEntity<Map<String,Object>> savePosition(@RequestBody BzaDirectoryService.PositionRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(service.savePosition(request, operatorId));
    }

    @GetMapping("/job-titles")
    @CpfOnlineTransaction(id = "OBZADR0003", name = "BzaJobTitleList")
    @Operation(operationId = "bzaDirectoryJobTitles", summary = "직책 목록")
    public ResponseEntity<List<Map<String,Object>>> jobTitles() { return ResponseEntity.ok(service.findJobTitles()); }

    @PostMapping("/job-titles")
    @CpfOnlineTransaction(id = "OBZADR0004", name = "BzaJobTitleSave")
    @Operation(operationId = "bzaDirectorySaveJobTitle", summary = "직책 등록·수정")
    public ResponseEntity<Map<String,Object>> saveJobTitle(@RequestBody BzaDirectoryService.JobTitleRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(service.saveJobTitle(request, operatorId));
    }

    @GetMapping("/assignments")
    @CpfOnlineTransaction(id = "OBZADR0005", name = "BzaAssignmentList")
    @Operation(operationId = "bzaDirectoryAssignments", summary = "유효 직원 소속·발령 조회")
    public ResponseEntity<List<Map<String,Object>>> assignments(
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) Instant effectiveAt) {
        return ResponseEntity.ok(service.findAssignments(employeeNo, organizationCode, effectiveAt));
    }

    @PostMapping("/assignments")
    @CpfOnlineTransaction(id = "OBZADR0006", name = "BzaAssignmentSave")
    @Operation(operationId = "bzaDirectorySaveAssignment", summary = "직원 소속·겸직·파견·대행 발령 저장")
    public ResponseEntity<Map<String,Object>> saveAssignment(@RequestBody BzaDirectoryService.AssignmentRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(service.saveAssignment(request, operatorId));
    }

    @GetMapping("/responsibilities")
    @CpfOnlineTransaction(id = "OBZADR0007", name = "BzaResponsibilityList")
    @Operation(operationId = "bzaDirectoryResponsibilities", summary = "조직 책임자/대행자 조회")
    public ResponseEntity<List<Map<String,Object>>> responsibilities(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) Instant effectiveAt) {
        return ResponseEntity.ok(service.findResponsibilities(organizationCode, effectiveAt));
    }

    @PostMapping("/responsibilities")
    @CpfOnlineTransaction(id = "OBZADR0008", name = "BzaResponsibilitySave")
    @Operation(operationId = "bzaDirectorySaveResponsibility", summary = "조직 책임/대결/승인 Owner 저장")
    public ResponseEntity<Map<String,Object>> saveResponsibility(
            @RequestBody BzaDirectoryService.ResponsibilityRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(service.saveResponsibility(request, operatorId));
    }

    @GetMapping("/user-roles")
    @CpfOnlineTransaction(id = "OBZADR0009", name = "BzaUserRoleList")
    @Operation(operationId = "bzaDirectoryUserRoles", summary = "유효 다중 Role 조회")
    public ResponseEntity<List<Map<String,Object>>> userRoles(
            @RequestParam(required = false) String loginId,
            @RequestParam(required = false) Instant effectiveAt) {
        return ResponseEntity.ok(service.findUserRoles(loginId, effectiveAt));
    }

    @PostMapping("/user-roles")
    @CpfOnlineTransaction(id = "OBZADR0010", name = "BzaUserRoleSave")
    @Operation(operationId = "bzaDirectorySaveUserRole", summary = "사용자 Role 유효기간 매핑")
    public ResponseEntity<Map<String,Object>> saveUserRole(@RequestBody BzaDirectoryService.UserRoleRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(service.saveUserRole(request, operatorId));
    }
}

package com.cpf.admin.opr.server.controller;

import com.cpf.admin.common.base.AdmBaseController;
import com.cpf.admin.opr.server.dto.AdmManagedServerDisableRequest;
import com.cpf.admin.opr.server.dto.AdmManagedServerPageResponse;
import com.cpf.admin.opr.server.dto.AdmManagedServerResponse;
import com.cpf.admin.opr.server.dto.AdmManagedServerSaveRequest;
import com.cpf.admin.opr.server.dto.AdmRuntimeInventoryPageResponse;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.platform.operations.runtimecontrol.CpfManagedServerCommand;
import com.cpf.platform.operations.runtimecontrol.CpfManagedServerSnapshot;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeControlPlane;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "ADM-ManagedServer", description = "Central Managed Server와 Runtime Inventory 운영 API")
public class AdmManagedServerController extends AdmBaseController {
    private final CpfRuntimeControlPlane controlPlane;
    private final AdmAuditLogService audit;

    public AdmManagedServerController(CpfRuntimeControlPlane controlPlane, AdmAuditLogService audit) {
        this.controlPlane = controlPlane;
        this.audit = audit;
    }

    @GetMapping("/adm/api/servers")
    @Operation(operationId = "admManagedServerFindAll", summary = "Central Managed Server 목록",
            description = "Gateway/Batch/Logging/Health/Configuration이 공통 참조하는 Managed Server master를 server-side page로 조회합니다.")
    public ResponseEntity<AdmManagedServerPageResponse> findAll(
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest request) {
        requireOperator(request);
        return ResponseEntity.ok(AdmManagedServerPageResponse.from(
                controlPlane.findManagedServers(environment, status, keyword, page, size)));
    }

    @GetMapping("/adm/api/servers/{managedServerId}")
    @Operation(operationId = "admManagedServerFindOne", summary = "Central Managed Server 상세")
    public ResponseEntity<AdmManagedServerResponse> findOne(
            @PathVariable String managedServerId, HttpServletRequest request) {
        requireOperator(request);
        return ResponseEntity.ok(AdmManagedServerResponse.from(controlPlane.getManagedServer(managedServerId)));
    }

    @PostMapping("/adm/api/servers")
    @Operation(operationId = "admManagedServerSave", summary = "Central Managed Server 등록/수정")
    public ResponseEntity<AdmManagedServerResponse> save(
            @RequestBody AdmManagedServerSaveRequest body, HttpServletRequest request) {
        String operator = requireOperator(request);
        CpfManagedServerSnapshot saved = controlPlane.saveManagedServer(new CpfManagedServerCommand(
                body.managedServerId(), body.serverName(), body.displayName(), body.hostname(), body.managementIdentity(),
                body.environment(), body.serverGroup(), body.zone(), body.location(), body.description(), body.tagsJson(),
                body.expectedVersion(), body.reason(), operator));
        audit(request, operator, "MANAGED_SERVER_SAVE", saved.managedServerId(), body.reason(), saved);
        return ResponseEntity.ok(AdmManagedServerResponse.from(saved));
    }

    @PostMapping("/adm/api/servers/{managedServerId}/disable")
    @Operation(operationId = "admManagedServerDisable", summary = "Managed Server 비활성화",
            description = "OS shutdown이 아니라 CPF 관리 대상 master를 DISABLED로 전환합니다.")
    public ResponseEntity<Void> disable(
            @PathVariable String managedServerId,
            @RequestBody AdmManagedServerDisableRequest body,
            HttpServletRequest request) {
        String operator = requireOperator(request);
        controlPlane.disableManagedServer(managedServerId, body.expectedVersion(), body.reason(), operator);
        audit(request, operator, "MANAGED_SERVER_DISABLE", managedServerId, body.reason(), Map.of("status", "DISABLED"));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/adm/api/runtime-inventory")
    @Operation(operationId = "admRuntimeInventoryFindAll", summary = "Central Runtime Inventory",
            description = "Managed Server/Runtime Instance/Capability의 공통 projection을 server-side page로 조회합니다.")
    public ResponseEntity<AdmRuntimeInventoryPageResponse> runtimeInventory(
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String capability,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest request) {
        requireOperator(request);
        return ResponseEntity.ok(AdmRuntimeInventoryPageResponse.from(
                controlPlane.findRuntimeInventory(environment, capability, status, keyword, page, size)));
    }

    private void audit(HttpServletRequest request, String operator, String action, String id, String reason, Object after) {
        audit.record(CpfContexts.transactionId(), operator, action, "OPS_MANAGED_SERVER", id, reason, "",
                String.valueOf(after), "Managed Server Registry", clientIp(request));
    }
}

package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmOperationsGovernanceService;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

/** CPF 운영 정책·SLO·Alert·Runbook·Drift·DR 상태를 하나의 Workbench로 제공하는 ADM API입니다. */
@RestController
@RequestMapping("/adm/api/operations-governance")
@Tag(name = "ADM-OperationsGovernance", description = "운영 정책, SLO, Alert, Runbook, Drift, DR 통합 Projection")
public class AdmOperationsGovernanceController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmOperationsGovernanceService service;

    public AdmOperationsGovernanceController(AdmOperationsGovernanceService service) { this.service = service; }

    @GetMapping("/snapshot")    @Operation(operationId = "admOperationsGovernanceSnapshot", summary = "운영 정책·SLO 통합 Snapshot",
            description = "거래 Metric/SLO, Alert/Incident, Runbook, Self-healing, Topology, Drift, Capacity, DR, 외부기관 상태를 실제 Runtime 원천에서 통합 조회합니다.")
    public ResponseEntity<Map<String, Object>> snapshot(HttpServletRequest request) {
        requireOperator(request);
        return ResponseEntity.ok(service.snapshot());
    }
}

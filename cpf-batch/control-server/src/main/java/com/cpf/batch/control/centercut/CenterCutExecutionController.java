package com.cpf.batch.control.centercut;

import com.cpf.batch.api.CenterCutExecutionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Center-Cut 실행 생성/제어 Owner API. 위험 상태 변경은 요청자/승인자 분리를 강제합니다. */
@RestController
@RequestMapping("/api/v1/batch/center-cut/executions")
public class CenterCutExecutionController {
    private final CenterCutExecutionService service;
    public CenterCutExecutionController(CenterCutExecutionService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody CenterCutExecutionRequest request) throws Exception {
        return ResponseEntity.status(201).body(service.create(request));
    }

    @GetMapping("/{id}")
    public Map<String, Object> detail(@PathVariable String id) { return service.detail(id); }

    @PostMapping("/{id}/{action}")
    public ResponseEntity<Map<String, Object>> action(@PathVariable String id, @PathVariable String action,
                                                       @RequestBody ApprovedOperationRequest request) {
        return ResponseEntity.accepted().body(
                service.transition(id, action, request.requestedBy(), request.approvedBy(), request.reason()));
    }

    public record ApprovedOperationRequest(String requestedBy, String approvedBy, String reason) {}
}

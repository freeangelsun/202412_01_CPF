package com.cpf.batch.control.deploy;

import com.cpf.batch.control.security.BatVerifiedActorResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/batch/cells")
public class CellOperationsController {
    private final CellOperationsService service;
    private final BatVerifiedActorResolver actorResolver;

    public CellOperationsController(
            CellOperationsService service,
            BatVerifiedActorResolver actorResolver) {
        this.service = service;
        this.actorResolver = actorResolver;
    }

    @GetMapping("/{cellId}")
    public Map<String, Object> status(@PathVariable String cellId) {
        return service.status(cellId);
    }

    @PostMapping("/{cellId}/scale")
    public ResponseEntity<CellOperationsService.OperationResult> scale(
            @PathVariable String cellId,
            @RequestBody ScaleRequest request,
            HttpServletRequest http) {
        return ResponseEntity.accepted().body(
                service.scale(
                        cellId,
                        request.desiredCount(),
                        verified(http, request.approval())));
    }

    @PostMapping("/{cellId}/reconcile")
    public ResponseEntity<CellOperationsService.OperationResult> reconcile(
            @PathVariable String cellId,
            @RequestBody CellOperationsService.ApprovedRequest request,
            HttpServletRequest http) {
        return ResponseEntity.accepted().body(service.reconcile(cellId, verified(http, request)));
    }

    private CellOperationsService.ApprovedRequest verified(
            HttpServletRequest http,
            CellOperationsService.ApprovedRequest request) {
        var actors = actorResolver.approved(
                http,
                request.requestedBy(),
                request.approvedBy(),
                null);
        return new CellOperationsService.ApprovedRequest(
                actors.requestedBy(),
                actors.approvedBy(),
                request.reason());
    }

    public record ScaleRequest(
            int desiredCount,
            CellOperationsService.ApprovedRequest approval) {
    }
}

package com.cpf.batch.control.deploy;
import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/v1/batch/cells")
public class CellOperationsController {
 private final CellOperationsService service;public CellOperationsController(CellOperationsService service){this.service=service;}
 @GetMapping("/{cellId}") public Map<String,Object> status(@PathVariable String cellId){return service.status(cellId);}
 @PostMapping("/{cellId}/scale") public ResponseEntity<CellOperationsService.OperationResult> scale(@PathVariable String cellId,@RequestBody ScaleRequest r){return ResponseEntity.accepted().body(service.scale(cellId,r.desiredCount(),r.approval()));}
 @PostMapping("/{cellId}/reconcile") public ResponseEntity<CellOperationsService.OperationResult> reconcile(@PathVariable String cellId,@RequestBody CellOperationsService.ApprovedRequest r){return ResponseEntity.accepted().body(service.reconcile(cellId,r));}
 public record ScaleRequest(int desiredCount,CellOperationsService.ApprovedRequest approval){}
}

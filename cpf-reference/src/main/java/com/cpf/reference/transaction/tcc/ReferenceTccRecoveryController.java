package com.cpf.reference.transaction.tcc;

import com.cpf.core.api.transaction.CpfTccResult;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/reference/tcc")
public class ReferenceTccRecoveryController {
 private final ReferenceTccRecoveryService recovery;
 public ReferenceTccRecoveryController(ReferenceTccRecoveryService recovery){this.recovery=recovery;}
 @GetMapping("/{transactionId}/{branchId}") ResponseEntity<CpfTccResult> reconcile(@PathVariable String transactionId,@PathVariable String branchId){return ResponseEntity.ok(recovery.reconcile(transactionId,branchId));}
 @PostMapping("/{transactionId}/{branchId}/manual-review") ResponseEntity<CpfTccResult> manual(@PathVariable String transactionId,@PathVariable String branchId,@RequestParam String reason){return ResponseEntity.ok(recovery.requestManualReview(transactionId,branchId,reason));}
 @PostMapping("/recover-expired") ResponseEntity<Integer> recover(@RequestParam(defaultValue="100") int limit){return ResponseEntity.ok(recovery.markExpiredForReview(limit));}
}

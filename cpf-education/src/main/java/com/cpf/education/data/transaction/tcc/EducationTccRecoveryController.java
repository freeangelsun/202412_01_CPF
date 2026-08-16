package com.cpf.education.data.transaction.tcc;
import com.cpf.core.api.transaction.CpfTccResult;import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/education/tcc")
/** EducationTccRecoveryController 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationTccRecoveryController {
 private final EducationTccRecoveryService recovery;
 public EducationTccRecoveryController(EducationTccRecoveryService recovery){this.recovery=recovery;}
 @GetMapping("/{transactionId}/{branchId}") ResponseEntity<CpfTccResult> reconcile(@PathVariable String transactionId,@PathVariable String branchId){return ResponseEntity.ok(recovery.reconcile(transactionId,branchId));}
 @PostMapping("/{transactionId}/{branchId}/manual-review") ResponseEntity<CpfTccResult> manual(@PathVariable String transactionId,@PathVariable String branchId,@RequestParam String reason){return ResponseEntity.ok(recovery.requestManualReview(transactionId,branchId,reason));}
 @PostMapping("/recover-expired") ResponseEntity<Integer> recover(@RequestParam(defaultValue="100") int limit){return ResponseEntity.ok(recovery.markExpiredForReview(limit));}
}

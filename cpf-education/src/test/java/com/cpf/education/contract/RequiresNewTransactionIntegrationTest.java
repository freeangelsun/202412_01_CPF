package com.cpf.education.contract;

import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.education.online.transactionrequiresnew.controller.OrderProcessingController;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** EDU-ONLINE-10 Source가 실제 CPF Online Operation 계약을 소비하는지 검증합니다. */
class RequiresNewTransactionIntegrationTest {
 @Test void canonicalOperationMetadataIsPresent() {
   var operations=java.util.Arrays.stream(OrderProcessingController.class.getDeclaredMethods())
       .map(m -> m.getAnnotation(CpfOnlineTransaction.class)).filter(java.util.Objects::nonNull).toList();
   assertFalse(operations.isEmpty(), "@CpfOnlineTransaction runtime consumer가 필요합니다.");
   assertTrue(operations.stream().allMatch(a -> !a.operationId().isBlank() && !a.name().isBlank() && !a.description().isBlank()));
 }
}

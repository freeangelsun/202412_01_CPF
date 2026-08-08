package com.cpf.reference.transaction.tcc;

import com.cpf.core.api.transaction.*;import java.util.Objects;

/** Restart-safe recovery/reconcile boundary. UNKNOWN is never guessed into success. */
public final class ReferenceTccRecoveryService implements CpfTccRecoveryOperations {
 private final ReferenceTccReservationStore store;
 public ReferenceTccRecoveryService(ReferenceTccReservationStore store){this.store=Objects.requireNonNull(store);}
 @Override public CpfTccResult reconcile(String tx,String branch){var r=store.find(tx,branch);if(r.isEmpty())return CpfTccResult.EMPTY_ROLLBACK;return switch(r.get().state()){case CONFIRMED,CANCELED->CpfTccResult.ALREADY_APPLIED;case TRYING,TRIED->CpfTccResult.RETRYABLE_FAILURE;case UNKNOWN->CpfTccResult.UNKNOWN;case MANUAL_REVIEW->CpfTccResult.MANUAL_REVIEW;};}
 @Override public CpfTccResult requestManualReview(String tx,String branch,String reason){if(reason==null||reason.isBlank())throw new IllegalArgumentException("reason required");return store.markManualReview(tx,branch,reason)?CpfTccResult.MANUAL_REVIEW:CpfTccResult.RETRYABLE_FAILURE;}
 public int markExpiredForReview(int limit){int n=0;for(var r:store.findExpired(limit)){if(store.markManualReview(r.transactionId(),r.branchId(),"TCC_DEADLINE_EXPIRED"))n++;}return n;}
}

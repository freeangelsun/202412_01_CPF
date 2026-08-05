package com.cpf.core.common.logging.fallback;
public final class TransactionLogRecoveryWorkerBackoffHarness {
 private TransactionLogRecoveryWorkerBackoffHarness(){}
 public static void main(String[] args){
  check(TransactionLogRecoveryWorker.retryDelayMs(1000,300000,1)==1000,"attempt1");
  check(TransactionLogRecoveryWorker.retryDelayMs(1000,300000,4)==8000,"exponential");
  check(TransactionLogRecoveryWorker.retryDelayMs(Long.MAX_VALUE/4,Long.MAX_VALUE,5)==Long.MAX_VALUE,"overflow saturates");
  boolean invalid=false; try{TransactionLogRecoveryWorker.retryDelayMs(2,1,1);}catch(IllegalArgumentException e){invalid=true;}
  check(invalid,"invalid bounds fail closed");
  System.out.println("CPF_LOG_RECOVERY_BACKOFF_HARNESS_PASS");
 }
 private static void check(boolean c,String m){if(!c)throw new AssertionError(m);}
}

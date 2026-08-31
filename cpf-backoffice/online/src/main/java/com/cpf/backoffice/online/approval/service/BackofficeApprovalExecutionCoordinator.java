package com.cpf.backoffice.online.approval.service;

import com.cpf.backoffice.online.base.BackofficeBaseService;

import com.cpf.backoffice.online.approval.repository.BackofficeApprovalPolicyRepository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.cpf.foundation.annotation.CpfService;

import java.util.*;

/** 승인 Transaction에는 실행 PENDING만 기록하고 실제 mutation은 commit 이후 수행합니다. */
@CpfService
public class BackofficeApprovalExecutionCoordinator extends BackofficeBaseService {
    private final BackofficeApprovalPolicyRepository repository;
    private final BackofficeApprovalExecutionRunner runner;

    public BackofficeApprovalExecutionCoordinator(BackofficeApprovalPolicyRepository repository,
                                                   BackofficeApprovalExecutionRunner runner) {
        this.repository = repository; this.runner = runner;
    }

    public void prepareAfterApproval(long approvalId, Map<String,Object> document, String approvedBy) {
        if (repository.findExecution(approvalId).isPresent()) return;
        String action = Objects.toString(document.get("approvalType"), "").trim().toUpperCase(Locale.ROOT);
        String payloadHash = Objects.toString(document.get("payloadHash"), "");
        String commandRequestId = "MBW-APPROVAL-" + approvalId + "-" + payloadHash;
        if (commandRequestId.length() > 120) commandRequestId = commandRequestId.substring(0,120);
        repository.insertExecution(approvalId, commandRequestId, action, approvedBy,
                Objects.toString(document.get("transactionId"), null), approvedBy);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { runner.execute(approvalId); }
            });
        } else {
            runner.execute(approvalId);
        }
    }

    public Map<String,Object> reconcile(long approvalId, String operatorId) {
        return runner.reconcile(approvalId, operatorId);
    }
}

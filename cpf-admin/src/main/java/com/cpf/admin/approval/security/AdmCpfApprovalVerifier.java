package com.cpf.admin.approval.security;

import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.security.api.approval.CpfApprovalVerification;
import com.cpf.security.api.approval.CpfApprovalVerifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** ADM 승인 Engine의 immutable 완료상태를 Security @CpfApprovalRequired에 제공하는 Adapter입니다. */
@Component
public final class AdmCpfApprovalVerifier implements CpfApprovalVerifier {
    private final AdmApprovalService approvals;
    public AdmCpfApprovalVerifier(AdmApprovalService approvals){this.approvals=Objects.requireNonNull(approvals);}
    @Override public void verify(CpfApprovalVerification request) {
        Map<String,Object> detail=approvals.detail(request.approvalId());
        if(!"APPROVED".equals(text(detail.get("approvalStatus")))) throw new SecurityException("CPF_APPROVAL_NOT_APPROVED");
        if(!request.action().equals(text(detail.get("actionType")))) throw new SecurityException("CPF_APPROVAL_ACTION_MISMATCH");
        if(request.reason()!=null && !request.reason().isBlank()) {
            String stored=text(detail.get("requestReason"));
            if(stored!=null && !stored.equals(request.reason())) throw new SecurityException("CPF_APPROVAL_REASON_MISMATCH");
        }
        int approved=0;
        Object raw=detail.get("participants");
        if(raw instanceof List<?> rows) for(Object item:rows) if(item instanceof Map<?,?> row && "APPROVED".equals(text(row.get("decisionStatus")))) approved++;
        if(approved<request.requiredApprovals()) throw new SecurityException("CPF_APPROVAL_COUNT_INSUFFICIENT");
        String tx=text(detail.get("transactionId"));
        if(tx!=null && !tx.equals(request.transactionId())) throw new SecurityException("CPF_APPROVAL_TRANSACTION_MISMATCH");
    }
    private static String text(Object v){return v==null?null:String.valueOf(v).trim();}
}

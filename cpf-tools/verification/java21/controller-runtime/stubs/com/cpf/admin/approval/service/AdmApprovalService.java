package com.cpf.admin.approval.service;
import java.util.Map;
public class AdmApprovalService {
    public long lastId; public String lastReason; public String lastOperator;
    public Map<String,Object> execute(long id,String reason,String operatorId){
        lastId=id; lastReason=reason; lastOperator=operatorId;
        return Map.of("state","ACCEPTED","approvalRequestId",Long.toString(id),"requestedBy",operatorId);
    }
}

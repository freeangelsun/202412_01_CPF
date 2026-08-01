package com.cpf.reference.edu.runtime.api;
import com.cpf.reference.edu.runtime.model.EduFailurePoint;
import java.util.Map;
public record EduExecutionApiRequest(String businessKey,String idempotencyKey,long expectedVersion,
        String requestReason,Map<String,Object> payload,EduFailurePoint failurePoint,
        boolean autoApprove,boolean autoAcknowledge) {}

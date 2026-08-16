package com.cpf.education.operations.runtime.api;
import com.cpf.education.operations.runtime.model.EduFailurePoint;
import java.util.Map;
/** EduExecutionApiRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record EduExecutionApiRequest(String businessKey,String idempotencyKey,long expectedVersion,
        String requestReason,Map<String,Object> payload,EduFailurePoint failurePoint,
        boolean autoApprove,boolean autoAcknowledge) {}

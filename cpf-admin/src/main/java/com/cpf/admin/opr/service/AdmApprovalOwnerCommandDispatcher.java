package com.cpf.admin.opr.service;

import java.util.Map;

/** 승인 완료 후 실제 Owner Module Command를 호출하는 ADM 내부 SPI입니다. */
public interface AdmApprovalOwnerCommandDispatcher {
    boolean supports(String ownerModule, String ownerCommand);
    Object execute(Map<String,Object> approvalRequest);
}

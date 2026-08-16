package com.cpf.security.api.approval;
/** 승인 저장소/ADM 구현을 Security Runtime과 분리하는 Owner Port입니다. 실패 시 예외로 fail-closed합니다. */
@FunctionalInterface
public interface CpfApprovalVerifier { void verify(CpfApprovalVerification request); }

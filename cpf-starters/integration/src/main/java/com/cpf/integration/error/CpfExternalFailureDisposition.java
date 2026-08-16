package com.cpf.integration.error;

/** 외부 연계 실패 후 재처리/대사 판단입니다. */
public enum CpfExternalFailureDisposition { FAIL_FAST, RETRY, RECONCILE }

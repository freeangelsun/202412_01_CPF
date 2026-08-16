package com.cpf.admin.approval.api;

/**
 * 승인 없이 직접 실행하면 안 되는 대표 ADM 위험조치 분류입니다.
 *
 * <p>고객 확장은 문자열 action type으로 가능해야 하며 이 Enum은 기본 제품 정책과
 * OpenAPI/JavaDoc에서 공통 의미를 제공하는 표준 분류입니다.</p>
 */
public enum AdmDangerousOperation {
    SERVICE_CONTROL, ROUTING_OVERRIDE, BATCH_CONTROL, CENTER_CUT_REPROCESS,
    UNKNOWN_RESULT_RESOLUTION, COMPENSATION, DLQ_REPLAY, BULK_DOWNLOAD,
    UNMASK, RUNTIME_CONFIG_CHANGE, CREDENTIAL_ROTATION, CERTIFICATE_ROTATION, BREAK_GLASS
}

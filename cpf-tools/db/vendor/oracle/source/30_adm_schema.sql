-- AUTO-GENERATED from cpf-tools/db/canonical/platform-schema.json
-- vendor=oracle
-- DO NOT EDIT generated DDL directly.

-- CPF_LOGICAL_DATABASE=admDB
CREATE TABLE adm_api_permission (
    API_PERMISSION_ID VARCHAR2(120 CHAR) NOT NULL,
    API_GROUP_CODE VARCHAR2(50 CHAR) NOT NULL,
    HTTP_METHOD VARCHAR2(10 CHAR) NOT NULL,
    API_PATH VARCHAR2(300 CHAR) NOT NULL,
    API_NAME VARCHAR2(150 CHAR) NOT NULL,
    PERMISSION_CODE VARCHAR2(50 CHAR) NOT NULL,
    MENU_ID VARCHAR2(50 CHAR),
    BUTTON_ID VARCHAR2(80 CHAR),
    USE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_api_permission PRIMARY KEY (API_PERMISSION_ID),
    CONSTRAINT uk_adm_api_permission_method_path UNIQUE (HTTP_METHOD, API_PATH),
    CONSTRAINT fk_adm_api_permission_menu FOREIGN KEY (MENU_ID) REFERENCES adm_menu (MENU_ID) ON DELETE SET NULL,
    CONSTRAINT fk_adm_api_permission_button FOREIGN KEY (BUTTON_ID) REFERENCES adm_button (BUTTON_ID) ON DELETE SET NULL
);
CREATE INDEX ix_adm_api_permission_group ON adm_api_permission (API_GROUP_CODE, USE_YN);
CREATE INDEX ix_adm_api_permission_menu ON adm_api_permission (MENU_ID, BUTTON_ID);
COMMENT ON TABLE adm_api_permission IS 'ADM API 권한';
COMMENT ON COLUMN adm_api_permission.API_PERMISSION_ID IS 'API 권한 ID';
COMMENT ON COLUMN adm_api_permission.API_GROUP_CODE IS 'API 그룹 코드';
COMMENT ON COLUMN adm_api_permission.HTTP_METHOD IS 'HTTP 메서드';
COMMENT ON COLUMN adm_api_permission.API_PATH IS 'API 경로 패턴';
COMMENT ON COLUMN adm_api_permission.API_NAME IS 'API명';
COMMENT ON COLUMN adm_api_permission.PERMISSION_CODE IS '권한 코드';
COMMENT ON COLUMN adm_api_permission.MENU_ID IS '연결 메뉴 ID';
COMMENT ON COLUMN adm_api_permission.BUTTON_ID IS '연결 버튼/행위 ID';
COMMENT ON COLUMN adm_api_permission.USE_YN IS '사용 여부';
COMMENT ON COLUMN adm_api_permission.created_by IS '등록자';
COMMENT ON COLUMN adm_api_permission.created_at IS '등록일시';
COMMENT ON COLUMN adm_api_permission.updated_by IS '수정자';
COMMENT ON COLUMN adm_api_permission.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_api_permission BEFORE UPDATE ON adm_api_permission FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_approval_execution (
    APPROVAL_REQUEST_ID NUMBER(19) NOT NULL,
    COMMAND_REQUEST_ID VARCHAR2(120 CHAR) NOT NULL,
    EXECUTION_STATUS VARCHAR2(30 CHAR) NOT NULL,
    OWNER_RESULT_CODE VARCHAR2(80 CHAR),
    OWNER_RESULT_MESSAGE VARCHAR2(1000 CHAR),
    STARTED_AT TIMESTAMP(3),
    COMPLETED_AT TIMESTAMP(3),
    RECOVERY_REQUIRED_YN CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_approval_execution PRIMARY KEY (APPROVAL_REQUEST_ID),
    CONSTRAINT uk_adm_approval_execution_command UNIQUE (COMMAND_REQUEST_ID),
    CONSTRAINT ck_adm_approval_execution_status CHECK (EXECUTION_STATUS IN ('PENDING','RUNNING','SUCCEEDED','FAILED','UNKNOWN','RECOVERED')),
    CONSTRAINT ck_adm_approval_execution_recovery CHECK (RECOVERY_REQUIRED_YN IN ('Y','N')),
    CONSTRAINT ck_adm_approval_execution_time CHECK (COMPLETED_AT IS NULL OR STARTED_AT IS NULL OR COMPLETED_AT >= STARTED_AT),
    CONSTRAINT fk_adm_approval_execution_request FOREIGN KEY (APPROVAL_REQUEST_ID) REFERENCES adm_approval_request (APPROVAL_REQUEST_ID)
);
CREATE INDEX ix_adm_approval_execution_recovery ON adm_approval_execution (RECOVERY_REQUIRED_YN, EXECUTION_STATUS);
COMMENT ON TABLE adm_approval_execution IS 'ADM 승인 후 Owner Command 실행 상태';
COMMENT ON COLUMN adm_approval_execution.APPROVAL_REQUEST_ID IS '승인 요청 순번';
COMMENT ON COLUMN adm_approval_execution.COMMAND_REQUEST_ID IS 'Owner Command 멱등 요청 ID';
COMMENT ON COLUMN adm_approval_execution.EXECUTION_STATUS IS 'PENDING/RUNNING/SUCCEEDED/FAILED/UNKNOWN/RECOVERED';
COMMENT ON COLUMN adm_approval_execution.OWNER_RESULT_CODE IS 'Owner 응답 코드';
COMMENT ON COLUMN adm_approval_execution.OWNER_RESULT_MESSAGE IS '마스킹된 Owner 응답 메시지';
COMMENT ON COLUMN adm_approval_execution.STARTED_AT IS '실행 시작시각';
COMMENT ON COLUMN adm_approval_execution.COMPLETED_AT IS '실행 종료시각';
COMMENT ON COLUMN adm_approval_execution.RECOVERY_REQUIRED_YN IS '결과불명/복구 필요 여부';
COMMENT ON COLUMN adm_approval_execution.created_by IS '등록자';
COMMENT ON COLUMN adm_approval_execution.created_at IS '등록일시';
COMMENT ON COLUMN adm_approval_execution.updated_by IS '수정자';
COMMENT ON COLUMN adm_approval_execution.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_approval_execution BEFORE UPDATE ON adm_approval_execution FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_approval_history (
    APPROVAL_HISTORY_ID NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    APPROVAL_REQUEST_ID NUMBER(19) NOT NULL,
    EVENT_TYPE VARCHAR2(40 CHAR) NOT NULL,
    ACTOR_ID VARCHAR2(50 CHAR) NOT NULL,
    BEFORE_STATUS VARCHAR2(30 CHAR),
    AFTER_STATUS VARCHAR2(30 CHAR) NOT NULL,
    REASON VARCHAR2(1000 CHAR) NOT NULL,
    EVENT_DATA CLOB,
    TRANSACTION_ID CHAR(34 CHAR),
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_approval_history PRIMARY KEY (APPROVAL_HISTORY_ID),
    CONSTRAINT fk_adm_approval_history_request FOREIGN KEY (APPROVAL_REQUEST_ID) REFERENCES adm_approval_request (APPROVAL_REQUEST_ID)
);
CREATE INDEX ix_adm_approval_history_request ON adm_approval_history (APPROVAL_REQUEST_ID, APPROVAL_HISTORY_ID);
COMMENT ON TABLE adm_approval_history IS 'ADM 승인 Immutable 이력';
COMMENT ON COLUMN adm_approval_history.APPROVAL_HISTORY_ID IS '승인 이력 순번';
COMMENT ON COLUMN adm_approval_history.APPROVAL_REQUEST_ID IS '승인 요청 순번';
COMMENT ON COLUMN adm_approval_history.EVENT_TYPE IS 'REQUEST/APPROVE/REJECT/CANCEL/EXPIRE/BREAK_GLASS/EXECUTE/RESULT/REVIEW';
COMMENT ON COLUMN adm_approval_history.ACTOR_ID IS '행위 운영자/시스템 ID';
COMMENT ON COLUMN adm_approval_history.BEFORE_STATUS IS '변경 전 상태';
COMMENT ON COLUMN adm_approval_history.AFTER_STATUS IS '변경 후 상태';
COMMENT ON COLUMN adm_approval_history.REASON IS '행위 사유';
COMMENT ON COLUMN adm_approval_history.EVENT_DATA IS '마스킹된 사건 Snapshot';
COMMENT ON COLUMN adm_approval_history.TRANSACTION_ID IS 'CPF transactionId';
COMMENT ON COLUMN adm_approval_history.created_by IS '등록자';
COMMENT ON COLUMN adm_approval_history.created_at IS '등록일시';

CREATE TABLE adm_approval_participant (
    APPROVAL_PARTICIPANT_ID NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    APPROVAL_REQUEST_ID NUMBER(19) NOT NULL,
    STEP_NO NUMBER(10) NOT NULL,
    OPERATOR_ID VARCHAR2(50 CHAR) NOT NULL,
    SOURCE_TARGET_TYPE VARCHAR2(30 CHAR) NOT NULL,
    SOURCE_TARGET_CODE VARCHAR2(100 CHAR) NOT NULL,
    ORGANIZATION_CODE_SNAPSHOT VARCHAR2(50 CHAR),
    POSITION_CODE_SNAPSHOT VARCHAR2(50 CHAR),
    JOB_TITLE_CODE_SNAPSHOT VARCHAR2(50 CHAR),
    DECISION_STATUS VARCHAR2(30 CHAR) NOT NULL DEFAULT 'WAITING',
    IDEMPOTENCY_KEY VARCHAR2(120 CHAR),
    DECISION_REASON VARCHAR2(1000 CHAR),
    DECIDED_AT TIMESTAMP(3),
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_approval_participant PRIMARY KEY (APPROVAL_PARTICIPANT_ID),
    CONSTRAINT uk_adm_approval_participant UNIQUE (APPROVAL_REQUEST_ID, STEP_NO, OPERATOR_ID),
    CONSTRAINT uk_adm_approval_participant_idem UNIQUE (IDEMPOTENCY_KEY),
    CONSTRAINT ck_adm_approval_participant_status CHECK (DECISION_STATUS IN ('WAITING','APPROVED','REJECTED','SKIPPED')),
    CONSTRAINT ck_adm_approval_participant_step CHECK (STEP_NO >= 1),
    CONSTRAINT fk_adm_approval_participant_request FOREIGN KEY (APPROVAL_REQUEST_ID) REFERENCES adm_approval_request (APPROVAL_REQUEST_ID) ON DELETE CASCADE
);
CREATE INDEX ix_adm_approval_participant_inbox ON adm_approval_participant (OPERATOR_ID, DECISION_STATUS, APPROVAL_REQUEST_ID);
COMMENT ON TABLE adm_approval_participant IS 'ADM 승인 참여자 Snapshot';
COMMENT ON COLUMN adm_approval_participant.APPROVAL_PARTICIPANT_ID IS '승인 참여자 순번';
COMMENT ON COLUMN adm_approval_participant.APPROVAL_REQUEST_ID IS '승인 요청 순번';
COMMENT ON COLUMN adm_approval_participant.STEP_NO IS '승인 단계';
COMMENT ON COLUMN adm_approval_participant.OPERATOR_ID IS '해석된 실제 승인 운영자';
COMMENT ON COLUMN adm_approval_participant.SOURCE_TARGET_TYPE IS '정책 대상 유형 Snapshot';
COMMENT ON COLUMN adm_approval_participant.SOURCE_TARGET_CODE IS '정책 대상 코드 Snapshot';
COMMENT ON COLUMN adm_approval_participant.ORGANIZATION_CODE_SNAPSHOT IS '승인 시 조직 Snapshot';
COMMENT ON COLUMN adm_approval_participant.POSITION_CODE_SNAPSHOT IS '승인 시 직급 Snapshot';
COMMENT ON COLUMN adm_approval_participant.JOB_TITLE_CODE_SNAPSHOT IS '승인 시 직책 Snapshot';
COMMENT ON COLUMN adm_approval_participant.DECISION_STATUS IS 'WAITING/APPROVED/REJECTED/SKIPPED';
COMMENT ON COLUMN adm_approval_participant.IDEMPOTENCY_KEY IS '결정 멱등 키';
COMMENT ON COLUMN adm_approval_participant.DECISION_REASON IS '승인/반려 사유';
COMMENT ON COLUMN adm_approval_participant.DECIDED_AT IS '결정 시각';
COMMENT ON COLUMN adm_approval_participant.created_by IS '등록자';
COMMENT ON COLUMN adm_approval_participant.created_at IS '등록일시';
COMMENT ON COLUMN adm_approval_participant.updated_by IS '수정자';
COMMENT ON COLUMN adm_approval_participant.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_approval_participant BEFORE UPDATE ON adm_approval_participant FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_approval_policy (
    POLICY_CODE VARCHAR2(80 CHAR) NOT NULL,
    POLICY_VERSION NUMBER(10) NOT NULL,
    POLICY_NAME VARCHAR2(150 CHAR) NOT NULL,
    ACTION_TYPE VARCHAR2(80 CHAR) NOT NULL,
    EFFECTIVE_FROM TIMESTAMP(3) NOT NULL,
    EFFECTIVE_TO TIMESTAMP(3),
    ENABLED_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    SELF_APPROVAL_ALLOWED_YN CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    BREAK_GLASS_ALLOWED_YN CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    DESCRIPTION VARCHAR2(1000 CHAR),
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_approval_policy PRIMARY KEY (POLICY_CODE, POLICY_VERSION),
    CONSTRAINT ck_adm_approval_policy_version CHECK (POLICY_VERSION > 0),
    CONSTRAINT ck_adm_approval_policy_flags CHECK (ENABLED_YN IN ('Y','N') AND SELF_APPROVAL_ALLOWED_YN IN ('Y','N') AND BREAK_GLASS_ALLOWED_YN IN ('Y','N')),
    CONSTRAINT ck_adm_approval_policy_effective CHECK (EFFECTIVE_TO IS NULL OR EFFECTIVE_TO > EFFECTIVE_FROM)
);
CREATE INDEX ix_adm_approval_policy_action ON adm_approval_policy (ACTION_TYPE, ENABLED_YN, EFFECTIVE_FROM, EFFECTIVE_TO);
COMMENT ON TABLE adm_approval_policy IS 'ADM 위험조치 승인 정책 Version';
COMMENT ON COLUMN adm_approval_policy.POLICY_CODE IS '위험조치 승인 정책 코드';
COMMENT ON COLUMN adm_approval_policy.POLICY_VERSION IS '정책 버전';
COMMENT ON COLUMN adm_approval_policy.POLICY_NAME IS '정책명';
COMMENT ON COLUMN adm_approval_policy.ACTION_TYPE IS '대상 위험조치 유형';
COMMENT ON COLUMN adm_approval_policy.EFFECTIVE_FROM IS '시행 시작시각';
COMMENT ON COLUMN adm_approval_policy.EFFECTIVE_TO IS '시행 종료시각';
COMMENT ON COLUMN adm_approval_policy.ENABLED_YN IS '활성 여부';
COMMENT ON COLUMN adm_approval_policy.SELF_APPROVAL_ALLOWED_YN IS '자기승인 허용 여부';
COMMENT ON COLUMN adm_approval_policy.BREAK_GLASS_ALLOWED_YN IS '긴급 우회 허용 여부';
COMMENT ON COLUMN adm_approval_policy.DESCRIPTION IS '정책 설명';
COMMENT ON COLUMN adm_approval_policy.created_by IS '등록자';
COMMENT ON COLUMN adm_approval_policy.created_at IS '등록일시';
COMMENT ON COLUMN adm_approval_policy.updated_by IS '수정자';
COMMENT ON COLUMN adm_approval_policy.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_approval_policy BEFORE UPDATE ON adm_approval_policy FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_approval_policy_step (
    POLICY_CODE VARCHAR2(80 CHAR) NOT NULL,
    POLICY_VERSION NUMBER(10) NOT NULL,
    STEP_NO NUMBER(10) NOT NULL,
    STEP_TYPE VARCHAR2(30 CHAR) NOT NULL DEFAULT 'APPROVAL',
    TARGET_TYPE VARCHAR2(30 CHAR) NOT NULL,
    TARGET_CODE VARCHAR2(100 CHAR) NOT NULL,
    DECISION_RULE VARCHAR2(20 CHAR) NOT NULL DEFAULT 'ALL',
    REQUIRED_COUNT NUMBER(10),
    REQUIRED_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_approval_policy_step PRIMARY KEY (POLICY_CODE, POLICY_VERSION, STEP_NO, TARGET_TYPE, TARGET_CODE),
    CONSTRAINT ck_adm_approval_policy_step_no CHECK (STEP_NO >= 1),
    CONSTRAINT ck_adm_approval_policy_step_type CHECK (STEP_TYPE IN ('APPROVAL','REVIEW')),
    CONSTRAINT ck_adm_approval_policy_step_target CHECK (TARGET_TYPE IN ('OPERATOR','ROLE','ORGANIZATION','ORG_MANAGER')),
    CONSTRAINT ck_adm_approval_policy_step_rule CHECK (DECISION_RULE IN ('ALL','ANY','N_OF_M')),
    CONSTRAINT ck_adm_approval_policy_step_required CHECK (REQUIRED_YN IN ('Y','N') AND ( (DECISION_RULE = 'N_OF_M' AND REQUIRED_COUNT IS NOT NULL AND REQUIRED_COUNT > 0) OR (DECISION_RULE <> 'N_OF_M' AND REQUIRED_COUNT IS NULL) )),
    CONSTRAINT fk_adm_approval_policy_step_policy FOREIGN KEY (POLICY_CODE, POLICY_VERSION) REFERENCES adm_approval_policy (POLICY_CODE, POLICY_VERSION) ON DELETE CASCADE
);
COMMENT ON TABLE adm_approval_policy_step IS 'ADM 승인 정책 단계';
COMMENT ON COLUMN adm_approval_policy_step.POLICY_CODE IS '승인 정책 코드';
COMMENT ON COLUMN adm_approval_policy_step.POLICY_VERSION IS '승인 정책 버전';
COMMENT ON COLUMN adm_approval_policy_step.STEP_NO IS '승인 단계';
COMMENT ON COLUMN adm_approval_policy_step.STEP_TYPE IS 'APPROVAL/REVIEW';
COMMENT ON COLUMN adm_approval_policy_step.TARGET_TYPE IS 'OPERATOR/ROLE/ORGANIZATION/ORG_MANAGER';
COMMENT ON COLUMN adm_approval_policy_step.TARGET_CODE IS '대상 운영자/역할/조직 코드';
COMMENT ON COLUMN adm_approval_policy_step.DECISION_RULE IS 'ALL/ANY/N_OF_M';
COMMENT ON COLUMN adm_approval_policy_step.REQUIRED_COUNT IS 'N_OF_M 최소 승인 수';
COMMENT ON COLUMN adm_approval_policy_step.REQUIRED_YN IS '필수 단계 여부';
COMMENT ON COLUMN adm_approval_policy_step.created_by IS '등록자';
COMMENT ON COLUMN adm_approval_policy_step.created_at IS '등록일시';
COMMENT ON COLUMN adm_approval_policy_step.updated_by IS '수정자';
COMMENT ON COLUMN adm_approval_policy_step.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_approval_policy_step BEFORE UPDATE ON adm_approval_policy_step FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_approval_request (
    APPROVAL_REQUEST_ID NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    REQUEST_KEY VARCHAR2(120 CHAR) NOT NULL,
    POLICY_CODE VARCHAR2(80 CHAR) NOT NULL,
    POLICY_VERSION NUMBER(10) NOT NULL,
    ACTION_TYPE VARCHAR2(80 CHAR) NOT NULL,
    OWNER_MODULE VARCHAR2(30 CHAR) NOT NULL,
    OWNER_COMMAND VARCHAR2(120 CHAR) NOT NULL,
    TARGET_TYPE VARCHAR2(80 CHAR) NOT NULL,
    TARGET_ID VARCHAR2(200 CHAR) NOT NULL,
    REQUESTED_BY VARCHAR2(50 CHAR) NOT NULL,
    REQUEST_REASON VARCHAR2(1000 CHAR) NOT NULL,
    COMMAND_PAYLOAD_HASH CHAR(64 CHAR) NOT NULL,
    COMMAND_PAYLOAD_SNAPSHOT CLOB,
    APPROVAL_STATUS VARCHAR2(30 CHAR) NOT NULL DEFAULT 'PENDING',
    CURRENT_STEP_NO NUMBER(10) NOT NULL DEFAULT 1,
    EXPIRE_AT TIMESTAMP(3),
    TRANSACTION_ID CHAR(34 CHAR),
    VERSION_NO NUMBER(19) NOT NULL DEFAULT 0,
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_approval_request PRIMARY KEY (APPROVAL_REQUEST_ID),
    CONSTRAINT uk_adm_approval_request_key UNIQUE (REQUEST_KEY),
    CONSTRAINT ck_adm_approval_request_status CHECK (APPROVAL_STATUS IN ('PENDING','APPROVED','REJECTED','CANCELLED','EXPIRED','EXECUTING','COMPLETED','FAILED','UNKNOWN')),
    CONSTRAINT ck_adm_approval_request_version CHECK (VERSION_NO >= 0),
    CONSTRAINT ck_adm_approval_request_step CHECK (CURRENT_STEP_NO >= 1),
    CONSTRAINT ck_adm_approval_request_hash CHECK (CHAR_LENGTH(COMMAND_PAYLOAD_HASH) = 64),
    CONSTRAINT fk_adm_approval_request_policy FOREIGN KEY (POLICY_CODE, POLICY_VERSION) REFERENCES adm_approval_policy (POLICY_CODE, POLICY_VERSION)
);
CREATE INDEX ix_adm_approval_request_status ON adm_approval_request (APPROVAL_STATUS, EXPIRE_AT, APPROVAL_REQUEST_ID);
CREATE INDEX ix_adm_approval_request_actor ON adm_approval_request (REQUESTED_BY, created_at);
COMMENT ON TABLE adm_approval_request IS 'ADM 위험조치 승인 요청';
COMMENT ON COLUMN adm_approval_request.APPROVAL_REQUEST_ID IS '위험조치 승인 요청 순번';
COMMENT ON COLUMN adm_approval_request.REQUEST_KEY IS '멱등 승인 요청 키';
COMMENT ON COLUMN adm_approval_request.POLICY_CODE IS '적용 정책 코드';
COMMENT ON COLUMN adm_approval_request.POLICY_VERSION IS '적용 정책 버전 Snapshot';
COMMENT ON COLUMN adm_approval_request.ACTION_TYPE IS '위험조치 유형';
COMMENT ON COLUMN adm_approval_request.OWNER_MODULE IS '실제 Command Owner Module';
COMMENT ON COLUMN adm_approval_request.OWNER_COMMAND IS '실행할 Owner Command';
COMMENT ON COLUMN adm_approval_request.TARGET_TYPE IS '위험조치 대상 유형';
COMMENT ON COLUMN adm_approval_request.TARGET_ID IS '위험조치 대상 ID';
COMMENT ON COLUMN adm_approval_request.REQUESTED_BY IS '요청 운영자';
COMMENT ON COLUMN adm_approval_request.REQUEST_REASON IS '요청 사유';
COMMENT ON COLUMN adm_approval_request.COMMAND_PAYLOAD_HASH IS '승인 대상 Command payload SHA-256';
COMMENT ON COLUMN adm_approval_request.COMMAND_PAYLOAD_SNAPSHOT IS '마스킹된 승인 대상 Command Snapshot';
COMMENT ON COLUMN adm_approval_request.APPROVAL_STATUS IS 'PENDING/APPROVED/REJECTED/CANCELLED/EXPIRED/EXECUTING/COMPLETED/FAILED/UNKNOWN';
COMMENT ON COLUMN adm_approval_request.CURRENT_STEP_NO IS '현재 승인 단계';
COMMENT ON COLUMN adm_approval_request.EXPIRE_AT IS '승인 만료시각';
COMMENT ON COLUMN adm_approval_request.TRANSACTION_ID IS 'CPF transactionId';
COMMENT ON COLUMN adm_approval_request.VERSION_NO IS '낙관적 잠금 버전';
COMMENT ON COLUMN adm_approval_request.created_by IS '등록자';
COMMENT ON COLUMN adm_approval_request.created_at IS '등록일시';
COMMENT ON COLUMN adm_approval_request.updated_by IS '수정자';
COMMENT ON COLUMN adm_approval_request.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_approval_request BEFORE UPDATE ON adm_approval_request FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_audit_delivery (
    DELIVERY_ID NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    TRANSACTION_ID CHAR(34 CHAR) NOT NULL,
    TRACE_ID VARCHAR2(64 CHAR),
    OPERATOR_ID VARCHAR2(100 CHAR) NOT NULL,
    ACTION_TYPE VARCHAR2(100 CHAR) NOT NULL,
    TARGET_TYPE VARCHAR2(100 CHAR),
    TARGET_ID VARCHAR2(255 CHAR),
    REASON VARCHAR2(1000 CHAR) NOT NULL,
    BEFORE_DATA CLOB,
    AFTER_DATA CLOB,
    DIFF_DATA CLOB,
    CLIENT_IP VARCHAR2(64 CHAR),
    OPERATION_STATUS VARCHAR2(20 CHAR) NOT NULL DEFAULT 'REQUESTED',
    DELIVERY_STATUS VARCHAR2(20 CHAR) NOT NULL DEFAULT 'PENDING',
    ATTEMPT_COUNT NUMBER(10) NOT NULL DEFAULT 0,
    MAX_ATTEMPTS NUMBER(10) NOT NULL DEFAULT 10,
    NEXT_ATTEMPT_AT TIMESTAMP(3),
    LAST_ERROR VARCHAR2(1000 CHAR),
    AUDIT_ID NUMBER(19),
    REQUESTED_AT TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    DELIVERED_AT TIMESTAMP(3),
    CREATED_BY VARCHAR2(100 CHAR) NOT NULL,
    UPDATED_BY VARCHAR2(100 CHAR) NOT NULL,
    CREATED_AT TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    UPDATED_AT TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_audit_delivery PRIMARY KEY (DELIVERY_ID),
    CONSTRAINT ck_adm_audit_delivery_operation CHECK (OPERATION_STATUS IN ('REQUESTED','SUCCEEDED','FAILED','UNKNOWN')),
    CONSTRAINT ck_adm_audit_delivery_status CHECK (DELIVERY_STATUS IN ('PENDING','RETRY','FAILED','DELIVERED'))
);
CREATE INDEX ix_adm_audit_delivery_status ON adm_audit_delivery (DELIVERY_STATUS, OPERATION_STATUS, NEXT_ATTEMPT_AT);
CREATE INDEX ix_adm_audit_delivery_tx ON adm_audit_delivery (TRANSACTION_ID);
CREATE INDEX ix_adm_audit_delivery_operator ON adm_audit_delivery (OPERATOR_ID, REQUESTED_AT);
COMMENT ON TABLE adm_audit_delivery IS 'ADM 필수 감사 Delivery 원장';
COMMENT ON COLUMN adm_audit_delivery.DELIVERY_ID IS 'Delivery identifier';
COMMENT ON COLUMN adm_audit_delivery.TRANSACTION_ID IS 'CPF 전역 transactionId';
COMMENT ON COLUMN adm_audit_delivery.TRACE_ID IS 'Trace identifier';
COMMENT ON COLUMN adm_audit_delivery.OPERATOR_ID IS 'Operator identifier';
COMMENT ON COLUMN adm_audit_delivery.ACTION_TYPE IS 'Action type';
COMMENT ON COLUMN adm_audit_delivery.TARGET_TYPE IS 'Target type';
COMMENT ON COLUMN adm_audit_delivery.TARGET_ID IS 'Target identifier';
COMMENT ON COLUMN adm_audit_delivery.REASON IS 'Reason';
COMMENT ON COLUMN adm_audit_delivery.BEFORE_DATA IS 'Before data';
COMMENT ON COLUMN adm_audit_delivery.AFTER_DATA IS 'After data';
COMMENT ON COLUMN adm_audit_delivery.DIFF_DATA IS 'Change data';
COMMENT ON COLUMN adm_audit_delivery.CLIENT_IP IS 'Client IP';
COMMENT ON COLUMN adm_audit_delivery.OPERATION_STATUS IS 'Operation status';
COMMENT ON COLUMN adm_audit_delivery.DELIVERY_STATUS IS 'Delivery status';
COMMENT ON COLUMN adm_audit_delivery.ATTEMPT_COUNT IS 'Attempt count';
COMMENT ON COLUMN adm_audit_delivery.MAX_ATTEMPTS IS 'Max attempts';
COMMENT ON COLUMN adm_audit_delivery.NEXT_ATTEMPT_AT IS 'Next attempt time';
COMMENT ON COLUMN adm_audit_delivery.LAST_ERROR IS 'Last error';
COMMENT ON COLUMN adm_audit_delivery.AUDIT_ID IS 'Audit identifier';
COMMENT ON COLUMN adm_audit_delivery.REQUESTED_AT IS 'Request time';
COMMENT ON COLUMN adm_audit_delivery.DELIVERED_AT IS 'Delivery time';
COMMENT ON COLUMN adm_audit_delivery.CREATED_BY IS 'Creator';
COMMENT ON COLUMN adm_audit_delivery.UPDATED_BY IS 'Last updater';
COMMENT ON COLUMN adm_audit_delivery.CREATED_AT IS 'Creation time';
COMMENT ON COLUMN adm_audit_delivery.UPDATED_AT IS 'Last update time';

CREATE TABLE adm_audit_log (
    AUDIT_ID NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    TRANSACTION_ID CHAR(34 CHAR),
    TRACE_ID VARCHAR2(80 CHAR),
    OPERATOR_ID VARCHAR2(50 CHAR) NOT NULL,
    MENU_ID VARCHAR2(50 CHAR),
    BUTTON_ID VARCHAR2(80 CHAR),
    ACTION_TYPE VARCHAR2(30 CHAR) NOT NULL,
    TARGET_TYPE VARCHAR2(50 CHAR),
    TARGET_ID VARCHAR2(100 CHAR),
    REASON VARCHAR2(500 CHAR) NOT NULL,
    BEFORE_DATA CLOB,
    AFTER_DATA CLOB,
    DIFF_DATA CLOB,
    REQUEST_BODY CLOB,
    CLIENT_IP VARCHAR2(50 CHAR),
    RETENTION_UNTIL DATE,
    IMMUTABLE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_audit_log PRIMARY KEY (AUDIT_ID)
);
CREATE INDEX ix_adm_audit_log_tx ON adm_audit_log (TRANSACTION_ID);
CREATE INDEX ix_adm_audit_log_operator_time ON adm_audit_log (OPERATOR_ID, created_at);
CREATE INDEX ix_adm_audit_log_action_time ON adm_audit_log (ACTION_TYPE, created_at);
CREATE INDEX ix_adm_audit_log_target_time ON adm_audit_log (TARGET_TYPE, TARGET_ID, created_at);
CREATE INDEX ix_adm_audit_log_retention ON adm_audit_log (RETENTION_UNTIL, IMMUTABLE_YN);
COMMENT ON TABLE adm_audit_log IS 'ADM 감사 로그';
COMMENT ON COLUMN adm_audit_log.AUDIT_ID IS '감사 로그 순번';
COMMENT ON COLUMN adm_audit_log.TRANSACTION_ID IS 'CPF 전역 transactionId';
COMMENT ON COLUMN adm_audit_log.TRACE_ID IS '분산 추적 ID';
COMMENT ON COLUMN adm_audit_log.OPERATOR_ID IS '운영자 ID';
COMMENT ON COLUMN adm_audit_log.MENU_ID IS '메뉴 ID';
COMMENT ON COLUMN adm_audit_log.BUTTON_ID IS '버튼/행위 ID';
COMMENT ON COLUMN adm_audit_log.ACTION_TYPE IS '행위 유형';
COMMENT ON COLUMN adm_audit_log.TARGET_TYPE IS '대상 유형';
COMMENT ON COLUMN adm_audit_log.TARGET_ID IS '대상 ID';
COMMENT ON COLUMN adm_audit_log.REASON IS '감사 사유';
COMMENT ON COLUMN adm_audit_log.BEFORE_DATA IS '변경 전 데이터';
COMMENT ON COLUMN adm_audit_log.AFTER_DATA IS '변경 후 데이터';
COMMENT ON COLUMN adm_audit_log.DIFF_DATA IS '변경 차이 데이터';
COMMENT ON COLUMN adm_audit_log.REQUEST_BODY IS '요청 본문';
COMMENT ON COLUMN adm_audit_log.CLIENT_IP IS '클라이언트 IP';
COMMENT ON COLUMN adm_audit_log.RETENTION_UNTIL IS '보존 만료 기준일';
COMMENT ON COLUMN adm_audit_log.IMMUTABLE_YN IS '삭제 불가 여부';
COMMENT ON COLUMN adm_audit_log.created_by IS '등록자';
COMMENT ON COLUMN adm_audit_log.created_at IS '등록일시';
COMMENT ON COLUMN adm_audit_log.updated_by IS '수정자';
COMMENT ON COLUMN adm_audit_log.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_audit_log BEFORE UPDATE ON adm_audit_log FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_break_glass_session (
    session_id CHAR(36 CHAR) NOT NULL,
    operator_id VARCHAR2(100 CHAR) NOT NULL,
    scope_type VARCHAR2(60 CHAR) NOT NULL,
    scope_value VARCHAR2(200 CHAR) NOT NULL,
    reason VARCHAR2(1000 CHAR) NOT NULL,
    status VARCHAR2(20 CHAR) NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP(3) NOT NULL,
    closed_at TIMESTAMP(3),
    close_reason VARCHAR2(1000 CHAR),
    post_review_status VARCHAR2(20 CHAR) NOT NULL DEFAULT 'PENDING',
    reviewed_by VARCHAR2(100 CHAR),
    reviewed_at TIMESTAMP(3),
    review_reason VARCHAR2(1000 CHAR),
    created_by VARCHAR2(100 CHAR) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_by VARCHAR2(100 CHAR) NOT NULL,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_break_glass_session PRIMARY KEY (session_id),
    CONSTRAINT ck_adm_break_glass_status CHECK (status IN ('ACTIVE','CLOSED','EXPIRED')),
    CONSTRAINT ck_adm_break_glass_review CHECK (post_review_status IN ('PENDING','APPROVED','REJECTED'))
);
CREATE INDEX ix_adm_break_glass_operator ON adm_break_glass_session (operator_id, status, expires_at);
CREATE INDEX ix_adm_break_glass_scope ON adm_break_glass_session (scope_type, scope_value, status);
CREATE INDEX ix_adm_break_glass_review ON adm_break_glass_session (post_review_status, closed_at);
COMMENT ON TABLE adm_break_glass_session IS 'ADM 범위/TTL 제한 Break-glass 세션';
COMMENT ON COLUMN adm_break_glass_session.session_id IS '비상 권한 세션 UUID';
COMMENT ON COLUMN adm_break_glass_session.operator_id IS '세션 소유 운영자';
COMMENT ON COLUMN adm_break_glass_session.scope_type IS 'SERVICE/BATCH/CENTER_CUT/RECOVERY/SECURITY 등 좁은 Scope 종류';
COMMENT ON COLUMN adm_break_glass_session.scope_value IS 'Scope 대상 식별자';
COMMENT ON COLUMN adm_break_glass_session.reason IS '발급 사유';
COMMENT ON COLUMN adm_break_glass_session.status IS 'ACTIVE/CLOSED/EXPIRED';
COMMENT ON COLUMN adm_break_glass_session.expires_at IS '강제 만료시각';
COMMENT ON COLUMN adm_break_glass_session.closed_at IS '종료/만료시각';
COMMENT ON COLUMN adm_break_glass_session.close_reason IS '종료/만료 사유';
COMMENT ON COLUMN adm_break_glass_session.post_review_status IS 'PENDING/APPROVED/REJECTED';
COMMENT ON COLUMN adm_break_glass_session.reviewed_by IS '사후검토자';
COMMENT ON COLUMN adm_break_glass_session.reviewed_at IS '사후검토시각';
COMMENT ON COLUMN adm_break_glass_session.review_reason IS '사후검토 의견';
COMMENT ON COLUMN adm_break_glass_session.created_by IS 'Creator';
COMMENT ON COLUMN adm_break_glass_session.created_at IS 'Creation time';
COMMENT ON COLUMN adm_break_glass_session.updated_by IS 'Last updater';
COMMENT ON COLUMN adm_break_glass_session.updated_at IS 'Last update time';
CREATE OR REPLACE TRIGGER trg_touch_adm_break_glass_session BEFORE UPDATE ON adm_break_glass_session FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_button (
    BUTTON_ID VARCHAR2(80 CHAR) NOT NULL,
    MENU_ID VARCHAR2(50 CHAR) NOT NULL,
    ACTION_CODE VARCHAR2(50 CHAR) NOT NULL,
    BUTTON_NAME VARCHAR2(100 CHAR) NOT NULL,
    HTTP_METHOD VARCHAR2(10 CHAR),
    API_PATTERN VARCHAR2(300 CHAR),
    SORT_ORDER NUMBER(10) NOT NULL DEFAULT 0,
    USE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_button PRIMARY KEY (BUTTON_ID),
    CONSTRAINT uk_adm_button_menu_action UNIQUE (MENU_ID, ACTION_CODE),
    CONSTRAINT fk_adm_button_menu FOREIGN KEY (MENU_ID) REFERENCES adm_menu (MENU_ID) ON DELETE CASCADE
);
CREATE INDEX ix_adm_button_menu ON adm_button (MENU_ID, SORT_ORDER);
COMMENT ON TABLE adm_button IS 'ADM 메뉴별 버튼/행위';
COMMENT ON COLUMN adm_button.BUTTON_ID IS '버튼/행위 ID';
COMMENT ON COLUMN adm_button.MENU_ID IS '메뉴 ID';
COMMENT ON COLUMN adm_button.ACTION_CODE IS '행위 코드';
COMMENT ON COLUMN adm_button.BUTTON_NAME IS '버튼/행위명';
COMMENT ON COLUMN adm_button.HTTP_METHOD IS '대상 HTTP 메서드';
COMMENT ON COLUMN adm_button.API_PATTERN IS '대상 API 경로 패턴';
COMMENT ON COLUMN adm_button.SORT_ORDER IS '정렬 순서';
COMMENT ON COLUMN adm_button.USE_YN IS '사용 여부';
COMMENT ON COLUMN adm_button.created_by IS '등록자';
COMMENT ON COLUMN adm_button.created_at IS '등록일시';
COMMENT ON COLUMN adm_button.updated_by IS '수정자';
COMMENT ON COLUMN adm_button.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_button BEFORE UPDATE ON adm_button FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_download_audit_log (
    DOWNLOAD_ID NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    ADMIN_ID VARCHAR2(50 CHAR) NOT NULL,
    MENU_ID VARCHAR2(50 CHAR),
    SCREEN_ID VARCHAR2(100 CHAR),
    DOWNLOAD_TYPE VARCHAR2(50 CHAR) NOT NULL,
    TARGET_TYPE VARCHAR2(50 CHAR),
    SEARCH_CONDITION_SUMMARY CLOB,
    ROW_COUNT NUMBER(10) NOT NULL DEFAULT 0,
    MASKED_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    INCLUDE_SENSITIVE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    REASON VARCHAR2(500 CHAR) NOT NULL,
    CLIENT_IP VARCHAR2(50 CHAR),
    USER_AGENT VARCHAR2(500 CHAR),
    CSV_POLICY_VERSION VARCHAR2(30 CHAR) NOT NULL DEFAULT 'CPF-CSV-1',
    REQUESTED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    COMPLETED_AT TIMESTAMP,
    STATUS VARCHAR2(20 CHAR) NOT NULL DEFAULT 'REQUESTED',
    FAILURE_REASON VARCHAR2(1000 CHAR),
    FILE_NAME VARCHAR2(300 CHAR),
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_download_audit_log PRIMARY KEY (DOWNLOAD_ID)
);
CREATE INDEX ix_adm_download_audit_log_admin_time ON adm_download_audit_log (ADMIN_ID, REQUESTED_AT);
CREATE INDEX ix_adm_download_audit_log_type_time ON adm_download_audit_log (DOWNLOAD_TYPE, REQUESTED_AT);
CREATE INDEX ix_adm_download_audit_log_status_time ON adm_download_audit_log (STATUS, REQUESTED_AT);
COMMENT ON TABLE adm_download_audit_log IS 'ADM 다운로드 감사 로그';
COMMENT ON COLUMN adm_download_audit_log.DOWNLOAD_ID IS '다운로드 감사 로그 순번';
COMMENT ON COLUMN adm_download_audit_log.ADMIN_ID IS '요청 운영자 ID';
COMMENT ON COLUMN adm_download_audit_log.MENU_ID IS '메뉴 ID';
COMMENT ON COLUMN adm_download_audit_log.SCREEN_ID IS '화면 ID';
COMMENT ON COLUMN adm_download_audit_log.DOWNLOAD_TYPE IS '다운로드 유형';
COMMENT ON COLUMN adm_download_audit_log.TARGET_TYPE IS '대상 유형';
COMMENT ON COLUMN adm_download_audit_log.SEARCH_CONDITION_SUMMARY IS '검색 조건 요약';
COMMENT ON COLUMN adm_download_audit_log.ROW_COUNT IS '다운로드 행 수';
COMMENT ON COLUMN adm_download_audit_log.MASKED_YN IS '마스킹 적용 여부';
COMMENT ON COLUMN adm_download_audit_log.INCLUDE_SENSITIVE_YN IS '민감정보 포함 요청 여부';
COMMENT ON COLUMN adm_download_audit_log.REASON IS '다운로드 사유';
COMMENT ON COLUMN adm_download_audit_log.CLIENT_IP IS '클라이언트 IP';
COMMENT ON COLUMN adm_download_audit_log.USER_AGENT IS 'User-Agent';
COMMENT ON COLUMN adm_download_audit_log.CSV_POLICY_VERSION IS 'CSV spreadsheet injection protection policy version';
COMMENT ON COLUMN adm_download_audit_log.REQUESTED_AT IS '요청 일시';
COMMENT ON COLUMN adm_download_audit_log.COMPLETED_AT IS '완료 일시';
COMMENT ON COLUMN adm_download_audit_log.STATUS IS '처리 상태';
COMMENT ON COLUMN adm_download_audit_log.FAILURE_REASON IS '실패 사유';
COMMENT ON COLUMN adm_download_audit_log.FILE_NAME IS '파일명';
COMMENT ON COLUMN adm_download_audit_log.created_by IS '등록자';
COMMENT ON COLUMN adm_download_audit_log.created_at IS '등록일시';
COMMENT ON COLUMN adm_download_audit_log.updated_by IS '수정자';
COMMENT ON COLUMN adm_download_audit_log.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_download_audit_log BEFORE UPDATE ON adm_download_audit_log FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_dynamic_log_level_rule (
    RULE_ID VARCHAR2(80 CHAR) NOT NULL,
    TRANSACTION_ID CHAR(34 CHAR),
    BUSINESS_TRANSACTION_ID VARCHAR2(20 CHAR),
    MODULE_ID VARCHAR2(10 CHAR),
    LOG_LEVEL VARCHAR2(10 CHAR) NOT NULL,
    EXPIRE_AT TIMESTAMP NOT NULL,
    REASON VARCHAR2(500 CHAR),
    USE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_dynamic_log_level_rule PRIMARY KEY (RULE_ID)
);
CREATE INDEX ix_adm_dynamic_log_level_rule_biz_tx ON adm_dynamic_log_level_rule (BUSINESS_TRANSACTION_ID, EXPIRE_AT);
CREATE INDEX ix_adm_dynamic_log_level_rule_tx ON adm_dynamic_log_level_rule (TRANSACTION_ID, EXPIRE_AT);
CREATE INDEX ix_adm_dynamic_log_level_rule_active ON adm_dynamic_log_level_rule (USE_YN, EXPIRE_AT);
COMMENT ON TABLE adm_dynamic_log_level_rule IS 'ADM 동적 로그 레벨 규칙';
COMMENT ON COLUMN adm_dynamic_log_level_rule.RULE_ID IS '동적 로그 레벨 규칙 ID';
COMMENT ON COLUMN adm_dynamic_log_level_rule.TRANSACTION_ID IS '프레임워크 거래 ID';
COMMENT ON COLUMN adm_dynamic_log_level_rule.BUSINESS_TRANSACTION_ID IS '업무 거래 ID';
COMMENT ON COLUMN adm_dynamic_log_level_rule.MODULE_ID IS '모듈 ID';
COMMENT ON COLUMN adm_dynamic_log_level_rule.LOG_LEVEL IS '적용 로그 레벨';
COMMENT ON COLUMN adm_dynamic_log_level_rule.EXPIRE_AT IS '만료일시';
COMMENT ON COLUMN adm_dynamic_log_level_rule.REASON IS '적용 사유';
COMMENT ON COLUMN adm_dynamic_log_level_rule.USE_YN IS '사용 여부';
COMMENT ON COLUMN adm_dynamic_log_level_rule.created_by IS '등록자';
COMMENT ON COLUMN adm_dynamic_log_level_rule.created_at IS '등록일시';
COMMENT ON COLUMN adm_dynamic_log_level_rule.updated_by IS '수정자';
COMMENT ON COLUMN adm_dynamic_log_level_rule.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_dynamic_log_level_rule BEFORE UPDATE ON adm_dynamic_log_level_rule FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_file_job (
    job_id VARCHAR2(36 CHAR) NOT NULL,
    operation_id VARCHAR2(100 CHAR) NOT NULL,
    request_hash VARCHAR2(64 CHAR) NOT NULL,
    job_type VARCHAR2(20 CHAR) NOT NULL,
    template_code VARCHAR2(100 CHAR) NOT NULL,
    template_version NUMBER(10) NOT NULL,
    file_format VARCHAR2(10 CHAR) NOT NULL,
    job_state VARCHAR2(30 CHAR) NOT NULL,
    dry_run CHAR(1 CHAR) NOT NULL,
    rollback_supported CHAR(1 CHAR) NOT NULL,
    source_path VARCHAR2(1000 CHAR),
    result_path VARCHAR2(1000 CHAR),
    source_sha256 VARCHAR2(64 CHAR),
    result_sha256 VARCHAR2(64 CHAR),
    total_rows NUMBER(19) NOT NULL DEFAULT 0,
    success_rows NUMBER(19) NOT NULL DEFAULT 0,
    failed_rows NUMBER(19) NOT NULL DEFAULT 0,
    lease_owner VARCHAR2(100 CHAR),
    fencing_token NUMBER(19) NOT NULL DEFAULT 0,
    lease_until TIMESTAMP(6),
    retention_until TIMESTAMP(6) NOT NULL,
    requested_by VARCHAR2(100 CHAR) NOT NULL,
    reason VARCHAR2(500 CHAR) NOT NULL,
    client_ip VARCHAR2(64 CHAR),
    error_code VARCHAR2(80 CHAR),
    error_message VARCHAR2(1000 CHAR),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    approval_id VARCHAR2(120 CHAR),
    applied_by VARCHAR2(100 CHAR),
    resolved_by VARCHAR2(100 CHAR),
    control_by VARCHAR2(100 CHAR),
    control_reason VARCHAR2(500 CHAR),
    control_updated_at TIMESTAMP(6),
    CONSTRAINT pk_adm_file_job PRIMARY KEY (job_id),
    CONSTRAINT uk_adm_file_job_operation UNIQUE (operation_id)
);
CREATE INDEX ix_adm_file_job_claim ON adm_file_job (job_state, lease_until, created_at);
CREATE INDEX ix_adm_file_job_retention ON adm_file_job (retention_until, job_state);
CREATE INDEX ix_adm_file_job_approval ON adm_file_job (approval_id, job_state);
COMMENT ON TABLE adm_file_job IS 'ADM 비동기 대량 File Job 원장';
COMMENT ON COLUMN adm_file_job.job_id IS 'File Job ID';
COMMENT ON COLUMN adm_file_job.operation_id IS '멱등 Operation ID';
COMMENT ON COLUMN adm_file_job.request_hash IS '요청 SHA-256';
COMMENT ON COLUMN adm_file_job.job_type IS 'UPLOAD 또는 DOWNLOAD';
COMMENT ON COLUMN adm_file_job.template_code IS 'Template Code';
COMMENT ON COLUMN adm_file_job.template_version IS 'Template Version';
COMMENT ON COLUMN adm_file_job.file_format IS 'CSV 또는 XLSX';
COMMENT ON COLUMN adm_file_job.job_state IS 'Job 상태';
COMMENT ON COLUMN adm_file_job.dry_run IS 'Dry-run 여부';
COMMENT ON COLUMN adm_file_job.rollback_supported IS 'Rollback 지원 여부';
COMMENT ON COLUMN adm_file_job.source_path IS 'Source Artifact 경로';
COMMENT ON COLUMN adm_file_job.result_path IS 'Result Artifact 경로';
COMMENT ON COLUMN adm_file_job.source_sha256 IS 'Source SHA-256';
COMMENT ON COLUMN adm_file_job.result_sha256 IS 'Result SHA-256';
COMMENT ON COLUMN adm_file_job.total_rows IS '전체 행 수';
COMMENT ON COLUMN adm_file_job.success_rows IS '성공 행 수';
COMMENT ON COLUMN adm_file_job.failed_rows IS '실패 행 수';
COMMENT ON COLUMN adm_file_job.lease_owner IS 'Lease 소유자';
COMMENT ON COLUMN adm_file_job.fencing_token IS 'Fencing Token';
COMMENT ON COLUMN adm_file_job.lease_until IS 'Lease 만료';
COMMENT ON COLUMN adm_file_job.retention_until IS 'Artifact 보존 만료';
COMMENT ON COLUMN adm_file_job.requested_by IS '요청 운영자';
COMMENT ON COLUMN adm_file_job.reason IS '요청 사유';
COMMENT ON COLUMN adm_file_job.client_ip IS '요청 Client IP';
COMMENT ON COLUMN adm_file_job.error_code IS '오류 코드';
COMMENT ON COLUMN adm_file_job.error_message IS '마스킹된 오류 메시지';
COMMENT ON COLUMN adm_file_job.created_at IS '생성 일시';
COMMENT ON COLUMN adm_file_job.updated_at IS '수정 일시';
COMMENT ON COLUMN adm_file_job.approval_id IS '승인 ID';
COMMENT ON COLUMN adm_file_job.applied_by IS '적용 운영자';
COMMENT ON COLUMN adm_file_job.resolved_by IS '결과 불명 확정 운영자';
COMMENT ON COLUMN adm_file_job.control_by IS '최근 제어 운영자';
COMMENT ON COLUMN adm_file_job.control_reason IS '최근 제어 사유';
COMMENT ON COLUMN adm_file_job.control_updated_at IS '최근 제어 시각';
CREATE OR REPLACE TRIGGER trg_touch_adm_file_job BEFORE UPDATE ON adm_file_job FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_file_job_row (
    job_id VARCHAR2(36 CHAR) NOT NULL,
    row_no NUMBER(19) NOT NULL,
    row_state VARCHAR2(30 CHAR) NOT NULL,
    business_key VARCHAR2(200 CHAR),
    payload_json CLOB NOT NULL,
    error_code VARCHAR2(80 CHAR),
    error_message VARCHAR2(1000 CHAR),
    rollback_token VARCHAR2(1000 CHAR),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_adm_file_job_row PRIMARY KEY (job_id, row_no),
    CONSTRAINT fk_adm_file_job_row_job FOREIGN KEY (job_id) REFERENCES adm_file_job (job_id)
);
COMMENT ON TABLE adm_file_job_row IS 'ADM File Job 행별 결과 원장';
COMMENT ON COLUMN adm_file_job_row.job_id IS 'File Job ID';
COMMENT ON COLUMN adm_file_job_row.row_no IS '행 번호';
COMMENT ON COLUMN adm_file_job_row.row_state IS '행 처리 상태';
COMMENT ON COLUMN adm_file_job_row.business_key IS '업무 Key';
COMMENT ON COLUMN adm_file_job_row.payload_json IS '보호된 행 Payload';
COMMENT ON COLUMN adm_file_job_row.error_code IS '행 오류 코드';
COMMENT ON COLUMN adm_file_job_row.error_message IS '마스킹된 행 오류';
COMMENT ON COLUMN adm_file_job_row.rollback_token IS 'Rollback Token';
COMMENT ON COLUMN adm_file_job_row.created_at IS '생성 일시';
COMMENT ON COLUMN adm_file_job_row.updated_at IS '수정 일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_file_job_row BEFORE UPDATE ON adm_file_job_row FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_incident (
    incident_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    incident_no VARCHAR2(64 CHAR) NOT NULL,
    severity VARCHAR2(16 CHAR) NOT NULL,
    title VARCHAR2(300 CHAR) NOT NULL,
    summary VARCHAR2(2000 CHAR),
    source_type VARCHAR2(40 CHAR) NOT NULL DEFAULT 'MANUAL',
    source_id VARCHAR2(200 CHAR),
    status VARCHAR2(32 CHAR) NOT NULL DEFAULT 'OPEN',
    detected_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    acknowledged_at TIMESTAMP(3),
    mitigated_at TIMESTAMP(3),
    resolved_at TIMESTAMP(3),
    reason VARCHAR2(1000 CHAR) NOT NULL,
    version NUMBER(19) NOT NULL DEFAULT 0,
    created_by VARCHAR2(100 CHAR) NOT NULL,
    updated_by VARCHAR2(100 CHAR) NOT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_incident PRIMARY KEY (incident_id),
    CONSTRAINT uk_adm_incident_no UNIQUE (incident_no)
);
CREATE INDEX idx_adm_incident_status ON adm_incident (status, severity, detected_at);
CREATE INDEX idx_adm_incident_source ON adm_incident (source_type, source_id);
COMMENT ON TABLE adm_incident IS 'ADM Incident Lifecycle';
COMMENT ON COLUMN adm_incident.incident_id IS 'Incident identifier';
COMMENT ON COLUMN adm_incident.incident_no IS 'Incident number';
COMMENT ON COLUMN adm_incident.severity IS 'Severity';
COMMENT ON COLUMN adm_incident.title IS 'Title';
COMMENT ON COLUMN adm_incident.summary IS 'Summary';
COMMENT ON COLUMN adm_incident.source_type IS 'Source type';
COMMENT ON COLUMN adm_incident.source_id IS 'Source identifier';
COMMENT ON COLUMN adm_incident.status IS 'Status';
COMMENT ON COLUMN adm_incident.detected_at IS 'Detection time';
COMMENT ON COLUMN adm_incident.acknowledged_at IS 'Acknowledgement time';
COMMENT ON COLUMN adm_incident.mitigated_at IS 'Mitigation time';
COMMENT ON COLUMN adm_incident.resolved_at IS 'Resolution time';
COMMENT ON COLUMN adm_incident.reason IS 'Reason';
COMMENT ON COLUMN adm_incident.version IS 'Version';
COMMENT ON COLUMN adm_incident.created_by IS 'Creator';
COMMENT ON COLUMN adm_incident.updated_by IS 'Last updater';
COMMENT ON COLUMN adm_incident.created_at IS 'Creation time';
COMMENT ON COLUMN adm_incident.updated_at IS 'Last update time';

CREATE TABLE adm_ip_allowlist (
    ALLOW_ID NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    IP_PATTERN VARCHAR2(100 CHAR) NOT NULL,
    DESCRIPTION VARCHAR2(500 CHAR),
    USE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_ip_allowlist PRIMARY KEY (ALLOW_ID),
    CONSTRAINT uk_adm_ip_allowlist_pattern UNIQUE (IP_PATTERN)
);
CREATE INDEX ix_adm_ip_allowlist_use ON adm_ip_allowlist (USE_YN);
COMMENT ON TABLE adm_ip_allowlist IS 'ADM 관리자 IP 허용 목록';
COMMENT ON COLUMN adm_ip_allowlist.ALLOW_ID IS 'IP 허용 목록 순번';
COMMENT ON COLUMN adm_ip_allowlist.IP_PATTERN IS '허용 IP 또는 CIDR 패턴';
COMMENT ON COLUMN adm_ip_allowlist.DESCRIPTION IS '허용 사유';
COMMENT ON COLUMN adm_ip_allowlist.USE_YN IS '사용 여부';
COMMENT ON COLUMN adm_ip_allowlist.created_by IS '등록자';
COMMENT ON COLUMN adm_ip_allowlist.created_at IS '등록일시';
COMMENT ON COLUMN adm_ip_allowlist.updated_by IS '수정자';
COMMENT ON COLUMN adm_ip_allowlist.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_ip_allowlist BEFORE UPDATE ON adm_ip_allowlist FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_maintenance_action (
    action_id NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    service_id VARCHAR2(100 CHAR) NOT NULL,
    endpoint_code VARCHAR2(100 CHAR) NOT NULL,
    instance_id VARCHAR2(150 CHAR) NOT NULL,
    action_type VARCHAR2(20 CHAR) NOT NULL,
    before_status VARCHAR2(40 CHAR),
    after_status VARCHAR2(40 CHAR),
    result_status VARCHAR2(20 CHAR) NOT NULL,
    reason VARCHAR2(1000 CHAR) NOT NULL,
    requested_by VARCHAR2(100 CHAR) NOT NULL,
    requested_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    result_detail CLOB,
    CONSTRAINT pk_adm_maintenance_action PRIMARY KEY (action_id)
);
CREATE INDEX idx_adm_maintenance_target ON adm_maintenance_action (service_id, endpoint_code, instance_id, requested_at);
CREATE INDEX idx_adm_maintenance_result ON adm_maintenance_action (result_status, requested_at);
COMMENT ON TABLE adm_maintenance_action IS 'ADM Maintenance Command Audit';
COMMENT ON COLUMN adm_maintenance_action.action_id IS 'Action identifier';
COMMENT ON COLUMN adm_maintenance_action.service_id IS 'Service identifier';
COMMENT ON COLUMN adm_maintenance_action.endpoint_code IS 'Endpoint code';
COMMENT ON COLUMN adm_maintenance_action.instance_id IS 'Instance identifier';
COMMENT ON COLUMN adm_maintenance_action.action_type IS 'Action type';
COMMENT ON COLUMN adm_maintenance_action.before_status IS 'Before status';
COMMENT ON COLUMN adm_maintenance_action.after_status IS 'After status';
COMMENT ON COLUMN adm_maintenance_action.result_status IS 'Result status';
COMMENT ON COLUMN adm_maintenance_action.reason IS 'Reason';
COMMENT ON COLUMN adm_maintenance_action.requested_by IS 'Requester';
COMMENT ON COLUMN adm_maintenance_action.requested_at IS 'Request time';
COMMENT ON COLUMN adm_maintenance_action.result_detail IS 'Result detail';

CREATE TABLE adm_menu (
    MENU_ID VARCHAR2(50 CHAR) NOT NULL,
    PARENT_MENU_ID VARCHAR2(50 CHAR),
    MENU_NAME VARCHAR2(100 CHAR) NOT NULL,
    MENU_PATH VARCHAR2(200 CHAR) NOT NULL,
    SORT_ORDER NUMBER(10) NOT NULL DEFAULT 0,
    USE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_menu PRIMARY KEY (MENU_ID),
    CONSTRAINT fk_adm_menu_parent FOREIGN KEY (PARENT_MENU_ID) REFERENCES adm_menu (MENU_ID) ON DELETE SET NULL
);
CREATE INDEX ix_adm_menu_parent ON adm_menu (PARENT_MENU_ID, SORT_ORDER);
COMMENT ON TABLE adm_menu IS 'ADM 메뉴';
COMMENT ON COLUMN adm_menu.MENU_ID IS '메뉴 ID';
COMMENT ON COLUMN adm_menu.PARENT_MENU_ID IS '상위 메뉴 ID';
COMMENT ON COLUMN adm_menu.MENU_NAME IS '메뉴명';
COMMENT ON COLUMN adm_menu.MENU_PATH IS '메뉴 경로';
COMMENT ON COLUMN adm_menu.SORT_ORDER IS '정렬 순서';
COMMENT ON COLUMN adm_menu.USE_YN IS '사용 여부';
COMMENT ON COLUMN adm_menu.created_by IS '등록자';
COMMENT ON COLUMN adm_menu.created_at IS '등록일시';
COMMENT ON COLUMN adm_menu.updated_by IS '수정자';
COMMENT ON COLUMN adm_menu.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_menu BEFORE UPDATE ON adm_menu FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_mfa_otp_secret (
    OPERATOR_ID VARCHAR2(50 CHAR) NOT NULL,
    SECRET_REF VARCHAR2(500 CHAR) NOT NULL,
    ENABLED_YN CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    VERIFIED_AT TIMESTAMP,
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_mfa_otp_secret PRIMARY KEY (OPERATOR_ID),
    CONSTRAINT fk_adm_mfa_otp_secret_operator FOREIGN KEY (OPERATOR_ID) REFERENCES adm_operator (OPERATOR_ID) ON DELETE CASCADE
);
COMMENT ON TABLE adm_mfa_otp_secret IS 'ADM 운영자 MFA OTP secret 메타';
COMMENT ON COLUMN adm_mfa_otp_secret.OPERATOR_ID IS '운영자 ID';
COMMENT ON COLUMN adm_mfa_otp_secret.SECRET_REF IS 'OTP secret 참조';
COMMENT ON COLUMN adm_mfa_otp_secret.ENABLED_YN IS 'MFA 사용 여부';
COMMENT ON COLUMN adm_mfa_otp_secret.VERIFIED_AT IS 'MFA 검증일시';
COMMENT ON COLUMN adm_mfa_otp_secret.created_by IS '등록자';
COMMENT ON COLUMN adm_mfa_otp_secret.created_at IS '등록일시';
COMMENT ON COLUMN adm_mfa_otp_secret.updated_by IS '수정자';
COMMENT ON COLUMN adm_mfa_otp_secret.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_mfa_otp_secret BEFORE UPDATE ON adm_mfa_otp_secret FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_operator (
    OPERATOR_ID VARCHAR2(50 CHAR) NOT NULL,
    OPERATOR_NAME VARCHAR2(100 CHAR) NOT NULL,
    PASSWORD_HASH VARCHAR2(512 CHAR) NOT NULL,
    ACCOUNT_STATUS VARCHAR2(30 CHAR) NOT NULL DEFAULT 'PENDING_ACTIVATION',
    VERSION_NO NUMBER(19) NOT NULL DEFAULT 0,
    CREATE_OPERATION_ID VARCHAR2(100 CHAR),
    LOCKED_YN CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    FAIL_COUNT NUMBER(10) NOT NULL DEFAULT 0,
    PASSWORD_CHANGED_AT TIMESTAMP,
    PASSWORD_EXPIRE_AT TIMESTAMP,
    PASSWORD_CHANGE_REQUIRED_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    LAST_LOGIN_AT TIMESTAMP,
    LAST_LOGIN_IP VARCHAR2(50 CHAR),
    USE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_operator PRIMARY KEY (OPERATOR_ID),
    CONSTRAINT uk_adm_operator_create_operation UNIQUE (CREATE_OPERATION_ID),
    CONSTRAINT ck_adm_operator_status CHECK (ACCOUNT_STATUS IN ('PENDING_ACTIVATION','ACTIVE','LOCKED','SUSPENDED','DISABLED'))
);
CREATE INDEX ix_adm_operator_use ON adm_operator (USE_YN);
CREATE INDEX ix_adm_operator_status ON adm_operator (ACCOUNT_STATUS, USE_YN);
CREATE INDEX ix_adm_operator_lock ON adm_operator (LOCKED_YN, FAIL_COUNT);
COMMENT ON TABLE adm_operator IS 'ADM 운영자';
COMMENT ON COLUMN adm_operator.OPERATOR_ID IS '운영자 ID';
COMMENT ON COLUMN adm_operator.OPERATOR_NAME IS '운영자명';
COMMENT ON COLUMN adm_operator.PASSWORD_HASH IS '비밀번호 해시';
COMMENT ON COLUMN adm_operator.ACCOUNT_STATUS IS '계정 상태: PENDING_ACTIVATION/ACTIVE/LOCKED/SUSPENDED/DISABLED';
COMMENT ON COLUMN adm_operator.VERSION_NO IS '낙관적 잠금 버전';
COMMENT ON COLUMN adm_operator.CREATE_OPERATION_ID IS '운영자 생성 멱등 Operation ID';
COMMENT ON COLUMN adm_operator.LOCKED_YN IS '잠금 여부';
COMMENT ON COLUMN adm_operator.FAIL_COUNT IS '로그인 실패 횟수';
COMMENT ON COLUMN adm_operator.PASSWORD_CHANGED_AT IS '비밀번호 변경일시';
COMMENT ON COLUMN adm_operator.PASSWORD_EXPIRE_AT IS '비밀번호 만료일시';
COMMENT ON COLUMN adm_operator.PASSWORD_CHANGE_REQUIRED_YN IS '비밀번호 변경 필요 여부';
COMMENT ON COLUMN adm_operator.LAST_LOGIN_AT IS '마지막 로그인 일시';
COMMENT ON COLUMN adm_operator.LAST_LOGIN_IP IS '마지막 로그인 IP';
COMMENT ON COLUMN adm_operator.USE_YN IS '사용 여부';
COMMENT ON COLUMN adm_operator.created_by IS '등록자';
COMMENT ON COLUMN adm_operator.created_at IS '등록일시';
COMMENT ON COLUMN adm_operator.updated_by IS '수정자';
COMMENT ON COLUMN adm_operator.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_operator BEFORE UPDATE ON adm_operator FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_operator_profile (
    OPERATOR_ID VARCHAR2(50 CHAR) NOT NULL,
    DISPLAY_NAME VARCHAR2(100 CHAR),
    EMPLOYEE_NO VARCHAR2(50 CHAR),
    EXTERNAL_SUBJECT VARCHAR2(200 CHAR),
    ORGANIZATION_CODE VARCHAR2(50 CHAR),
    POSITION_CODE VARCHAR2(50 CHAR),
    POSITION_NAME VARCHAR2(100 CHAR),
    JOB_TITLE_CODE VARCHAR2(50 CHAR),
    JOB_TITLE_NAME VARCHAR2(100 CHAR),
    EMAIL VARCHAR2(200 CHAR),
    MOBILE_NO VARCHAR2(50 CHAR),
    OFFICE_PHONE_NO VARCHAR2(50 CHAR),
    EFFECTIVE_FROM TIMESTAMP(3),
    EFFECTIVE_TO TIMESTAMP(3),
    VERSION_NO NUMBER(19) NOT NULL DEFAULT 0,
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_operator_profile PRIMARY KEY (OPERATOR_ID),
    CONSTRAINT uk_adm_operator_profile_employee UNIQUE (EMPLOYEE_NO),
    CONSTRAINT ck_adm_operator_profile_effective CHECK (EFFECTIVE_TO IS NULL OR EFFECTIVE_FROM IS NULL OR EFFECTIVE_TO > EFFECTIVE_FROM),
    CONSTRAINT fk_adm_operator_profile_operator FOREIGN KEY (OPERATOR_ID) REFERENCES adm_operator (OPERATOR_ID) ON DELETE CASCADE,
    CONSTRAINT fk_adm_operator_profile_org FOREIGN KEY (ORGANIZATION_CODE) REFERENCES adm_organization (ORGANIZATION_CODE) ON DELETE SET NULL
);
CREATE INDEX ix_adm_operator_profile_org ON adm_operator_profile (ORGANIZATION_CODE, EFFECTIVE_TO);
COMMENT ON TABLE adm_operator_profile IS 'ADM 운영자 조직/직급 Profile';
COMMENT ON COLUMN adm_operator_profile.OPERATOR_ID IS '운영자 ID';
COMMENT ON COLUMN adm_operator_profile.DISPLAY_NAME IS 'Directory/Profile 표시 이름';
COMMENT ON COLUMN adm_operator_profile.EMPLOYEE_NO IS '외부/내부 사번';
COMMENT ON COLUMN adm_operator_profile.EXTERNAL_SUBJECT IS 'LDAP/IAM 등 외부 Identity Subject';
COMMENT ON COLUMN adm_operator_profile.ORGANIZATION_CODE IS '대표 운영 조직 코드';
COMMENT ON COLUMN adm_operator_profile.POSITION_CODE IS '직급 코드';
COMMENT ON COLUMN adm_operator_profile.POSITION_NAME IS '직급명 Snapshot/표시값';
COMMENT ON COLUMN adm_operator_profile.JOB_TITLE_CODE IS '직책 코드';
COMMENT ON COLUMN adm_operator_profile.JOB_TITLE_NAME IS '직책명 Snapshot/표시값';
COMMENT ON COLUMN adm_operator_profile.EMAIL IS '업무 이메일';
COMMENT ON COLUMN adm_operator_profile.MOBILE_NO IS '연락처(휴대폰); 숫자형이 아닌 문자열로 국가번호와 선행 0을 보존';
COMMENT ON COLUMN adm_operator_profile.OFFICE_PHONE_NO IS '내부 전화번호/내선; 휴대폰 연락처와 분리';
COMMENT ON COLUMN adm_operator_profile.EFFECTIVE_FROM IS 'Profile 적용 시작시각';
COMMENT ON COLUMN adm_operator_profile.EFFECTIVE_TO IS 'Profile 적용 종료시각';
COMMENT ON COLUMN adm_operator_profile.VERSION_NO IS 'Profile 낙관적 잠금 버전';
COMMENT ON COLUMN adm_operator_profile.created_by IS '등록자';
COMMENT ON COLUMN adm_operator_profile.created_at IS '등록일시';
COMMENT ON COLUMN adm_operator_profile.updated_by IS '수정자';
COMMENT ON COLUMN adm_operator_profile.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_operator_profile BEFORE UPDATE ON adm_operator_profile FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_operator_role (
    OPERATOR_ID VARCHAR2(50 CHAR) NOT NULL,
    ROLE_ID VARCHAR2(50 CHAR) NOT NULL,
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_operator_role PRIMARY KEY (OPERATOR_ID, ROLE_ID),
    CONSTRAINT fk_adm_operator_role_operator FOREIGN KEY (OPERATOR_ID) REFERENCES adm_operator (OPERATOR_ID) ON DELETE CASCADE,
    CONSTRAINT fk_adm_operator_role_role FOREIGN KEY (ROLE_ID) REFERENCES adm_role (ROLE_ID) ON DELETE CASCADE
);
COMMENT ON TABLE adm_operator_role IS 'ADM 운영자 역할 매핑';
COMMENT ON COLUMN adm_operator_role.OPERATOR_ID IS '운영자 ID';
COMMENT ON COLUMN adm_operator_role.ROLE_ID IS '역할 ID';
COMMENT ON COLUMN adm_operator_role.created_by IS '등록자';
COMMENT ON COLUMN adm_operator_role.created_at IS '등록일시';
COMMENT ON COLUMN adm_operator_role.updated_by IS '수정자';
COMMENT ON COLUMN adm_operator_role.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_operator_role BEFORE UPDATE ON adm_operator_role FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_operator_session (
    SESSION_ID VARCHAR2(80 CHAR) NOT NULL,
    TOKEN_HASH VARCHAR2(512 CHAR) NOT NULL,
    OPERATOR_ID VARCHAR2(50 CHAR) NOT NULL,
    ROLE_IDS VARCHAR2(1000 CHAR),
    ISSUED_AT TIMESTAMP NOT NULL,
    EXPIRE_AT TIMESTAMP NOT NULL,
    REVOKED_YN CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    CLIENT_IP VARCHAR2(50 CHAR),
    USER_AGENT VARCHAR2(500 CHAR),
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_operator_session PRIMARY KEY (SESSION_ID)
);
CREATE INDEX ix_adm_operator_session_token ON adm_operator_session (TOKEN_HASH);
CREATE INDEX ix_adm_operator_session_user ON adm_operator_session (OPERATOR_ID, EXPIRE_AT);
CREATE INDEX ix_adm_operator_session_active ON adm_operator_session (REVOKED_YN, EXPIRE_AT);
COMMENT ON TABLE adm_operator_session IS 'ADM 운영자 세션';
COMMENT ON COLUMN adm_operator_session.SESSION_ID IS '세션 ID';
COMMENT ON COLUMN adm_operator_session.TOKEN_HASH IS '토큰 해시';
COMMENT ON COLUMN adm_operator_session.OPERATOR_ID IS '운영자 ID';
COMMENT ON COLUMN adm_operator_session.ROLE_IDS IS '역할 ID 목록';
COMMENT ON COLUMN adm_operator_session.ISSUED_AT IS '발급일시';
COMMENT ON COLUMN adm_operator_session.EXPIRE_AT IS '만료일시';
COMMENT ON COLUMN adm_operator_session.REVOKED_YN IS '폐기 여부';
COMMENT ON COLUMN adm_operator_session.CLIENT_IP IS '클라이언트 IP';
COMMENT ON COLUMN adm_operator_session.USER_AGENT IS 'User-Agent';
COMMENT ON COLUMN adm_operator_session.created_by IS '등록자';
COMMENT ON COLUMN adm_operator_session.created_at IS '등록일시';
COMMENT ON COLUMN adm_operator_session.updated_by IS '수정자';
COMMENT ON COLUMN adm_operator_session.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_operator_session BEFORE UPDATE ON adm_operator_session FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_organization (
    ORGANIZATION_CODE VARCHAR2(50 CHAR) NOT NULL,
    PARENT_ORGANIZATION_CODE VARCHAR2(50 CHAR),
    ORGANIZATION_NAME VARCHAR2(120 CHAR) NOT NULL,
    ORGANIZATION_TYPE VARCHAR2(30 CHAR) NOT NULL DEFAULT 'DEPARTMENT',
    MANAGER_OPERATOR_ID VARCHAR2(50 CHAR),
    EFFECTIVE_FROM DATE,
    EFFECTIVE_TO DATE,
    USE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_adm_organization PRIMARY KEY (ORGANIZATION_CODE),
    CONSTRAINT ck_adm_organization_use CHECK (USE_YN IN ('Y','N')),
    CONSTRAINT ck_adm_organization_effective CHECK (EFFECTIVE_TO IS NULL OR EFFECTIVE_FROM IS NULL OR EFFECTIVE_TO > EFFECTIVE_FROM),
    CONSTRAINT fk_adm_organization_parent FOREIGN KEY (PARENT_ORGANIZATION_CODE) REFERENCES adm_organization (ORGANIZATION_CODE) ON DELETE SET NULL,
    CONSTRAINT fk_adm_organization_manager FOREIGN KEY (MANAGER_OPERATOR_ID) REFERENCES adm_operator (OPERATOR_ID) ON DELETE SET NULL
);
CREATE INDEX ix_adm_organization_parent ON adm_organization (PARENT_ORGANIZATION_CODE, USE_YN);
COMMENT ON TABLE adm_organization IS 'ADM 운영 조직 Directory 기본 Adapter';
COMMENT ON COLUMN adm_organization.ORGANIZATION_CODE IS '운영 조직 코드';
COMMENT ON COLUMN adm_organization.PARENT_ORGANIZATION_CODE IS '상위 조직 코드';
COMMENT ON COLUMN adm_organization.ORGANIZATION_NAME IS '운영 조직명';
COMMENT ON COLUMN adm_organization.ORGANIZATION_TYPE IS '조직 유형';
COMMENT ON COLUMN adm_organization.MANAGER_OPERATOR_ID IS '기본 DB Directory Adapter의 조직 책임자 운영자 ID';
COMMENT ON COLUMN adm_organization.EFFECTIVE_FROM IS '적용 시작일';
COMMENT ON COLUMN adm_organization.EFFECTIVE_TO IS '적용 종료일';
COMMENT ON COLUMN adm_organization.USE_YN IS '사용 여부';
COMMENT ON COLUMN adm_organization.created_by IS '등록자';
COMMENT ON COLUMN adm_organization.created_at IS '등록일시';
COMMENT ON COLUMN adm_organization.updated_by IS '수정자';
COMMENT ON COLUMN adm_organization.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_organization BEFORE UPDATE ON adm_organization FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_password_history (
    HISTORY_ID NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL,
    OPERATOR_ID VARCHAR2(50 CHAR) NOT NULL,
    PASSWORD_HASH VARCHAR2(512 CHAR) NOT NULL,
    CHANGED_REASON VARCHAR2(500 CHAR),
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_password_history PRIMARY KEY (HISTORY_ID),
    CONSTRAINT fk_adm_password_history_operator FOREIGN KEY (OPERATOR_ID) REFERENCES adm_operator (OPERATOR_ID) ON DELETE CASCADE
);
CREATE INDEX ix_adm_password_history_operator_time ON adm_password_history (OPERATOR_ID, created_at);
COMMENT ON TABLE adm_password_history IS 'ADM 비밀번호 변경 이력';
COMMENT ON COLUMN adm_password_history.HISTORY_ID IS '비밀번호 이력 순번';
COMMENT ON COLUMN adm_password_history.OPERATOR_ID IS '운영자 ID';
COMMENT ON COLUMN adm_password_history.PASSWORD_HASH IS '이전 비밀번호 해시';
COMMENT ON COLUMN adm_password_history.CHANGED_REASON IS '변경 사유';
COMMENT ON COLUMN adm_password_history.created_by IS '등록자';
COMMENT ON COLUMN adm_password_history.created_at IS '등록일시';
COMMENT ON COLUMN adm_password_history.updated_by IS '수정자';
COMMENT ON COLUMN adm_password_history.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_password_history BEFORE UPDATE ON adm_password_history FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_password_policy (
    POLICY_ID VARCHAR2(50 CHAR) NOT NULL,
    MIN_LENGTH NUMBER(10) NOT NULL DEFAULT 12,
    REQUIRE_UPPER_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    REQUIRE_LOWER_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    REQUIRE_DIGIT_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    REQUIRE_SPECIAL_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    MAX_FAIL_COUNT NUMBER(10) NOT NULL DEFAULT 5,
    EXPIRE_DAYS NUMBER(10) NOT NULL DEFAULT 90,
    HISTORY_LIMIT NUMBER(10) NOT NULL DEFAULT 5,
    USE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_password_policy PRIMARY KEY (POLICY_ID)
);
COMMENT ON TABLE adm_password_policy IS 'ADM 비밀번호 정책';
COMMENT ON COLUMN adm_password_policy.POLICY_ID IS '비밀번호 정책 ID';
COMMENT ON COLUMN adm_password_policy.MIN_LENGTH IS '최소 길이';
COMMENT ON COLUMN adm_password_policy.REQUIRE_UPPER_YN IS '대문자 필수 여부';
COMMENT ON COLUMN adm_password_policy.REQUIRE_LOWER_YN IS '소문자 필수 여부';
COMMENT ON COLUMN adm_password_policy.REQUIRE_DIGIT_YN IS '숫자 필수 여부';
COMMENT ON COLUMN adm_password_policy.REQUIRE_SPECIAL_YN IS '특수문자 필수 여부';
COMMENT ON COLUMN adm_password_policy.MAX_FAIL_COUNT IS '최대 실패 횟수';
COMMENT ON COLUMN adm_password_policy.EXPIRE_DAYS IS '만료 일수';
COMMENT ON COLUMN adm_password_policy.HISTORY_LIMIT IS '재사용 금지 이력 수';
COMMENT ON COLUMN adm_password_policy.USE_YN IS '사용 여부';
COMMENT ON COLUMN adm_password_policy.created_by IS '등록자';
COMMENT ON COLUMN adm_password_policy.created_at IS '등록일시';
COMMENT ON COLUMN adm_password_policy.updated_by IS '수정자';
COMMENT ON COLUMN adm_password_policy.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_password_policy BEFORE UPDATE ON adm_password_policy FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_role (
    ROLE_ID VARCHAR2(50 CHAR) NOT NULL,
    ROLE_NAME VARCHAR2(100 CHAR) NOT NULL,
    ROLE_TYPE VARCHAR2(30 CHAR) NOT NULL DEFAULT 'BUSINESS_OPERATOR',
    DESCRIPTION VARCHAR2(500 CHAR),
    USE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_role PRIMARY KEY (ROLE_ID)
);
CREATE INDEX ix_adm_role_type ON adm_role (ROLE_TYPE, USE_YN);
COMMENT ON TABLE adm_role IS 'ADM 역할';
COMMENT ON COLUMN adm_role.ROLE_ID IS '역할 ID';
COMMENT ON COLUMN adm_role.ROLE_NAME IS '역할명';
COMMENT ON COLUMN adm_role.ROLE_TYPE IS '역할 유형';
COMMENT ON COLUMN adm_role.DESCRIPTION IS '역할 설명';
COMMENT ON COLUMN adm_role.USE_YN IS '사용 여부';
COMMENT ON COLUMN adm_role.created_by IS '등록자';
COMMENT ON COLUMN adm_role.created_at IS '등록일시';
COMMENT ON COLUMN adm_role.updated_by IS '수정자';
COMMENT ON COLUMN adm_role.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_role BEFORE UPDATE ON adm_role FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_role_api_permission (
    ROLE_ID VARCHAR2(50 CHAR) NOT NULL,
    API_PERMISSION_ID VARCHAR2(120 CHAR) NOT NULL,
    ALLOW_YN CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_role_api_permission PRIMARY KEY (ROLE_ID, API_PERMISSION_ID),
    CONSTRAINT fk_adm_role_api_permission_role FOREIGN KEY (ROLE_ID) REFERENCES adm_role (ROLE_ID) ON DELETE CASCADE,
    CONSTRAINT fk_adm_role_api_permission_api FOREIGN KEY (API_PERMISSION_ID) REFERENCES adm_api_permission (API_PERMISSION_ID) ON DELETE CASCADE
);
COMMENT ON TABLE adm_role_api_permission IS 'ADM 역할별 API 권한';
COMMENT ON COLUMN adm_role_api_permission.ROLE_ID IS '역할 ID';
COMMENT ON COLUMN adm_role_api_permission.API_PERMISSION_ID IS 'API 권한 ID';
COMMENT ON COLUMN adm_role_api_permission.ALLOW_YN IS '허용 여부';
COMMENT ON COLUMN adm_role_api_permission.created_by IS '등록자';
COMMENT ON COLUMN adm_role_api_permission.created_at IS '등록일시';
COMMENT ON COLUMN adm_role_api_permission.updated_by IS '수정자';
COMMENT ON COLUMN adm_role_api_permission.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_role_api_permission BEFORE UPDATE ON adm_role_api_permission FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_role_button (
    ROLE_ID VARCHAR2(50 CHAR) NOT NULL,
    BUTTON_ID VARCHAR2(80 CHAR) NOT NULL,
    ALLOW_YN CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_role_button PRIMARY KEY (ROLE_ID, BUTTON_ID),
    CONSTRAINT fk_adm_role_button_role FOREIGN KEY (ROLE_ID) REFERENCES adm_role (ROLE_ID) ON DELETE CASCADE,
    CONSTRAINT fk_adm_role_button_button FOREIGN KEY (BUTTON_ID) REFERENCES adm_button (BUTTON_ID) ON DELETE CASCADE
);
COMMENT ON TABLE adm_role_button IS 'ADM 역할별 버튼/행위 권한';
COMMENT ON COLUMN adm_role_button.ROLE_ID IS '역할 ID';
COMMENT ON COLUMN adm_role_button.BUTTON_ID IS '버튼/행위 ID';
COMMENT ON COLUMN adm_role_button.ALLOW_YN IS '허용 여부';
COMMENT ON COLUMN adm_role_button.created_by IS '등록자';
COMMENT ON COLUMN adm_role_button.created_at IS '등록일시';
COMMENT ON COLUMN adm_role_button.updated_by IS '수정자';
COMMENT ON COLUMN adm_role_button.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_role_button BEFORE UPDATE ON adm_role_button FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

CREATE TABLE adm_role_menu (
    ROLE_ID VARCHAR2(50 CHAR) NOT NULL,
    MENU_ID VARCHAR2(50 CHAR) NOT NULL,
    READ_YN CHAR(1 CHAR) NOT NULL DEFAULT 'Y',
    WRITE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    DELETE_YN CHAR(1 CHAR) NOT NULL DEFAULT 'N',
    created_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR2(50 CHAR) NOT NULL DEFAULT 'ADM',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_adm_role_menu PRIMARY KEY (ROLE_ID, MENU_ID),
    CONSTRAINT fk_adm_role_menu_role FOREIGN KEY (ROLE_ID) REFERENCES adm_role (ROLE_ID) ON DELETE CASCADE,
    CONSTRAINT fk_adm_role_menu_menu FOREIGN KEY (MENU_ID) REFERENCES adm_menu (MENU_ID) ON DELETE CASCADE
);
COMMENT ON TABLE adm_role_menu IS 'ADM 역할별 메뉴 권한';
COMMENT ON COLUMN adm_role_menu.ROLE_ID IS '역할 ID';
COMMENT ON COLUMN adm_role_menu.MENU_ID IS '메뉴 ID';
COMMENT ON COLUMN adm_role_menu.READ_YN IS '조회 권한 여부';
COMMENT ON COLUMN adm_role_menu.WRITE_YN IS '등록/수정 권한 여부';
COMMENT ON COLUMN adm_role_menu.DELETE_YN IS '삭제 권한 여부';
COMMENT ON COLUMN adm_role_menu.created_by IS '등록자';
COMMENT ON COLUMN adm_role_menu.created_at IS '등록일시';
COMMENT ON COLUMN adm_role_menu.updated_by IS '수정자';
COMMENT ON COLUMN adm_role_menu.updated_at IS '수정일시';
CREATE OR REPLACE TRIGGER trg_touch_adm_role_menu BEFORE UPDATE ON adm_role_menu FOR EACH ROW BEGIN :NEW.updated_at := CURRENT_TIMESTAMP; END;
/

import { getAdmAccessToken } from "../shared/cpfApi";

export function createAdmState() {
      return {
        activeMenu: "dashboard",
        token: getAdmAccessToken(),
        currentOperator: {} as Record<string, any>,
        authorizedMenus: [],
        authorizedButtons: [] as string[],
        buttonsLoaded: false,
        permissionsLoaded: false,
        authMessage: "",
        uiMessage: "",
        loginForm: { operatorId: "admin", password: "" },
        forcedPasswordForm: {
          currentPassword: "",
          newPassword: "",
          newPasswordConfirm: "",
          reason: "최초 로그인 비밀번호 변경"
        },
        menus: [
          { id: "dashboard", menuId: "DASHBOARD", label: "운영 대시보드" },
          { id: "topology", menuId: "TOPOLOGY", label: "서비스 토폴로지" },
          { id: "capacity", menuId: "CAPACITY", label: "용량·SLO" },
          { id: "logs", menuId: "LOG_LIST", label: "거래 로그" },
          { id: "transactionGroups", menuId: "LOG_LIST", label: "거래 그룹" },
          { id: "transactions", menuId: "TRANSACTION_META", label: "거래 메타" },
          { id: "remoteLogs", menuId: "REMOTE_LOG", label: "원격 로그" },
          { id: "auditLogs", menuId: "AUDIT_LOG", label: "감사 로그" },
          { id: "businessCalendar", menuId: "BUSINESS_CALENDAR", label: "영업일 · 휴일" },
          { id: "logLevel", menuId: "DYNAMIC_LOG", label: "동적 로그" },
          { id: "logPolicies", menuId: "LOG_POLICY", label: "로그 정책" },
          { id: "standardExecutions", menuId: "STANDARD_EXECUTION", label: "표준 실행" },
          { id: "channelPolicy", menuId: "CHANNEL_POLICY", label: "채널 정책" },
          { id: "serviceRegistry", menuId: "SERVICE_REGISTRY", label: "서비스 레지스트리" },
          { id: "gateway-dashboard", menuId: "GATEWAY_DASHBOARD", label: "Gateway 대시보드" },
          { id: "gateway-servers", menuId: "GATEWAY_SERVERS", label: "Gateway 연동 서버" },
          { id: "gateway-groups", menuId: "GATEWAY_GROUPS", label: "Gateway 서버 그룹" },
          { id: "gateway-routes", menuId: "GATEWAY_ROUTES", label: "Gateway 경로·라우팅" },
          { id: "gateway-security", menuId: "GATEWAY_SECURITY", label: "Gateway 보안·제한" },
          { id: "gateway-health", menuId: "GATEWAY_HEALTH", label: "Gateway Health·연결시험" },
          { id: "gateway-transactions", menuId: "GATEWAY_TRANSACTIONS", label: "Gateway 거래 조회" },
          { id: "gateway-log-policies", menuId: "GATEWAY_LOG_POLICY", label: "Gateway 로그 정책" },
          { id: "gateway-apply-status", menuId: "GATEWAY_APPLY_STATUS", label: "Gateway 적용 상태·이력" },
          { id: "runtimeControl", menuId: "RUNTIME_CONTROL", label: "Runtime Change Center" },
          { id: "maintenance", menuId: "MAINTENANCE", label: "점검·Drain" },
          { id: "cache", menuId: "CACHE", label: "캐시" },
          { id: "configs", menuId: "CONFIG", label: "설정" },
          { id: "responseCodes", menuId: "RESPONSE_CODE", label: "응답코드" },
          { id: "recoveryCenter", menuId: "RECOVERY_CENTER", label: "복구 센터" },
          { id: "incidents", menuId: "INCIDENT", label: "Incident" },
          { id: "reliability", menuId: "RELIABILITY", label: "신뢰성 상세" },
          { id: "notifications", menuId: "NOTIFICATION", label: "알림" },
          { id: "batch", menuId: "BATCH", label: "Batch / Center-Cut" },
          { id: "batch-overview", menuId: "BATCH_OVERVIEW", label: "Batch Overview" },
          { id: "batch-runtime", menuId: "BATCH_RUNTIME", label: "Runtime Topology" },
          { id: "batch-instances", menuId: "BATCH_INSTANCES", label: "Runtime Instances" },
          { id: "batch-scheduler", menuId: "BATCH_SCHEDULER", label: "Scheduler HA" },
          { id: "batch-worker-pools", menuId: "BATCH_WORKER_POOLS", label: "Worker Pools" },
          { id: "batch-center-cut", menuId: "BATCH_CENTER_CUT", label: "Center-Cut" },
          { id: "batch-agents", menuId: "BATCH_AGENTS", label: "Host Agents" },
          { id: "batch-job-packs", menuId: "BATCH_JOB_PACKS", label: "Job Packs" },
          { id: "batch-executions", menuId: "BATCH_EXECUTIONS", label: "Executions" },
          { id: "batch-deployment", menuId: "BATCH_DEPLOYMENT", label: "Deployment / Rollback" },
          { id: "batch-recovery", menuId: "BATCH_RECOVERY", label: "Recovery / Unknown" },
          { id: "batch-leases", menuId: "BATCH_LEASES", label: "Lease / Fencing" },
          { id: "batch-alerts", menuId: "BATCH_ALERTS", label: "Batch Alerts" },
          { id: "batch-audit", menuId: "BATCH_AUDIT", label: "Audit / Evidence" },
          { id: "workers", menuId: "WORKER", label: "Agent / Worker" },
          { id: "downloads", menuId: "DOWNLOAD", label: "다운로드" },
          { id: "file-jobs", menuId: "FILE_JOB", label: "대량파일 Job" },
          { id: "messages", menuId: "MESSAGE", label: "메시지" },
          { id: "codes", menuId: "CODE", label: "코드" },
          { id: "permissions", menuId: "PERMISSION", label: "권한" },
          { id: "password", menuId: "PASSWORD", label: "비밀번호" },
          { id: "security", menuId: "SECURITY", label: "보안" },
          { id: "operators", menuId: "OPERATOR", label: "운영자" },
          { id: "secrets", menuId: "SECRET", label: "Secret / Key" },
          { id: "approvals", menuId: "APPROVAL", label: "위험조치 승인" },
          { id: "breakGlass", menuId: "BREAK_GLASS", label: "Break-glass" }
        ],
        logSearch: {
          transactionId: "",
          traceId: "",
          businessTransactionId: "",
          uri: "",
          responseCode: "",
          httpStatus: "",
          memberNo: "",
          customerNo: "",
          channelCode: "",
          logType: ""
        },
        transactionGroupSearch: {
          startedAtFrom: "",
          startedAtTo: "",
          transactionId: "",
          transactionSegmentId: "",
          status: "",
          failureYn: "",
          moduleCode: "",
          sourceModuleCode: "",
          targetModuleCode: "",
          transactionRole: "",
          direction: "",
          customerNo: "",
          memberNo: "",
          userId: "",
          operatorId: "",
          channelCode: "",
          originalChannelCode: "",
          externalInstitutionCode: "",
          externalTransactionId: "",
          apiPath: "",
          transactionName: "",
          failureCode: "",
          durationMsFrom: "",
          durationMsTo: "",
          standardHeaderValue: "",
          extensionHeaderValue: ""
        },
        transactionSearch: { moduleCode: "", activeYn: "Y", transactionId: "", selectedTransactionId: "", reason: "거래 메타 운영" },
        standardExecutionSearch: { type: "", ownerDomain: "", keyword: "", selectedId: "" },
        channelSnapshot: { version: 0, channels: {}, policies: [] } as any,
        channelForm: {
          channelCode: "WEB",
          channelName: "웹",
          channelType: "CLIENT",
          trustLevel: "EXTERNAL",
          clientChannel: true,
          internalChannel: false,
          authenticationRequired: true,
          signatureRequired: false,
          active: true,
          description: "웹 브라우저 채널",
          reason: "채널 레지스트리 변경",
          requestUser: ""
        },
        channelPolicyForm: {
          policyKey: "WEB.DEFAULT",
          standardExecutionId: "*",
          originalChannelCode: "WEB",
          callerChannelCode: "WEB",
          requestType: "*",
          allowed: true,
          authenticationRequired: true,
          signatureRequired: false,
          maxTps: 0,
          effectiveFrom: null,
          effectiveTo: null,
          active: true,
          reason: "거래별 채널 정책 변경",
          requestUser: ""
        },
        channelPackageText: "",
        channelImportDryRun: true,
        remoteLogSearch: {
          environment: "",
          module: "",
          service: "",
          instance: "",
          logType: "",
          fileName: "",
          standardTransactionId: "",
          standardBatchId: "",
          transactionId: "",
          segmentId: "",
          jobInstanceId: "",
          jobExecutionId: "",
          stepExecutionId: "",
          schedulerId: "",
          modifiedFrom: "",
          modifiedTo: "",
          minSize: "",
          maxSize: "",
          compressed: "",
          active: "",
          limit: 100,
          lastLines: 200,
          keyword: "",
          reason: "운영 로그 확인"
        },
        logSort: { key: "LOG_IDX", direction: "desc" },
        logPage: { page: 1, size: 10 },
        logDetailTab: "요약",
        logDetailTabs: ["요약", "수신 헤더", "해석 헤더", "전파 헤더", "응답 헤더", "요청", "응답", "오류", "상세", "전문"],
        transactionGroupSort: "startedAtDesc",
        transactionGroupPage: { page: 1, size: 10 },
        transactionGroupDetailTab: "요약",
        transactionGroupDetailTabs: ["요약", "Timeline", "Segments", "표준 헤더", "확장 헤더", "External Logs", "원본 JSON"],
        auditSearch: { operatorId: "", actionType: "", targetType: "", targetId: "", limit: 100 },
        auditDeliveryState: "FAILED",
        auditRetryReason: "감사 전달 수동 재처리",
        batchForm: {
          jobId: "CPF_EDU_TASKLET_JOB",
          jobName: "CPF EDU Tasklet Job",
          jobType: "TASKLET",
          executionId: null,
          scheduleId: "CPF_EDU_TASKLET_DAILY",
          jobParameters: "{\"edu\":true}",
          calendarId: "DEFAULT",
          businessDate: new Date().toISOString().slice(0, 10),
          simulationDays: 14,
          dispatchStatus: "WAITING",
          heartbeatTimeoutSeconds: 120,
          lockKey: "",
          ghostActionType: "FAIL",
          holidayYn: "N",
          businessDayYn: "Y",
          description: "ADM batch education data",
          reason: "배치 운영 변경"
        },
        centerCutForm: {
          centerCutJobId: "CPF_REF_CENTER_CUT_SAMPLE_JOB",
          statusCode: "",
          resultStatus: "",
          limit: 100
        },
        notificationForm: {
          ruleId: null,
          eventType: "BATCH",
          eventSubType: "FAILED",
          channelCode: "ADM",
          templateCode: "",
          severity: "WARN",
          receiverGroup: "ADM_OPERATOR",
          useYn: "Y",
          targetType: "ADM_TEST",
          targetId: "TEST",
          receiver: "ADM_OPERATOR",
          message: "ADM notification test message.",
          reason: "알림 규칙 변경",
          requestUser: ""
        },
        notificationDeliveryForm: {
          deliveryId: null,
          expectedVersion: null,
          deliveryStatus: "",
          operationId: "",
          reason: "알림 발송 운영 조치"
        },
        downloadForm: {
          downloadType: "TRANSACTION_LOGS",
          targetType: "LOG_LIST",
          fromDate: "",
          toDate: "",
          transactionId: "",
          traceId: "",
          jobId: "",
          limit: 1000,
          includeSensitive: false,
          reason: "운영 증적 다운로드",
          requestUser: ""
        },
        cacheTargets: ["ALL", "CODE", "MESSAGE", "RESPONSE_CODE", "CONFIG"],
        cacheReason: "캐시 운영 조치",
        cacheControl: { tenantId: "DEFAULT", namespace: "", key: "", version: 1 },
        responseCodeReason: "응답코드 변경",
        logLevelForm: { businessTransactionId: "", transactionId: "", logLevel: "DEBUG", ttlSeconds: 600, reason: "운영 진단" },
        logPolicyForm: {
          policyId: null,
          policyKey: "ONLINE_DEFAULT",
          policyName: "온라인 거래 기본 로그 정책",
          targetType: "ONLINE_TRANSACTION",
          targetId: "*",
          logLevel: "INFO",
          dbLogEnabledYn: "Y",
          fileLogEnabledYn: "Y",
          requestBodyLogYn: "N",
          responseBodyLogYn: "N",
          queryCaptureMode: "NONE",
          requestHeaderCaptureMode: "ALLOWLIST",
          responseHeaderCaptureMode: "ALLOWLIST",
          requestBodyCaptureMode: "NONE",
          responseBodyCaptureMode: "NONE",
          errorStackCaptureMode: "SUMMARY",
          queryAllowlist: "",
          headerAllowlist: "content-type,x-cpf-transaction-id,x-cpf-trace-id",
          fieldAllowlist: "",
          maskingPolicyKey: "CPF_SAFE_DEFAULT",
          maxQueryBytes: 4096,
          maxHeaderBytes: 8192,
          maxRequestBodyBytes: 65536,
          maxResponseBodyBytes: 65536,
          maxStackBytes: 32768,
          errorStackLogYn: "Y",
          selectedOverrideId: "",
          retentionDays: 90,
          samplingRate: 100,
          priority: 100,
          activeYn: "Y",
          description: "ADM managed log policy",
          traceBoostTransactionId: "",
          traceBoostBusinessTransactionId: "",
          traceBoostApiPath: "",
          traceBoostStatus: "",
          traceBoostFailureCode: "",
          traceBoostDurationMsGreaterThan: null,
          traceBoostTtlSeconds: 600,
          effectiveStartAt: "",
          effectiveEndAt: "",
          reason: "로그 정책 변경",
          requestUser: ""
        },
        operatorForm: { operatorId: "", operatorName: "", operationId: crypto.randomUUID(), mobileNo: "", officePhoneNo: "", password: "", reason: "운영자 등록" },
        messageForm: {
          messageId: null,
          messageCode: "MCPF990099",
          locale: "ko",
          messageFormatType: "FIXED",
          externalMessage: "샘플 메시지",
          internalMessage: "샘플 내부 메시지",
          parameterCount: 0,
          parameterSample: "[]",
          description: "ADM sample",
          useYn: "Y",
          requestUser: "",
          reason: "메시지 변경"
        },
        codeForm: {
          codeId: null,
          parentId: null,
          codeKey: "ADM_SAMPLE",
          codeValue: "SAMPLE",
          description: "ADM sample code",
          useYn: "Y",
          requestUser: "",
          reason: "코드 변경"
        },
        configForm: {
          configId: null,
          configKey: "CPF.ADM.SAMPLE",
          configValue: "Y",
          configType: "BOOLEAN",
          description: "ADM sample config",
          encryptedYn: "N",
          useYn: "Y",
          requestUser: "",
          reason: "설정 변경"
        },
        permissionForm: { roleId: "ADM_VIEWER", menuId: "LOG_LIST", buttonId: "LOG_LIST_READ", apiPermissionId: "API_LOG_LIST_READ", readYn: "Y", writeYn: "N", deleteYn: "N", buttonAllowYn: "N", apiAllowYn: "N", reason: "권한 변경" },
        roleForm: { roleId: "ADM_SAMPLE_ROLE", roleName: "샘플 운영 역할", roleType: "BUSINESS_OPERATOR", description: "ADM permission sample role", useYn: "Y", requestUser: "", reason: "역할 관리" },
        menuManageForm: { menuId: "SAMPLE_MENU", parentMenuId: "", menuName: "샘플 메뉴", menuPath: "/adm#sample", sortOrder: 990, useYn: "Y", requestUser: "", reason: "메뉴 관리" },
        buttonForm: { buttonId: "SAMPLE_MENU_READ", menuId: "SAMPLE_MENU", actionCode: "READ", buttonName: "샘플 조회", httpMethod: "GET", apiPattern: "/adm/api/sample/**", sortOrder: 10, useYn: "Y", requestUser: "", reason: "버튼 관리" },
        apiPermissionForm: { apiPermissionId: "API_SAMPLE_MENU_READ", apiGroupCode: "SAMPLE_MENU", httpMethod: "GET", apiPath: "/adm/api/sample/**", apiName: "샘플 API 조회", permissionCode: "READ", menuId: "SAMPLE_MENU", buttonId: "SAMPLE_MENU_READ", useYn: "Y", requestUser: "", reason: "API 권한 관리" },
        passwordForm: { operatorId: "", newPassword: "", forceChange: true, sessionId: "", reason: "비밀번호 운영" },
        securityForm: { ipPattern: "127.0.0.1", description: "local development", operatorId: "admin", secretRef: "ENV:ADM_ADMIN_OTP_SECRET", otpCode: "", reason: "보안 운영" },
        approvalForm: {
          actionType: "CACHE_CLEAR",
          policyCode: "",
          policyVersion: "",
          ownerModule: "cpf-core",
          ownerCommand: "CACHE_CLEAR",
          targetType: "CACHE",
          targetId: "DEFAULT",
          payloadSnapshot: "{}",
          requestKey: "",
          expireAt: "",
          reason: "위험조치 승인 요청",
          decisionAction: "APPROVE",
          idempotencyKey: "",
          selectedRequestId: ""
        },
        responseCodeForm: {
          responseCode: "EREF010001",
          messageCode: "MREF090001",
          resultType: "E",
          moduleId: "REF",
          responseGroup: "01",
          sequenceNo: "0001",
          httpStatus: 400,
          description: "REF sample response code",
          useYn: "Y",
          requestUser: ""
        },
        serviceRegistrySearch: {
          serviceId: "",
          endpointCode: "",
          instanceStatus: "",
          transactionId: "",
          limit: 50
        },
        reliabilitySearch: {
          scope: "",
          status: "",
          key: "",
          transactionId: "",
          topic: "",
          endpointCode: "",
          type: "",
          businessDate: "",
          jobName: "",
          jobInstanceId: null,
          serverInstanceId: "",
          limit: 100
        },
        reliabilityAction: {
          messageId: "",
          unknownId: "",
          targetStatus: "MANUAL_REVIEW",
          reason: "신뢰성 운영 조치"
        },
        logs: [],
        transactionGroupResult: { items: [] },
        transactionGroupDetail: {} as Record<string, any>,
        transactionResult: {} as Record<string, any>,
        standardExecutionResult: { items: [], summary: {} },
        standardExecutionDetail: {} as Record<string, any>,
        auditLogs: [],
        auditDeliveries: [],
        logDetail: {} as Record<string, any>,
        auditResult: {} as Record<string, any>,
        batchResult: {} as Record<string, any>,
        centerCutResult: {} as Record<string, any>,
        notificationResult: { rules: [], deliveryLogs: [], attempts: [], action: {} } as Record<string, any>,
        selectedNotificationDelivery: null as Record<string, any> | null,
        downloadResult: {} as Record<string, any>,
        cacheResult: {} as Record<string, any>,
        responseCodeResult: {} as Record<string, any>,
        logLevelResult: {} as Record<string, any>,
        logPolicyResult: {} as Record<string, any>,
        logPolicyDistributionResult: {} as Record<string, any>,
        operatorResult: {} as Record<string, any>,
        operatorRawResult: {} as Record<string, any>,
        operatorRawOpen: false,
        operatorRawReason: "",
        operatorRawTarget: null as Record<string, any> | null,
        operatorRawError: "",
        operatorRawLoading: false,
        messageResult: {} as Record<string, any>,
        codeResult: {} as Record<string, any>,
        configResult: {} as Record<string, any>,
        permissionResult: {} as Record<string, any>,
        passwordResult: {} as Record<string, any>,
        securityResult: {} as Record<string, any>,
        serviceRegistryResult: {} as Record<string, any>,
        reliabilityResult: {} as Record<string, any>,
        approvalResult: {} as Record<string, any>,
        approvalPolicyResult: [] as any[],
        remoteLogResult: [],
        remoteLogPreview: {} as Record<string, any>,
        selectedRemoteLog: null,
        remoteLogSelectedIds: [],
        remoteLogDiagnostics: {} as Record<string, any>,
        remoteLogBundleJob: {} as Record<string, any>,
        remoteLogBundleGrant: {} as Record<string, any>
      };
    }

/**
 * 로그아웃/401 시 다른 운영자에게 이전 운영 조회 결과가 노출되지 않도록
 * 인증정보 외 운영 조회·민감 조회 결과도 함께 초기화한다.
 */
export function resetAdmSensitiveState(state: Record<string, any>) {
  state.logs = [];
  state.logDetail = {};
  state.transactionGroupResult = { items: [] };
  state.transactionGroupDetail = {};
  state.transactionResult = {};
  state.standardExecutionResult = { items: [], summary: {} };
  state.standardExecutionDetail = {};
  state.auditLogs = [];
  state.auditDeliveries = [];
  state.auditResult = {};
  state.remoteLogResult = [];
  state.remoteLogPreview = {};
  state.selectedRemoteLog = null;
  state.remoteLogSelectedIds = [];
  state.remoteLogDiagnostics = {};
  state.remoteLogBundleJob = {};
  state.remoteLogBundleGrant = {};
  state.batchResult = {};
  state.centerCutResult = {};
  state.notificationResult = { rules: [], deliveryLogs: [], attempts: [], action: {} };
  state.selectedNotificationDelivery = null;
  state.downloadResult = {};
  state.cacheResult = {};
  state.responseCodeResult = {};
  state.logLevelResult = {};
  state.logPolicyResult = {};
  state.logPolicyDistributionResult = {};
  state.operatorResult = {};
  state.operatorRawResult = {};
  state.operatorRawOpen = false;
  state.operatorRawReason = "";
  state.operatorRawTarget = null;
  state.operatorRawError = "";
  state.operatorRawLoading = false;
  state.messageResult = {};
  state.codeResult = {};
  state.configResult = {};
  state.permissionResult = {};
  state.passwordResult = {};
  state.securityResult = {};
  state.serviceRegistryResult = {};
  state.reliabilityResult = {};
  state.approvalResult = {};
  state.approvalPolicyResult = [];
  if (state.forcedPasswordForm) {
    state.forcedPasswordForm.currentPassword = "";
    state.forcedPasswordForm.newPassword = "";
    state.forcedPasswordForm.newPasswordConfirm = "";
  }
  if (state.loginForm) state.loginForm.password = "";
  if (state.passwordForm) state.passwordForm.newPassword = "";
  if (state.securityForm) state.securityForm.otpCode = "";
}

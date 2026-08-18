import { createAdmEducationFixture } from "./createAdmEducationFixture";
import { admCapabilityRegistry } from "../app/routes";

export function createAdmState() {
      const edu = import.meta.env.VITE_CPF_EDU_PROFILE === "true" ? createAdmEducationFixture() : undefined;
      return {
    integrationTimeHealth: null as any,
    integrationDataQualityResult: null as any,
    integrationWebhookDlq: [] as any[],
    healthInstanceSearch: { systemId: "", readiness: "", includeStale: false, page: 0, size: 50 },
    healthInstanceResult: null as any,
    healthInstanceDetail: null as any,
        activeMenu: "dashboard",
        token: "", // BFF 전환 후 Browser credential 상태는 사용하지 않는다.
        initializationStatus: "IDLE",
        initializationFailures: [] as Array<{ name: string; message: string }>,
        currentOperator: {} as Record<string, any>,
        authorizedMenus: [],
        authorizedButtons: [] as string[],
        buttonsLoaded: false,
        permissionsLoaded: false,
        authMessage: "",
        uiMessage: "",
        loginForm: { operatorId: "admin", password: "", otpCode: "" },
        forcedPasswordForm: {
          currentPassword: "",
          newPassword: "",
          newPasswordConfirm: "",
          reason: "최초 로그인 비밀번호 변경"
        },
        menus: Object.values(admCapabilityRegistry).map(route => ({
          id: route.routeId,
          menuId: route.menuId,
          label: route.label
        })),
        logSearch: {
          transactionId: "",
          traceId: "",
          businessTransactionId: "",
          clientId: "",
          originalChannel: "",
          currentChannel: "",
          callerChannel: "",
          targetChannel: "",
          targetOperationId: "",
          domainCode: "",
          application: "",
          moduleId: "",
          wasId: "",
          instanceId: "",
          hostName: "",
          starterId: "",
          capabilityId: "",
          provider: "",
          capabilityOperation: "",
          uri: "",
          responseCode: "",
          httpStatus: "",
          memberNo: "",
          customerNo: "",
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
          clientId: "",
          originalChannel: "",
          currentChannel: "",
          callerChannel: "",
          targetChannel: "",
          targetOperationId: "",
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
        transactionSearch: { moduleCode: "", activeYn: "Y", operationId: "", selectedOperationId: "", policyVersion: 0, reason: "Operation 정책 운영" },
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
        },
        channelPolicyForm: {
          policyKey: "WEB.DEFAULT",
          operationId: "*",
          callerChannel: "WEB",
          allowed: true,
          authenticationRequired: true,
          signatureRequired: false,
          maxTps: 0,
          effectiveFrom: null,
          effectiveTo: null,
          active: true,
          reason: "거래별 채널 정책 변경",
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
          jobId: edu?.batch.jobId ?? "",
          jobName: edu?.batch.jobName ?? "",
          jobType: edu?.batch.jobType ?? "TASKLET",
          executionId: null,
          scheduleId: edu?.batch.scheduleId ?? "",
          jobParameters: edu?.batch.jobParameters ?? "{}",
          calendarId: "DEFAULT",
          businessDate: new Date().toISOString().slice(0, 10),
          simulationDays: 14,
          dispatchStatus: "WAITING",
          heartbeatTimeoutSeconds: 120,
          lockKey: "",
          ghostActionType: "FAIL",
          approvalRequestId: "",
          idempotencyKey: "",
          expectedVersion: null,
          holidayYn: "N",
          businessDayYn: "Y",
          description: edu?.batch.description ?? "",
          reason: "배치 운영 변경"
        },
        centerCutForm: {
          centerCutJobId: edu?.centerCut.centerCutJobId ?? "",
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
          reason: "메시지 변경"
        },
        codeForm: {
          codeId: null,
          parentId: null,
          codeKey: "ADM_SAMPLE",
          codeValue: "SAMPLE",
          description: "ADM sample code",
          useYn: "Y",
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
          reason: "설정 변경"
        },
        permissionForm: { roleId: "ADM_VIEWER", menuId: "LOG_LIST", buttonId: "LOG_LIST_READ", apiPermissionId: "API_LOG_LIST_READ", readYn: "Y", writeYn: "N", deleteYn: "N", buttonAllowYn: "N", apiAllowYn: "N", reason: "권한 변경" },
        roleForm: { roleId: "ADM_SAMPLE_ROLE", roleName: "샘플 운영 역할", roleType: "BUSINESS_OPERATOR", description: "ADM permission sample role", useYn: "Y", reason: "역할 관리" },
        menuManageForm: { menuId: "SAMPLE_MENU", parentMenuId: "", menuName: "샘플 메뉴", menuPath: "/adm#sample", sortOrder: 990, useYn: "Y", reason: "메뉴 관리" },
        buttonForm: { buttonId: "SAMPLE_MENU_READ", menuId: "SAMPLE_MENU", actionCode: "READ", buttonName: "샘플 조회", httpMethod: "GET", apiPattern: "/adm/api/sample/**", sortOrder: 10, useYn: "Y", reason: "버튼 관리" },
        apiPermissionForm: { apiPermissionId: "API_SAMPLE_MENU_READ", apiGroupCode: "SAMPLE_MENU", httpMethod: "GET", apiPath: "/adm/api/sample/**", apiName: "샘플 API 조회", permissionCode: "READ", menuId: "SAMPLE_MENU", buttonId: "SAMPLE_MENU_READ", useYn: "Y", reason: "API 권한 관리" },
        passwordForm: { operatorId: "", newPassword: "", forceChange: true, sessionId: "", reason: "비밀번호 운영" },
        securityForm: { ipPattern: "127.0.0.1", description: "local development", operatorId: "admin", secretRef: "ENV:ADM_ADMIN_OTP_SECRET", otpCode: "", reason: "보안 운영" },
        approvalForm: {
          operationId: "", changeType: "CACHE_REFRESH", payloadSchemaVersion: 1, expectedVersion: 0,
          rolloutMode: "ALL_AT_ONCE", waveSize: 1, quorumPercent: 100, scheduledAt: "", expiresAt: "",
          approvalId: "", breakGlassId: "", targetJson: "{}", payloadJson: "{}",
          reason: "Runtime 위험 변경", selectedRequestId: "", controlOperationId: "", decisionAction: "CANCEL"
        },
        responseCodeForm: {
          responseCode: "EEDU010001",
          messageCode: "MEDU010001",
          resultType: "E",
          moduleId: "EDU",
          responseGroup: "01",
          sequenceNo: "0001",
          httpStatus: 400,
          description: "EDU sample response code",
          useYn: "Y",
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
          instanceId: "",
          limit: 100
        },
        reliabilityAction: {
          messageId: "",
          unknownId: "",
          targetStatus: "MANUAL_REVIEW",
          reason: "신뢰성 운영 조치"
        },
        operationForm: {
          exportId: "",
          ruleId: "",
          policyId: "",
          maintenanceAction: "DRAIN",
          serviceId: "",
          instanceId: "",
          expectedVersion: 0,
          calendarId: "DEFAULT",
          date: new Date().toISOString().slice(0, 10),
          offset: 1,
          recoveryTarget: "TRANSACTION_LOG",
          recoveryEventId: "",
          notificationRuleId: "",
          fileJobId: "",
          operatorId: "",
          roleIds: "",
          breakGlassSessionId: "",
          reviewStatus: "APPROVED",
          changeId: "",
          operationId: "",
          approvalId: "",
          reason: "운영 조치"
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
        operationResult: {} as Record<string, any>,
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
  state.operationResult = {};
  state.approvalResult = {};
  state.approvalPolicyResult = [];
  if (state.forcedPasswordForm) {
    state.forcedPasswordForm.currentPassword = "";
    state.forcedPasswordForm.newPassword = "";
    state.forcedPasswordForm.newPasswordConfirm = "";
  }
  if (state.loginForm) { state.loginForm.password = ""; state.loginForm.otpCode = ""; }
  if (state.passwordForm) state.passwordForm.newPassword = "";
  if (state.securityForm) state.securityForm.otpCode = "";
}

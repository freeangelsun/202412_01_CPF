package fps.xyz.edu.controller;

import fps.cmn.cde.dto.CommonCodeRequest;
import fps.cmn.cde.service.CodeCacheService;
import fps.cmn.cfg.dto.CommonConfigRequest;
import fps.cmn.cfg.service.ConfigCacheService;
import fps.cmn.msg.dto.CommonMessageRequest;
import fps.cmn.msg.service.MessageCacheService;
import fps.cmn.utils.DateTimeUtils;
import fps.cmn.utils.IdUtils;
import fps.cmn.utils.MaskingUtils;
import fps.cmn.utils.TextUtils;
import fps.pfw.common.exception.FpsBusinessException;
import fps.pfw.common.exception.FpsDynamicErrorCode;
import fps.pfw.common.exception.FpsExternalServiceException;
import fps.pfw.common.exception.FpsValidationException;
import fps.pfw.common.http.FpsWebClient;
import fps.pfw.common.logging.DynamicLogLevelRequest;
import fps.pfw.common.logging.DynamicLogLevelRule;
import fps.pfw.common.logging.DynamicTransactionLogLevelService;
import fps.pfw.common.logging.FpsLogLevel;
import fps.pfw.common.logging.FpsTransaction;
import fps.pfw.common.logging.TransactionContext;
import fps.pfw.common.workflow.FpsWorkflow;
import fps.pfw.common.workflow.FpsWorkflowContext;
import fps.pfw.common.workflow.FpsWorkflowFailurePolicy;
import fps.pfw.common.workflow.FpsWorkflowStep;
import fps.xyz.edu.dto.XyzSampleRequest;
import fps.xyz.edu.dto.XyzSampleResponse;
import fps.xyz.edu.service.XyzSampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 신규 개발자 교육용 XYZ 샘플 컨트롤러입니다.
 *
 * <p>이 컨트롤러는 FPS 프레임워크의 핵심 사용법을 한 곳에서 확인하기 위한 예제입니다.
 * 목록/단건/등록, CMN 캐시 조회/리프레시, 표준 예외, 트랜잭션 분리, 동적 거래 로그레벨을
 * 모두 {@link FpsTransaction} 거래ID와 함께 보여줍니다.</p>
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 교육용 샘플", description = "신규 개발자가 FPS 프레임워크 표준을 실습하는 교육용 API")
public class XyzEducationController {
    private final XyzSampleService xyzSampleService;
    private final CodeCacheService codeCacheService;
    private final MessageCacheService messageCacheService;
    private final ConfigCacheService configCacheService;
    private final DynamicTransactionLogLevelService dynamicLogLevelService;
    private final FpsWebClient fpsWebClient;

    public XyzEducationController(
            XyzSampleService xyzSampleService,
            CodeCacheService codeCacheService,
            MessageCacheService messageCacheService,
            ConfigCacheService configCacheService,
            DynamicTransactionLogLevelService dynamicLogLevelService,
            FpsWebClient fpsWebClient) {
        this.xyzSampleService = xyzSampleService;
        this.codeCacheService = codeCacheService;
        this.messageCacheService = messageCacheService;
        this.configCacheService = configCacheService;
        this.dynamicLogLevelService = dynamicLogLevelService;
        this.fpsWebClient = fpsWebClient;
    }

    /**
     * 목록 조회 개발 패턴을 보여줍니다.
     *
     * @return 샘플 목록
     */
    @GetMapping("/samples")
    @FpsTransaction(id = "XYZ01EDU0001", name = "XYZ교육목록조회")
    @Operation(summary = "XYZ 교육 샘플 목록 조회", description = "목록 조회, readOnly 트랜잭션, 거래 로그 적재 기준을 확인합니다.")
    public ResponseEntity<List<XyzSampleResponse>> findSamples() {
        return ResponseEntity.ok(xyzSampleService.findSamples());
    }

    /**
     * 단건 조회 개발 패턴을 보여줍니다.
     *
     * @param sampleId 샘플 ID
     * @return 샘플 단건
     */
    @GetMapping("/samples/detail")
    @FpsTransaction(id = "XYZ01EDU0002", name = "XYZ교육단건조회")
    @Operation(summary = "XYZ 교육 샘플 단건 조회", description = "명시 쿼리 파라미터와 표준 NotFound 예외 처리 방식을 확인합니다.")
    public ResponseEntity<XyzSampleResponse> getSample(@RequestParam Long sampleId) {
        return ResponseEntity.ok(xyzSampleService.getSample(sampleId));
    }

    /**
     * 등록 개발 패턴과 표준 입력값 검증 방식을 보여줍니다.
     *
     * @param request 등록 요청
     * @return 등록된 샘플
     */
    @PostMapping("/samples")
    @FpsTransaction(id = "XYZ02EDU0001", name = "XYZ교육샘플등록")
    @Operation(summary = "XYZ 교육 샘플 등록", description = "Body DTO를 이용한 등록 거래와 표준 거래ID 부여 방식을 확인합니다.")
    public ResponseEntity<XyzSampleResponse> createSample(@RequestBody XyzSampleRequest request) {
        return ResponseEntity.ok(xyzSampleService.createSample(request));
    }

    /**
     * 수정 개발 패턴을 보여줍니다.
     *
     * <p>업무 개발자는 수정 대상 식별자를 명시 파라미터로 받고, 변경 값은 Body DTO로 받습니다.
     * 이렇게 하면 로그에서 조회 키와 변경 본문을 분리해서 추적할 수 있습니다.</p>
     *
     * @param sampleId 수정할 샘플 ID
     * @param request 수정 요청
     * @return 수정된 샘플
     */
    @PutMapping("/samples")
    @FpsTransaction(id = "XYZ03EDU0001", name = "XYZ교육샘플수정")
    @Operation(summary = "XYZ 교육 샘플 수정", description = "식별자는 쿼리 파라미터로, 변경 값은 Body로 받는 수정 거래 표준을 확인합니다.")
    public ResponseEntity<XyzSampleResponse> updateSample(
            @RequestParam Long sampleId,
            @RequestBody XyzSampleRequest request) {
        return ResponseEntity.ok(xyzSampleService.updateSample(sampleId, request));
    }

    /**
     * 삭제 개발 패턴을 보여줍니다.
     *
     * <p>실무에서는 물리 삭제보다 상태 변경을 우선 검토하지만,
     * 이 샘플은 DELETE 메서드와 표준 예외 처리 흐름을 보여주기 위한 교육용 API입니다.</p>
     *
     * @param sampleId 삭제할 샘플 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/samples")
    @FpsTransaction(id = "XYZ04EDU0001", name = "XYZ교육샘플삭제")
    @Operation(summary = "XYZ 교육 샘플 삭제", description = "명시 파라미터 기반 삭제 거래와 삭제 시 표준 예외 흐름을 확인합니다.")
    public ResponseEntity<Map<String, Object>> deleteSample(@RequestParam Long sampleId) {
        xyzSampleService.deleteSample(sampleId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("deleted", true);
        response.put("sampleId", sampleId);
        return ResponseEntity.ok(response);
    }

    /**
     * CMN 코드/메시지/설정 캐시 조회 방법을 보여줍니다.
     *
     * @param codeKey    코드 키
     * @param messageKey 메시지 키
     * @param configKey  설정 키
     * @return 캐시 조회 결과
     */
    @GetMapping("/cache")
    @FpsTransaction(id = "XYZ09EDU0001", name = "XYZ교육캐시조회")
    @Operation(summary = "CMN 캐시 조회 샘플", description = "코드, 메시지, 설정값을 Caffeine 캐시 기반 공통 서비스로 조회합니다.")
    public ResponseEntity<Map<String, Object>> getCacheSamples(
            @RequestParam(defaultValue = "USER_STATUS") String codeKey,
            @RequestParam(defaultValue = "WELCOME_MSG") String messageKey,
            @RequestParam(defaultValue = "FPS.LOGIN.MAX_FAIL_COUNT") String configKey) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("code", codeCacheService.getCodesByKey(codeKey));
        response.put("message", messageCacheService.getMessageByKeyAndLocale(messageKey, "ko"));
        response.put("config", configCacheService.getConfigByKey(configKey));
        return ResponseEntity.ok(response);
    }

    /**
     * CMN 캐시 수동 리프레시 방법을 보여줍니다.
     *
     * @return 리프레시 결과
     */
    @PostMapping("/cache/refresh")
    @FpsTransaction(id = "XYZ09EDU0002", name = "XYZ교육캐시리프레시")
    @Operation(summary = "CMN 캐시 리프레시 샘플", description = "코드, 메시지, 설정 캐시를 수동 리프레시하고 다중 WAS 이벤트 전파 기준을 확인합니다.")
    public ResponseEntity<Map<String, Object>> refreshCaches() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("codes", codeCacheService.refreshCodesAndPublish());
        response.put("messages", messageCacheService.refreshMessagesAndPublish());
        response.put("configs", configCacheService.refreshConfigsAndPublish());
        return ResponseEntity.ok(response);
    }

    /**
     * CMN 공통 코드 등록 샘플입니다.
     *
     * <p>공통관리 CRUD를 업무단에서 직접 사용할 때는 요청 DTO를 명확히 만들고,
     * 등록/수정/삭제 후 캐시 리프레시 이벤트가 함께 발행되는 공통 서비스를 호출합니다.</p>
     *
     * @param codeKey   코드 키
     * @param codeValue 코드 값
     * @return 등록된 코드 정보
     */
    @PostMapping("/cmn/code")
    @FpsTransaction(id = "XYZ02EDU0010", name = "XYZ교육공통코드등록")
    @Operation(summary = "CMN 공통코드 등록 샘플", description = "업무단에서 CMN 코드 관리 서비스를 호출하는 표준 예입니다.")
    public ResponseEntity<Map<String, Object>> createCommonCode(
            @RequestParam(required = false) String codeKey,
            @RequestParam(defaultValue = "READY") String codeValue) {

        CommonCodeRequest request = new CommonCodeRequest();
        request.setCodeKey(TextUtils.hasText(codeKey)
                ? TextUtils.normalizeCode(codeKey)
                : "XYZ_SAMPLE_" + IdUtils.uuid32().substring(0, 8).toUpperCase());
        request.setCodeValue(TextUtils.normalizeCode(codeValue));
        request.setDescription("XYZ 교육용 공통코드 샘플");
        request.setUseYn("Y");
        request.setRequestUser("XYZ_EDU");
        return ResponseEntity.ok(codeCacheService.createCode(request));
    }

    /**
     * CMN 공통 메시지 등록 샘플입니다.
     *
     * <p>고객에게 내려가는 문구는 {@code EXTERNAL}, 로그와 운영자 화면에서 보는 문구는
     * {@code INTERNAL}로 분리 등록합니다.</p>
     *
     * @return 등록된 메시지 정보
     */
    @PostMapping("/cmn/message")
    @FpsTransaction(id = "XYZ02EDU0020", name = "XYZ교육공통메시지등록")
    @Operation(summary = "CMN 공통메시지 등록 샘플", description = "고객용 EXTERNAL 메시지와 내부용 INTERNAL 메시지 분리 기준을 확인합니다.")
    public ResponseEntity<Map<String, Object>> createCommonMessage() {
        CommonMessageRequest request = new CommonMessageRequest();
        request.setMessageKey("XYZ.EDU.SAMPLE." + IdUtils.uuid32().substring(0, 8).toUpperCase() + ".EXTERNAL");
        request.setMessageValue("XYZ 교육용 메시지입니다.");
        request.setLocale("ko");
        request.setMessageType("EXTERNAL");
        request.setDescription("XYZ 교육용 고객 메시지 샘플");
        request.setUseYn("Y");
        request.setRequestUser("XYZ_EDU");
        return ResponseEntity.ok(messageCacheService.createMessage(request));
    }

    /**
     * CMN 공통 설정값 등록 샘플입니다.
     *
     * <p>배포 없이 변경할 수 있는 기능 토글, 제한값, 외부 연계 옵션은 설정값으로 관리합니다.
     * 비밀번호나 토큰 같은 민감값은 {@code encryptedYn=Y} 기준을 별도 암호화 구현과 함께 사용합니다.</p>
     *
     * @return 등록된 설정값 정보
     */
    @PostMapping("/cmn/config")
    @FpsTransaction(id = "XYZ02EDU0030", name = "XYZ교육공통설정등록")
    @Operation(summary = "CMN 공통설정 등록 샘플", description = "기능 토글, 한도값 같은 운영 변경 설정값 등록 방식을 확인합니다.")
    public ResponseEntity<Map<String, Object>> createCommonConfig() {
        CommonConfigRequest request = new CommonConfigRequest();
        request.setConfigKey("XYZ.EDU.FEATURE." + IdUtils.uuid32().substring(0, 8).toUpperCase() + ".ENABLED");
        request.setConfigValue("Y");
        request.setConfigType("BOOLEAN");
        request.setDescription("XYZ 교육용 기능 토글");
        request.setEncryptedYn("N");
        request.setUseYn("Y");
        request.setRequestUser("XYZ_EDU");
        return ResponseEntity.ok(configCacheService.createConfig(request));
    }

    /**
     * 표준 예외 사용법을 보여줍니다.
     *
     * @param type 예외 유형
     * @return 이 메서드는 예외를 던지는 샘플이므로 정상 응답을 반환하지 않습니다.
     */
    @GetMapping("/exception")
    @FpsTransaction(id = "XYZ09EDU0003", name = "XYZ교육표준예외")
    @Operation(summary = "표준 예외 샘플", description = "검증, 업무, 외부연계 예외가 공통 오류 응답과 거래 로그로 변환되는 흐름을 확인합니다.")
    public ResponseEntity<String> throwStandardException(@RequestParam(defaultValue = "validation") String type) {
        String normalizedType = TextUtils.normalizeCode(type);
        if ("BUSINESS".equals(normalizedType)) {
            throw new FpsBusinessException("XYZ 업무 규칙 위반 샘플입니다. type=" + type);
        }
        if ("EXTERNAL".equals(normalizedType)) {
            throw new FpsExternalServiceException("XYZ 외부 연계 실패 샘플입니다.", new IllegalStateException("sample external failure"));
        }
        throw new FpsValidationException("XYZ 입력값 검증 실패 샘플입니다. type=" + type);
    }

    /**
     * 표준 오류코드와 동적 메시지 인자를 함께 사용하는 예외 처리 샘플입니다.
     *
     * <p>업무 개발자는 고객용 문구와 내부 추적용 문구를 분리하고,
     * {@code {fieldName}}, {@code {fieldValue}} 같은 플레이스홀더 값은
     * {@code Map}으로 전달합니다. PFW 표준 예외 핸들러와 거래 로그 AOP가 같은 인자로
     * 메시지를 조립하므로 응답, DB 로그, 파일 로그의 오류 문구가 서로 맞게 남습니다.</p>
     *
     * @param fieldName  오류가 발생한 업무 필드명
     * @param fieldValue 오류가 발생한 업무 필드값
     * @return 이 메서드는 예외를 던지는 샘플이므로 정상 응답을 반환하지 않습니다.
     */
    @GetMapping("/exception/dynamic-message")
    @FpsTransaction(id = "XYZ09EDU0009", name = "XYZ교육동적메시지예외")
    @Operation(summary = "동적 오류코드/메시지 샘플", description = "enum 추가 없이 동적 오류정의와 메시지 인자로 오류 응답과 로그 메시지를 조립합니다.")
    public ResponseEntity<String> throwDynamicMessageException(
            @RequestParam(defaultValue = "회원번호") String fieldName,
            @RequestParam(defaultValue = "M0001") String fieldValue) {

        // 고객에게 보여줄 수 있는 정제된 문구입니다. 내부 테이블명, SQL, 시스템명은 넣지 않습니다.
        String externalMessage = "이미 등록된 {fieldName}입니다.";

        // 운영자/개발자가 로그 상세에서 볼 내부 문구입니다. 원인 분석에 필요한 값을 명확히 남깁니다.
        String internalMessage = "{fieldName}={fieldValue} 값이 이미 존재합니다. duplicateCheck=XYZ_EDU_SAMPLE";

        // 메시지 템플릿의 플레이스홀더에 들어갈 값입니다. 값은 PFW가 응답과 거래 로그에 동일하게 적용합니다.
        Map<String, Object> messageArguments = Map.of(
                "fieldName", fieldName,
                "fieldValue", fieldValue);

        throw new FpsBusinessException(
                FpsDynamicErrorCode.duplicate("ERR.DUPLICATE", externalMessage, internalMessage),
                externalMessage,
                internalMessage,
                "XYZ 동적 메시지 조립 샘플입니다.",
                messageArguments);
    }

    /**
     * CMN 유틸리티 사용 예를 보여줍니다.
     *
     * @param name 마스킹할 이름
     * @return 유틸리티 처리 결과
     */
    @GetMapping("/utils")
    @FpsTransaction(id = "XYZ09EDU0004", name = "XYZ교육공통유틸")
    @Operation(summary = "CMN 공통 유틸 샘플", description = "날짜, UUID, 임시 ID, 마스킹 유틸 사용법을 확인합니다.")
    public ResponseEntity<Map<String, Object>> useCommonUtils(@RequestParam(defaultValue = "홍길동") String name) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("today", DateTimeUtils.today());
        response.put("now", DateTimeUtils.nowDateTimeMillis());
        response.put("uuid32", IdUtils.uuid32());
        response.put("temporaryId", IdUtils.temporaryId("XYZ"));
        response.put("maskedName", MaskingUtils.maskName(name));
        response.put("maskedSensitive", MaskingUtils.maskSensitive("accountNo=123456789012&password=abc123"));
        return ResponseEntity.ok(response);
    }

    /**
     * 현재 요청의 거래 헤더와 전파 헤더를 확인하는 샘플입니다.
     *
     * <p>하위 주제영역 호출 시 개발자가 헤더를 직접 복사하지 않아도
     * {@code FpsWebClient}가 {@link TransactionContext#propagationHeaders()}와
     * {@link FpsWorkflowContext#propagationHeaders()} 값을 자동으로 전파합니다.</p>
     *
     * @return 현재 거래 컨텍스트 정보
     */
    @GetMapping("/headers")
    @FpsTransaction(id = "XYZ09EDU0008", name = "XYZ교육거래헤더확인")
    @Operation(summary = "거래 헤더 확인 샘플", description = "현재 요청의 글로벌 거래ID, TraceId, SpanId, 전파 헤더를 확인합니다.")
    public ResponseEntity<Map<String, Object>> getCurrentHeaders() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionId", TransactionContext.getOrCreateTransactionId());
        response.put("traceId", TransactionContext.getOrCreateTraceId());
        response.put("spanId", TransactionContext.getOrCreateSpanId());
        response.put("transactionHeader", TransactionContext.currentHeader());
        response.put("propagationHeaders", TransactionContext.propagationHeaders());
        response.put("workflowPropagationHeaders", FpsWorkflowContext.propagationHeaders());
        return ResponseEntity.ok(response);
    }

    /**
     * PFW 표준 WebClient로 MBR 회원 상세를 호출하는 샘플입니다.
     *
     * <p>서비스 ID인 {@code mbr}은 {@code application-pfw.yml}의 {@code fps.services.mbr.base-url}
     * 설정을 사용합니다. 호출 시 글로벌 거래ID, TraceId, 채널, 회원/고객번호, 워크플로우 헤더는
     * 프레임워크 필터에서 자동 전파됩니다.</p>
     *
     * @param memberId 조회할 MBR 회원 ID
     * @return MBR 응답
     */
    @GetMapping("/service-call/mbr-detail")
    @FpsTransaction(id = "XYZ08EDU0001", name = "XYZ교육MBR상세조회호출")
    @FpsWorkflow(id = "XYZ08EDU9001", name = "XYZ교육서비스호출워크플로우")
    @FpsWorkflowStep(name = "XYZ에서MBR회원상세조회", failurePolicy = FpsWorkflowFailurePolicy.VERIFY)
    @Operation(summary = "MBR 서비스 호출 샘플", description = "FpsWebClient가 거래/워크플로우 헤더를 자동 전파하는 흐름을 확인합니다.")
    public ResponseEntity<Map<String, Object>> callMbrDetail(@RequestParam Long memberId) {
        Map<String, Object> mbrResponse = fpsWebClient.get(
                "mbr",
                uriBuilder -> uriBuilder
                        .path("/mbr/detail")
                        .queryParam("memberId", memberId)
                        .build(),
                new ParameterizedTypeReference<>() {
                });

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("guide", "FpsWebClient가 거래/워크플로우 헤더를 자동 전파한 MBR 호출 샘플입니다.");
        response.put("mbrResponse", mbrResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * 하나의 로컬 트랜잭션 사용 예를 보여줍니다.
     *
     * @return 처리 결과
     */
    @PostMapping("/transaction/single")
    @FpsTransaction(id = "XYZ05EDU0001", name = "XYZ교육단일트랜잭션")
    @Operation(summary = "단일 트랜잭션 샘플", description = "한 업무 처리 안에서 같은 트랜잭션으로 묶는 기준을 확인합니다.")
    public ResponseEntity<String> runSingleTransactionSample() {
        return ResponseEntity.ok(xyzSampleService.runSingleTransactionSample());
    }

    /**
     * 원거래와 감사 이력을 분리 트랜잭션으로 처리하는 예를 보여줍니다.
     *
     * @param failAfterAudit 감사 기록 후 실패 여부
     * @return 처리 결과와 감사 메시지
     */
    @PostMapping("/transaction/separated")
    @FpsTransaction(id = "XYZ05EDU0002", name = "XYZ교육분리트랜잭션")
    @FpsWorkflow(id = "XYZ05EDU9001", name = "XYZ분리트랜잭션교육워크플로우")
    @FpsWorkflowStep(name = "XYZ분리트랜잭션교육스텝", failurePolicy = FpsWorkflowFailurePolicy.MANUAL)
    @Operation(summary = "분리 트랜잭션 샘플", description = "REQUIRES_NEW 감사 이력과 수동 처리 실패 정책을 확인합니다.")
    public ResponseEntity<Map<String, Object>> runSeparatedTransactionSample(
            @RequestParam(defaultValue = "false") boolean failAfterAudit) {

        String result = xyzSampleService.runSeparatedTransactionSample(failAfterAudit);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("result", result);
        response.put("auditMessages", xyzSampleService.getAuditMessages());
        return ResponseEntity.ok(response);
    }

    /**
     * 특정 거래만 운영 중 임시 로그레벨을 올리는 규칙을 등록합니다.
     *
     * @param businessTransactionId 업무 거래ID
     * @param transactionId         글로벌 거래ID
     * @param logLevel              적용 로그레벨
     * @param ttlSeconds            유지 시간
     * @param reason                등록 사유
     * @param requestUser           등록자
     * @return 등록된 규칙
     */
    @PutMapping("/admin/log-level")
    @FpsTransaction(id = "XYZ09EDU0005", name = "XYZ교육동적로그레벨등록")
    @Operation(summary = "동적 로그레벨 등록", description = "운영 중 특정 거래ID 또는 업무 거래ID만 임시 DEBUG/TRACE로 올리는 규칙을 등록합니다.")
    public ResponseEntity<DynamicLogLevelRule> registerDynamicLogLevel(
            @RequestParam(required = false) String businessTransactionId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(defaultValue = "DEBUG") FpsLogLevel logLevel,
            @RequestParam(defaultValue = "600") long ttlSeconds,
            @RequestParam(defaultValue = "XYZ 교육용 동적 로그레벨 샘플") String reason,
            @RequestParam(defaultValue = "SYSTEM") String requestUser) {

        DynamicLogLevelRequest request = new DynamicLogLevelRequest();
        request.setBusinessTransactionId(businessTransactionId);
        request.setTransactionId(transactionId);
        request.setModuleId("XYZ");
        request.setLogLevel(logLevel);
        request.setTtl(Duration.ofSeconds(ttlSeconds));
        request.setReason(reason);
        request.setRequestUser(requestUser);
        return ResponseEntity.ok(dynamicLogLevelService.register(request));
    }

    /**
     * 현재 WAS 메모리에 등록된 동적 로그레벨 규칙을 조회합니다.
     *
     * @return 동적 로그레벨 규칙 목록
     */
    @GetMapping("/admin/log-level")
    @FpsTransaction(id = "XYZ09EDU0006", name = "XYZ교육동적로그레벨조회")
    @Operation(summary = "동적 로그레벨 조회", description = "현재 WAS 메모리에 등록된 동적 로그레벨 규칙을 조회합니다.")
    public ResponseEntity<List<DynamicLogLevelRule>> findDynamicLogLevelRules() {
        return ResponseEntity.ok(dynamicLogLevelService.findActiveRules());
    }

    /**
     * 동적 로그레벨 규칙을 삭제합니다.
     *
     * @param ruleId 규칙 ID
     * @return 삭제 결과
     */
    @DeleteMapping("/admin/log-level")
    @FpsTransaction(id = "XYZ09EDU0007", name = "XYZ교육동적로그레벨삭제")
    @Operation(summary = "동적 로그레벨 삭제", description = "동적 로그레벨 규칙 ID로 임시 진단 설정을 삭제합니다.")
    public ResponseEntity<Map<String, Object>> removeDynamicLogLevelRule(@RequestParam String ruleId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("removed", dynamicLogLevelService.remove(ruleId));
        response.put("ruleId", ruleId);
        return ResponseEntity.ok(response);
    }
}

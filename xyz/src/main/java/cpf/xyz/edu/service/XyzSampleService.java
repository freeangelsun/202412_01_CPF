package cpf.xyz.edu.service;

import cpf.cmn.utils.DateTimeUtils;
import cpf.cmn.utils.IdUtils;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.CpfBusinessException;
import cpf.pfw.common.exception.CpfNotFoundException;
import cpf.pfw.common.exception.CpfValidationException;
import cpf.xyz.edu.dto.XyzSampleRequest;
import cpf.xyz.edu.dto.XyzSampleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * XYZ CRUD 교육 샘플 서비스입니다.
 *
 * <p>신규 업무 모듈에서 Service 계층이 담당해야 하는 입력 검증, 예외 변환, 트랜잭션 경계,
 * 감사성 부가 처리 호출 방식을 보여줍니다. 저장소는 교육 편의를 위해 메모리를 사용하지만,
 * 실제 업무에서는 Mapper 또는 Repository 호출부만 교체하면 같은 구조로 확장할 수 있습니다.</p>
 */
@Service
public class XyzSampleService {
    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentMap<Long, XyzSampleResponse> samples = new ConcurrentHashMap<>();
    private final XyzAuditSampleService auditSampleService;

    public XyzSampleService(XyzAuditSampleService auditSampleService) {
        this.auditSampleService = auditSampleService;
        createSeedData();
    }

    /**
     * 조회성 서비스는 readOnly 트랜잭션을 사용합니다.
     *
     * @return 샘플 목록
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public List<XyzSampleResponse> findSamples() {
        return samples.values().stream()
                .sorted(Comparator.comparing(XyzSampleResponse::sampleId))
                .toList();
    }

    /**
     * 단건 조회는 식별자 검증과 미존재 예외 처리를 함께 보여줍니다.
     *
     * @param sampleId 샘플 ID
     * @return 샘플 상세
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public XyzSampleResponse getSample(Long sampleId) {
        if (sampleId == null || sampleId <= 0) {
            throw new CpfValidationException("sampleId는 1 이상의 값이어야 합니다. sampleId=" + sampleId);
        }
        XyzSampleResponse response = samples.get(sampleId);
        if (response == null) {
            throw new CpfNotFoundException("XYZ 샘플을 찾을 수 없습니다. sampleId=" + sampleId);
        }
        return response;
    }

    /**
     * 등록 서비스는 필수값 검증 후 신규 식별자를 발급합니다.
     *
     * @param request 등록 요청
     * @return 등록된 샘플
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public XyzSampleResponse createSample(XyzSampleRequest request) {
        String title = TextUtils.requireText(request.title(), "title");
        Long sampleId = sequence.incrementAndGet();
        XyzSampleResponse response = new XyzSampleResponse(
                sampleId,
                title,
                "CREATED",
                TextUtils.defaultIfBlank(request.description(), "XYZ 교육 샘플"),
                DateTimeUtils.nowDateTimeMillis());
        samples.put(sampleId, response);
        return response;
    }

    /**
     * 수정 서비스는 기존 데이터 조회 후 변경 가능한 필드만 갱신합니다.
     *
     * @param sampleId 수정 대상 샘플 ID
     * @param request 수정 요청
     * @return 수정된 샘플
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public XyzSampleResponse updateSample(Long sampleId, XyzSampleRequest request) {
        XyzSampleResponse current = getSample(sampleId);
        String title = TextUtils.requireText(request.title(), "title");
        XyzSampleResponse response = new XyzSampleResponse(
                current.sampleId(),
                title,
                "UPDATED",
                TextUtils.defaultIfBlank(request.description(), current.description()),
                current.createdAt());
        samples.put(sampleId, response);
        return response;
    }

    /**
     * 삭제 서비스는 대상 존재 여부를 먼저 확인한 뒤 삭제합니다.
     *
     * @param sampleId 삭제 대상 샘플 ID
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public void deleteSample(Long sampleId) {
        getSample(sampleId);
        samples.remove(sampleId);
    }

    /**
     * 단일 트랜잭션 샘플을 실행합니다.
     *
     * @return 처리 결과 메시지
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public String runSingleTransactionSample() {
        XyzSampleResponse response = createSample(new XyzSampleRequest(
                "SINGLE-" + IdUtils.temporaryId("XYZ"),
                "단일 트랜잭션 교육 샘플",
                "SYSTEM"));
        return "단일 트랜잭션으로 샘플을 등록했습니다. sampleId=" + response.sampleId();
    }

    /**
     * REQUIRES_NEW 감사성 처리와 주 트랜잭션 실패 흐름을 보여줍니다.
     *
     * @param failAfterAudit 감사 기록 후 강제 실패 여부
     * @return 처리 결과 메시지
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public String runSeparatedTransactionSample(boolean failAfterAudit) {
        XyzSampleResponse response = createSample(new XyzSampleRequest(
                "SEPARATED-" + IdUtils.temporaryId("XYZ"),
                "분리 트랜잭션 교육 샘플",
                "SYSTEM"));

        auditSampleService.writeAuditRequiresNew("분리 트랜잭션 감사 샘플. sampleId=" + response.sampleId());

        if (failAfterAudit) {
            throw new CpfBusinessException("감사 기록 이후 주 트랜잭션 실패를 발생시킨 교육 샘플입니다. sampleId=" + response.sampleId());
        }
        return "분리 트랜잭션 샘플을 정상 처리했습니다. sampleId=" + response.sampleId();
    }

    /**
     * REQUIRES_NEW 감사 샘플 메시지를 조회합니다.
     *
     * @return 감사 샘플 메시지 목록
     */
    public List<String> getAuditMessages() {
        return auditSampleService.getAuditMessages();
    }

    private void createSeedData() {
        List<XyzSampleResponse> seed = new ArrayList<>();
        seed.add(new XyzSampleResponse(1L, "목록 조회 샘플", "READY", "GET /xyz/edu/samples", DateTimeUtils.nowDateTimeMillis()));
        seed.add(new XyzSampleResponse(2L, "상세 조회 샘플", "READY", "GET /xyz/edu/samples/detail", DateTimeUtils.nowDateTimeMillis()));
        seed.forEach(sample -> samples.put(sample.sampleId(), sample));
        sequence.set(2L);
    }
}

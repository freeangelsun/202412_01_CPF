package fps.xyz.edu.service;

import fps.cmn.utils.DateTimeUtils;
import fps.cmn.utils.IdUtils;
import fps.cmn.utils.TextUtils;
import fps.pfw.common.exception.FpsBusinessException;
import fps.pfw.common.exception.FpsNotFoundException;
import fps.pfw.common.exception.FpsValidationException;
import fps.xyz.edu.dto.XyzSampleRequest;
import fps.xyz.edu.dto.XyzSampleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * XYZ 교육용 샘플 업무 서비스입니다.
 *
 * <p>실제 DB Mapper 대신 메모리 저장소를 사용해 신규 개발자가 컨트롤러, 서비스,
 * 표준 예외, 트랜잭션 선언 흐름을 부담 없이 확인할 수 있게 합니다.</p>
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
     * 목록 조회 샘플입니다.
     *
     * <p>readOnly 트랜잭션 선언은 조회 전용 의도를 명확히 보여주기 위한 예시입니다.</p>
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
     * 단건 조회 샘플입니다.
     *
     * @param sampleId 샘플 ID
     * @return 샘플 응답
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public XyzSampleResponse getSample(Long sampleId) {
        if (sampleId == null || sampleId <= 0) {
            throw new FpsValidationException("sampleId는 양수여야 합니다. sampleId=" + sampleId);
        }
        XyzSampleResponse response = samples.get(sampleId);
        if (response == null) {
            throw new FpsNotFoundException("XYZ 샘플 데이터가 없습니다. sampleId=" + sampleId);
        }
        return response;
    }

    /**
     * 등록 샘플입니다.
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
                TextUtils.defaultIfBlank(request.description(), "XYZ 교육용 샘플"),
                DateTimeUtils.nowDateTimeMillis());
        samples.put(sampleId, response);
        return response;
    }

    /**
     * 수정 샘플입니다.
     *
     * <p>실제 업무에서는 수정 전 현재 상태 검증, 권한 검증, 변경 이력 저장을 함께 고려합니다.
     * 교육용 샘플에서는 메모리 저장소의 값을 교체하는 방식으로 CRUD 흐름만 보여줍니다.</p>
     *
     * @param sampleId 수정할 샘플 ID
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
     * 삭제 샘플입니다.
     *
     * <p>실제 금융 업무에서는 물리 삭제보다 사용 여부 변경, 해지 상태 변경, 삭제 이력 저장을 우선 검토합니다.
     * 교육용 샘플에서는 REST DELETE 흐름과 표준 예외 처리를 보여주기 위해 메모리 값만 제거합니다.</p>
     *
     * @param sampleId 삭제할 샘플 ID
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public void deleteSample(Long sampleId) {
        getSample(sampleId);
        samples.remove(sampleId);
    }

    /**
     * 하나의 트랜잭션 안에서 업무 처리와 감사 처리가 함께 수행되는 샘플입니다.
     *
     * @return 처리 결과 메시지
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public String runSingleTransactionSample() {
        XyzSampleResponse response = createSample(new XyzSampleRequest(
                "SINGLE-" + IdUtils.temporaryId("XYZ"),
                "하나의 트랜잭션 샘플",
                "SYSTEM"));
        return "하나의 트랜잭션으로 샘플을 등록했습니다. sampleId=" + response.sampleId();
    }

    /**
     * 원거래와 감사 이력을 분리 트랜잭션으로 처리하는 샘플입니다.
     *
     * @param failAfterAudit 감사 기록 후 의도적으로 실패시킬지 여부
     * @return 처리 결과 메시지
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public String runSeparatedTransactionSample(boolean failAfterAudit) {
        XyzSampleResponse response = createSample(new XyzSampleRequest(
                "SEPARATED-" + IdUtils.temporaryId("XYZ"),
                "분리 트랜잭션 샘플",
                "SYSTEM"));

        auditSampleService.writeAuditRequiresNew("분리 트랜잭션 감사 샘플. sampleId=" + response.sampleId());

        if (failAfterAudit) {
            throw new FpsBusinessException("감사 이력 저장 후 원거래 실패를 가정한 샘플입니다. sampleId=" + response.sampleId());
        }
        return "분리 트랜잭션 샘플을 처리했습니다. sampleId=" + response.sampleId();
    }

    /**
     * 분리 트랜잭션 샘플에서 남긴 감사 메시지를 조회합니다.
     *
     * @return 감사 메시지 목록
     */
    public List<String> getAuditMessages() {
        return auditSampleService.getAuditMessages();
    }

    private void createSeedData() {
        List<XyzSampleResponse> seed = new ArrayList<>();
        seed.add(new XyzSampleResponse(1L, "목록 조회 샘플", "READY", "GET /xyz/edu/samples", DateTimeUtils.nowDateTimeMillis()));
        seed.add(new XyzSampleResponse(2L, "단건 조회 샘플", "READY", "GET /xyz/edu/samples/detail", DateTimeUtils.nowDateTimeMillis()));
        seed.forEach(sample -> samples.put(sample.sampleId(), sample));
        sequence.set(2L);
    }
}

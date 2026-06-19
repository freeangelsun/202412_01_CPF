package cpf.xyz.edu.service;

import cpf.cmn.utils.DateTimeUtils;
import cpf.cmn.utils.IdUtils;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.CpfBusinessException;
import cpf.pfw.common.exception.CpfNotFoundException;
import cpf.pfw.common.exception.CpfValidationException;
import cpf.xyz.edu.dto.XyzCrudEducationRequest;
import cpf.xyz.edu.dto.XyzCrudEducationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * XYZ CRUD 교육 서비스입니다.
 *
 * <p>신규 업무 모듈에서 Service 계층이 담당해야 하는 입력 검증, 예외 변환, 트랜잭션 경계,
 * 감사성 보조 처리 호출 방식을 보여줍니다. EDU 모듈은 학습 편의를 위해 메모리 저장소를 사용하지만,
 * 운영 모듈에서는 동일한 구조에서 Mapper 또는 Repository 호출부만 연결합니다.</p>
 */
@Service
public class XyzCrudEducationService {
    private final AtomicLong sequence = new AtomicLong();
    private final ConcurrentMap<Long, XyzCrudEducationResponse> educationItems = new ConcurrentHashMap<>();
    private final XyzTransactionEducationAuditService auditService;

    public XyzCrudEducationService(XyzTransactionEducationAuditService auditService) {
        this.auditService = auditService;
        createSeedData();
    }

    /**
     * 조회성 서비스는 readOnly 트랜잭션을 사용합니다.
     *
     * @return 교육 항목 목록
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public List<XyzCrudEducationResponse> findEducationItems() {
        return educationItems.values().stream()
                .sorted(Comparator.comparing(XyzCrudEducationResponse::educationItemId))
                .toList();
    }

    /**
     * 단건 조회는 식별자 검증과 미존재 예외 처리를 함께 보여줍니다.
     *
     * @param educationItemId 교육 항목 ID
     * @return 교육 항목 상세
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public XyzCrudEducationResponse getEducationItem(Long educationItemId) {
        if (educationItemId == null || educationItemId <= 0) {
            throw new CpfValidationException("educationItemId는 1 이상의 값이어야 합니다. educationItemId=" + educationItemId);
        }
        XyzCrudEducationResponse response = educationItems.get(educationItemId);
        if (response == null) {
            throw new CpfNotFoundException("XYZ 교육 항목을 찾을 수 없습니다. educationItemId=" + educationItemId);
        }
        return response;
    }

    /**
     * 등록 서비스는 필수값 검증 후 신규 식별자를 발급합니다.
     *
     * @param request 등록 요청
     * @return 등록된 교육 항목
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public XyzCrudEducationResponse createEducationItem(XyzCrudEducationRequest request) {
        String title = TextUtils.requireText(request.title(), "title");
        Long educationItemId = sequence.incrementAndGet();
        XyzCrudEducationResponse response = new XyzCrudEducationResponse(
                educationItemId,
                title,
                "CREATED",
                TextUtils.defaultIfBlank(request.description(), "XYZ CRUD 교육 항목"),
                DateTimeUtils.nowDateTimeMillis());
        educationItems.put(educationItemId, response);
        return response;
    }

    /**
     * 수정 서비스는 기존 데이터 조회 후 변경 가능한 필드만 갱신합니다.
     *
     * @param educationItemId 수정 대상 교육 항목 ID
     * @param request 수정 요청
     * @return 수정된 교육 항목
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public XyzCrudEducationResponse updateEducationItem(Long educationItemId, XyzCrudEducationRequest request) {
        XyzCrudEducationResponse current = getEducationItem(educationItemId);
        String title = TextUtils.requireText(request.title(), "title");
        XyzCrudEducationResponse response = new XyzCrudEducationResponse(
                current.educationItemId(),
                title,
                "UPDATED",
                TextUtils.defaultIfBlank(request.description(), current.description()),
                current.createdAt());
        educationItems.put(educationItemId, response);
        return response;
    }

    /**
     * 삭제 서비스는 대상 존재 여부를 먼저 확인한 뒤 삭제합니다.
     *
     * @param educationItemId 삭제 대상 교육 항목 ID
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public void deleteEducationItem(Long educationItemId) {
        getEducationItem(educationItemId);
        educationItems.remove(educationItemId);
    }

    /**
     * 단일 트랜잭션 교육 흐름을 실행합니다.
     *
     * @return 처리 결과 메시지
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public String runSingleTransactionEducation() {
        XyzCrudEducationResponse response = createEducationItem(new XyzCrudEducationRequest(
                "SINGLE-" + IdUtils.temporaryId("XYZ"),
                "단일 트랜잭션 교육 항목",
                "SYSTEM"));
        return "단일 트랜잭션으로 교육 항목을 등록했습니다. educationItemId=" + response.educationItemId();
    }

    /**
     * REQUIRES_NEW 감사성 처리와 주 트랜잭션 실패 흐름을 보여줍니다.
     *
     * @param failAfterAudit 감사 기록 후 강제 실패 여부
     * @return 처리 결과 메시지
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public String runSeparatedTransactionEducation(boolean failAfterAudit) {
        XyzCrudEducationResponse response = createEducationItem(new XyzCrudEducationRequest(
                "SEPARATED-" + IdUtils.temporaryId("XYZ"),
                "분리 트랜잭션 교육 항목",
                "SYSTEM"));

        auditService.writeAuditRequiresNew("분리 트랜잭션 감사 교육. educationItemId=" + response.educationItemId());

        if (failAfterAudit) {
            throw new CpfBusinessException("감사 기록 이후 주 트랜잭션 실패를 발생시킨 교육 흐름입니다. educationItemId=" + response.educationItemId());
        }
        return "분리 트랜잭션 교육을 정상 처리했습니다. educationItemId=" + response.educationItemId();
    }

    /**
     * REQUIRES_NEW 감사 교육 메시지를 조회합니다.
     *
     * @return 감사 교육 메시지 목록
     */
    public List<String> getAuditMessages() {
        return auditService.getAuditMessages();
    }

    private void createSeedData() {
        List<XyzCrudEducationResponse> seed = new ArrayList<>();
        seed.add(new XyzCrudEducationResponse(1L, "목록 조회 교육", "READY", "GET /xyz/edu/crud-items", DateTimeUtils.nowDateTimeMillis()));
        seed.add(new XyzCrudEducationResponse(2L, "상세 조회 교육", "READY", "GET /xyz/edu/crud-items/detail", DateTimeUtils.nowDateTimeMillis()));
        seed.forEach(item -> educationItems.put(item.educationItemId(), item));
        sequence.set(2L);
    }
}

package com.cpf.reference.crud.application;

import com.cpf.reference.transaction.application.ReferenceTransactionEducationAuditService;
import com.cpf.common.utils.IdUtils;
import com.cpf.common.utils.TextUtils;
import com.cpf.core.common.exception.CpfBusinessException;
import com.cpf.core.common.exception.CpfNotFoundException;
import com.cpf.core.common.exception.CpfValidationException;
import com.cpf.reference.crud.dto.ReferenceCrudEducationRequest;
import com.cpf.reference.crud.dto.ReferenceCrudEducationResponse;
import com.cpf.reference.crud.dto.ReferenceCrudEducationStatusRequest;
import com.cpf.reference.query.dto.ReferenceQueryEducationItem;
import com.cpf.reference.query.adapter.ReferenceQueryEducationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * REF CRUD 교육 서비스입니다.
 *
 * <p>이 서비스는 Controller, Service, Repository, Mapper, SQL fixture가 실제로 연결되는 최소 업무 흐름을 보여줍니다.
 * 신규 업무 모듈을 만들 때는 이 구조를 복사하되, ID 발급은 CMN 채번 또는 업무별 sequence 정책으로 승격합니다.</p>
 */
@Service
public class ReferenceCrudEducationService extends com.cpf.reference.common.base.ReferenceBaseService {
    private final ReferenceQueryEducationRepository repository;
    private final ReferenceTransactionEducationAuditService auditService;

    public ReferenceCrudEducationService(
            ReferenceQueryEducationRepository repository,
            ReferenceTransactionEducationAuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    /**
     * CRUD 교육 항목 목록을 조회합니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public List<ReferenceCrudEducationResponse> findEducationItems(
            String keyword,
            String statusCode,
            String sort,
            int limit) {
        return repository.findItems(keyword, statusCode, sort, limit).stream()
                .map(this::toCrudResponse)
                .toList();
    }

    /**
     * CRUD 교육 항목 단건을 조회합니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager", readOnly = true)
    public ReferenceCrudEducationResponse getEducationItem(Long educationItemId) {
        return toCrudResponse(findExistingItem(educationItemId));
    }

    /**
     * CRUD 교육 항목을 등록합니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public ReferenceCrudEducationResponse createEducationItem(ReferenceCrudEducationRequest request) {
        validateItemIdRangeRequest(request);
        Long itemId = repository.nextCrudItemId();
        String requestUser = repository.normalizeRequestUser(request.requestUser());
        repository.insertCrudItem(
                itemId,
                TextUtils.requireText(request.title(), "title"),
                repository.normalizeCategoryCode(request.categoryCode()),
                "ACTIVE",
                TextUtils.defaultIfBlank(request.ownerMemberNo(), null),
                requestUser);
        return getEducationItem(itemId);
    }

    /**
     * CRUD 교육 항목을 수정합니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public ReferenceCrudEducationResponse updateEducationItem(Long educationItemId, ReferenceCrudEducationRequest request) {
        findExistingItem(educationItemId);
        validateItemIdRangeRequest(request);
        int updatedRows = repository.updateCrudItem(
                educationItemId,
                TextUtils.requireText(request.title(), "title"),
                repository.normalizeCategoryCode(request.categoryCode()),
                TextUtils.defaultIfBlank(request.ownerMemberNo(), null),
                repository.normalizeRequestUser(request.requestUser()));
        if (updatedRows != 1) {
            throw new CpfNotFoundException("REF CRUD 교육 항목을 수정할 수 없습니다. educationItemId=" + educationItemId);
        }
        return getEducationItem(educationItemId);
    }

    /**
     * CRUD 교육 항목 상태를 변경합니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public ReferenceCrudEducationResponse changeEducationItemStatus(
            Long educationItemId,
            ReferenceCrudEducationStatusRequest request) {
        findExistingItem(educationItemId);
        int updatedRows = repository.updateCrudItemStatus(
                educationItemId,
                TextUtils.requireText(request.statusCode(), "statusCode"),
                repository.normalizeRequestUser(request.requestUser()));
        if (updatedRows != 1) {
            throw new CpfNotFoundException("REF CRUD 교육 항목 상태를 변경할 수 없습니다. educationItemId=" + educationItemId);
        }
        return getEducationItem(educationItemId);
    }

    /**
     * CRUD 교육 항목을 논리 삭제합니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public void deleteEducationItem(Long educationItemId, String requestUser) {
        findExistingItem(educationItemId);
        int updatedRows = repository.logicalDeleteCrudItem(
                educationItemId,
                repository.normalizeRequestUser(requestUser));
        if (updatedRows != 1) {
            throw new CpfNotFoundException("REF CRUD 교육 항목을 삭제할 수 없습니다. educationItemId=" + educationItemId);
        }
    }

    /**
     * 단일 트랜잭션 교육 흐름을 실행합니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public String runSingleTransactionEducation() {
        ReferenceCrudEducationResponse response = createEducationItem(new ReferenceCrudEducationRequest(
                "SINGLE-" + IdUtils.temporaryId("REF"),
                "단일 트랜잭션 교육 항목",
                "SYSTEM",
                "TX_SINGLE",
                "MBR-EDU-SINGLE"));
        return "단일 트랜잭션으로 교육 항목을 등록했습니다. educationItemId=" + response.educationItemId();
    }

    /**
     * REQUIRES_NEW 감사 처리와 주 트랜잭션 실패 흐름을 보여줍니다.
     */
    @Transactional(transactionManager = "cmnTransactionManager")
    public String runSeparatedTransactionEducation(boolean failAfterAudit) {
        ReferenceCrudEducationResponse response = createEducationItem(new ReferenceCrudEducationRequest(
                "SEPARATED-" + IdUtils.temporaryId("REF"),
                "분리 트랜잭션 교육 항목",
                "SYSTEM",
                "TX_SEPARATED",
                "MBR-EDU-SEPARATED"));

        auditService.writeAuditRequiresNew("분리 트랜잭션 감사 교육. educationItemId=" + response.educationItemId());

        if (failAfterAudit) {
            throw new CpfBusinessException("감사 기록 이후 주 트랜잭션 실패를 발생시키는 교육 흐름입니다. educationItemId="
                    + response.educationItemId());
        }
        return "분리 트랜잭션 교육이 정상 처리되었습니다. educationItemId=" + response.educationItemId();
    }

    /**
     * REQUIRES_NEW 감사 교육 메시지를 조회합니다.
     */
    public List<String> getAuditMessages() {
        return auditService.getAuditMessages();
    }

    private ReferenceQueryEducationItem findExistingItem(Long educationItemId) {
        if (educationItemId == null || educationItemId <= 0) {
            throw new CpfValidationException("educationItemId는 1 이상의 값이어야 합니다. educationItemId=" + educationItemId);
        }
        return repository.findById(educationItemId)
                .orElseThrow(() -> new CpfNotFoundException("REF CRUD 교육 항목을 찾을 수 없습니다. educationItemId=" + educationItemId));
    }

    private void validateItemIdRangeRequest(ReferenceCrudEducationRequest request) {
        if (request == null) {
            throw new CpfValidationException("CRUD 교육 요청 본문이 필요합니다.");
        }
    }

    private ReferenceCrudEducationResponse toCrudResponse(ReferenceQueryEducationItem item) {
        return new ReferenceCrudEducationResponse(
                item.itemId(),
                item.itemName(),
                item.statusCode(),
                "분류=" + item.categoryCode(),
                item.createdAt(),
                item.categoryCode(),
                item.ownerMemberNo());
    }
}

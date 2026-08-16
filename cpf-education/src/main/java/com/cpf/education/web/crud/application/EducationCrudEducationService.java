package com.cpf.education.web.crud.application;
import com.cpf.education.data.transaction.application.EducationTransactionEducationAuditService;
import com.cpf.foundation.id.CpfIds;
import com.cpf.foundation.util.CpfStrings;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.education.web.crud.dto.EducationCrudEducationRequest;
import com.cpf.education.web.crud.dto.EducationCrudEducationResponse;
import com.cpf.education.web.crud.dto.EducationCrudEducationStatusRequest;
import com.cpf.education.data.query.dto.EducationQueryEducationItem;
import com.cpf.education.data.query.adapter.EducationQueryEducationRepository;
import org.springframework.stereotype.Service;
import com.cpf.data.persistence.api.annotation.CpfTx;

import java.util.List;

/**
 * EDU CRUD 교육 서비스입니다.
 *
 * <p>이 서비스는 Controller, Service, Repository, Mapper, SQL fixture가 실제로 연결되는 최소 업무 흐름을 보여줍니다.
 * 신규 업무 모듈을 만들 때는 이 구조를 Template으로 사용하되, 업무 번호는 해당 Domain 정책으로 소유합니다.</p>
 */
@Service
public class EducationCrudEducationService extends com.cpf.education.base.EducationBaseService {
    private final EducationQueryEducationRepository repository;
    private final EducationTransactionEducationAuditService auditService;

    public EducationCrudEducationService(
            EducationQueryEducationRepository repository,
            EducationTransactionEducationAuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    /**
     * CRUD 교육 항목 목록을 조회합니다.
     */
    @CpfTx(id = "EDU-ATIONSERVICE-01", name = "EducationCrudEducationServiceTx1", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager", readOnly = true)
    public List<EducationCrudEducationResponse> findEducationItems(
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
    @CpfTx(id = "EDU-ATIONSERVICE-02", name = "EducationCrudEducationServiceTx2", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager", readOnly = true)
    public EducationCrudEducationResponse getEducationItem(Long educationItemId) {
        return toCrudResponse(findExistingItem(educationItemId));
    }

    /**
     * CRUD 교육 항목을 등록합니다.
     */
    @CpfTx(id = "EDU-ATIONSERVICE-03", name = "EducationCrudEducationServiceTx3", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager")
    public EducationCrudEducationResponse createEducationItem(EducationCrudEducationRequest request) {
        validateItemIdRangeRequest(request);
        Long itemId = repository.nextCrudItemId();
        String requestUser = repository.normalizeRequestUser(request.requestUser());
        repository.insertCrudItem(
                itemId,
                CpfStrings.requireText(request.title(), "title"),
                repository.normalizeCategoryCode(request.categoryCode()),
                "ACTIVE",
                CpfStrings.defaultIfBlank(request.ownerEducation(), null),
                requestUser);
        return getEducationItem(itemId);
    }

    /**
     * CRUD 교육 항목을 수정합니다.
     */
    @CpfTx(id = "EDU-ATIONSERVICE-04", name = "EducationCrudEducationServiceTx4", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager")
    public EducationCrudEducationResponse updateEducationItem(Long educationItemId, EducationCrudEducationRequest request) {
        findExistingItem(educationItemId);
        validateItemIdRangeRequest(request);
        int updatedRows = repository.updateCrudItem(
                educationItemId,
                CpfStrings.requireText(request.title(), "title"),
                repository.normalizeCategoryCode(request.categoryCode()),
                CpfStrings.defaultIfBlank(request.ownerEducation(), null),
                repository.normalizeRequestUser(request.requestUser()));
        if (updatedRows != 1) {
            throw new CpfNotFoundException("EDU CRUD 교육 항목을 수정할 수 없습니다. educationItemId=" + educationItemId);
        }
        return getEducationItem(educationItemId);
    }

    /**
     * CRUD 교육 항목 상태를 변경합니다.
     */
    @CpfTx(id = "EDU-ATIONSERVICE-05", name = "EducationCrudEducationServiceTx5", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager")
    public EducationCrudEducationResponse changeEducationItemStatus(
            Long educationItemId,
            EducationCrudEducationStatusRequest request) {
        findExistingItem(educationItemId);
        int updatedRows = repository.updateCrudItemStatus(
                educationItemId,
                CpfStrings.requireText(request.statusCode(), "statusCode"),
                repository.normalizeRequestUser(request.requestUser()));
        if (updatedRows != 1) {
            throw new CpfNotFoundException("EDU CRUD 교육 항목 상태를 변경할 수 없습니다. educationItemId=" + educationItemId);
        }
        return getEducationItem(educationItemId);
    }

    /**
     * CRUD 교육 항목을 논리 삭제합니다.
     */
    @CpfTx(id = "EDU-ATIONSERVICE-06", name = "EducationCrudEducationServiceTx6", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager")
    public void deleteEducationItem(Long educationItemId, String requestUser) {
        findExistingItem(educationItemId);
        int updatedRows = repository.logicalDeleteCrudItem(
                educationItemId,
                repository.normalizeRequestUser(requestUser));
        if (updatedRows != 1) {
            throw new CpfNotFoundException("EDU CRUD 교육 항목을 삭제할 수 없습니다. educationItemId=" + educationItemId);
        }
    }

    /**
     * 단일 트랜잭션 교육 흐름을 실행합니다.
     */
    @CpfTx(id = "EDU-ATIONSERVICE-07", name = "EducationCrudEducationServiceTx7", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager")
    public String runSingleTransactionEducation() {
        EducationCrudEducationResponse response = createEducationItem(new EducationCrudEducationRequest(
                "SINGLE-" + CpfIds.temporaryId("EDU"),
                "단일 트랜잭션 교육 항목",
                "SYSTEM",
                "TX_SINGLE",
                "EDU-OWNER-SINGLE"));
        return "단일 트랜잭션으로 교육 항목을 등록했습니다. educationItemId=" + response.educationItemId();
    }

    /**
     * REQUIRES_NEW 감사 처리와 주 트랜잭션 실패 흐름을 보여줍니다.
     */
    @CpfTx(id = "EDU-ATIONSERVICE-08", name = "EducationCrudEducationServiceTx8", ownerDomain = "EDU", transactionManager = "educationReferenceFixtureTransactionManager")
    public String runSeparatedTransactionEducation(boolean failAfterAudit) {
        EducationCrudEducationResponse response = createEducationItem(new EducationCrudEducationRequest(
                "SEPARATED-" + CpfIds.temporaryId("EDU"),
                "분리 트랜잭션 교육 항목",
                "SYSTEM",
                "TX_SEPARATED",
                "EDU-OWNER-SEPARATED"));

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

    private EducationQueryEducationItem findExistingItem(Long educationItemId) {
        if (educationItemId == null || educationItemId <= 0) {
            throw new CpfValidationException("educationItemId는 1 이상의 값이어야 합니다. educationItemId=" + educationItemId);
        }
        return repository.findById(educationItemId)
                .orElseThrow(() -> new CpfNotFoundException("EDU CRUD 교육 항목을 찾을 수 없습니다. educationItemId=" + educationItemId));
    }

    private void validateItemIdRangeRequest(EducationCrudEducationRequest request) {
        if (request == null) {
            throw new CpfValidationException("CRUD 교육 요청 본문이 필요합니다.");
        }
    }

    private EducationCrudEducationResponse toCrudResponse(EducationQueryEducationItem item) {
        return new EducationCrudEducationResponse(
                item.itemId(),
                item.itemName(),
                item.statusCode(),
                "분류=" + item.categoryCode(),
                item.createdAt() == null ? null : item.createdAt().toString(),
                item.categoryCode(),
                item.ownerEducation());
    }
}

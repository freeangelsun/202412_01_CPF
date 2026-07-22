package com.cpf.account.account.service;

import com.cpf.account.account.dto.AccAccountCreateRequest;
import com.cpf.account.account.dto.AccAccountResponse;
import com.cpf.account.account.dto.AccAccountSearchCriteria;
import com.cpf.account.account.dto.AccAccountUpdateRequest;
import com.cpf.account.account.port.AccAccountRepository;
import com.cpf.account.common.base.AccBaseService;
import com.cpf.core.common.exception.CpfBusinessException;
import com.cpf.core.common.exception.CpfErrorCode;
import com.cpf.core.common.exception.CpfNotFoundException;
import com.cpf.core.common.exception.CpfValidationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 계정 등록·조회·수정·논리 삭제의 트랜잭션 경계를 소유합니다. */
@Service
public class AccAccountService extends AccBaseService {
    private final AccAccountRepository repository;

    public AccAccountService(AccAccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AccAccountResponse create(AccAccountCreateRequest request, String actor, String auditReason) {
        String resolvedActor = requireText(actor, "operatorId");
        String resolvedReason = requireText(auditReason, "auditReason");
        try {
            long accountId = repository.create(request, resolvedActor);
            AccAccountResponse created = get(accountId);
            repository.recordChange(accountId, "CREATE", null, created, resolvedActor, resolvedReason);
            return created;
        } catch (DuplicateKeyException ex) {
            throw new CpfBusinessException(CpfErrorCode.DUPLICATE,
                    "이미 등록된 ACC 계정번호입니다. accountNo=" + request.accountNo());
        }
    }

    @Transactional(readOnly = true)
    public AccAccountResponse get(long accountId) {
        if (accountId < 1) {
            throw new CpfValidationException("accountId는 1 이상이어야 합니다.");
        }
        return repository.find(accountId)
                .orElseThrow(() -> new CpfNotFoundException("ACC 계정을 찾을 수 없습니다. accountId=" + accountId));
    }

    @Transactional(readOnly = true)
    public List<AccAccountResponse> search(AccAccountSearchCriteria criteria) {
        return repository.search(criteria == null
                ? new AccAccountSearchCriteria(null, null, null, null, null, null, null, null).normalized()
                : criteria.normalized());
    }

    @Transactional
    public AccAccountResponse update(
            long accountId,
            AccAccountUpdateRequest request,
            String actor,
            String auditReason) {
        String resolvedActor = requireText(actor, "operatorId");
        String resolvedReason = requireText(auditReason, "auditReason");
        AccAccountResponse before = get(accountId);
        if (!repository.update(accountId, request, resolvedActor)) {
            throw new CpfBusinessException(CpfErrorCode.DUPLICATE,
                    "다른 요청이 먼저 ACC 계정을 수정했습니다. 최신 version으로 다시 시도해 주세요.");
        }
        AccAccountResponse after = get(accountId);
        repository.recordChange(accountId, "UPDATE", before, after, resolvedActor, resolvedReason);
        return after;
    }

    @Transactional
    public void delete(long accountId, long version, String actor, String auditReason) {
        String resolvedActor = requireText(actor, "operatorId");
        String resolvedReason = requireText(auditReason, "auditReason");
        AccAccountResponse before = get(accountId);
        if (!repository.logicalDelete(accountId, version, resolvedActor)) {
            throw new CpfBusinessException(CpfErrorCode.DUPLICATE,
                    "다른 요청이 먼저 ACC 계정을 변경했습니다. 최신 version으로 다시 시도해 주세요.");
        }
        repository.recordChange(accountId, "DELETE", before, null, resolvedActor, resolvedReason);
    }
}

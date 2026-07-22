package com.cpf.account.account.port;

import com.cpf.account.account.dto.AccAccountCreateRequest;
import com.cpf.account.account.dto.AccAccountResponse;
import com.cpf.account.account.dto.AccAccountSearchCriteria;
import com.cpf.account.account.dto.AccAccountUpdateRequest;

import java.util.List;
import java.util.Optional;

/** ACC 계정 저장소의 업무 경계를 정의합니다. */
public interface AccAccountRepository {
    long create(AccAccountCreateRequest request, String actor);

    Optional<AccAccountResponse> find(long accountId);

    List<AccAccountResponse> search(AccAccountSearchCriteria criteria);

    boolean update(long accountId, AccAccountUpdateRequest request, String actor);

    boolean logicalDelete(long accountId, long version, String actor);

    void recordChange(long accountId, String actionCode, Object beforeValue, Object afterValue,
                      String actor, String auditReason);
}

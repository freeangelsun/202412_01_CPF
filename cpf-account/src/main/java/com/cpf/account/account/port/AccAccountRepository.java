package cpf.acc.account.port;

import cpf.acc.account.dto.AccAccountCreateRequest;
import cpf.acc.account.dto.AccAccountResponse;
import cpf.acc.account.dto.AccAccountSearchCriteria;
import cpf.acc.account.dto.AccAccountUpdateRequest;

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

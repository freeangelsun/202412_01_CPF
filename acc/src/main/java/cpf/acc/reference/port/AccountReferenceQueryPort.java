package cpf.acc.port;

import cpf.acc.dto.AccountSearchRequest;

import java.util.Map;

/**
 * Account 조회 구현을 local 또는 remote adapter로 교체하기 위한 업무 포트입니다.
 */
public interface AccountQueryPort {
    Map<String, Object> search(AccountSearchRequest request);
}
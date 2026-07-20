package cpf.acc.repository;

import cpf.acc.dto.AccountSearchRequest;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * MyBatis mapper 호출을 캡슐화하는 업무 저장소입니다.
 */
@Repository
public class AccountRepository {
    private final SqlSessionTemplate sqlSessionTemplate;

    public AccountRepository(
            @Qualifier("accSqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public Map<String, Object> search(AccountSearchRequest request) {
        return Map.of(
                "items", sqlSessionTemplate.selectList("cpf.acc.mapper.AccountMapper.search", request),
                "criteria", request);
    }
}

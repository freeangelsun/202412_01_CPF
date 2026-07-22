package com.cpf.account.reference.repository;

import com.cpf.account.reference.dto.AccountReferenceSearchRequest;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * MyBatis Mapper 호출을 캡슐화하는 ACC 기준 조회 저장소입니다.
 */
@Repository
public class AccountReferenceRepository {
    private final SqlSessionTemplate sqlSessionTemplate;

    public AccountReferenceRepository(
            @Qualifier("accSqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    public Map<String, Object> search(AccountReferenceSearchRequest request) {
        return Map.of(
                "items", sqlSessionTemplate.selectList(
                        "com.cpf.account.reference.mapper.AccountReferenceMapper.search", request),
                "criteria", request);
    }
}

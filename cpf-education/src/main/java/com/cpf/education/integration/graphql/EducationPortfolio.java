package com.cpf.education.integration.graphql;
import java.util.List;

/** GraphQL EDU에서 사용하는 Application 결과 DTO. */
public record EducationPortfolio(String id, String name, List<String> accountIds, List<String> accounts) {
    public EducationPortfolio { accountIds=List.copyOf(accountIds); accounts=List.copyOf(accounts); }
}

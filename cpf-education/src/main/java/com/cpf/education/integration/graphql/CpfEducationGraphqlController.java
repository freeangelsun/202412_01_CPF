package com.cpf.education.integration.graphql;
import com.cpf.platform.operations.api.annotation.CpfAudit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

/**
 * GraphQL BFF의 실제 Query/Mutation/Batch Resolver 예제다.
 * <p>REST와 동일한 {@link CpfEducationPortfolioService}를 사용하고 Resolver에는 업무 로직을 두지 않는다.</p>
 */
@Controller
@PreAuthorize("isAuthenticated()")
public class CpfEducationGraphqlController {
    private final CpfEducationPortfolioService service;

    public CpfEducationGraphqlController(CpfEducationPortfolioService service) {
        this.service = Objects.requireNonNull(service, "service");
    }

    @QueryMapping
    /** portfolio 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationPortfolio portfolio(@Argument String id) {
        return toGraphql(service.find(id));
    }

    @QueryMapping
    public String schemaVersion() {
        return "1";
    }

    @MutationMapping
    @CpfAudit(action = "EDUCATION_PORTFOLIO_RENAME", includeSafeResultSummary = true)
    /** renamePortfolio 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationPortfolio renamePortfolio(@Argument String id, @Argument String name) {
        return toGraphql(service.rename(id, name));
    }

    /**
     * Portfolio.accounts N+1을 막는 실제 BatchMapping consumer다.
     */
    @BatchMapping(typeName = "Portfolio", field = "accounts")
    public Map<EducationPortfolio, List<String>> accounts(List<EducationPortfolio> portfolios) {
        Objects.requireNonNull(portfolios, "portfolios");
        List<String> accountIds = portfolios.stream().flatMap(p -> p.accountIds().stream()).distinct().toList();
        Map<String, String> accountNames = service.accounts(accountIds);
        Map<EducationPortfolio, List<String>> result = new LinkedHashMap<>();
        for (EducationPortfolio portfolio : portfolios) {
            result.put(portfolio, portfolio.accountIds().stream()
                    .map(id -> accountNames.getOrDefault(id, "UNKNOWN"))
                    .toList());
        }
        return Map.copyOf(result);
    }

    private static EducationPortfolio toGraphql(CpfEducationPortfolioService.Portfolio portfolio) {
        return portfolio == null ? null : new EducationPortfolio(
                portfolio.id(), portfolio.name(), portfolio.accountIds(), List.of());
    }
}

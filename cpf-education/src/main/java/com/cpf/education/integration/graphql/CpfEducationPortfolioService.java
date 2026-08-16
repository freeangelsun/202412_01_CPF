package com.cpf.education.integration.graphql;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * REST와 GraphQL이 함께 사용하는 Education Application Service facade다.
 * Resolver/Controller가 업무 로직을 복제하지 않도록 실제 상태 변경은 내부 application service에 위임한다.
 */
@Service
public class CpfEducationPortfolioService {
    private final EducationPortfolioApplicationService delegate;

    public CpfEducationPortfolioService(EducationPortfolioApplicationService delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    /** find 작업을 CPF 표준 계약에 따라 수행한다. */
    public Portfolio find(String id) {
        return toPortfolio(delegate.find(id));
    }

    public Portfolio rename(String id, String name) {
        return toPortfolio(delegate.rename(id, name));
    }

    /** accounts 작업을 CPF 표준 계약에 따라 수행한다. */
    public Map<String, String> accounts(List<String> ids) {
        Objects.requireNonNull(ids, "ids");
        return ids.stream().filter(Objects::nonNull).distinct()
                .collect(Collectors.toUnmodifiableMap(id -> id, id -> "ACCOUNT-" + id));
    }

    private static Portfolio toPortfolio(EducationPortfolio source) {
        return source == null ? null : new Portfolio(source.id(), source.name(), source.accountIds());
    }

    /** Portfolio 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public record Portfolio(String id, String name, List<String> accountIds) {
        public Portfolio {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(name, "name");
            accountIds = List.copyOf(Objects.requireNonNull(accountIds, "accountIds"));
        }
    }
}

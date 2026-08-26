package com.cpf.data.persistence.jpa;

import com.cpf.data.persistence.api.CpfPersistencePolicy;
import com.cpf.data.persistence.api.CpfSearchSpec;
import jakarta.persistence.criteria.Path;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.jpa.domain.Specification;

/** Allow-list 기반 CPF Dynamic Condition을 JPA Specification으로 변환합니다. */
public final class CpfJpaSpecificationFactory {
    private CpfJpaSpecificationFactory() { }
    public static <T> Specification<T> from(CpfSearchSpec spec, Map<String,String> allowedFieldMappings) {
        Objects.requireNonNull(allowedFieldMappings,"allowedFieldMappings");
        CpfSearchSpec safe = CpfPersistencePolicy.requireAllowedFilters(spec, allowedFieldMappings.keySet());
        return (root, query, cb) -> {
            var predicates = safe.criteria().stream().map(c -> {
                Path<?> path = resolve(root, allowedFieldMappings.get(c.field()));
                Object value = c.values().isEmpty() ? null : c.values().get(0);
                return switch (c.operator()) {
                    case EQ -> cb.equal(path, value);
                    case NE -> cb.notEqual(path, value);
                    case GT -> compare(cb, path, value, CpfSearchSpec.Operator.GT);
                    case GE -> compare(cb, path, value, CpfSearchSpec.Operator.GE);
                    case LT -> compare(cb, path, value, CpfSearchSpec.Operator.LT);
                    case LE -> compare(cb, path, value, CpfSearchSpec.Operator.LE);
                    case LIKE -> cb.like(path.as(String.class), "%" + escapeLike(String.valueOf(value)) + "%", '\\');
                    case PREFIX -> cb.like(path.as(String.class), escapeLike(String.valueOf(value)) + "%", '\\');
                    case IN -> path.in(c.values());
                    case IS_NULL -> cb.isNull(path);
                    case IS_NOT_NULL -> cb.isNotNull(path);
                };
            }).toArray(jakarta.persistence.criteria.Predicate[]::new);
            return cb.and(predicates);
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static jakarta.persistence.criteria.Predicate compare(jakarta.persistence.criteria.CriteriaBuilder cb, Path<?> path, Object value, CpfSearchSpec.Operator operator) {
        if (!(value instanceof Comparable comparable)) throw new IllegalArgumentException("Comparable filter value is required");
        Class<? extends Comparable> valueType = comparable.getClass().asSubclass(Comparable.class);
        jakarta.persistence.criteria.Expression<? extends Comparable> expression = path.as(valueType);
        return switch (operator) {
            case GT -> cb.greaterThan(expression, comparable);
            case GE -> cb.greaterThanOrEqualTo(expression, comparable);
            case LT -> cb.lessThan(expression, comparable);
            case LE -> cb.lessThanOrEqualTo(expression, comparable);
            default -> throw new IllegalArgumentException("Unsupported comparison operator: " + operator);
        };
    }
    private static Path<?> resolve(Path<?> root, String mapped) {
        if (mapped == null || mapped.isBlank()) throw new IllegalArgumentException("JPA field mapping is required");
        Path<?> current = root;
        for (String segment : mapped.split("[.]")) current = current.get(segment);
        return current;
    }
    private static String escapeLike(String value) { return value.replace("\\","\\\\").replace("%","\\%").replace("_","\\_"); }
}

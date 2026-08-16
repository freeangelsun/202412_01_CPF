package com.cpf.data.persistence.jpa;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Objects;

/** EntityGraph 기반 N+1 방어 helper. 고급 fetch plan은 Native JPA API를 그대로 사용할 수 있습니다. */
public final class CpfJpaFetchPlans {
    private static final String FETCH_GRAPH = "jakarta.persistence.fetchgraph";
    private CpfJpaFetchPlans() { }

    public static Query applyNamedFetchGraph(EntityManager entityManager, Query query, String graphName) {
        Objects.requireNonNull(entityManager, "entityManager");
        Objects.requireNonNull(query, "query");
        if (graphName == null || graphName.isBlank()) throw new IllegalArgumentException("graphName is required");
        EntityGraph<?> graph = entityManager.getEntityGraph(graphName);
        return query.setHint(FETCH_GRAPH, graph);
    }
}

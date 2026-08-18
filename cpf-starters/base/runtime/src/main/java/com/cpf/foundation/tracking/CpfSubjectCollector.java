package com.cpf.foundation.tracking;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.tracking.CpfSubjectCandidate;
import com.cpf.core.api.tracking.CpfSubjectCandidateProvider;
import com.cpf.core.api.tracking.CpfSubjectTrackingOperations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Transaction Start/Identity Boundary가 공통으로 사용하는 Subject Collector입니다.
 * 값이 없어도 호출되며, Provider가 없는 비회원/비식별 거래는 정상 진행합니다.
 */
public final class CpfSubjectCollector {
    private final CpfSubjectTrackingOperations operations;
    private final List<CpfSubjectCandidateProvider> providers;

    public CpfSubjectCollector(CpfSubjectTrackingOperations operations, List<CpfSubjectCandidateProvider> providers) {
        this.operations = operations;
        this.providers = providers == null ? List.of() : List.copyOf(providers);
    }

    public void collect(CpfContext context) { collect(context, List.of()); }

    public void collect(CpfContext context, Collection<CpfSubjectCandidate> boundaryCandidates) {
        if (context == null || context.transactionId() == null || operations == null) return;
        ArrayList<CpfSubjectCandidate> candidates = new ArrayList<>();
        if (boundaryCandidates != null) candidates.addAll(boundaryCandidates);
        for (CpfSubjectCandidateProvider provider : providers) {
            List<CpfSubjectCandidate> provided = provider.candidates(context);
            if (provided != null) candidates.addAll(provided);
        }
        operations.collect(context.transactionId(), List.copyOf(candidates));
    }
}

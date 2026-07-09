package cpf.exs.edu.reconciliation;

import java.util.List;

/**
 * 대외기관 원장과 내부 원장을 비교할 대사 후보 샘플입니다.
 */
public class ExsReconciliationEducationSample {

    public List<String> mismatch(List<String> internalIds, List<String> externalIds) {
        return internalIds.stream()
                .filter(id -> !externalIds.contains(id))
                .toList();
    }
}

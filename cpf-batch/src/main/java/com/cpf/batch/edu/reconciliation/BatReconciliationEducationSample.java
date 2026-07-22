package cpf.bat.edu.reconciliation;

import java.util.List;

/**
 * 실패/불명확 데이터 재처리 후보를 산출하는 샘플입니다.
 */
public class BatReconciliationEducationSample {

    public List<String> candidates(List<String> failedIds, List<String> unknownIds) {
        return java.util.stream.Stream.concat(failedIds.stream(), unknownIds.stream())
                .distinct()
                .toList();
    }
}

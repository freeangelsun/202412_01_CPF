package cpf.bat.edu.transaction;

import java.util.List;

/**
 * 부분 실패 시 성공/실패 데이터를 분리하는 샘플입니다.
 */
public class BatPartialRollbackEducationSample {

    public PartialRollbackPlan split(List<String> successIds, List<String> failedIds) {
        return new PartialRollbackPlan(List.copyOf(successIds), List.copyOf(failedIds), !failedIds.isEmpty());
    }

    public record PartialRollbackPlan(List<String> committedIds, List<String> rollbackIds, boolean hasRollback) {
    }
}

package cpf.bat.edu.centercut;

import java.util.List;

/**
 * center-cut 대상 조회 결과를 표현하는 샘플입니다.
 */
public class BatCenterCutTargetEducationSample {

    public List<String> selectTargets(List<String> candidateIds, int limit) {
        return candidateIds.stream().limit(limit).toList();
    }
}

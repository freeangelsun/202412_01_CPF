package cpf.xyz.edu.query;

import java.util.List;

/**
 * 검색 조건과 정렬 조건을 분리하는 목록 조회 샘플입니다.
 */
public class XyzQueryEducationSample {

    public List<String> filterByStatus(List<String> rows, String status) {
        return rows.stream()
                .filter(row -> row.endsWith(":" + status))
                .sorted()
                .toList();
    }
}

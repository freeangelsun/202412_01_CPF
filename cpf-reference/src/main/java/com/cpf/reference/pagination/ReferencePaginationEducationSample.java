package cpf.xyz.pagination;

import java.util.List;

/**
 * offset/keyset 조회 차이를 보여주는 pagination 샘플입니다.
 */
public class XyzPaginationEducationSample {

    public List<Integer> offsetPage(List<Integer> ids, int offset, int size) {
        return ids.stream().skip(offset).limit(size).toList();
    }

    public List<Integer> keysetPage(List<Integer> ids, int lastSeenId, int size) {
        return ids.stream().filter(id -> id > lastSeenId).limit(size).toList();
    }
}

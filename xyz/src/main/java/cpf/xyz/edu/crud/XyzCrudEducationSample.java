package cpf.xyz.edu.crud;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 온라인 업무 CRUD 처리 흐름을 보여주는 교육 샘플입니다.
 */
public class XyzCrudEducationSample {
    private final Map<String, String> store = new LinkedHashMap<>();

    public String create(String id, String name) {
        validate(id, name);
        store.put(id, name);
        return id;
    }

    public String update(String id, String name) {
        validate(id, name);
        if (!store.containsKey(id)) {
            throw new IllegalArgumentException("수정 대상이 없습니다.");
        }
        store.put(id, name);
        return name;
    }

    public String find(String id) {
        return store.get(id);
    }

    private void validate(String id, String name) {
        if (id == null || id.isBlank() || name == null || name.isBlank()) {
            throw new IllegalArgumentException("id와 name은 필수입니다.");
        }
    }
}

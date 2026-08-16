package com.cpf.education.security;
import java.util.Set;

/**
 * API/버튼 권한을 업무 코드에서 확인하는 샘플입니다.
 */
public class EducationApiPermissionEducationSample {

    public boolean allowed(Set<String> permissions, String action) {
        return permissions.contains(action);
    }
}

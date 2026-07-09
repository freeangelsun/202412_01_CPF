package cpf.bizadm.edu.api;

import java.util.Set;

/**
 * API 권한 판단 샘플입니다.
 */
public class BizAdmApiPermissionEducationSample {

    public boolean allowed(Set<String> apiPermissions, String httpMethod, String apiPath) {
        return apiPermissions.contains(httpMethod + " " + apiPath);
    }
}

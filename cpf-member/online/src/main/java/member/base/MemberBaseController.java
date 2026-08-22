package member.base;

import com.cpf.web.api.CpfBaseController;

/** member Web API의 Domain 공통 정책과 응답 helper를 제공하는 2단계 Base Controller입니다. */
public abstract class MemberBaseController extends CpfBaseController {
    protected static final String DOMAIN_NAME = "member";
    protected static final String SYSTEM_CODE = "MBR";
    protected final int normalizePageSize(Integer requested) {
        if (requested == null || requested <= 0) return 20;
        requireRule(requested <= 200, "size는 200 이하여야 합니다.");
        return requested;
    }
}

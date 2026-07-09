package cpf.bizadm.edu.audit;

import cpf.pfw.common.audit.PfwAuditContextEducationSample;

/**
 * 권한/설정 변경 감사 사유와 diff를 남기는 샘플입니다.
 */
public class BizAdmAuditEducationSample {

    public PfwAuditContextEducationSample.AuditContext audit(String actorId, String reason) {
        return new PfwAuditContextEducationSample().changed(actorId, reason);
    }
}

package cpf.bizadm.edu.masking;

import cpf.pfw.common.masking.PfwMaskingPolicyEducationSample;

/**
 * 관리자 화면에서 개인정보를 마스킹해서 보여주는 샘플입니다.
 */
public class BizAdmMaskingPolicyEducationSample {

    public String email(String email) {
        return new PfwMaskingPolicyEducationSample().maskEmail(email);
    }
}

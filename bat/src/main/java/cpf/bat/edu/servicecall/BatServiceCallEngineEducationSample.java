package cpf.bat.edu.servicecall;

import cpf.pfw.common.servicecall.PfwServiceCallEducationSample;

/**
 * BAT가 PFW Service Call Engine 정책을 재사용하는 샘플입니다.
 */
public class BatServiceCallEngineEducationSample {

    public PfwServiceCallEducationSample.CallPlan buildMemberCallPlan() {
        return new PfwServiceCallEducationSample().buildPlan("MBR", "member-grade-summary");
    }
}

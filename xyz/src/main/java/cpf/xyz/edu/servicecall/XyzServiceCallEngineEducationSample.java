package cpf.xyz.edu.servicecall;

import cpf.pfw.common.servicecall.PfwServiceCallEducationSample;

/**
 * XYZ가 PFW Service Call Engine 정책을 통해 타 주제영역을 호출하는 샘플입니다.
 */
public class XyzServiceCallEngineEducationSample {

    public PfwServiceCallEducationSample.CallPlan buildAccountCallPlan() {
        return new PfwServiceCallEducationSample().buildPlan("ACC", "account-summary");
    }
}

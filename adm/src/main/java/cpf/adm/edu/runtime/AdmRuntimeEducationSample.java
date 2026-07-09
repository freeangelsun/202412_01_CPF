package cpf.adm.edu.runtime;

import cpf.pfw.common.runtime.PfwRuntimeEducationSample;

/**
 * ADM runtime 상태 조회가 PFW runtime contract를 사용하는 샘플입니다.
 */
public class AdmRuntimeEducationSample {

    public PfwRuntimeEducationSample.RuntimeStatus status(String moduleId, String instanceId) {
        return new PfwRuntimeEducationSample().heartbeat(moduleId, instanceId);
    }
}

package cpf.bat.edu.servicecall;

import cpf.pfw.common.servicecall.CpfServiceCallEngine;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import cpf.pfw.common.servicecall.ServiceCallResult;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class BatServiceCallEngineEducationSampleTest {

    @Test
    void serviceCallSampleInvokesActualPfwEngine() {
        CpfServiceCallEngine engine = mock(CpfServiceCallEngine.class);
        ServiceCallResult<String> expected = ServiceCallResult.success(null, "GOLD", 200, 1L, 1);
        when(engine.invoke(any(ServiceCallRequest.class), any(Function.class))).thenReturn(expected);
        BatServiceCallEngineEducationSample sample = new BatServiceCallEngineEducationSample(engine);

        ServiceCallResult<String> result = sample.callMemberGrade("M-1", target -> "GOLD");

        assertThat(result).isSameAs(expected);
        verify(engine).invoke(any(ServiceCallRequest.class), any(Function.class));
    }
}

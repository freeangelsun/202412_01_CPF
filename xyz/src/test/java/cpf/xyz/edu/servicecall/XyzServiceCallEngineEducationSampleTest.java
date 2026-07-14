package cpf.xyz.edu.servicecall;

import cpf.pfw.common.servicecall.CpfServiceCallEngine;
import cpf.pfw.common.servicecall.ServiceCallRequest;
import cpf.pfw.common.servicecall.ServiceCallResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class XyzServiceCallEngineEducationSampleTest {

    @Test
    void serviceCallSampleInvokesActualPfwEngine() {
        CpfServiceCallEngine engine = mock(CpfServiceCallEngine.class);
        ServiceCallResult<String> expected = ServiceCallResult.success(null, "OK", 200, 1L, 1);
        when(engine.invoke(any(ServiceCallRequest.class), any(Function.class))).thenReturn(expected);
        XyzServiceCallEngineEducationSample sample = new XyzServiceCallEngineEducationSample(engine);

        ServiceCallResult<String> result = sample.callAccountSummary("A-1", target -> "OK");

        ArgumentCaptor<ServiceCallRequest> requestCaptor = ArgumentCaptor.forClass(ServiceCallRequest.class);
        verify(engine).invoke(requestCaptor.capture(), any(Function.class));
        assertThat(result).isSameAs(expected);
        assertThat(requestCaptor.getValue().serviceId()).isEqualTo("ACC");
        assertThat(requestCaptor.getValue().attributes()).containsEntry("sourceModuleCode", "XYZ");
    }
}

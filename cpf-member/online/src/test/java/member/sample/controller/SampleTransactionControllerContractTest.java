package member.sample.controller;

import member.base.MemberBaseController;
import com.cpf.web.api.CpfController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/** Generated API가 CPF 3단 Base와 업무 Operation Identity 계약을 유지하는지 검증합니다. */
class SampleTransactionControllerContractTest {
    @Test void keepsThreeLayerControllerAndCpfAnnotation() {
        assertThat(MemberBaseController.class.getSuperclass().getSimpleName()).isEqualTo("CpfBaseController");
        assertThat(SampleTransactionController.class.getSuperclass()).isEqualTo(MemberBaseController.class);
        assertThat(SampleTransactionController.class.getAnnotation(CpfController.class)).isNotNull();
    }

    @Test void keepsCpfAndOpenApiOperationIdentityAligned() {
        Set<String> operationIds=new HashSet<>();
        for (Method method : SampleTransactionController.class.getDeclaredMethods()) {
            CpfOnlineTransaction cpf=method.getAnnotation(CpfOnlineTransaction.class);
            if (cpf == null) continue;
            Operation openApi=method.getAnnotation(Operation.class);
            assertThat(openApi).as(method.getName()+" OpenAPI annotation").isNotNull();
            assertThat(cpf.operationId()).isEqualTo(openApi.operationId());
            assertThat(cpf.name()).isNotBlank();
            assertThat(cpf.description()).isNotBlank();
            assertThat(operationIds.add(cpf.operationId())).as("unique operationId "+cpf.operationId()).isTrue();
        }
        assertThat(operationIds).containsExactlyInAnyOrder(
            "MBR_SAMPLE_TX_CREATE", "MBR_SAMPLE_TX_DETAIL", "MBR_SAMPLE_TX_SEARCH",
            "MBR_SAMPLE_TX_SLICE", "MBR_SAMPLE_TX_UPDATE", "MBR_SAMPLE_TX_DELETE");
    }
}

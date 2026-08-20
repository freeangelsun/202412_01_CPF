package external.online.external.controller;

import external.online.base.ExternalBaseController;
import com.cpf.web.api.CpfRestController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/** Generated API가 CPF 3단 Base와 Controller Annotation 계약을 유지하는지 검증합니다. */
class SampleTransactionControllerContractTest {
    @Test void keepsThreeLayerControllerAndCpfAnnotation() {
        assertThat(ExternalBaseController.class.getSuperclass().getSimpleName()).isEqualTo("CpfBaseController");
        assertThat(SampleTransactionController.class.getSuperclass()).isEqualTo(ExternalBaseController.class);
        assertThat(SampleTransactionController.class.getAnnotation(CpfRestController.class)).isNotNull();
    }
}

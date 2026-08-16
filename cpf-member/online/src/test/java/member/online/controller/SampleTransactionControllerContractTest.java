package member.online.controller;

import member.online.base.MemberBaseController;
import com.cpf.web.api.CpfController;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/** Generated API가 CPF 3단 Base와 Controller Annotation 계약을 유지하는지 검증합니다. */
class SampleTransactionControllerContractTest {
    @Test void keepsThreeLayerControllerAndCpfAnnotation() {
        assertThat(MemberBaseController.class.getSuperclass().getSimpleName()).isEqualTo("CpfBaseController");
        assertThat(SampleTransactionController.class.getSuperclass()).isEqualTo(MemberBaseController.class);
        assertThat(SampleTransactionController.class.getAnnotation(CpfController.class)).isNotNull();
    }
}

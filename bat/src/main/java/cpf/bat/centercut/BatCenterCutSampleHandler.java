package cpf.bat.centercut;

import cpf.pfw.common.batch.centercut.CenterCutHandler;
import cpf.pfw.common.batch.centercut.CpfCenterCutResult;
import cpf.pfw.common.batch.centercut.CpfCenterCutTarget;
import org.springframework.stereotype.Component;

/**
 * center-cut smoke 대상 하나를 처리하는 학습용 handler입니다.
 */
@Component
public class BatCenterCutSampleHandler implements CenterCutHandler {

    @Override
    public CpfCenterCutResult handle(CpfCenterCutTarget target) {
        String resultPayload = "{\"businessKey\":\"" + target.businessKey() + "\",\"processed\":true}";
        return CpfCenterCutResult.success(
                target,
                "center-cut sample 대상 처리 완료",
                resultPayload,
                target.childTransactionGlobalId());
    }
}

package com.cpf.batch.job.centercut;

import com.cpf.core.common.batch.centercut.CenterCutHandler;
import com.cpf.core.common.batch.centercut.CpfCenterCutResult;
import com.cpf.core.common.batch.centercut.CpfCenterCutTarget;
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

package cpf.xyz.centercut;

import cpf.pfw.common.batch.centercut.CenterCutHandler;
import cpf.pfw.common.batch.centercut.CpfCenterCutResult;
import cpf.pfw.common.batch.centercut.CpfCenterCutTarget;
import org.springframework.stereotype.Component;

/**
 * XYZ 업무 대상 한 건을 center-cut으로 처리하는 교육용 handler입니다.
 *
 * <p>실제 업무에서는 이 위치에 결제, 정산, 통지, 대외 연계 같은 업무 로직을 둡니다.
 * EDU 샘플은 성공/실패 흐름을 모두 보여주기 위해 payload의 {@code forceFail} 값으로 실패 건을 만듭니다.</p>
 */
@Component("xyzCenterCutHandler")
public class XyzCenterCutHandler implements CenterCutHandler {

    @Override
    public CpfCenterCutResult handle(CpfCenterCutTarget target) {
        if (target.payload() != null && target.payload().contains("\"forceFail\":true")) {
            return CpfCenterCutResult.failed(
                    target,
                    "XYZ center-cut 샘플에서 의도된 실패가 발생했습니다.",
                    "{\"businessKey\":\"" + target.businessKey() + "\",\"processed\":false}",
                    target.childTransactionGlobalId());
        }

        return CpfCenterCutResult.success(
                target,
                "XYZ center-cut 업무 대상 처리 완료",
                "{\"businessKey\":\"" + target.businessKey() + "\",\"processed\":true}",
                target.childTransactionGlobalId());
    }
}

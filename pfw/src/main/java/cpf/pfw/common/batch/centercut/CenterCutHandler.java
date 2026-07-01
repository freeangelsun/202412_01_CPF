package cpf.pfw.common.batch.centercut;

/**
 * center-cut 단일 대상을 실제 업무 로직으로 처리하는 adapter 계약입니다.
 */
public interface CenterCutHandler {

    CpfCenterCutResult handle(CpfCenterCutTarget target);
}

package com.cpf.core.spi.centercut;

import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.core.api.centercut.CpfCenterCutTarget;

/**
 * center-cut 단일 대상을 실제 업무 로직으로 처리하는 adapter 계약입니다.
 */
public interface CenterCutHandler {

    CpfCenterCutResult handle(CpfCenterCutTarget target);
}

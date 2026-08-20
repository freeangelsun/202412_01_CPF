package com.cpf.common.template.api;

import java.util.Map;

/**
 * 고객 업무에서 Versioned Template을 안전하게 렌더링하는 CPF Common Template 공개 API입니다.
 *
 * <p>Template 저장소, 활성 Version 선택, Rendering 구현은 Framework가 소유하며 고객 Source는
 * templateCode/channel/typed variables만 전달합니다.</p>
 */
public interface CpfTemplateService {
    /** 활성 Template Version을 조회해 변수를 적용한 최종 Content를 반환합니다. */
    String render(String templateCode, String channel, Map<String, ?> variables);
}

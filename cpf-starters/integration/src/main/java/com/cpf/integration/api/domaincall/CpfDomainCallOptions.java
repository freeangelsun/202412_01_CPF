package com.cpf.integration.api.domaincall;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed Domain 호출의 선택적 사용자 Header 옵션입니다.
 * Canonical/보호 Header는 transport boundary에서 덮어쓰기가 차단됩니다.
 */
public record CpfDomainCallOptions(Map<String, String> headers) {
    public CpfDomainCallOptions {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
    }

    public static CpfDomainCallOptions none() { return new CpfDomainCallOptions(Map.of()); }

    /** 보호 Header 위조를 차단하는 Custom Header Builder를 생성합니다. */
    public static Builder builder() { return new Builder(); }

    /** 업무 Custom Header만 안전하게 추가·삭제하고 CPF 보호 Header는 변경하지 못하게 하는 Builder입니다. */
    public static final class Builder {
        private final Map<String, String> headers = new LinkedHashMap<>();
        /** Domain 호출에 전달할 허용 Custom Header를 추가합니다. */
        public Builder header(String name, String value) {
            if (name != null && !name.isBlank() && value != null) headers.put(name, value);
            return this;
        }
        /** 설정된 값으로 변경 불가 호출 옵션을 생성합니다. */
        public CpfDomainCallOptions build() { return new CpfDomainCallOptions(headers); }
    }
}

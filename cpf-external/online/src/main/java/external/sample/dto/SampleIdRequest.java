package external.sample.dto;

import com.cpf.core.api.base.CpfRequest;

/** Path ID를 Typed Domain Call에서도 사용하는 명시적 요청 계약입니다. */
public record SampleIdRequest(long id) implements CpfRequest { }

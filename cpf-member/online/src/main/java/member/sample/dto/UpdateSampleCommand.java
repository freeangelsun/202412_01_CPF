package member.sample.dto;

import com.cpf.core.api.base.CpfRequest;

/** Update의 Path ID와 Body를 하나의 Typed Domain Call 요청으로 묶습니다. */
public record UpdateSampleCommand(long id, UpdateSampleRequest request) implements CpfRequest { }

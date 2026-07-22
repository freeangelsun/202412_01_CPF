package cpf.cmn.dto;

import jakarta.validation.Valid;
import lombok.Data;

/**
 * CPF 표준 요청 DTO입니다.
 *
 * <p>모든 거래 요청에서 헤더와 업무 본문을 같은 구조로 주고받기 위한 공통 래퍼입니다.</p>
 *
 * @param <T> 업무 본문 타입
 */
@Data
public class CpfDTO<T> {

    @Valid
    private HeaderDTO header;

    @Valid
    private DataDTO<T> data;
}


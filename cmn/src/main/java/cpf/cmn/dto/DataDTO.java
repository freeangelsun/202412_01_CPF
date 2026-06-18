package cpf.cmn.dto;

import jakarta.validation.Valid;
import lombok.Data;

/**
 * CPF 표준 본문 DTO입니다.
 *
 * <p>업무 요청 또는 응답의 실제 본문을 담는 공통 래퍼입니다.</p>
 *
 * @param <T> 업무 본문 타입
 */
@Data
public class DataDTO<T> {
    @Valid
    private T body;
}


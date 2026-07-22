package cpf.cmn.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 표준 거래 헤더 DTO입니다.
 *
 * <p>모든 거래에서 공통으로 필요한 거래 ID, 최초 채널, 현재 채널, 요청 시각을 담습니다.</p>
 */
@Data
public class HeaderDTO {
    @NotEmpty(message = "Transaction ID는 필수 값입니다.")
    private String transactionId;

    @NotEmpty(message = "Initial Channel Code는 필수 값입니다.")
    private String initialChannelCode;

    @NotEmpty(message = "Channel Code는 필수 값입니다.")
    private String channelCode;

    @NotNull(message = "Timestamp는 필수 값입니다.")
    private Long timestamp;
}


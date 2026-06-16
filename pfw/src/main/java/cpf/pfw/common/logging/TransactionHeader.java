package cpf.pfw.common.logging;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TransactionHeader {
    String requestType;
    String originalChannelCode;
    String channelCode;
    String memberNo;
    String customerNo;
    String userId;
    String screenId;
    String deviceId;
    String clientRequestTime;
    String clientIp;
    String reservedField1;
    String reservedField2;
    String reservedField3;
    String reservedField4;
    String reservedField5;
    String wasId;
}


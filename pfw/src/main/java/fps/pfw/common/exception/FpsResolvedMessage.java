package fps.pfw.common.exception;

/**
 * 메시지 저장소에서 해석된 고객용/내부용 메시지 묶음입니다.
 */
public record FpsResolvedMessage(String externalMessage, String internalMessage) {
}

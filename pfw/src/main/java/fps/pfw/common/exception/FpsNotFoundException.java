package fps.pfw.common.exception;

/**
 * 조회 대상이 없을 때 사용하는 표준 예외입니다.
 */
public class FpsNotFoundException extends FpsException {
    public FpsNotFoundException(String detail) {
        super(FpsErrorCode.NOT_FOUND, detail);
    }
}

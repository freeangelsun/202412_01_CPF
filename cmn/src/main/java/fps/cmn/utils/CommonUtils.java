package fps.cmn.utils;

/**
 * CMN 공통 유틸 진입점 성격의 클래스입니다.
 *
 * <p>기존 샘플 호환성을 위해 남겨두되, 신규 코드는 기능별 유틸 클래스
 * {@link TextUtils}, {@link DateTimeUtils}, {@link IdUtils}, {@link MaskingUtils},
 * {@link CollectionSafeUtils}를 직접 사용하는 것을 권장합니다.</p>
 */
public class CommonUtils {

    /**
     * CMN 모듈 연결 여부를 간단히 확인하는 샘플 메서드입니다.
     *
     * @return CMN 모듈 인사말
     */
    public static String sayHello() {
        return "Hello from Common Module!";
    }
}

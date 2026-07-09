package cpf.cmn.edu.file;

import java.nio.file.Path;

/**
 * 공통 파일명/경로 규칙 helper 샘플입니다.
 */
public class CmnFileRuleEducationSample {

    public Path safeBusinessPath(Path baseDirectory, String businessKey, String fileName) {
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("파일명에는 경로 구분자를 사용할 수 없습니다.");
        }
        return baseDirectory.resolve(businessKey).resolve(fileName).normalize();
    }
}

package com.cpf.file.api.util;
import com.cpf.foundation.util.CpfStrings;

import java.nio.file.Path;

/** Path Traversal을 줄이는 파일명/하위경로 편의 API입니다. */
public final class CpfFiles {
    private CpfFiles() {}
    /** 단일 파일명만 허용하고 path traversal 요소를 거부합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     * @throws IllegalArgumentException blank이거나 경로 구분자/점 경로가 포함된 경우
     */
    public static String safeFileName(String value) {
        String fileName = CpfStrings.requireText(value, "fileName").replace('\\', '/');
        if (fileName.contains("/") || fileName.equals(".") || fileName.equals("..")) {
            throw new IllegalArgumentException("경로가 포함된 fileName은 허용하지 않습니다.");
        }
        return fileName;
    }
    /** 검증된 파일명의 소문자 확장자를 반환합니다.
     * @param value 입력 값. 각 메서드의 nullable 규칙을 따릅니다.
     * @return 정규화/변환된 문자열 또는 계약상 null
     * @throws IllegalArgumentException 안전하지 않은 파일명인 경우
     */
    public static String extension(String value) {
        String fileName = safeFileName(value);
        int index = fileName.lastIndexOf('.');
        return index <= 0 || index == fileName.length() - 1 ? "" : fileName.substring(index + 1).toLowerCase();
    }
    /** 허용 Root 아래의 정규화된 child 경로만 반환합니다.
     * @param root 허용 기준 Root 경로
     * @param child Root 아래에서 해석할 상대 경로
     * @return 계약에 따른 결과 값
     * @throws NullPointerException root가 null인 경우
     * @throws IllegalArgumentException 정규화 결과가 Root 밖을 가리키는 경우
     */
    public static Path resolveChild(Path root, String child) {
        Path base = root.toAbsolutePath().normalize();
        Path resolved = base.resolve(child).normalize();
        if (!resolved.startsWith(base)) throw new IllegalArgumentException("허용 Root 밖의 경로는 사용할 수 없습니다.");
        return resolved;
    }
}

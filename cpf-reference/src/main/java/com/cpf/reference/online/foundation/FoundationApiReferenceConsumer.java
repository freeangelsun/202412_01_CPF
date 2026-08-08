package com.cpf.reference.online.foundation;

import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.util.CpfFiles;
import com.cpf.core.api.util.CpfHeaders;
import com.cpf.core.api.util.CpfIds;
import com.cpf.core.api.util.CpfPages;
import com.cpf.core.api.util.CpfValidation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * CPF 공개 Foundation API를 실제 downstream 모듈에서 조합하는 실행 가능한 reference consumer입니다.
 * Core utility를 다시 구현하지 않고 Paging/File/Validation/ID/Header 계약을 동일 호출 경로에서 사용합니다.
 */
public final class FoundationApiReferenceConsumer {
    /**
     * 검색 결과를 공통 Paging 계약으로 변환합니다.
     * @param source nullable 전체 목록
     * @param page 0 이상의 page
     * @param size 1~200 size
     * @return CPF 표준 page
     * @throws IllegalArgumentException page/size가 공통 범위를 벗어난 경우
     * @throws ArithmeticException offset이 int 범위를 넘는 경우
     */
    public <T> CpfPage<T> page(List<T> source, int page, int size) {
        CpfPageRequest request = CpfPages.request(page, size);
        return CpfPages.offsetPage(source, request);
    }

    /**
     * 업로드 파일명을 검증하고 지정 root 아래 경로로만 해석합니다.
     * @param root 허용 파일 root
     * @param fileName 사용자 입력 파일명
     * @return root 아래 정규화 경로
     * @throws IllegalArgumentException path traversal 또는 blank 파일명인 경우
     */
    public Path uploadTarget(Path root, String fileName) {
        return CpfFiles.resolveChild(root, CpfFiles.safeFileName(fileName));
    }

    /**
     * System code를 검증한 뒤 임시 기술 식별자를 생성합니다.
     * @param systemCode 영문/숫자 3자리 System code
     * @return 거래 ID와 별도인 임시 ID
     * @throws IllegalArgumentException systemCode가 규칙에 맞지 않는 경우
     */
    public String temporaryId(String systemCode) {
        return CpfIds.temporaryId(CpfIds.normalizeSystemCode(systemCode));
    }

    /**
     * canonical transactionId를 검증해 downstream 전파 Header를 만듭니다.
     * @param transactionId CPF canonical transactionId
     * @return immutable 표준 Header Map
     * @throws IllegalArgumentException transactionId가 canonical 형식이 아닌 경우
     */
    public Map<String, String> transactionHeaders(String transactionId) {
        CpfValidation.requireText(transactionId, "transactionId");
        return CpfHeaders.transaction(transactionId);
    }
}

package com.cpf.core.api.archive;

/** CPF 기본 archive 구현을 공개 계약 뒤에서 생성하는 stateless factory입니다. */
public final class CpfArchives {
    private CpfArchives() {
    }

    /**
     * 로컬 파일시스템 archive adapter를 반환합니다.
     *
     * <p>업무 코드는 반환된 {@link CpfArchiveService} 계약에만 의존합니다.</p>
     */
    public static CpfArchiveService local() {
        return new com.cpf.core.common.archive.LocalCpfArchiveService();
    }
}

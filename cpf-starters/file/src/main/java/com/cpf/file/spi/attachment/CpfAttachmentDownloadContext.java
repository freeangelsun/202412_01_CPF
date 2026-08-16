package com.cpf.file.spi.attachment;

import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 첨부파일 Adapter와 워터마크 구현이 함께 사용하는 Provider-neutral 다운로드 권한 Context입니다.
 *
 * <p>다운로드 처리 범위 안에서만 운영자·승인·만료 정보를 ThreadLocal로 전달하고 종료 시 이전 값을 복원합니다.</p>
 */
public final class CpfAttachmentDownloadContext {
    private static final ThreadLocal<Context> CURRENT = new ThreadLocal<>();

    private CpfAttachmentDownloadContext() {
    }

    /** 권한 Context를 현재 실행 범위에 연결하고 작업 종료 시 기존 Context를 복원합니다. */
    public static <T> T with(Context context, Supplier<T> action) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(action, "action");
        Context previous = CURRENT.get();
        CURRENT.set(context);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }

    /** 현재 다운로드 실행 범위의 권한 Context를 조회합니다. */
    public static Context current() {
        return CURRENT.get();
    }

    /** 다운로드 요청의 운영자·권한·승인·유효시간을 나타내는 불변 값입니다. */
    public record Context(
            String operatorId,
            boolean permitted,
            String approvalId,
            Instant requestedAt,
            Instant expiresAt) {
        public Context {
            requestedAt = requestedAt == null ? Instant.now() : requestedAt;
        }
    }
}

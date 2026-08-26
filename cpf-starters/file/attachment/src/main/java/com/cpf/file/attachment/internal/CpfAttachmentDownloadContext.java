package com.cpf.file.attachment.internal;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * @deprecated use the provider-neutral {@code com.cpf.file.spi.attachment} context.
 */
@Deprecated(forRemoval = false)
public final class CpfAttachmentDownloadContext {
    private CpfAttachmentDownloadContext() {
    }

    @Deprecated
    public static <T> T with(Context context, Supplier<T> action) {
        return com.cpf.file.spi.attachment.CpfAttachmentDownloadContext.with(context.toApi(), action);
    }

    @Deprecated
    public static Context current() {
        com.cpf.file.spi.attachment.CpfAttachmentDownloadContext.Context context =
                com.cpf.file.spi.attachment.CpfAttachmentDownloadContext.current();
        return context == null ? null : Context.fromApi(context);
    }

    @Deprecated
    public record Context(
            String operatorId,
            boolean permitted,
            String approvalId,
            Instant requestedAt,
            Instant expiresAt) {
        private com.cpf.file.spi.attachment.CpfAttachmentDownloadContext.Context toApi() {
            return new com.cpf.file.spi.attachment.CpfAttachmentDownloadContext.Context(
                    operatorId, permitted, approvalId, requestedAt, expiresAt);
        }

        private static Context fromApi(
                com.cpf.file.spi.attachment.CpfAttachmentDownloadContext.Context context) {
            return new Context(
                    context.operatorId(),
                    context.permitted(),
                    context.approvalId(),
                    context.requestedAt(),
                    context.expiresAt());
        }
    }
}

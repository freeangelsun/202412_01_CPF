package com.cpf.file.common.archive;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/** Resolves archive entries only inside the allowed extraction root. */
final class CpfZipSlipGuard {
    private CpfZipSlipGuard() {}

    static Path safeResolve(Path baseDirectory, String entryName) throws IOException {
        Path base = Objects.requireNonNull(baseDirectory, "baseDirectory").toAbsolutePath().normalize();
        if (entryName == null || entryName.isBlank()) throw new IOException("archive entry name is required");
        Path relative = Path.of(entryName.replace('\\', '/'));
        if (relative.isAbsolute()) throw new IOException("absolute archive entry is not allowed");
        Path resolved = base.resolve(relative).normalize();
        if (!resolved.startsWith(base)) throw new IOException("archive entry escapes allowed base directory");
        return resolved;
    }
}

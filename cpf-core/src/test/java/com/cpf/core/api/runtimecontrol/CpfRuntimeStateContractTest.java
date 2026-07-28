package com.cpf.core.api.runtimecontrol;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfRuntimeStateContractTest {
    @Test
    void canonicalSchemaAndJavaEnumsMustMatch() throws IOException {
        String canonical = Files.readString(findCanonicalSchema());
        assertEquals(names(CpfRuntimeChangeState.values()), constraint(canonical, "change_state"));
        assertEquals(names(CpfRuntimeDeliveryState.values()), constraint(canonical, "delivery_state"));
        assertEquals(names(CpfRuntimeDriftState.values()), constraint(canonical, "drift_state"));
    }

    @Test
    void apiRejectsUnknownStateBeforePersistenceOrResponse() {
        assertThrows(IllegalArgumentException.class, () -> CpfRuntimeChangeState.require("DONE"));
        assertThrows(IllegalArgumentException.class, () -> CpfRuntimeAckState.require("RETRYING"));
    }

    private Path findCanonicalSchema() {
        Path cwd = Path.of("").toAbsolutePath().normalize();
        for (Path candidate : new Path[]{
                cwd.resolve("cpf-tools/db/canonical/platform-schema.json"),
                cwd.resolve("../cpf-tools/db/canonical/platform-schema.json").normalize(),
                cwd.resolve("../../cpf-tools/db/canonical/platform-schema.json").normalize()}) {
            if (Files.isRegularFile(candidate)) return candidate;
        }
        throw new IllegalStateException("platform-schema.json을 찾을 수 없습니다. cwd=" + cwd);
    }

    private Set<String> constraint(String canonical, String column) {
        Pattern pattern = Pattern.compile(
                "\"expression\"\\s*:\\s*\"" + Pattern.quote(column + " IN (") +
                        "([^\"]+)" + Pattern.quote(")") + "\"");
        Matcher matcher = pattern.matcher(canonical);
        if (!matcher.find()) throw new IllegalStateException(column + " check constraint를 찾을 수 없습니다.");
        LinkedHashSet<String> result = new LinkedHashSet<>();
        Matcher value = Pattern.compile("'([A-Z_]+)'").matcher(matcher.group(1));
        while (value.find()) result.add(value.group(1));
        return result;
    }

    private Set<String> names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

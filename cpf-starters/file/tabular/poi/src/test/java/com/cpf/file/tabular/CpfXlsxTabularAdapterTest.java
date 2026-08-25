package com.cpf.file.tabular;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.file.tabular.api.CpfTabularColumn;
import com.cpf.file.tabular.api.CpfTabularFormat;
import com.cpf.file.tabular.api.CpfTabularReadRequest;
import com.cpf.file.tabular.api.CpfTabularReadResult;
import com.cpf.file.tabular.api.CpfTabularRow;
import com.cpf.file.tabular.api.CpfTabularSchema;
import com.cpf.file.tabular.api.CpfTabularWriteRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class CpfXlsxTabularAdapterTest {
    private final CpfTabularSchema schema = new CpfTabularSchema(
            "TEST", 1,
            List.of(
                    new CpfTabularColumn(
                            "id", "ID", CpfTabularColumn.Type.INTEGER, true, 10, false),
                    new CpfTabularColumn(
                            "name", "NAME", CpfTabularColumn.Type.STRING, true, 20, false)),
            100, 100);

    @Test
    void streamingRoundTripUsesCloseOwnedWorkbookLifecycle() {
        CpfXlsxTabularAdapter adapter = new CpfXlsxTabularAdapter();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        String writeDigest = adapter.write(new CpfTabularWriteRequest(
                CpfTabularFormat.XLSX,
                schema,
                Stream.of(new CpfTabularRow(1, Map.of("id", "1", "name", "sample"))),
                output,
                true));
        List<CpfTabularRow> rows = new ArrayList<>();
        CpfTabularReadResult result = adapter.read(new CpfTabularReadRequest(
                CpfTabularFormat.XLSX,
                schema,
                new ByteArrayInputStream(output.toByteArray()),
                true,
                true), rows::add);

        assertThat(writeDigest).hasSize(64);
        assertThat(result.sha256()).hasSize(64);
        assertThat(result.acceptedRows()).isEqualTo(1);
        assertThat(result.rejectedRows()).isZero();
        // Adapter는 값 Map을 Schema Column 순서대로 LinkedHashMap에 채우므로 순서까지 계약이다.
        // 기대값에 Map.of를 쓰면 JVM 실행마다 반복 순서가 무작위로 바뀌어 테스트가 flaky해지므로
        // 순서가 보장되는 LinkedHashMap으로 Schema 순서(id -> name)를 명시한다.
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("id", "1");
        expected.put("name", "sample");
        assertThat(rows).singleElement().satisfies(row ->
                assertThat(row.values()).containsExactlyEntriesOf(expected));
    }
}

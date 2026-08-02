package com.cpf.common.tabular;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.core.api.tabular.CpfTabularColumn;
import com.cpf.core.api.tabular.CpfTabularFormat;
import com.cpf.core.api.tabular.CpfTabularReadRequest;
import com.cpf.core.api.tabular.CpfTabularReadResult;
import com.cpf.core.api.tabular.CpfTabularRow;
import com.cpf.core.api.tabular.CpfTabularSchema;
import com.cpf.core.api.tabular.CpfTabularWriteRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
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
        assertThat(rows).singleElement().satisfies(row ->
                assertThat(row.values()).containsExactlyEntriesOf(Map.of("id", "1", "name", "sample")));
    }
}

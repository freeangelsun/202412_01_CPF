package com.cpf.file.tabular;

import com.cpf.file.tabular.api.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfCsvTabularAdapterTest {
    private final CpfTabularSchema schema=new CpfTabularSchema("TEST",1,
            List.of(new CpfTabularColumn("id","ID",CpfTabularColumn.Type.INTEGER,true,10,false),
                    new CpfTabularColumn("name","NAME",CpfTabularColumn.Type.STRING,true,20,false)),100,100);

    @Test void streamingRoundTripAndFormulaEscape() {
        CpfCsvTabularAdapter adapter=new CpfCsvTabularAdapter();
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        adapter.write(new CpfTabularWriteRequest(CpfTabularFormat.CSV,schema,
                Stream.of(new CpfTabularRow(1,Map.of("id","1","name","=unsafe"))),out,true));
        List<CpfTabularRow> rows=new ArrayList<>();
        CpfTabularReadResult result=adapter.read(new CpfTabularReadRequest(CpfTabularFormat.CSV,schema,
                new ByteArrayInputStream(out.toByteArray()),true,true),rows::add);
        assertThat(result.acceptedRows()).isEqualTo(1);
        assertThat(rows.getFirst().values().get("name")).isEqualTo("'=unsafe");
    }
    @Test void malformedQuoteAndUnescapedFormulaAreRejected() {
        CpfCsvTabularAdapter adapter=new CpfCsvTabularAdapter();
        String malformed="ID,NAME\n1,\"not-closed";
        assertThatThrownBy(() -> adapter.read(new CpfTabularReadRequest(CpfTabularFormat.CSV,schema,
                new ByteArrayInputStream(malformed.getBytes(StandardCharsets.UTF_8)),true,true),row -> {}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> adapter.write(new CpfTabularWriteRequest(CpfTabularFormat.CSV,schema,
                Stream.of(new CpfTabularRow(1,Map.of("id","1","name","=unsafe"))),
                new ByteArrayOutputStream(),false)))
                .isInstanceOf(IllegalArgumentException.class);
    }

}

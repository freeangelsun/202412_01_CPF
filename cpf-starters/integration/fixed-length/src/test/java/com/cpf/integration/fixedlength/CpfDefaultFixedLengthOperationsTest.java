package com.cpf.integration.fixedlength;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.integration.fixedlength.api.CpfFixedLengthAlignment;
import com.cpf.integration.fixedlength.api.CpfFixedLengthConverterRegistry;
import com.cpf.integration.fixedlength.api.CpfFixedLengthFieldSpec;
import com.cpf.integration.fixedlength.api.CpfFixedLengthFieldType;
import com.cpf.integration.fixedlength.api.CpfFixedLengthLayout;
import com.cpf.integration.fixedlength.api.CpfFixedLengthLayoutRegistry;
import com.cpf.integration.fixedlength.api.CpfFixedLengthOperations;
import com.cpf.integration.fixedlength.internal.DefaultCpfFixedLengthCodec;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Starter 기본 구현만으로 Domain이 고정길이 전문 생성/파싱/마스킹을 수행할 수 있어야 한다. */
class CpfDefaultFixedLengthOperationsTest {
    @Test
    void roundTripUsesByteLengthAndMasksSensitiveField() {
        CpfFixedLengthLayout layout = new CpfFixedLengthLayout("PAY", "1", StandardCharsets.US_ASCII, 12,
                List.of(
                        new CpfFixedLengthFieldSpec("code", 1, 4, CpfFixedLengthFieldType.STRING, true,
                                ' ', CpfFixedLengthAlignment.LEFT, false),
                        new CpfFixedLengthFieldSpec("amount", 5, 4, CpfFixedLengthFieldType.NUMBER, true,
                                '0', CpfFixedLengthAlignment.RIGHT, false),
                        new CpfFixedLengthFieldSpec("secret", 9, 4, CpfFixedLengthFieldType.STRING, true,
                                ' ', CpfFixedLengthAlignment.LEFT, true)),
                List.of());
        CpfFixedLengthLayoutRegistry layouts = new CpfFixedLengthLayoutRegistry();
        layouts.register(layout);
        DefaultCpfFixedLengthCodec codec = new DefaultCpfFixedLengthCodec(new CpfFixedLengthConverterRegistry());
        CpfFixedLengthOperations operations = new CpfFixedLengthOperations(codec, codec, layouts);

        var write = operations.write(Map.of("code", "A", "amount", 12, "secret", "ABCD"), "PAY", "1");
        assertThat(write.message()).isEqualTo("A   0012ABCD");
        assertThat(write.maskedFields().get("secret")).isEqualTo("***");

        var parse = operations.parse(write.bytes(), "PAY", "1");
        assertThat(parse.valid()).isTrue();
        assertThat(parse.fields().get("code")).isEqualTo("A");
        assertThat(parse.maskedFields().get("secret")).isEqualTo("***");
        assertThat(operations.logView(write.message(), "PAY", "1").fields().get("secret")).isEqualTo("***");
    }
}

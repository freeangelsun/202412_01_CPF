package com.cpf.education.integration.telegram;
import com.cpf.education.integration.telegram.dto.EducationFixedLengthEducationTelegram;
import com.cpf.integration.fixedlength.api.CpfFixedLengthField;
import com.cpf.integration.fixedlength.api.CpfFixedLengthLayout;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EducationFixedLengthBusinessUseEducationSampleTest {

    @Test
    void businessLayoutUsesCanonicalFixedLengthApiContract() {
        CpfFixedLengthLayout layout = new EducationFixedLengthBusinessUseEducationSample().layout();

        assertThat(layout.totalLength()).isEqualTo(12);
        assertThat(layout.fields()).extracting("name").containsExactly("bankCode", "userNo");
    }

    @Test
    void telegramDtoUsesCanonicalFixedLengthApiAnnotation() {
        CpfFixedLengthField field = EducationFixedLengthEducationTelegram.class
                .getRecordComponents()[0]
                .getAnnotation(CpfFixedLengthField.class);

        assertThat(field).isNotNull();
        assertThat(field.length()).isEqualTo(10);
        assertThat(field.sensitive()).isTrue();
    }
}

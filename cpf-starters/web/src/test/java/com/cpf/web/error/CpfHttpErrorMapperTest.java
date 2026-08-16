package com.cpf.web.error;

import com.cpf.core.api.error.CpfErrorDefinition;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.assertj.core.api.Assertions.assertThat;

class CpfHttpErrorMapperTest {
    @Test void ownsTransportMappingOutsideCommonCatalog() {
        assertThat(CpfHttpErrorMapper.status(CpfErrorDefinition.Category.VALIDATION)).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(CpfHttpErrorMapper.status(CpfErrorDefinition.Category.AUTHENTICATION)).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(CpfHttpErrorMapper.status(CpfErrorDefinition.Category.AUTHORIZATION)).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(CpfHttpErrorMapper.status(CpfErrorDefinition.Category.NOT_FOUND)).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(CpfHttpErrorMapper.status(CpfErrorDefinition.Category.CONFLICT)).isEqualTo(HttpStatus.CONFLICT);
        assertThat(CpfHttpErrorMapper.status(CpfErrorDefinition.Category.RATE_LIMIT)).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(CpfHttpErrorMapper.status(CpfErrorDefinition.Category.BUSINESS)).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(CpfHttpErrorMapper.status(CpfErrorDefinition.Category.EXTERNAL)).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(CpfHttpErrorMapper.status(CpfErrorDefinition.Category.INFRASTRUCTURE)).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(CpfHttpErrorMapper.status(CpfErrorDefinition.Category.INTERNAL)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(CpfHttpErrorMapper.status((CpfErrorDefinition.Category) null)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(CpfHttpErrorMapper.status((CpfErrorDefinition) null)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

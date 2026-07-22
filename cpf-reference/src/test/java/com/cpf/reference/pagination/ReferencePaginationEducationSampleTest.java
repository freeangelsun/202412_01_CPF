package cpf.xyz.pagination;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class XyzPaginationEducationSampleTest {

    @Test
    void offsetAndKeysetPageReturnExpectedRows() {
        XyzPaginationEducationSample sample = new XyzPaginationEducationSample();

        assertThat(sample.offsetPage(List.of(1, 2, 3, 4), 1, 2)).containsExactly(2, 3);
        assertThat(sample.keysetPage(List.of(1, 2, 3, 4), 2, 2)).containsExactly(3, 4);
    }
}

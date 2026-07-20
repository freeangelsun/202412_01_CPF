package cpf.xyz.crud;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XyzCrudEducationSampleTest {

    @Test
    void createUpdateAndFindAreSeparated() {
        XyzCrudEducationSample sample = new XyzCrudEducationSample();
        sample.create("1", "first");
        sample.update("1", "second");

        assertThat(sample.find("1")).isEqualTo("second");
    }
}

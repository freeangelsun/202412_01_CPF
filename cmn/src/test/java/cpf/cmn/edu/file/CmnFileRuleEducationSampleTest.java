package cpf.cmn.edu.file;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CmnFileRuleEducationSampleTest {

    @Test
    void safeBusinessPathRejectsTraversalFileName() {
        CmnFileRuleEducationSample sample = new CmnFileRuleEducationSample();

        assertThat(sample.safeBusinessPath(Path.of("work"), "MBR", "a.txt").toString()).contains("MBR");
        assertThatThrownBy(() -> sample.safeBusinessPath(Path.of("work"), "MBR", "../a.txt"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

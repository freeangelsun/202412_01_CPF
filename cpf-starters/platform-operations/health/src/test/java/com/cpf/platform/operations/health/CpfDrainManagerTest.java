package com.cpf.platform.operations.health;
import static org.assertj.core.api.Assertions.assertThat; import com.cpf.platform.operations.health.api.*; import java.time.Duration; import org.junit.jupiter.api.Test;
class CpfDrainManagerTest { @Test void drainBlocksNewWork(){var d=new CpfDrainManager(); assertThat(d.tryEnter()).isTrue(); assertThat(d.beginDrain(Duration.ZERO)).isEqualTo(CpfDrainState.DRAINING); d.leave(); assertThat(d.beginDrain(Duration.ofMillis(5))).isEqualTo(CpfDrainState.STOPPED); assertThat(d.tryEnter()).isFalse();} }

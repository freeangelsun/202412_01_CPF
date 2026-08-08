package com.cpf.foundation.money;
import static org.assertj.core.api.Assertions.assertThat; import java.math.*; import org.junit.jupiter.api.Test;
class CpfAmountsTest { @Test void appliesPolicy(){ assertThat(CpfAmounts.normalize(new BigDecimal("1.235"),new CpfRoundingPolicy(2,RoundingMode.HALF_UP))).isEqualByComparingTo("1.24"); } }

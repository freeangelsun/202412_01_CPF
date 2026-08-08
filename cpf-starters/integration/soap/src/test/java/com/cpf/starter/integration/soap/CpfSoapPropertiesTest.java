package com.cpf.starter.integration.soap;
import static org.assertj.core.api.Assertions.*;import java.time.Duration;import org.junit.jupiter.api.Test;
class CpfSoapPropertiesTest{@Test void rejectsNonPositiveTimeout(){var p=new CpfSoapProperties();assertThatThrownBy(()->p.setReadTimeout(Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);}}

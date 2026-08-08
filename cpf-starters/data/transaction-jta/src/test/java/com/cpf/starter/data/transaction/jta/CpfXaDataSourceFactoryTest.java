package com.cpf.starter.data.transaction.jta;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
class CpfXaDataSourceFactoryTest {
 @Test void rejectsUnofficialVendor(){ assertThatThrownBy(() -> new CpfXaDataSourceFactory().create("mysql","x","u",new char[0])).isInstanceOf(IllegalArgumentException.class); }
}

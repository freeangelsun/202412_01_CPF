package com.cpf.data.cache.valkey;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class CpfValkeyPropertiesTest {
 @Test void enabledDefaultsRemainValid(){ var p=new CpfValkeyProperties(); p.setEnabled(true); assertDoesNotThrow(() -> p.validate()); }
 @Test void invalidPrefixFailsClosed(){ var p=new CpfValkeyProperties(); p.setEnabled(true); p.setKeyPrefix("bad prefix"); assertThrows(IllegalStateException.class,p::validate); }
}

package com.cpf.data.cache.redis;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class CpfRedisCachePropertiesTest {
 @Test void disabledRequiresNoConnectionSettings(){ new CpfRedisCacheProperties().validate(); }
 @Test void enabledValidDefaults(){ var p=new CpfRedisCacheProperties(); p.setEnabled(true); assertDoesNotThrow(() -> p.validate()); }
 @Test void invalidPrefixFailsClosed(){ var p=new CpfRedisCacheProperties(); p.setEnabled(true); p.setKeyPrefix("bad prefix"); assertThrows(IllegalStateException.class,p::validate); }
}

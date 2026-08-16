package com.cpf.data.cache.rediscommon;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
class CpfRedisProtocolProviderSelectionTest {
 @Test void oneProviderOrNoneIsAllowed(){
   assertDoesNotThrow(() -> CpfRedisProtocolProviderSelection.requireExclusive(false,false));
   assertDoesNotThrow(() -> CpfRedisProtocolProviderSelection.requireExclusive(true,false));
   assertDoesNotThrow(() -> CpfRedisProtocolProviderSelection.requireExclusive(false,true));
 }
 @Test void bothProvidersFailClosed(){
   assertThrows(IllegalStateException.class, () -> CpfRedisProtocolProviderSelection.requireExclusive(true,true));
 }
}

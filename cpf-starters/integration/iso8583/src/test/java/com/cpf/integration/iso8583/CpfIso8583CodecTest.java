package com.cpf.integration.iso8583;import java.nio.charset.StandardCharsets;import java.util.*;import org.junit.jupiter.api.Test;import static org.assertj.core.api.Assertions.*;
class CpfIso8583CodecTest {
 @Test void roundTripsBitmapAndVariableFields(){var specs=Map.of(2,new CpfIso8583FieldSpec(2,CpfIso8583FieldSpec.Length.LLVAR,19,StandardCharsets.US_ASCII),3,new CpfIso8583FieldSpec(3,CpfIso8583FieldSpec.Length.FIXED,6,StandardCharsets.US_ASCII));var codec=new CpfIso8583Codec(specs);var message=new CpfIso8583Message("0200",Map.of(2,"4111111111111111",3,"000000"));assertThat(codec.decode(codec.encode(message)).fields()).containsEntry(2,"4111111111111111").containsEntry(3,"000000");}
}

package com.cpf.core.api.security.secret;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CpfSecretValueTest { @Test void neverExposesValueInToString(){ try(var v=new CpfSecretValue("secret".toCharArray())){ assertEquals("[REDACTED]",v.toString()); } } }

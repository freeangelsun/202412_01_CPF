package com.cpf.batch.runtime;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class SensitiveTextSanitizerTest {@Test void masksCommonSecrets(){String v=SensitiveTextSanitizer.sanitize("password=abc Bearer token123");assertFalse(v.contains("abc"));assertFalse(v.contains("token123"));}}

package com.cpf.core.api.data.encryption;
import com.cpf.core.api.security.crypto.CpfEnvelopeCiphertext;
/** CpfEncryptedField 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfEncryptedField(CpfFieldClassification classification,String searchableToken,String keyVersion,CpfEnvelopeCiphertext envelope,String maskedPreview){
    public CpfEncryptedField { if(classification==null||keyVersion==null||keyVersion.isBlank()||envelope==null||maskedPreview==null) throw new IllegalArgumentException("encrypted field values are required"); }
}

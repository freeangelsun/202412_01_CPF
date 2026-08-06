package com.cpf.core.api.data.encryption;
import com.cpf.core.api.security.crypto.CpfEnvelopeCiphertext;
public record CpfEncryptedField(CpfFieldClassification classification,String searchableToken,String keyVersion,CpfEnvelopeCiphertext envelope,String maskedPreview){
    public CpfEncryptedField { if(classification==null||keyVersion==null||keyVersion.isBlank()||envelope==null||maskedPreview==null) throw new IllegalArgumentException("encrypted field values are required"); }
}

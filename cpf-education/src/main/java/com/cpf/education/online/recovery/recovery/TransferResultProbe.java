package com.cpf.education.online.recovery.recovery;
import com.cpf.core.api.result.CpfResult; import com.cpf.education.online.recovery.client.TransferInstitutionClient;
/** UNKNOWN 결과를 blind retry하지 않고 기관 Result Inquiry로 확인합니다. */
public final class TransferResultProbe { private final TransferInstitutionClient client; public TransferResultProbe(TransferInstitutionClient client){this.client=client;} public CpfResult<String> probe(String key){return client.probe(key);} }

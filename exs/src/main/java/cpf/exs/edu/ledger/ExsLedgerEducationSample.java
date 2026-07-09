package cpf.exs.edu.ledger;

/**
 * 대외 송수신 ledger 기록 항목 샘플입니다.
 */
public class ExsLedgerEducationSample {

    public LedgerRecord sent(String transactionGlobalId, String institutionCode) {
        return new LedgerRecord(transactionGlobalId, institutionCode, "SENT");
    }

    public record LedgerRecord(String transactionGlobalId, String institutionCode, String status) {
    }
}

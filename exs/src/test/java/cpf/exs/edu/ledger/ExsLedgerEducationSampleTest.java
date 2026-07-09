package cpf.exs.edu.ledger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExsLedgerEducationSampleTest {

    @Test
    void sentLedgerKeepsInstitutionAndTrace() {
        assertThat(new ExsLedgerEducationSample().sent("T-1", "BANKA").status()).isEqualTo("SENT");
    }
}

package com.cpf.core.api.util;

import com.cpf.core.api.page.CpfCursor;
import com.cpf.core.api.page.CpfHmacCursorCodec;
import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.transaction.CpfTransactionIds;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Core Public Foundation API의 대표 사용 계약을 검증합니다. */
class CpfFoundationApiTest {
    @Test
    void nullSafeStringListPageCursor를같은표준으로사용한다() {
        assertThat(CpfStrings.trimToNull("  ")).isNull();
        assertThat(CpfLists.distinct(List.of("A","A","B"))).containsExactly("A","B");

        CpfPageRequest request = CpfPages.request(0, 2);
        CpfPage<String> page = CpfPages.page(List.of("A","B"), request, 3);
        assertThat(page.hasNext()).isTrue();

        CpfCursor cursor = CpfCursor.encode("id=100");
        assertThat(cursor.decode()).isEqualTo("id=100");
    }

    @Test
    void 외부Cursor는Hmac서명을검증하고Header는CanonicalTransactionId만허용한다() {
        byte[] keyMaterial = ("0123456789abcdef" + "0123456789abcdef")
                .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        CpfHmacCursorCodec codec = new CpfHmacCursorCodec(keyMaterial);
        String token = codec.encode("id=100&sort=id");
        assertThat(codec.decode(token)).isEqualTo("id=100&sort=id");
        assertThatThrownBy(() -> codec.decode(token + "x")).isInstanceOf(IllegalArgumentException.class);

        String txId = "20260725123456789ADMABC12340000001";
        assertThat(CpfTransactionIds.isCanonical(txId)).isTrue();
        assertThat(CpfHeaders.transaction(txId)).containsEntry(CpfHeaders.transactionId(), txId);
        assertThatThrownBy(() -> CpfHeaders.transaction("bad-id")).isInstanceOf(IllegalArgumentException.class);
    }
}

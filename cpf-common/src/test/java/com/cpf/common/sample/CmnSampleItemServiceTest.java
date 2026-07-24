package com.cpf.common.sample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class CmnSampleItemServiceTest {

    @Test
    void remainsDbLessWhenOptionalProvidersHaveNoBeans() {
        CmnSampleItemService service = new CmnSampleItemService(
                emptyProvider(),
                emptyProvider());

        assertThat(service.isEnabled()).isFalse();
        assertThatThrownBy(() -> service.find(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cpf.cmn.sample-db.enabled");
    }

    @Test
    void rejectsUnsupportedStatusBeforeDatabaseAccess() {
        CmnSampleItemService service = new CmnSampleItemService(
                emptyProvider(),
                emptyProvider());
        CmnSampleItemRequest request = new CmnSampleItemRequest(
                "CMN-SAMPLE-1",
                "sample",
                "GENERAL",
                "DELETED",
                null,
                null,
                0,
                "TEST");

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ACTIVE 또는 INACTIVE");
    }

    @Test
    void sliceDefensivelyCopiesItems() {
        List<CmnSampleItem> mutable = new ArrayList<>();
        CmnSampleSlice slice = new CmnSampleSlice(mutable, false, null);

        mutable.add(sampleItem());

        assertThat(slice.items()).isEmpty();
        assertThatThrownBy(() -> slice.items().add(sampleItem()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private CmnSampleItem sampleItem() {
        return new CmnSampleItem(
                1L, "CMN-SAMPLE-1", "sample", "GENERAL", "ACTIVE",
                null, null, 0L, 0L, Instant.EPOCH, Instant.EPOCH);
    }

    @SuppressWarnings("unchecked")
    private <T> ObjectProvider<T> emptyProvider() {
        return mock(ObjectProvider.class);
    }
}

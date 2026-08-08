package com.cpf.core.api.base;

import com.cpf.core.api.page.CpfSort;
import com.cpf.core.api.page.CpfSortPolicy;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CpfRepositoryContractsTest {
    @Test
    void sortPolicyRejectsUnlistedAndDuplicateFields() {
        assertEquals(2, CpfSortPolicy.validate(
                List.of(CpfSort.asc("createdAt"), CpfSort.desc("id")),
                Set.of("createdAt", "id")).size());
        assertThrows(IllegalArgumentException.class,
                () -> CpfSortPolicy.validate(List.of(CpfSort.asc("rawSql")), Set.of("id")));
        assertThrows(IllegalArgumentException.class,
                () -> CpfSortPolicy.validate(List.of(CpfSort.asc("id"), CpfSort.desc("id")), Set.of("id")));
    }
}

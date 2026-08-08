package com.cpf.core.api.persistence;

import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.page.CpfSort;
import java.util.List;

public final class CpfPersistencePolicyTest {
    public static void main(String[] args) {
        var sorts = CpfPersistencePolicy.requireAllowedSorts(List.of(CpfSort.asc("createdAt"), CpfSort.desc("id")), List.of("createdAt","id"));
        if (sorts.size()!=2) throw new AssertionError("multi sort");
        assertFails(() -> CpfPersistencePolicy.requireAllowedSorts(List.of(CpfSort.asc("1 desc; drop table x")), List.of("id")));
        var spec = new CpfSearchSpec(List.of(CpfFilterCriterion.eq("status","ACTIVE")));
        CpfPersistencePolicy.requireAllowedFilters(spec,List.of("status"));
        assertFails(() -> CpfPersistencePolicy.requireAllowedFilters(new CpfSearchSpec(List.of(CpfFilterCriterion.eq("secret","x"))), List.of("status")));
        if (CpfPersistencePolicy.requireBoundedPage(new CpfPageRequest(0,200)).size()!=200) throw new AssertionError("page");
        assertFails(() -> CpfPersistencePolicy.requireBulkSize(1001,1000));
    }
    private static void assertFails(Runnable task){try{task.run();throw new AssertionError("expected failure");}catch(IllegalArgumentException expected){}}
}

package com.cpf.common.message.service;

import com.cpf.common.message.api.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CmnErrorCatalogCacheTest {
    @Test void dbOutageKeepsPreloadedCacheHitAndFenceChangeInvalidates() {
        CmnErrorCatalogRepository repo=mock(CmnErrorCatalogRepository.class); CpfErrorCatalogSignalSink signals=mock(CpfErrorCatalogSignalSink.class);
        var r=new CpfResponseCodeRecord("E1","M1","FAIL","M","G","1","BUSINESS","NEVER","SAFE_MESSAGE_ONLY",null,null,1,"","Y",Instant.now());
        var m=new CpfMessageRecord(1,"M1","ko","FIXED","safe","internal",0,"",null,"Y","Y",null,null,1,"","Y",Instant.now());
        var f1=new CmnErrorCatalogRepository.CatalogFence(new CmnErrorCatalogRepository.FencePart(1,1,Instant.EPOCH),new CmnErrorCatalogRepository.FencePart(1,1,Instant.EPOCH));
        var f2=new CmnErrorCatalogRepository.CatalogFence(new CmnErrorCatalogRepository.FencePart(1,2,Instant.now()),new CmnErrorCatalogRepository.FencePart(1,2,Instant.now()));
        when(repo.readFence()).thenReturn(f1,f2); when(repo.searchResponseCodes(null)).thenReturn(List.of(r)); when(repo.searchMessages(null,null)).thenReturn(List.of(m));
        var cache=new CmnErrorCatalogCache(repo,signals); ReflectionTestUtils.setField(cache,"preload",true); cache.initialize();
        assertThat(cache.response("E1")).isSameAs(r); assertThat(cache.message("M1",Locale.KOREAN)).isSameAs(m);
        cache.reconcileFence();
        when(repo.findResponseCode("E1")).thenThrow(new IllegalStateException("db down"));
        assertThat(cache.response("E1")).isNull();
        verify(signals).catalogFallback(startsWith("RESPONSE_DB_FAILURE_"),eq("E1"));
    }
}

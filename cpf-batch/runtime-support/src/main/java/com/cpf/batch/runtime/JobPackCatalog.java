package com.cpf.batch.runtime;

import com.cpf.batch.spi.BusinessJobProvider;
import java.util.*;

public final class JobPackCatalog {
    private final Map<String,BusinessJobProvider> providers; private final List<com.cpf.batch.api.JobPackManifest> manifests;
    public JobPackCatalog(List<BusinessJobProvider> springProviders) {
        Map<String,BusinessJobProvider> map=new LinkedHashMap<>();
        List<BusinessJobProvider> all=new ArrayList<>(springProviders);
        ServiceLoader.load(BusinessJobProvider.class).forEach(all::add);
        for(BusinessJobProvider provider:all) {
            for(var job:provider.manifest().jobs()) {
                BusinessJobProvider old=map.put(job.jobId(),provider);
                if(old!=null&&old!=provider) throw new IllegalStateException("Duplicate CPF Batch jobId: "+job.jobId());
            }
        }
        providers=Map.copyOf(map); manifests=all.stream().map(value -> value.manifest()).distinct().toList();
    }
    public List<com.cpf.batch.api.JobPackManifest> manifests(){return manifests;}
    public BusinessJobProvider providerFor(String jobId) {
        BusinessJobProvider provider=providers.get(jobId);
        if(provider==null) throw new IllegalArgumentException("Approved Job Pack not found: "+jobId);
        return provider;
    }
}

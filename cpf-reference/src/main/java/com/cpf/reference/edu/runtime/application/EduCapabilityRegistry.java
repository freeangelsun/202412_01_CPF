package com.cpf.reference.edu.runtime.application;
import java.util.*;
/**
 * Registers the contributors explicitly supplied by Spring or a typed test fixture.
 * Core has no source dependency on Batch, Operations, Backoffice or Gateway packages;
 * optional feature discovery is owned by component scanning and build-time source selection.
 */
public final class EduCapabilityRegistry {
    private static final Map<String,Integer> EXPECTED_COUNTS=Map.of(
            "reference-core",60,
            "reference-operations",4,
            "reference-backoffice",14,
            "reference-gateway",14,
            "reference-batch",30);
    private final Map<String,AbstractEduCapabilityHandler> handlers;
    private final Set<String> featureIds;

    public EduCapabilityRegistry(Collection<? extends EduCapabilityContributor> contributors){
        Objects.requireNonNull(contributors,"contributors");
        Map<String,AbstractEduCapabilityHandler> map=new LinkedHashMap<>();
        Set<String> features=new LinkedHashSet<>();
        for(EduCapabilityContributor contributor:contributors){
            String feature=require(contributor.featureId(),"featureId");
            Integer expected=EXPECTED_COUNTS.get(feature);
            if(expected==null)throw new IllegalStateException("Unknown EDU feature contributor: "+feature);
            if(!features.add(feature))throw new IllegalStateException("Duplicate EDU feature contributor: "+feature);
            Collection<? extends AbstractEduCapabilityHandler> values=Objects.requireNonNull(contributor.handlers(),"handlers");
            if(values.size()!=expected)throw new IllegalStateException(feature+" handler count must be "+expected+" but was "+values.size());
            for(AbstractEduCapabilityHandler value:values){
                var old=map.put(value.definition().requirementId(),value);
                if(old!=null)throw new IllegalStateException("Duplicate EDU handler: "+value.definition().requirementId());
            }
        }
        if(!features.contains("reference-core"))throw new IllegalStateException("reference-core EDU contributor is required");
        int expected=features.stream().mapToInt(f->EXPECTED_COUNTS.get(f)).sum();
        if(map.size()!=expected)throw new IllegalStateException("EDU handler count must be "+expected+" but was "+map.size());
        handlers=Collections.unmodifiableMap(map);featureIds=Collections.unmodifiableSet(features);
    }
    public AbstractEduCapabilityHandler require(String id){var h=handlers.get(id);if(h==null)throw new NoSuchElementException("Unknown or disabled EDU requirement: "+id);return h;}
    public Collection<AbstractEduCapabilityHandler> all(){return handlers.values();}
    public Set<String> featureIds(){return featureIds;}
    public boolean featureEnabled(String featureId){return featureIds.contains(featureId);}
    private static String require(String value,String name){if(value==null||value.isBlank())throw new IllegalArgumentException(name+" is required");return value.trim();}
}

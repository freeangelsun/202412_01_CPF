package com.cpf.education.operations.runtime.application;
import java.util.*;
/**
 * Registers the contributors explicitly supplied by Spring or a typed test fixture.
 * Core has no source dependency on Batch, Operations, Backoffice or Gateway packages;
 * optional feature discovery is owned by component scanning and build-time source selection.
 */
/** EduCapabilityRegistry 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class EduCapabilityRegistry {
    private static final Map<String,Integer> EXPECTED_COUNTS=Map.of(
            "education-core",60,
            "education-operations",17,
            "education-backoffice",14,
            "education-gateway",14,
            "education-batch",30);
    private final Map<String,AbstractEduCapabilityHandler> handlers;
    private final Set<String> featureIds;

    /** EduCapabilityRegistry 작업을 CPF 표준 계약에 따라 수행한다. */
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
        if(!features.contains("education-core"))throw new IllegalStateException("education-core EDU contributor is required");
        int expected=features.stream().mapToInt(f->EXPECTED_COUNTS.get(f)).sum();
        if(map.size()!=expected)throw new IllegalStateException("EDU handler count must be "+expected+" but was "+map.size());
        handlers=Collections.unmodifiableMap(map);featureIds=Collections.unmodifiableSet(features);
    }
    /** Test 전용으로 exact handler count를 검증하며 Spring contributor 분리를 우회하지 않는 격리 Registry를 만듭니다. */
    public static EduCapabilityRegistry forVerification(EduCapabilityContributor contributor, int expectedCount) {
        Objects.requireNonNull(contributor, "contributor");
        Collection<? extends AbstractEduCapabilityHandler> values = Objects.requireNonNull(contributor.handlers(), "handlers");
        if (values.size() != expectedCount) throw new IllegalStateException("EDU verification handler count must be " + expectedCount + " but was " + values.size());
        Map<String, AbstractEduCapabilityHandler> map = new LinkedHashMap<>();
        for (AbstractEduCapabilityHandler value : values) {
            if (map.put(value.definition().requirementId(), value) != null) throw new IllegalStateException("Duplicate EDU handler: " + value.definition().requirementId());
        }
        return new EduCapabilityRegistry(map, Set.of("education-verification"));
    }

    private EduCapabilityRegistry(Map<String, AbstractEduCapabilityHandler> handlers, Set<String> featureIds) {
        this.handlers = Collections.unmodifiableMap(new LinkedHashMap<>(handlers));
        this.featureIds = Collections.unmodifiableSet(new LinkedHashSet<>(featureIds));
    }

    /** require 작업을 CPF 표준 계약에 따라 수행한다. */
    public AbstractEduCapabilityHandler require(String id){var h=handlers.get(id);if(h==null)throw new NoSuchElementException("Unknown or disabled EDU requirement: "+id);return h;}
    public Collection<AbstractEduCapabilityHandler> all(){return handlers.values();}
    public Set<String> featureIds(){return featureIds;}
    public boolean featureEnabled(String featureId){return featureIds.contains(featureId);}
    private static String require(String value,String name){if(value==null||value.isBlank())throw new IllegalArgumentException(name+" is required");return value.trim();}
}

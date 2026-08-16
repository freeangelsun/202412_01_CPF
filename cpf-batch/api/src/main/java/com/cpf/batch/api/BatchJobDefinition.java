package com.cpf.batch.api;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * ADM과 BAT Control Server가 공유하는 Versioned Job Definition 계약입니다.
 * Definition은 Draft→Validated→Approval→Published→Retired 상태를 가지며 Published Version은 불변입니다.
 */
public record BatchJobDefinition(
        String jobId,
        long definitionVersion,
        String jobName,
        ExecutorType executorType,
        State state,
        String ownerDomain,
        String description,
        Trigger trigger,
        List<BatchParameterDefinition> parameters,
        List<Dependency> dependencies,
        ResourcePolicy resourcePolicy,
        RecoveryPolicy recoveryPolicy,
        AlertPolicy alertPolicy,
        String executorReference,
        String checksum,
        String requestedBy,
        String reason,
        OffsetDateTime effectiveFrom,
        OffsetDateTime effectiveUntil,
        long expectedRowVersion) {

    public enum ExecutorType { SPRING_BATCH, APPROVED_SHELL, FILE_WATCH, FILE_PROCESS, FILE_TRANSFER, SERVICE_CALL, MESSAGE_TRIGGER, PROTOCOL_ADAPTER }
    public enum State { DRAFT, VALIDATED, APPROVAL, PUBLISHED, RETIRED }
    public enum TriggerType { CRON, CALENDAR, BUSINESS_DAY, FILE, MESSAGE, MANUAL, DEPENDENCY }
    public enum MisfirePolicy { FIRE_NOW, SKIP, NEXT_SCHEDULE, FAIL_CLOSED }
    public enum UnknownResultPolicy { RECONCILE, MANUAL_REVIEW, COMPENSATE, FAIL_CLOSED }

    public BatchJobDefinition {
        jobId = required(jobId, "jobId");
        if (!jobId.matches("[A-Z0-9][A-Z0-9._-]{2,79}")) throw new IllegalArgumentException("jobId format invalid");
        if (definitionVersion <= 0) throw new IllegalArgumentException("definitionVersion must be positive");
        jobName = required(jobName, "jobName");
        executorType = Objects.requireNonNull(executorType, "executorType");
        state = state == null ? State.DRAFT : state;
        ownerDomain = required(ownerDomain, "ownerDomain");
        description = clean(description);
        trigger = Objects.requireNonNull(trigger, "trigger");
        parameters = parameters == null ? List.of() : List.copyOf(parameters);
        dependencies = dependencies == null ? List.of() : List.copyOf(dependencies);
        resourcePolicy = resourcePolicy == null ? ResourcePolicy.defaults() : resourcePolicy;
        recoveryPolicy = recoveryPolicy == null ? RecoveryPolicy.defaults() : recoveryPolicy;
        alertPolicy = alertPolicy == null ? AlertPolicy.defaults() : alertPolicy;
        executorReference = required(executorReference, "executorReference");
        checksum = clean(checksum);
        requestedBy = required(requestedBy, "requestedBy");
        reason = required(reason, "reason");
        if (reason.length() < 5) throw new IllegalArgumentException("reason must be at least 5 characters");
        if (effectiveFrom != null && effectiveUntil != null && !effectiveFrom.isBefore(effectiveUntil)) throw new IllegalArgumentException("effectiveFrom must be before effectiveUntil");
        validateExecutor(executorType, executorReference, parameters);
        validateUniqueParameters(parameters);
        validateDependencies(jobId, dependencies);
    }

    public record Trigger(TriggerType type, String expression, String timezone, MisfirePolicy misfirePolicy, boolean enabled) {
        public Trigger { type=Objects.requireNonNull(type,"trigger.type"); expression=clean(expression); timezone=blank(timezone)?"Asia/Seoul":timezone.trim(); misfirePolicy=misfirePolicy==null?MisfirePolicy.FAIL_CLOSED:misfirePolicy;
            if (type != TriggerType.MANUAL && type != TriggerType.DEPENDENCY && blank(expression)) throw new IllegalArgumentException("trigger expression required"); }
    }
    public record Dependency(String relatedJobId, String condition, long timeoutSeconds, boolean required) {
        public Dependency { relatedJobId=BatchJobDefinition.required(relatedJobId,"relatedJobId");condition=blank(condition)?"SUCCESS":condition.trim().toUpperCase(Locale.ROOT);if(timeoutSeconds<0)throw new IllegalArgumentException("dependency timeout cannot be negative"); }
    }
    public record ResourcePolicy(String agentPool, String zone, int maxConcurrency, long timeoutSeconds, long memoryLimitMb, int cpuLimitMillicores) {
        public ResourcePolicy { agentPool=required(agentPool,"agentPool");zone=clean(zone);if(maxConcurrency<=0)maxConcurrency=1;if(timeoutSeconds<=0)timeoutSeconds=3600;if(memoryLimitMb<0||cpuLimitMillicores<0)throw new IllegalArgumentException("resource limit cannot be negative"); }
        public static ResourcePolicy defaults(){return new ResourcePolicy("DEFAULT","",1,3600,0,0);}
    }
    public record RecoveryPolicy(int maxAttempts, long initialBackoffSeconds, double multiplier, long maxBackoffSeconds, int skipLimit, boolean restartable, UnknownResultPolicy unknownResultPolicy, String compensationReference) {
        public RecoveryPolicy { if(maxAttempts<1)maxAttempts=1;if(initialBackoffSeconds<0)initialBackoffSeconds=0;if(multiplier<1)multiplier=1;if(maxBackoffSeconds<initialBackoffSeconds)maxBackoffSeconds=initialBackoffSeconds;if(skipLimit<0)skipLimit=0;unknownResultPolicy=unknownResultPolicy==null?UnknownResultPolicy.FAIL_CLOSED:unknownResultPolicy;compensationReference=clean(compensationReference); }
        public static RecoveryPolicy defaults(){return new RecoveryPolicy(1,0,1,0,0,true,UnknownResultPolicy.FAIL_CLOSED,"");}
    }
    public record AlertPolicy(long delayThresholdSeconds, long slaSeconds, boolean notifyOnFailure, boolean notifyOnMissed, List<String> providerKeys) {
        public AlertPolicy { if(delayThresholdSeconds<0||slaSeconds<0)throw new IllegalArgumentException("alert threshold cannot be negative");providerKeys=providerKeys==null?List.of():List.copyOf(providerKeys); }
        public static AlertPolicy defaults(){return new AlertPolicy(0,0,true,true,List.of());}
    }

    private static void validateExecutor(
            ExecutorType type, String ref, List<BatchParameterDefinition> params) {
        if (type == ExecutorType.APPROVED_SHELL && !ref.startsWith("SCRIPT:")) {
            throw new IllegalArgumentException("APPROVED_SHELL requires SCRIPT catalog reference");
        }
        if ((type == ExecutorType.FILE_WATCH || type == ExecutorType.FILE_PROCESS
                || type == ExecutorType.FILE_TRANSFER)
                && params.stream().noneMatch(parameter -> "PATH_ALIAS".equals(parameter.type()))) {
            throw new IllegalArgumentException("File executor requires PATH_ALIAS parameter");
        }
        if (type == ExecutorType.FILE_PROCESS) {
            String processorId = referenceId(ref, "PROCESSOR:", "FILE_PROCESS requires PROCESSOR catalog reference");
            if (!processorId.matches("[A-Za-z0-9._:-]{1,120}")) {
                throw new IllegalArgumentException("FILE_PROCESS processorId format invalid");
            }
            requireParameter(params, "sourceAlias", Set.of("PATH_ALIAS"));
            requireParameter(params, "sourcePath", Set.of("STRING", "FILE_REFERENCE"));
        }
        if (type == ExecutorType.SERVICE_CALL && !ref.startsWith("SERVICE:")) {
            throw new IllegalArgumentException("SERVICE_CALL requires typed service operation reference");
        }
    }

    /** FILE_PROCESS Runtime과 ADM Preview가 동일하게 사용하는 Processor ID입니다. */
    public String processorId() {
        if (executorType != ExecutorType.FILE_PROCESS) return "";
        return referenceId(executorReference, "PROCESSOR:",
                "FILE_PROCESS requires PROCESSOR catalog reference");
    }

    private static String referenceId(String reference, String prefix, String message) {
        if (reference == null || !reference.startsWith(prefix)) {
            throw new IllegalArgumentException(message);
        }
        String value = reference.substring(prefix.length()).trim();
        if (value.isEmpty()) throw new IllegalArgumentException(message);
        return value;
    }

    private static void requireParameter(
            List<BatchParameterDefinition> parameters, String name, Set<String> allowedTypes) {
        BatchParameterDefinition definition = parameters.stream()
                .filter(parameter -> name.equals(parameter.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "FILE_PROCESS requires parameter schema: " + name));
        if (!allowedTypes.contains(definition.type())) {
            throw new IllegalArgumentException(
                    "FILE_PROCESS parameter " + name + " type must be " + allowedTypes);
        }
        if (!definition.required()) {
            throw new IllegalArgumentException(
                    "FILE_PROCESS parameter must be required: " + name);
        }
    }
    private static void validateUniqueParameters(List<BatchParameterDefinition> params){Set<String> names=new HashSet<>();for(var p:params){if(!names.add(p.name()))throw new IllegalArgumentException("duplicate parameter: "+p.name());}}
    private static void validateDependencies(String jobId,List<Dependency> deps){Set<String> ids=new HashSet<>();for(var d:deps){if(jobId.equals(d.relatedJobId()))throw new IllegalArgumentException("self dependency prohibited");if(!ids.add(d.relatedJobId()))throw new IllegalArgumentException("duplicate dependency: "+d.relatedJobId());}}
    private static String required(String v,String n){if(blank(v))throw new IllegalArgumentException(n+" required");return v.trim();}
    private static boolean blank(String v){return v==null||v.isBlank();}private static String clean(String v){return v==null?"":v.trim();}
}

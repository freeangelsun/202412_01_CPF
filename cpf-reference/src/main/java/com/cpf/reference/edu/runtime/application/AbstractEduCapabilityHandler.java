package com.cpf.reference.edu.runtime.application;

import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.EduConsumerBinding;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

/** Concrete Manual EDU Capability contract; markers and fixed responses are forbidden. */
public abstract class AbstractEduCapabilityHandler {
    private final EduCapabilityDefinition definition;
    protected AbstractEduCapabilityHandler(EduCapabilityDefinition definition){this.definition=Objects.requireNonNull(definition);}
    public final EduCapabilityDefinition definition(){return definition;}
    public abstract String implementationPackage();
    public abstract boolean readOnly();
    public abstract List<String> businessStates();
    public abstract List<String> exceptionScenarios();
    public abstract List<String> requiredVerification();
    public abstract List<String> targetKeys(EduExecutionCommand command);
    public abstract Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload);
    /** Real product consumer binding; common ledger-only handlers are rejected. */
    public abstract EduConsumerBinding consumerBinding();

    public final void validate(EduExecutionCommand command){
        Objects.requireNonNull(command,"command");
        if(!command.roles().contains(definition.requiredRole()))throw new EduAuthorizationException("Required role missing: "+definition.requiredRole());
        if(command.dataScope().isBlank())throw new EduAuthorizationException("dataScope is required");
        for(String field:definition.requiredFields()){
            Object value=value(command,field);
            if(value==null||(value instanceof String s&&s.isBlank()))throw new EduValidationException(definition.requirementId()+" missing field: "+field);
        }
        if(definition.versioned()&&command.expectedVersion()<0)throw new EduValidationException("expectedVersion must be >= 0");
        if(businessStates().isEmpty()||exceptionScenarios().isEmpty()||requiredVerification().isEmpty())throw new IllegalStateException(definition.requirementId()+" executable contract incomplete");
        EduConsumerBinding binding=consumerBinding();
        if(!definition.requirementId().equals(binding.requirementId()))throw new IllegalStateException(definition.requirementId()+" consumer binding identity mismatch");
        validateBusinessInput(command);
    }
    protected Object value(EduExecutionCommand c,String field){return switch(field){case "businessKey"->c.businessKey();case "requestReason","reason"->c.requestReason();case "expectedVersion"->c.expectedVersion();case "dataScope"->c.dataScope();default->c.payload().get(field);};}
    protected void validateBusinessInput(EduExecutionCommand command){Object amount=command.payload().get("amount");if(amount!=null&&decimal(amount,"amount").signum()<0)throw new EduValidationException("amount must be >= 0");}
    public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken){Map<String,Object> result=new LinkedHashMap<>();result.put("requirementId",definition.requirementId());result.put("businessKey",command.businessKey());result.put("owner",definition.owner());result.put("fencingToken",fencingToken);result.put("appliedFields",new TreeSet<>(command.payload().keySet()));result.put("manualAnchor",definition.manualAnchor());result.put("requestId",command.requestId());result.put("traceId",command.traceId());return result;}
    protected final int payloadInt(EduExecutionCommand c,String field,int fallback){Object v=c.payload().get(field);return v==null?fallback:(int)number(v,field);}
    protected final void requireLongRange(EduExecutionCommand c,String field,long min,long max){long v=number(required(c,field),field);if(v<min||v>max)throw new EduValidationException(field+" must be "+min+".."+max);}
    protected final void requireDecimalNonNegative(EduExecutionCommand c,String field){if(decimal(required(c,field),field).signum()<0)throw new EduValidationException(field+" must be >= 0");}
    protected final void requireEnum(EduExecutionCommand c,String field,Set<String> allowed){String v=String.valueOf(required(c,field)).toLowerCase(Locale.ROOT);if(!allowed.contains(v))throw new EduValidationException(field+" unsupported value: "+v);}
    protected final void requireSha256(EduExecutionCommand c,String field){String v=String.valueOf(required(c,field));if(!v.matches("[0-9a-fA-F]{64}"))throw new EduValidationException(field+" must be SHA-256 hex");}
    protected final void requireSafePath(EduExecutionCommand c,String field){String v=String.valueOf(required(c,field));try{Path p=Path.of(v).normalize();if(p.isAbsolute()||p.startsWith("..")||v.indexOf('\0')>=0)throw new EduValidationException(field+" unsafe path");}catch(InvalidPathException e){throw new EduValidationException(field+" invalid path");}}
    protected final void requireSafeEndpoint(EduExecutionCommand c,String field){String raw=String.valueOf(required(c,field));final URI u;try{u=URI.create(raw);}catch(IllegalArgumentException e){throw new EduValidationException(field+" invalid URI");}if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null)throw new EduValidationException(field+" must be https URI");rejectPrivateHost(u.getHost(),field);}
    protected final void rejectPrivateNetworkLiteral(EduExecutionCommand c,String field){Object v=c.payload().get(field);if(v!=null)rejectPrivateHost(String.valueOf(v),field);}
    private void rejectPrivateHost(String host,String field){String v=host.toLowerCase(Locale.ROOT);if(v.equals("localhost")||v.equals("127.0.0.1")||v.equals("::1")||v.startsWith("10.")||v.startsWith("192.168.")||v.matches("172\\.(1[6-9]|2[0-9]|3[01])\\..*"))throw new EduValidationException(field+" private network target forbidden");}
    protected final void requireAllowedSort(EduExecutionCommand c,String field,Set<String> allowed){String f=String.valueOf(required(c,field)).split(",",2)[0];if(!allowed.contains(f))throw new EduValidationException("sort field not allowed: "+f);}
    protected final void requireLeadingSlash(EduExecutionCommand c,String field){String v=String.valueOf(required(c,field));if(!v.startsWith("/")||v.contains(".."))throw new EduValidationException(field+" invalid route");}
    protected final void requireDateOrder(EduExecutionCommand c,String from,String to){if(LocalDate.parse(String.valueOf(required(c,to))).isBefore(LocalDate.parse(String.valueOf(required(c,from)))))throw new EduValidationException(to+" must be >= "+from);}
    protected final void requireDifferent(EduExecutionCommand c,String left,String right){if(Objects.equals(String.valueOf(required(c,left)),String.valueOf(required(c,right))))throw new EduValidationException(left+" and "+right+" must differ");}
    private Object required(EduExecutionCommand c,String field){Object v=value(c,field);if(v==null||(v instanceof String s&&s.isBlank()))throw new EduValidationException(field+" is required");return v;}
    private static long number(Object v,String field){try{return Long.parseLong(String.valueOf(v));}catch(NumberFormatException e){throw new EduValidationException(field+" must be numeric");}}
    private static BigDecimal decimal(Object v,String field){try{return new BigDecimal(String.valueOf(v));}catch(NumberFormatException e){throw new EduValidationException(field+" must be decimal");}}
}

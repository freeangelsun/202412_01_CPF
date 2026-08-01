package com.cpf.reference.edu.runtime.consumer;
import java.util.*;
/** Explicit binding from one Manual EDU requirement to an executable product consumer. */
public record EduConsumerBinding(
        String requirementId,
        EduConsumerType type,
        String ownerModule,
        String entryPoint,
        String operation,
        String publicContract,
        String runtimeCommand,
        String configurationKey,
        int timeoutSeconds,
        List<String> argumentFields) {
    public EduConsumerBinding {
        requirementId=require(requirementId,"requirementId");
        type=Objects.requireNonNull(type,"type");
        ownerModule=require(ownerModule,"ownerModule");
        entryPoint=require(entryPoint,"entryPoint");
        operation=require(operation,"operation");
        publicContract=require(publicContract,"publicContract");
        runtimeCommand=require(runtimeCommand,"runtimeCommand");
        configurationKey=configurationKey==null?"":configurationKey.trim();
        if(timeoutSeconds<1||timeoutSeconds>3600)throw new IllegalArgumentException("timeoutSeconds must be 1..3600");
        argumentFields=List.copyOf(argumentFields==null?List.of():argumentFields);
    }
    private static String require(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
}

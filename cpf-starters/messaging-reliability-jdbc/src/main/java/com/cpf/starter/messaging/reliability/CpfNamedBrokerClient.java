package com.cpf.starter.messaging.reliability;
import com.cpf.core.api.broker.CpfBrokerClient;
public record CpfNamedBrokerClient(String name,String provider,boolean defaultBinding,CpfBrokerClient client){public CpfNamedBrokerClient{if(name==null||name.isBlank()||provider==null||provider.isBlank()||client==null)throw new IllegalArgumentException("name, provider and client are required");}}

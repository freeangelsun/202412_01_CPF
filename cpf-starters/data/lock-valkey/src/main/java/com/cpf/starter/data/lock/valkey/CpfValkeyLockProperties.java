package com.cpf.starter.data.lock.valkey;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("cpf.data.lock.valkey")
public class CpfValkeyLockProperties {
 private boolean enabled; private String namespace="cpf"; private int casRetries=16;
 public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;}
 public String getNamespace(){return namespace;} public void setNamespace(String v){namespace=v;}
 public int getCasRetries(){return casRetries;} public void setCasRetries(int v){casRetries=Math.max(1,v);}
}

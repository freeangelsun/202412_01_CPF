package com.cpf.security.resource;
import org.springframework.boot.context.properties.ConfigurationProperties;
/** Security Developer Annotation Runtime switch입니다. */
@ConfigurationProperties("cpf.security.annotation")
public class CpfSecurityAnnotationProperties { private boolean enabled=true; public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} }

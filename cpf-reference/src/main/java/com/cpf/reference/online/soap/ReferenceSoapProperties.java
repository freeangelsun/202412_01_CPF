package com.cpf.reference.online.soap;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Reference-only fixed SOAP target; request parameters cannot choose an arbitrary SSRF destination. */
@ConfigurationProperties("cpf.reference.soap")
public class ReferenceSoapProperties {
    private URI endpoint;
    private String soapAction;
    public URI getEndpoint(){return endpoint;}
    public void setEndpoint(URI endpoint){
        if(endpoint!=null&&!"https".equalsIgnoreCase(endpoint.getScheme()))throw new IllegalArgumentException("SOAP reference endpoint must use HTTPS");
        this.endpoint=endpoint;
    }
    public String getSoapAction(){return soapAction;}
    public void setSoapAction(String soapAction){this.soapAction=soapAction;}
}

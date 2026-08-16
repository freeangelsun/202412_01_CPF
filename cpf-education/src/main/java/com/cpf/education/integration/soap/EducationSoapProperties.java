package com.cpf.education.integration.soap;
import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Education-only fixed SOAP target; request parameters cannot choose an arbitrary SSRF destination. */
@ConfigurationProperties("cpf.education.soap")
/** EducationSoapProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationSoapProperties {
    private URI endpoint;
    private String soapAction;
    public URI getEndpoint(){return endpoint;}
    public void setEndpoint(URI endpoint){
        if(endpoint!=null&&!"https".equalsIgnoreCase(endpoint.getScheme()))throw new IllegalArgumentException("SOAP education endpoint must use HTTPS");
        this.endpoint=endpoint;
    }
    public String getSoapAction(){return soapAction;}
    public void setSoapAction(String soapAction){this.soapAction=soapAction;}
}

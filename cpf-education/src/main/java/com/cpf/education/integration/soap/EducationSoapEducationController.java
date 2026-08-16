package com.cpf.education.integration.soap;
import com.cpf.integration.soap.CpfSoapClient;
import com.cpf.integration.soap.CpfSoapResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Executable SOAP client education showing transaction lineage and UNKNOWN handling. */
@RestController
@RequestMapping("/education/soap")
@ConditionalOnBean(CpfSoapClient.class)
/** EducationSoapEducationController 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationSoapEducationController {
    private final CpfSoapClient client;
    private final EducationSoapProperties properties;
    public EducationSoapEducationController(CpfSoapClient client,EducationSoapProperties properties){this.client=client;this.properties=properties;}

    @PostMapping(consumes=MediaType.APPLICATION_XML_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
    public CpfSoapResult<String> call(@RequestHeader("X-CPF-Transaction-Id") String transactionId,@RequestBody String xml) {
        if(properties.getEndpoint()==null)throw new IllegalStateException("cpf.education.soap.endpoint is required");
        return client.callXml(properties.getEndpoint(),properties.getSoapAction(),transactionId,xml);
    }
}

package com.cpf.reference.online.soap;

import com.cpf.starter.integration.soap.CpfSoapClient;
import com.cpf.starter.integration.soap.CpfSoapResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Executable SOAP client reference showing transaction lineage and UNKNOWN handling. */
@RestController
@RequestMapping("/reference/soap")
@ConditionalOnBean(CpfSoapClient.class)
public class ReferenceSoapEducationController {
    private final CpfSoapClient client;
    private final ReferenceSoapProperties properties;
    public ReferenceSoapEducationController(CpfSoapClient client,ReferenceSoapProperties properties){this.client=client;this.properties=properties;}

    @PostMapping(consumes=MediaType.APPLICATION_XML_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
    public CpfSoapResult<String> call(@RequestHeader("X-CPF-Transaction-Id") String transactionId,@RequestBody String xml) {
        if(properties.getEndpoint()==null)throw new IllegalStateException("cpf.reference.soap.endpoint is required");
        return client.callXml(properties.getEndpoint(),properties.getSoapAction(),transactionId,xml);
    }
}

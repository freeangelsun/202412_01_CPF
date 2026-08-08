package com.cpf.starter.integration.soap;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import javax.xml.transform.stream.StreamSource;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;

/**
 * Spring-WS boilerplate를 줄이는 typed SOAP client. transactionId를 SOAP Header로 전파하며
 * timeout/transport-loss는 side effect를 단정할 수 없어 UNKNOWN으로 반환합니다.
 */
public final class CpfSoapClient {
    private static final String CPF_NS="urn:cpf:lineage:v1";
    private final WebServiceTemplate template;
    public CpfSoapClient(WebServiceTemplate template){this.template=Objects.requireNonNull(template,"template");}

    public <REQ,RES> CpfSoapResult<RES> call(URI endpoint,String soapAction,String transactionId,REQ request,Class<RES> responseType){
        Objects.requireNonNull(endpoint,"endpoint"); Objects.requireNonNull(request,"request"); Objects.requireNonNull(responseType,"responseType");
        if(transactionId==null||transactionId.isBlank())throw new IllegalArgumentException("transactionId is required");
        try {
            Object response=template.marshalSendAndReceive(endpoint.toString(),request,callback(soapAction,transactionId));
            if(!responseType.isInstance(response))return CpfSoapResult.failed("SOAP_RESPONSE_TYPE_MISMATCH");
            return CpfSoapResult.success(responseType.cast(response));
        } catch (WebServiceIOException e) {
            return CpfSoapResult.unknown(isTimeout(e)?"SOAP_TIMEOUT":"SOAP_TRANSPORT_UNKNOWN");
        } catch (MarshallingFailureException e) {
            return CpfSoapResult.failed("SOAP_MARSHALLING_FAILED");
        } catch (RuntimeException e) {
            return CpfSoapResult.failed("SOAP_CALL_FAILED");
        }
    }

    /** Legacy XML/WSDL integration escape without JAXB-generated types. */
    public CpfSoapResult<String> callXml(URI endpoint,String soapAction,String transactionId,String xmlRequest){
        Objects.requireNonNull(endpoint,"endpoint");
        if(transactionId==null||transactionId.isBlank())throw new IllegalArgumentException("transactionId is required");
        if(xmlRequest==null||xmlRequest.isBlank())throw new IllegalArgumentException("xmlRequest is required");
        try {
            java.io.StringWriter response=new java.io.StringWriter();
            boolean received=template.sendSourceAndReceiveToResult(endpoint.toString(),new StreamSource(new java.io.StringReader(xmlRequest)),callback(soapAction,transactionId),new javax.xml.transform.stream.StreamResult(response));
            if(!received)return CpfSoapResult.failed("SOAP_EMPTY_RESPONSE");
            return CpfSoapResult.success(response.toString());
        } catch (WebServiceIOException e) {
            return CpfSoapResult.unknown(isTimeout(e)?"SOAP_TIMEOUT":"SOAP_TRANSPORT_UNKNOWN");
        } catch (RuntimeException e) {
            return CpfSoapResult.failed("SOAP_CALL_FAILED");
        }
    }

    public WebServiceTemplate nativeTemplate(){return template;}

    private static WebServiceMessageCallback callback(String action,String tx){return message->{
        if(message instanceof SoapMessage soap){
            if(action!=null&&!action.isBlank())soap.setSoapAction(action);
            String xml="<cpf:lineage xmlns:cpf=\""+CPF_NS+"\"><cpf:transactionId>"+escape(tx)+"</cpf:transactionId></cpf:lineage>";
            soap.getSoapHeader().getResult();
            javax.xml.transform.TransformerFactory.newInstance().newTransformer().transform(new StreamSource(new java.io.StringReader(xml)),soap.getSoapHeader().getResult());
        }
    };}
    private static boolean isTimeout(Throwable e){for(Throwable t=e;t!=null;t=t.getCause())if(t instanceof SocketTimeoutException)return true;return false;}
    private static String escape(String value){return value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;");}
}

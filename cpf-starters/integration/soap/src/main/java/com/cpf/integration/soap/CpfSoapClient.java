package com.cpf.integration.soap;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Objects;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;

import javax.xml.transform.stream.StreamSource;
import org.springframework.oxm.MarshallingFailureException;
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
    private final CpfContextExecutionFactory contextFactory;
    @Deprecated(forRemoval = true) public CpfSoapClient(WebServiceTemplate template){this.template=Objects.requireNonNull(template,"template");this.contextFactory=null;}
    public CpfSoapClient(WebServiceTemplate template,CpfContextExecutionFactory contextFactory){this.template=Objects.requireNonNull(template,"template");this.contextFactory=Objects.requireNonNull(contextFactory,"contextFactory");}

    public <REQ,RES> CpfSoapResult<RES> call(URI endpoint,String soapAction,REQ request,Class<RES> responseType){
        return managed(endpoint,soapAction,()->call(endpoint,soapAction,CpfContexts.transactionId(),request,responseType));
    }
    public CpfSoapResult<String> callXml(URI endpoint,String soapAction,String xmlRequest){
        return managed(endpoint,soapAction,()->callXml(endpoint,soapAction,CpfContexts.transactionId(),xmlRequest));
    }
    private <T>T managed(URI endpoint,String operation,java.util.concurrent.Callable<T> call){
        if(contextFactory==null)throw new IllegalStateException("Managed SOAP execution requires CPF Context factory");
        var parent=CpfContexts.requireSnapshot();
        CpfContextSnapshot snap=contextFactory.childSnapshot(parent,new CpfContextExecutionFactory.ChildSpec(
                endpoint.getHost()+endpoint.getPath(),CpfContext.CpfExecutionType.INTEGRATION,1,
                parent.context().execution().deadline(),parent.context().operation()));
        try (var _ = CpfContexts.bind(snap)) {
            try { return call.call(); }
            catch (RuntimeException e) { throw e; }
            catch (Exception e) { throw new IllegalStateException(e); }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("CPF context close failed", e);
        }
    }

    public <REQ,RES> CpfSoapResult<RES> call(URI endpoint,String soapAction,String transactionId,REQ request,Class<RES> responseType){
        Objects.requireNonNull(endpoint,"endpoint"); Objects.requireNonNull(request,"request"); Objects.requireNonNull(responseType,"responseType");
        if(transactionId==null||transactionId.isBlank())throw new IllegalArgumentException("transactionId is required");
        CpfContext current=CpfContexts.current();if(current!=null&&!current.transaction().transactionId().equals(transactionId.trim()))throw new SecurityException("SOAP transactionId cannot override bound CPF context");
        try {
            Object response=template.marshalSendAndReceive(endpoint.toString(),request,callback(soapAction,transactionId,current));
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
        CpfContext current=CpfContexts.current();if(current!=null&&!current.transaction().transactionId().equals(transactionId.trim()))throw new SecurityException("SOAP transactionId cannot override bound CPF context");
        if(xmlRequest==null||xmlRequest.isBlank())throw new IllegalArgumentException("xmlRequest is required");
        try {
            java.io.StringWriter response=new java.io.StringWriter();
            boolean received=template.sendSourceAndReceiveToResult(endpoint.toString(),new StreamSource(new java.io.StringReader(xmlRequest)),callback(soapAction,transactionId,current),new javax.xml.transform.stream.StreamResult(response));
            if(!received)return CpfSoapResult.failed("SOAP_EMPTY_RESPONSE");
            return CpfSoapResult.success(response.toString());
        } catch (WebServiceIOException e) {
            return CpfSoapResult.unknown(isTimeout(e)?"SOAP_TIMEOUT":"SOAP_TRANSPORT_UNKNOWN");
        } catch (RuntimeException e) {
            return CpfSoapResult.failed("SOAP_CALL_FAILED");
        }
    }

    public WebServiceTemplate nativeTemplate(){return template;}

    private static WebServiceMessageCallback callback(String action,String tx,CpfContext context){return message->{
        if(message instanceof SoapMessage soap){
            if(action!=null&&!action.isBlank())soap.setSoapAction(action);
            StringBuilder xml=new StringBuilder("<cpf:lineage xmlns:cpf=\""+CPF_NS+"\">").append("<cpf:transactionId>").append(escape(tx)).append("</cpf:transactionId>");
            if(context!=null){xml.append("<cpf:rootTransactionId>").append(escape(context.transaction().rootTransactionId())).append("</cpf:rootTransactionId>")
                    .append("<cpf:businessDate>").append(context.transaction().businessDate()).append("</cpf:businessDate>")
                    .append("<cpf:executionId>").append(escape(context.execution().executionId())).append("</cpf:executionId>")
                    .append("<cpf:segmentId>").append(escape(context.execution().segmentId())).append("</cpf:segmentId>");
                if(context.operation()!=null&&context.operation().idempotencyKey()!=null)xml.append("<cpf:idempotencyKey>").append(escape(context.operation().idempotencyKey())).append("</cpf:idempotencyKey>");}
            xml.append("</cpf:lineage>");
            javax.xml.transform.TransformerFactory.newInstance().newTransformer().transform(new StreamSource(new java.io.StringReader(xml.toString())),soap.getSoapHeader().getResult());
        }
    };}
    private static boolean isTimeout(Throwable e){for(Throwable t=e;t!=null;t=t.getCause())if(t instanceof SocketTimeoutException)return true;return false;}
    private static String escape(String value){return value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&apos;");}
}

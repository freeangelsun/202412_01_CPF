package com.cpf.core.common.http;
import com.cpf.core.api.security.network.CpfNetworkEndpointPolicy;
import java.net.*; import java.util.*;
public final class NetworkRegistryHarness {
  static int assertions=0;
  static void check(boolean ok,String m){assertions++;if(!ok)throw new AssertionError(m);}
  static <T extends Throwable> T expect(Class<T> type,Runnable r,String contains){assertions++;try{r.run();}catch(Throwable t){if(!type.isInstance(t))throw new AssertionError("wrong failure "+t);if(contains!=null&&!String.valueOf(t.getMessage()).contains(contains))throw new AssertionError("message "+t.getMessage());return type.cast(t);}throw new AssertionError("expected "+type.getName());}
  static CpfServiceEndpointProperties props(String url, boolean dns, boolean priv, boolean pub, List<String> pins){
    var p=new CpfServiceEndpointProperties(); var e=new CpfServiceEndpointProperties.ServiceEndpoint();
    e.setBaseUrl(url);e.setAllowDns(dns);e.setAllowPrivate(priv);e.setAllowPublic(pub);e.setAllowedPorts(List.of(443));e.setPinnedAddresses(pins);p.setServices(Map.of("PAYMENT",e));return p;
  }
  static InetAddress ip(String x){try{return InetAddress.getByName(x);}catch(Exception e){throw new RuntimeException(e);}}
  public static void main(String[] a) throws Exception {
    var secure=CpfNetworkEndpointPolicy.secureDefault();
    var good=new CpfServiceEndpointRegistry(props("https://payment.example",true,false,true,List.of("1.1.1.1","8.8.8.8")),secure,h->new InetAddress[]{ip("8.8.8.8"),ip("1.1.1.1")});
    var r=good.resolvedEndpoint("PAYMENT");
    check(r.serviceId().equals("payment"),"normalized id"); check(r.baseUrl().equals("https://payment.example"),"base");
    check(r.pinnedAddress().getHostAddress().equals("1.1.1.1"),"sorted deterministic pin"); check(r.port()==443,"port");
    check(r.authority().equals("payment.example"),"authority"); check(r.validatedAddresses().equals(List.of("1.1.1.1","8.8.8.8")),"validated list");
    expect(IllegalArgumentException.class,()->new CpfServiceEndpointRegistry(props("https://payment.example",false,false,true,List.of()),secure,h->new InetAddress[]{ip("1.1.1.1")}).resolvedEndpoint("payment"),"allowDns");
    expect(IllegalArgumentException.class,()->new CpfServiceEndpointRegistry(props("https://payment.example",true,false,true,List.of("8.8.8.8")),secure,h->new InetAddress[]{ip("1.1.1.1")}).resolvedEndpoint("payment"),"pin 불일치");
    expect(IllegalArgumentException.class,()->new CpfServiceEndpointRegistry(props("https://payment.example",true,true,true,List.of()),secure,h->new InetAddress[]{ip("10.0.0.8"),ip("8.8.8.8")}).resolvedEndpoint("payment"),"mixed private/public");
    expect(IllegalArgumentException.class,()->new CpfServiceEndpointRegistry(props("https://payment.example",true,false,true,List.of()),secure,h->new InetAddress[]{ip("127.0.0.1")}).resolvedEndpoint("payment"),"Loopback");
    expect(IllegalArgumentException.class,()->new CpfServiceEndpointRegistry(props("http://payment.example",true,false,true,List.of()),secure,h->new InetAddress[]{ip("1.1.1.1")}).resolvedEndpoint("payment"),"scheme");
    expect(IllegalArgumentException.class,()->new CpfServiceEndpointRegistry(props("https://payment.example:444",true,false,true,List.of()),secure,h->new InetAddress[]{ip("1.1.1.1")}).resolvedEndpoint("payment"),"port");
    var lit=new CpfServiceEndpointRegistry(props("https://8.8.8.8",false,false,true,List.of("8.8.8.8")),secure,h->{throw new AssertionError("DNS called");}).resolvedEndpoint("payment");
    check(lit.pinnedAddress().getHostAddress().equals("8.8.8.8"),"literal pin");
    var runtime=new CpfServiceEndpointRegistry(props("https://8.8.8.8",false,false,true,List.of()),secure,h->new InetAddress[0]);
    runtime.replaceRuntime(1,Map.of("PAYMENT",new CpfServiceEndpointRegistry.RuntimeEndpoint("payment","HTTP","https://1.1.1.1","","", "",1000,true,false,Map.of("allowPublic","true","allowedPorts","443"))));
    check(runtime.resolvedEndpoint("payment").pinnedAddress().getHostAddress().equals("1.1.1.1"),"runtime endpoint");
    expect(IllegalArgumentException.class,()->runtime.replaceRuntime(0,Map.of()),"역행");
    var factory=new CpfPinnedHttpConnectorFactory(1234,5678);
    var connector=factory.connector(r);
    var http=((org.springframework.http.client.reactive.ReactorClientHttpConnector)connector).client;
    var socket=(java.net.InetSocketAddress)http.remote.get();
    check(socket.getAddress().getHostAddress().equals("1.1.1.1")&&socket.getPort()==443,"connector remoteAddress is validated pin");
    check(http.connectTimeout==1234&&http.responseTimeout.toMillis()==5678,"connector timeouts");
    expect(IllegalArgumentException.class,()->factory.connector(null),"resolved endpoint");
    System.out.println("PASS assertions="+assertions+" actualRegistry=true actualNetworkPolicy=true actualPinnedConnector=true");
  }
}

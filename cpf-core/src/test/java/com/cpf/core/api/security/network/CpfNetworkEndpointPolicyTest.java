package com.cpf.core.api.security.network;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.junit.jupiter.api.Test;
class CpfNetworkEndpointPolicyTest {
 @Test void acceptsPublicLiteralAndCidr(){var p=new CpfNetworkEndpointPolicy(List.of("8.8.8.0/24"),List.of(443),false,false,true);var d=p.validateEndpoint("https://8.8.8.8/api");
         assertTrue(d.literalAddress());assertTrue(p.contains("8.8.8.0/24","8.8.8.8"));}
 @Test void rejectsPrivateMetadataHostnameAndBadPort(){var p=CpfNetworkEndpointPolicy.secureDefault();assertThrows(IllegalArgumentException.class,
         ()->p.validateEndpoint("https://127.0.0.1"));assertThrows(IllegalArgumentException.class,()->p.validateEndpoint("https://169.254.169.254"));
         assertThrows(IllegalArgumentException.class,()->p.validateEndpoint("https://example.com"));assertThrows(IllegalArgumentException.class,()->p.validateEndpoint("https://8.8.8.8:22"));}
 @Test void validatesIpv6AndDnsRebinding(){var p=new CpfNetworkEndpointPolicy(List.of("2001:4860::/32"),List.of(443),false,true,true);
         assertTrue(p.validateEndpoint("https://example.com").resolveBeforeConnect());assertEquals(List.of("2001:4860:4860:0:0:0:0:8888"),p.validateResolvedAddresses("example.com",
         List.of("2001:4860:4860::8888")));assertThrows(IllegalArgumentException.class,()->p.validateResolvedAddresses("example.com",List.of("::1")));}
 @Test void supportsPrivateOnlyGatewayPolicy(){var p=new CpfNetworkEndpointPolicy(List.of("10.0.0.0/8"),List.of(8443),true,false,true,true);
         assertTrue(p.validateEndpoint("https://service.internal:8443").resolveBeforeConnect());assertEquals(List.of("10.1.2.3"),p.validateResolvedAddresses("service.internal",
         List.of("10.1.2.3")));assertThrows(IllegalArgumentException.class,()->p.validateResolvedAddresses("service.internal",List.of("8.8.8.8")));}
 @Test void alwaysRejectsSpecialUseEvenWhenPrivateAllowed(){var p=new CpfNetworkEndpointPolicy(List.of(),List.of(443),true,true,true,true);for(String value:List.of("127.0.0.1",
         "169.254.169.254","100.100.100.200","::1","fe80::1"))assertThrows(IllegalArgumentException.class,()->p.validateResolvedAddresses("service.internal",List.of(value)));}
 @Test void rejectsMalformedCorpus(){for(String value:List.of("999.1.1.1","01.2.3.4","fe80::1%eth0","2001:::1"))assertThrows(IllegalArgumentException.class,()->CpfNetworkEndpointPolicy.Address.parse(value));}
}

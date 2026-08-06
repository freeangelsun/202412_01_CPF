package com.cpf.starter.integration.webhook;
import java.net.*; import java.util.*;
/** Fail-closed SSRF endpoint validation for HTTPS callbacks. */
public final class CpfWebhookEndpointValidator {
 private final Set<String> allowedHosts;
 public CpfWebhookEndpointValidator(Set<String> allowedHosts){this.allowedHosts=allowedHosts==null?Set.of():allowedHosts.stream().map(s->s.toLowerCase(Locale.ROOT)).collect(java.util.stream.Collectors.toUnmodifiableSet());}
 public void validate(URI uri){if(uri==null||!"https".equalsIgnoreCase(uri.getScheme())||uri.getUserInfo()!=null||uri.getHost()==null||uri.getFragment()!=null)throw new IllegalArgumentException("Webhook endpoint must be an absolute HTTPS URI without user-info or fragment");String host=uri.getHost().toLowerCase(Locale.ROOT);if(!allowedHosts.isEmpty()&&!allowedHosts.contains(host))throw new SecurityException("Webhook host is not allowlisted: "+host);if(host.equals("localhost")||host.endsWith(".localhost")||host.endsWith(".local"))throw new SecurityException("Local webhook endpoint rejected");if(isLiteralPrivate(host))throw new SecurityException("Private/link-local webhook address rejected");}
 public void validateResolved(URI uri,InetAddress[] resolved){validate(uri);for(InetAddress address:resolved)if(address.isAnyLocalAddress()||address.isLoopbackAddress()||address.isLinkLocalAddress()||address.isSiteLocalAddress()||address.isMulticastAddress())throw new SecurityException("Resolved webhook address is not public: "+address.getHostAddress());}
 private static boolean isLiteralPrivate(String host){try{InetAddress a=InetAddress.getByName(host);return a.isAnyLocalAddress()||a.isLoopbackAddress()||a.isLinkLocalAddress()||a.isSiteLocalAddress()||a.isMulticastAddress();}catch(UnknownHostException ex){return false;}}
}

package com.cpf.integration.http.internal;
import java.util.*;
public class CpfServiceEndpointProperties {
  private Map<String,ServiceEndpoint> services=new LinkedHashMap<>();
  public Map<String,ServiceEndpoint> getServices(){return services;} public void setServices(Map<String,ServiceEndpoint> v){services=v;}
  public static class ServiceEndpoint {
    private String baseUrl,description; private boolean allowDns,allowPrivate,allowPublic=true;
    private List<String> allowedCidrs=List.of(), pinnedAddresses=List.of(); private List<Integer> allowedPorts=List.of(443,8443,9443);
    public String getBaseUrl(){return baseUrl;} public void setBaseUrl(String v){baseUrl=v;}
    public String getDescription(){return description;} public void setDescription(String v){description=v;}
    public boolean isAllowDns(){return allowDns;} public void setAllowDns(boolean v){allowDns=v;}
    public boolean isAllowPrivate(){return allowPrivate;} public void setAllowPrivate(boolean v){allowPrivate=v;}
    public boolean isAllowPublic(){return allowPublic;} public void setAllowPublic(boolean v){allowPublic=v;}
    public List<String> getAllowedCidrs(){return allowedCidrs;} public void setAllowedCidrs(List<String> v){allowedCidrs=v;}
    public List<Integer> getAllowedPorts(){return allowedPorts;} public void setAllowedPorts(List<Integer> v){allowedPorts=v;}
    public List<String> getPinnedAddresses(){return pinnedAddresses;} public void setPinnedAddresses(List<String> v){pinnedAddresses=v;}
  }
}

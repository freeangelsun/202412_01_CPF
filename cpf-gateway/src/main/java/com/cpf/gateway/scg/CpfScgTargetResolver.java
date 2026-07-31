package com.cpf.gateway.scg;

import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.servicecall.CpfServiceRegistryQueryPort;
import com.cpf.core.api.servicecall.CpfServiceRegistryView;
import java.net.URI;import java.util.List;import java.util.concurrent.ConcurrentHashMap;import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;
/** ACK Route의 Service Registry UP 인스턴스만 대상으로 선택합니다. */
@Component
public final class CpfScgTargetResolver {
 private final CpfServiceRegistryQueryPort registry;private final ConcurrentHashMap<String,AtomicLong> cursors=new ConcurrentHashMap<>();
 public CpfScgTargetResolver(CpfServiceRegistryQueryPort registry){this.registry=registry;}
 public Target resolve(CpfGatewayRoute route,String targetPath,String rawQuery){
  List<CpfServiceRegistryView.Instance> candidates=registry.instances(route.serviceId(),null,"UP",100).stream().filter(i->i.active()&&!i.maintenance()&&!i.draining()&&"UP".equals(i.status())).toList();
  if(candidates.isEmpty())throw new IllegalStateException("Gateway UP target가 없습니다: "+route.serviceId());
  long n=cursors.computeIfAbsent(route.serverGroupId(),k->new AtomicLong()).getAndIncrement();
  var selected=candidates.get(Math.floorMod(n,candidates.size())); URI base=URI.create(selected.baseUrl());
  if(!("http".equalsIgnoreCase(base.getScheme())||"https".equalsIgnoreCase(base.getScheme()))||base.getUserInfo()!=null||base.getFragment()!=null)throw new SecurityException("허용되지 않은 Gateway upstream URI");
  String path=targetPath.startsWith("/")?targetPath:"/"+targetPath;String query=rawQuery==null||rawQuery.isBlank()?"":"?"+rawQuery;
  return new Target(selected.instanceId(),URI.create(base.toString().replaceAll("/$","")+path+query));
 }
 public record Target(String instanceId,URI uri){}
}

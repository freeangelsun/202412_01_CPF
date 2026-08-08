package com.cpf.starter.foundation.base;
import com.cpf.core.api.config.*;import java.util.*;import org.springframework.beans.factory.SmartInitializingSingleton;import org.springframework.context.ApplicationContext;
/** Discovers @CpfConfigPolicy on actual configured beans and exposes a stable runtime catalog. */
public final class CpfConfigurationPolicyCatalog implements CpfConfigCatalog, SmartInitializingSingleton {
 private final ApplicationContext context;private volatile Map<String,CpfConfigDescriptor> descriptors=Map.of();
 public CpfConfigurationPolicyCatalog(ApplicationContext context){this.context=Objects.requireNonNull(context);}
 @Override public void afterSingletonsInstantiated(){Map<String,CpfConfigDescriptor> found=new TreeMap<>();for(String name:context.getBeanDefinitionNames()){Class<?> type=context.getType(name);if(type==null)continue;CpfConfigPolicy p=org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation(type,CpfConfigPolicy.class);if(p==null)continue;CpfConfigDescriptor d=new CpfConfigDescriptor(p.prefix(),p.mutability(),p.secretSeparated(),type.getName());CpfConfigDescriptor old=found.putIfAbsent(p.prefix(),d);if(old!=null&&!old.equals(d))throw new IllegalStateException("Conflicting CPF config policy for prefix "+p.prefix());}descriptors=Map.copyOf(found);}
 @Override public List<CpfConfigDescriptor> descriptors(){return descriptors.values().stream().toList();}
 @Override public Optional<CpfConfigDescriptor> find(String prefix){return Optional.ofNullable(descriptors.get(prefix));}
}

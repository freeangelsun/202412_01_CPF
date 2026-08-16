package com.cpf.data.persistence.runtime;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.data.persistence.api.CpfRepository;
import com.cpf.data.persistence.api.CpfRepositoryContract;
import java.util.LinkedHashSet;
import java.util.Set;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * @CpfRepository를 Interface/Concrete class 양쪽에서 찾아 동일한 Context 정책을 적용합니다.
 * Interface Port는 CpfRepositoryContract를 강제하고, concrete Repository는 native provider 구현을 허용합니다.
 */
public final class CpfRepositoryPolicyBeanPostProcessor implements BeanPostProcessor {
    private final CpfRepositoryPolicyProperties properties;
    public CpfRepositoryPolicyBeanPostProcessor(CpfRepositoryPolicyProperties properties){this.properties=properties;}

    @Override public Object postProcessAfterInitialization(Object bean,String beanName)throws BeansException{
        if(!properties.isEnabled()||bean==null)return bean;
        Class<?> beanClass=bean.getClass();
        CpfRepository classAnnotation=AnnotatedElementUtils.findMergedAnnotation(beanClass,CpfRepository.class);
        Set<Class<?>> repositories=annotatedRepositoryInterfaces(beanClass);
        if(classAnnotation==null&&repositories.isEmpty())return bean;
        for(Class<?> repository:repositories){
            if(!CpfRepositoryContract.class.isAssignableFrom(repository)){
                throw new IllegalStateException("@CpfRepository interface must extend CpfRepositoryContract: "+repository.getName());
            }
        }
        boolean contextRequired=classAnnotation!=null&&classAnnotation.contextRequired();
        contextRequired=contextRequired||repositories.stream()
                .map(r->AnnotatedElementUtils.findMergedAnnotation(r,CpfRepository.class))
                .filter(java.util.Objects::nonNull).anyMatch(CpfRepository::contextRequired);
        final boolean requireContext=contextRequired;
        ProxyFactory factory=new ProxyFactory(bean);
        repositories.forEach(factory::addInterface);
        if(repositories.isEmpty())factory.setProxyTargetClass(true);
        MethodInterceptor advice=invocation->{
            if(requireContext)CpfContexts.requireCurrent();
            try{return invocation.proceed();}
            catch(RuntimeException e){throw e;}
            catch(Throwable e){throw new IllegalStateException("CPF_REPOSITORY_INVOCATION_FAILED:"+invocation.getMethod(),e);}
        };
        factory.addAdvice(advice);
        return factory.getProxy(beanClass.getClassLoader());
    }

    static Set<Class<?>> annotatedRepositoryInterfaces(Class<?> type){
        Set<Class<?>> result=new LinkedHashSet<>();
        collect(type,result,new LinkedHashSet<>());
        return result;
    }
    private static void collect(Class<?> type,Set<Class<?>> result,Set<Class<?>> visited){
        if(type==null||!visited.add(type))return;
        for(Class<?> itf:type.getInterfaces()){
            if(AnnotatedElementUtils.findMergedAnnotation(itf,CpfRepository.class)!=null)result.add(itf);
            collect(itf,result,visited);
        }
        collect(type.getSuperclass(),result,visited);
    }
}

package com.cpf.data.persistence.runtime;

import com.cpf.data.persistence.api.CpfRepository;
import com.cpf.data.persistence.api.CpfRepositoryContract;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;

/** @CpfRepository Interface의 CPF Repository contract 준수 여부만 시작 시 검증합니다. */
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
        return bean;
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

package com.cpf.data.persistence.transaction;
import java.lang.reflect.Method;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
final class CpfTxPointcut extends StaticMethodMatcherPointcut {
 @Override public boolean matches(Method method,Class<?> targetClass){return CpfTxAnnotationResolver.resolve(method,targetClass)!=null;}
}

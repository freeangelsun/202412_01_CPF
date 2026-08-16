package com.cpf.data.persistence.transaction;
import com.cpf.data.persistence.api.annotation.CpfTx;
import java.lang.reflect.Method;
import org.springframework.core.annotation.AnnotatedElementUtils;
final class CpfTxAnnotationResolver {
 private CpfTxAnnotationResolver() { }
 static CpfTx resolve(Method method,Class<?> targetClass) {
  CpfTx a=AnnotatedElementUtils.findMergedAnnotation(method,CpfTx.class);
  return a!=null?a:(targetClass==null?null:AnnotatedElementUtils.findMergedAnnotation(targetClass,CpfTx.class));
 }
}

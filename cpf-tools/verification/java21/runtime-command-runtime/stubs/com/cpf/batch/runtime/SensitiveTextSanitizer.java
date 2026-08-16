package com.cpf.batch.runtime;
public final class SensitiveTextSanitizer {
 private SensitiveTextSanitizer(){}
 public static String sanitize(String s){
  if(s==null)return null;
  return s.replaceAll("(?i)(password|token|secret|authorization|cookie|session(?:id)?)\\s*[=:]\\s*[^,;\\s]+", "$1=<masked>");
 }
}

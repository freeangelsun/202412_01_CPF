package com.cpf.common.template;
import java.util.*;import java.util.regex.*;
/** ${variable}만 허용하는 fail-closed 템플릿 렌더러입니다. */
public final class CmnTemplateRenderer {
 private static final Pattern TOKEN=Pattern.compile("\\$\\{([A-Za-z][A-Za-z0-9_.-]{0,99})}");
 public String render(CmnTemplateDefinition def,Map<String,?> vars){Objects.requireNonNull(def);Map<String,?> values=vars==null?Map.of():vars;Matcher m=TOKEN.matcher(def.body());StringBuffer out=new StringBuffer();Set<String> used=new LinkedHashSet<>();while(m.find()){String name=m.group(1);used.add(name);if(!def.allowedVariables().contains(name))throw new IllegalArgumentException("Template variable is not allowed: "+name);if(!values.containsKey(name))throw new IllegalArgumentException("Template variable is missing: "+name);m.appendReplacement(out,Matcher.quoteReplacement(String.valueOf(values.get(name))));}m.appendTail(out);Set<String> unknown=new LinkedHashSet<>(values.keySet());unknown.removeAll(def.allowedVariables());if(!unknown.isEmpty())throw new IllegalArgumentException("Unknown template variables: "+unknown);return out.toString();}
}

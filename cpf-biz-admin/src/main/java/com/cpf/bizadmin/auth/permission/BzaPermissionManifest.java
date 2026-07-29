package com.cpf.bizadmin.auth.permission;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/** BZA Frontend와 API Filter가 공유하는 Menu/Action 권한 정본입니다. */
@Component
public class BzaPermissionManifest {
    static final String CLASSPATH_LOCATION="cpf/bza/bza-permission-manifest.json";
    private final Definition definition;
    public BzaPermissionManifest(ObjectMapper mapper){this.definition=read(mapper);}

    public Optional<ApiPermission> resolve(String method,String relativePath){
        String path=normalizePath(relativePath);
        String menu=definition.apiResourceGroups().entrySet().stream()
                .filter(e->path.equals(e.getKey())||path.startsWith(e.getKey()+"/"))
                .max(Comparator.comparingInt(e->e.getKey().length())).map(Map.Entry::getValue).map(this::canonicalMenuCode).orElse(null);
        if(menu==null)return Optional.empty();
        String normalizedMethod=method==null?"":method.trim().toUpperCase(Locale.ROOT);
        String action=definition.actionRules().stream().filter(r->r.matches(normalizedMethod,path)).map(ActionRule::actionCode).findFirst()
                .orElseGet(()->"GET".equals(normalizedMethod)?"READ":"DELETE".equals(normalizedMethod)?"DELETE":"WRITE");
        return Optional.of(new ApiPermission(menu,action));
    }
    public Optional<String> resolveApiMenuCode(String relativePath){return resolve("GET",relativePath).map(ApiPermission::menuCode);}
    public String canonicalMenuCode(String value){String n=normalizeCode(value);if(n.startsWith("BZA_"))n=n.substring(4);return definition.permissionAliases().getOrDefault(n,n);}
    public List<String> menuGroups(){return definition.menuGroups();}
    public String sourceProjection(){return definition.sourceProjection();}

    private Definition read(ObjectMapper mapper){
        try(InputStream in=new ClassPathResource(CLASSPATH_LOCATION).getInputStream()){
            RawDefinition raw=mapper.readValue(in,RawDefinition.class);
            if(raw.schemaVersion()!=2||raw.menuGroups()==null||raw.menuGroups().isEmpty()||raw.apiResourceGroups()==null||raw.apiResourceGroups().isEmpty())
                throw new IllegalStateException("BZA permission manifest v2 필수 Metadata가 없습니다.");
            Map<String,String> resources=new LinkedHashMap<>();raw.apiResourceGroups().forEach((k,v)->resources.put(normalizePath(k),normalizeCode(v)));
            Map<String,String> aliases=new LinkedHashMap<>();if(raw.permissionAliases()!=null)raw.permissionAliases().forEach((k,v)->aliases.put(normalizeCode(k),normalizeCode(v)));
            List<ActionRule> rules=Optional.ofNullable(raw.actionRules()).orElse(List.of()).stream().map(r->new ActionRule(normalizeCode(r.method()),normalizePath(r.pathPattern()),normalizeCode(r
                    .actionCode()))).toList();
            return new Definition(raw.schemaVersion(),raw.owner(),raw.sourceProjection(),raw.menuGroups().stream().map(BzaPermissionManifest::normalizeCode).toList(),Map.copyOf(resources),rules,Map.copyOf(aliases));
        }catch(IOException e){throw new IllegalStateException("BZA permission manifest를 읽을 수 없습니다.",e);}
    }
    private static String normalizePath(String v){return v==null?"":v.replace('\\','/').replaceAll("^/+|/+$","").toLowerCase(Locale.ROOT);}
    private static String normalizeCode(String v){return v==null?"":v.trim().toUpperCase(Locale.ROOT);}
    private record RawDefinition(int schemaVersion,String owner,String sourceProjection,List<String> menuGroups,Map<String,String> apiResourceGroups,List<RawActionRule> actionRules,Map<String,String> permissionAliases){}
    private record RawActionRule(String method,String pathPattern,String actionCode){}
    private record Definition(int schemaVersion,String owner,String sourceProjection,List<String> menuGroups,Map<String,String> apiResourceGroups,List<ActionRule> actionRules,Map<String,String> permissionAliases){}
    private record ActionRule(String method,String pathPattern,String actionCode){
        boolean matches(String actualMethod,String path){return ("*".equals(method)||method.equals(actualMethod))&&Pattern.matches(toRegex(pathPattern),path);}
        private static String toRegex(String glob){StringBuilder out=new StringBuilder("^");for(int i=0;i<glob.length();i++){char c=glob.charAt(i);if(c=='*'){if(i+1<glob.length()&&glob.charAt(i+
                1)=='*'){out.append(".*");i++;}else out.append("[^/]*");}else{if(".[]{}()+-^$|\\".indexOf(c)>=0)out.append('\\');out.append(c);}}return out.append('$').toString();}
    }
    public record ApiPermission(String menuCode,String actionCode){}
}

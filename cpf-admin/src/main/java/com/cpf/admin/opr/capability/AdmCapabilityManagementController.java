package com.cpf.admin.opr.capability;

import com.cpf.admin.common.base.AdmBaseController;
import com.cpf.admin.opr.health.AdmHealthInstanceRegistry;
import com.cpf.platform.operations.api.health.CpfDependencyHealth;
import com.cpf.platform.operations.api.health.CpfHealthStatus;
import com.cpf.platform.operations.api.health.CpfRuntimeHealth;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Public Starter/Capability의 자동 Runtime registration을 ADM 공통 운영 관점으로 집계합니다.
 * System/Domain/Application/Instance/Starter 정보는 Runtime이 자동 보고하며 운영자 수기 등록을 요구하지 않습니다.
 */
@RestController
@RequestMapping("/adm/api/capability-management")
@Tag(name="ADM-CapabilityManagement", description="시스템/도메인/인스턴스별 CPF Capability 상태·이슈 통합 관제")
public class AdmCapabilityManagementController extends AdmBaseController {
    private static final int MAX_FLEET_READ = 200;
    private final AdmHealthInstanceRegistry registry;
    public AdmCapabilityManagementController(AdmHealthInstanceRegistry registry){this.registry=registry;}

    @GetMapping("/overview")    @Operation(operationId="admCapabilityManagementOverview", summary="CPF Capability Fleet 통합 현황")
    public ResponseEntity<Overview> overview(
            @RequestParam(required=false) String environment,
            @RequestParam(required=false) String systemCode,
            @RequestParam(required=false) String systemId,
            @RequestParam(required=false) String domainCode,
            @RequestParam(required=false) String domainId,
            @RequestParam(required=false) String application,
            @RequestParam(required=false) String module,
            @RequestParam(required=false) String host,
            @RequestParam(required=false) String instanceId,
            @RequestParam(required=false) String starterId,
            @RequestParam(required=false) String capabilityId,
            @RequestParam(required=false) String provider,
            @RequestParam(required=false) String version,
            @RequestParam(required=false) String status,
            @RequestParam(defaultValue="false") boolean includeStale,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="50") int size) {
        int safeSize=Math.min(200,Math.max(1,size));
        int safePage=Math.max(0,page);
        String effectiveSystem = firstText(systemId, systemCode);
        List<AdmHealthInstanceRegistry.Entry> source=registry.search(effectiveSystem,status,includeStale,0,MAX_FLEET_READ);
        List<InstanceView> filtered=source.stream().map(AdmCapabilityManagementController::view)
                .filter(v->matches(v.environment(),environment))
                .filter(v->matches(v.systemCode(),systemCode))
                .filter(v->matches(v.systemId(),systemId))
                .filter(v->matches(v.domainCode(),domainCode))
                .filter(v->matches(v.domainId(),domainId))
                .filter(v->matches(v.application(),application))
                .filter(v->matches(v.module(),module))
                .filter(v->matches(v.host(),host))
                .filter(v->matches(v.instanceId(),instanceId))
                .filter(v->matches(v.version(),version))
                .filter(v->v.capabilities().stream().anyMatch(c->matchesCapability(c,starterId,capabilityId,provider)))
                .sorted(Comparator.comparing(InstanceView::systemId).thenComparing(InstanceView::instanceId)).toList();
        long total=filtered.size();
        List<InstanceView> items=filtered.stream().skip((long)safePage*safeSize).limit(safeSize).toList();
        long issueCount=filtered.stream().mapToLong(v->v.issues().size()).sum();
        long downCount=filtered.stream().filter(v->"DOWN".equals(v.readiness())||"OUT_OF_SERVICE".equals(v.readiness())).count();
        long unknownCount=filtered.stream().filter(v->"UNKNOWN".equals(v.readiness())||v.stale()).count();
        return ResponseEntity.ok(new Overview(items,total,issueCount,downCount,unknownCount,safePage,safeSize));
    }

    @GetMapping("/issues")    @Operation(operationId="admCapabilityManagementIssues", summary="CPF Capability 장애·UNKNOWN 현황")
    public ResponseEntity<List<IssueView>> issues(
            @RequestParam(required=false) String systemCode,
            @RequestParam(required=false) String systemId,
            @RequestParam(required=false) String starterId,
            @RequestParam(required=false) String capabilityId,
            @RequestParam(required=false) String provider,
            @RequestParam(defaultValue="false") boolean includeStale) {
        String effectiveSystem=firstText(systemId,systemCode);
        List<IssueView> out=new ArrayList<>();
        for(AdmHealthInstanceRegistry.Entry entry:registry.search(effectiveSystem,null,includeStale,0,MAX_FLEET_READ)){
            InstanceView v=view(entry);
            if(v.capabilities().stream().noneMatch(c->matchesCapability(c,starterId,capabilityId,provider)))continue;
            out.addAll(v.issues());
            if(entry.stale()) out.add(new IssueView(v.systemId(),v.domainCode(),v.instanceId(),"runtime-report","UNKNOWN","STALE_RUNTIME_REPORT",0,entry.reportedAt()));
        }
        out.sort(Comparator.comparing(IssueView::systemId).thenComparing(IssueView::instanceId).thenComparing(IssueView::dependency));
        return ResponseEntity.ok(List.copyOf(out));
    }

    private static InstanceView view(AdmHealthInstanceRegistry.Entry entry){
        CpfRuntimeHealth h=entry.health(); Map<String,String> d=h.details();
        List<IssueView> issues=h.dependencies().stream().filter(dep->dep.status()!=CpfHealthStatus.UP)
                .map(dep->issue(h,d,dep)).toList();
        List<CapabilityView> capabilities=capabilities(h,d);
        String systemCode=firstText(value(d,"systemCode"),h.systemId());
        return new InstanceView(value(d,"environment"),systemCode,h.systemId(),value(d,"domainCode"),value(d,"domainId"),
                value(d,"application"),value(d,"module"),value(d,"host"),h.instanceId(),h.readiness().name(),entry.stale(),
                h.version(),h.buildSha(),h.observedAt(),capabilities,issues);
    }

    private static List<CapabilityView> capabilities(CpfRuntimeHealth health,Map<String,String> details){
        List<CapabilityView> out=new ArrayList<>();
        for(String id:health.capabilities()){
            String p="starterMeta."+id+".";
            String artifact=value(details,p+"artifactId");
            if(artifact.isBlank()) artifact=id;
            boolean operatorVisible=bool(details,p+"operatorVisible");
            if(!operatorVisible) continue;
            out.add(new CapabilityView(id,artifact,value(details,p+"capability"),value(details,p+"provider"),
                    value(details,p+"category"),value(details,p+"usageLevel"),bool(details,p+"runtimeRequired"),
                    bool(details,p+"dedicatedWorkflow"),operatorVisible,bool(details,p+"automaticRegistration"),
                    value(details,p+"managementScope"),csv(details,p+"commonAreas"),support(details,p+"supports")));
        }
        return out.stream().sorted(Comparator.comparing(CapabilityView::starterArtifactId).thenComparing(CapabilityView::id)).toList();
    }
    private static SupportView support(Map<String,String> details,String key){
        Map<String,String> values=new LinkedHashMap<>();
        for(String pair:value(details,key).split(",")){int at=pair.indexOf('=');if(at>0)values.put(pair.substring(0,at),pair.substring(at+1));}
        return new SupportView(flag(values,"health"),flag(values,"metrics"),flag(values,"logs"),flag(values,"trace"),
                flag(values,"effectiveConfig"),flag(values,"failure"),flag(values,"audit"),flag(values,"dynamicConfig"),
                flag(values,"runtimeControl"),flag(values,"recovery"));
    }
    private static boolean matchesCapability(CapabilityView c,String starterId,String capabilityId,String provider){
        return matches(c.starterArtifactId(),starterId)&&matches(c.capabilityId(),capabilityId)&&matches(c.provider(),provider);
    }
    private static IssueView issue(CpfRuntimeHealth h,Map<String,String>d,CpfDependencyHealth dep){return new IssueView(h.systemId(),value(d,"domainCode"),h.instanceId(),dep.name(),dep.status().name(),dep.reasonCode(),dep.latencyMillis(),dep.checkedAt());}
    private static String value(Map<String,String> map,String key){String v=map.get(key);return v==null?"":v;}
    private static boolean bool(Map<String,String> map,String key){return Boolean.parseBoolean(value(map,key));}
    private static boolean flag(Map<String,String> map,String key){return Boolean.parseBoolean(map.getOrDefault(key,"false"));}
    private static List<String> csv(Map<String,String> map,String key){String value=value(map,key);if(value.isBlank())return List.of();return java.util.Arrays.stream(value.split(",")).map(String::trim).filter(v->!v.isEmpty()).toList();}
    private static String firstText(String... values){for(String v:values)if(v!=null&&!v.isBlank())return v.trim();return "";}
    private static boolean matches(String value,String filter){return filter==null||filter.isBlank()||(value!=null&&value.toLowerCase(Locale.ROOT).contains(filter.trim().toLowerCase(Locale.ROOT)));}

    public record Overview(List<InstanceView> items,long total,long issueCount,long downCount,long unknownCount,int page,int size){}
    public record InstanceView(String environment,String systemCode,String systemId,String domainCode,String domainId,String application,String module,
            String host,String instanceId,String readiness,boolean stale,String version,String buildSha,Instant observedAt,List<CapabilityView> capabilities,List<IssueView> issues){}
    public record CapabilityView(String id,String starterArtifactId,String capabilityId,String provider,String category,String usageLevel,
            boolean runtimeRequired,boolean dedicatedWorkflow,boolean operatorVisible,boolean automaticRegistration,String managementScope,List<String> commonAreas,SupportView support){}
    public record SupportView(boolean health,boolean metrics,boolean logs,boolean trace,boolean effectiveConfig,boolean failure,boolean audit,
            boolean dynamicConfig,boolean runtimeControl,boolean recovery){}
    public record IssueView(String systemId,String domainCode,String instanceId,String dependency,String status,String reasonCode,long latencyMillis,Instant checkedAt){}
}

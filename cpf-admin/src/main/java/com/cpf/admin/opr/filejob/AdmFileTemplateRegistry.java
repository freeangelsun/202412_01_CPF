package com.cpf.admin.opr.filejob;

import com.cpf.core.api.tabular.*;
import org.springframework.stereotype.Component;
import java.util.*;

/** Upload/Download Template와 versioned schema의 단일 Registry입니다. */
@Component
public class AdmFileTemplateRegistry {
    private final Map<String,Template> templates;

    public AdmFileTemplateRegistry(List<AdmFileJobConsumer> consumers) {
        Map<String,AdmFileJobConsumer> byCode=new HashMap<>();
        consumers.forEach(c -> {
            if (byCode.put(c.templateCode(),c)!=null) throw new IllegalStateException("중복 File Consumer: "+c.templateCode());
        });
        CpfTabularSchema notification=new CpfTabularSchema("ADM_NOTIFICATION_RULE_IMPORT",1,List.of(
                new CpfTabularColumn("eventType","이벤트유형",CpfTabularColumn.Type.STRING,true,80,false),
                new CpfTabularColumn("eventSubType","세부유형",CpfTabularColumn.Type.STRING,false,80,false),
                new CpfTabularColumn("channelCode","채널",CpfTabularColumn.Type.STRING,true,30,false),
                new CpfTabularColumn("templateCode","템플릿",CpfTabularColumn.Type.STRING,true,100,false),
                new CpfTabularColumn("severity","심각도",CpfTabularColumn.Type.STRING,true,20,false),
                new CpfTabularColumn("receiverGroup","수신그룹",CpfTabularColumn.Type.STRING,true,100,true),
                new CpfTabularColumn("useYn","사용여부",CpfTabularColumn.Type.BOOLEAN,true,1,false)
        ),100_000,4_000);
        AdmFileJobConsumer consumer=Optional.ofNullable(byCode.get(notification.templateCode()))
                .orElseThrow(() -> new IllegalStateException("Notification Rule import consumer가 없습니다."));
        templates=Map.of(notification.templateCode(),new Template(notification,true,true,true,consumer));
    }

    public Template require(String code,int version) {
        Template template=templates.get(code);
        if(template==null||template.schema().version()!=version) {
            throw new IllegalArgumentException("지원하지 않는 File Template/version입니다: "+code+"/"+version);
        }
        return template;
    }
    public Collection<Template> list(){return templates.values();}
    public record Template(CpfTabularSchema schema, boolean rollbackSupported, boolean atomicApply,
                           boolean approvalRequired, AdmFileJobConsumer consumer) {
        public Template { java.util.Objects.requireNonNull(schema, "schema"); java.util.Objects.requireNonNull(consumer, "consumer"); }
    }
}

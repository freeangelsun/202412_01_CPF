package com.cpf.admin.opr.integration;
import com.cpf.common.data.quality.InMemoryCpfDataQualityOperations;
import com.cpf.core.api.data.quality.*;
import com.cpf.core.api.time.*;
import com.cpf.core.api.webhook.*;
import java.time.*; import java.util.*;
/** ADM operational consumer. Risk operations require actor/reason and optimistic version. */
public final class AdmIntegrationClosureService {
 private final CpfDataQualityOperations quality; private final CpfTimeOperations time; private final CpfWebhookOperations webhook;
 public AdmIntegrationClosureService(CpfDataQualityOperations quality,CpfTimeOperations time,CpfWebhookOperations webhook){this.quality=quality;this.time=time;this.webhook=webhook;}
 public CpfTimeSnapshot timeHealth(String zone,long maxSkewMillis){return time.snapshot(ZoneId.of(zone),Duration.ofMillis(maxSkewMillis));}
 public CpfDataQualityDecision validate(String recordId,Map<String,Object> record){return quality.validate(recordId,record);}
 public CpfDataQualityOperations.QuarantineItem correct(String id,long expected,Map<String,Object> corrected,String actor,String reason,boolean approved){return quality.correct(id,expected,corrected,actor,reason,approved);}
 public CpfDataQualityDecision replayQuality(String id,String actor,String reason){return quality.replay(id,actor,reason);}
 public List<CpfWebhookDelivery> webhookDlq(int limit){return webhook.dlq(limit);}
 public CpfWebhookDelivery replayWebhook(String id,long expected,String actor,String reason){return webhook.replay(id,expected,actor,reason,time.now());}
}

package com.cpf.starter.messaging.reliability;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("cpf.messaging.reliability")
public class CpfMessagingReliabilityProperties {
 private boolean enabled=true; private boolean schemaRequired=true; private int claimLimit=100; private Duration lease=Duration.ofSeconds(30); private int maxReplayBatch=500;
 public boolean isEnabled(){return enabled;}public void setEnabled(boolean v){enabled=v;}public boolean isSchemaRequired(){return schemaRequired;}public void setSchemaRequired(boolean v){schemaRequired=v;}public int getClaimLimit(){return claimLimit;}public void setClaimLimit(int v){claimLimit=v;}public Duration getLease(){return lease;}public void setLease(Duration v){lease=v;}public int getMaxReplayBatch(){return maxReplayBatch;}public void setMaxReplayBatch(int v){maxReplayBatch=v;}
 public void validate(){if(claimLimit<1||claimLimit>1000)throw new IllegalStateException("claim-limit must be 1..1000");if(lease==null||lease.isZero()||lease.isNegative())throw new IllegalStateException("lease must be positive");if(maxReplayBatch<1||maxReplayBatch>5000)throw new IllegalStateException("max-replay-batch must be 1..5000");}
}

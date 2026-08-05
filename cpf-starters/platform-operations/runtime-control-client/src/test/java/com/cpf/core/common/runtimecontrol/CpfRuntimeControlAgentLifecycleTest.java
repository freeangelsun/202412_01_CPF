package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfRuntimeControlAgentLifecycleTest {
    @Test void stopIsIdempotentAndSubsequentPollCannotResurrectAgent(@TempDir Path tempDir){
        AtomicInteger registrations=new AtomicInteger();
        AtomicInteger heartbeats=new AtomicInteger();
        AtomicInteger deregistrations=new AtomicInteger();
        CpfRuntimeAgentPort port=new CpfRuntimeAgentPort(){
            @Override public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration registration){
                registrations.incrementAndGet();
                return new CpfRuntimeInstanceLease(registration.instanceId(),7L,0L,0L,null,null,"IN_SYNC",Instant.now().plusSeconds(60));
            }
            @Override public CpfRuntimeInstanceLease heartbeat(String instanceId,long fence,String hash,long version){
                heartbeats.incrementAndGet();
                return new CpfRuntimeInstanceLease(instanceId,fence,0L,version,null,hash,"IN_SYNC",Instant.now().plusSeconds(60));
            }
            @Override public List<CpfRuntimeDelivery> claim(String instanceId,long fence,int limit){return List.of();}
            @Override public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack){return null;}
            @Override public void deregister(String instanceId,long fence,String reason){deregistrations.incrementAndGet();}
        };
        CpfRuntimeControlAgent agent=new CpfRuntimeControlAgent(port,registration(),List.of(),
                new CpfRuntimeInstanceInboxStore(tempDir.resolve("inbox")));
        agent.start();
        agent.stop();
        agent.stop();
        agent.poll();
        assertEquals(1,registrations.get());
        assertEquals(0,heartbeats.get());
        assertEquals(1,deregistrations.get());
        assertThrows(IllegalStateException.class,agent::start);
    }

    @Test
    void liveLeaseFenceDefersRegistrationWithoutKillingApplicationAndPollRetries(@TempDir Path tempDir) {
        AtomicInteger registrations=new AtomicInteger();
        AtomicInteger heartbeats=new AtomicInteger();
        CpfRuntimeAgentPort port=new CpfRuntimeAgentPort(){
            @Override public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration value){
                if(registrations.getAndIncrement()==0) throw new CpfRuntimeFenceException("previous lease alive");
                return new CpfRuntimeInstanceLease(value.instanceId(),9L,0L,0L,null,null,"IN_SYNC",Instant.now().plusSeconds(60));
            }
            @Override public CpfRuntimeInstanceLease heartbeat(String instanceId,long fence,String hash,long version){
                heartbeats.incrementAndGet();
                return new CpfRuntimeInstanceLease(instanceId,fence,0L,version,null,hash,"IN_SYNC",Instant.now().plusSeconds(60));
            }
            @Override public List<CpfRuntimeDelivery> claim(String instanceId,long fence,int limit){return List.of();}
            @Override public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack){return null;}
        };
        CpfRuntimeControlAgent agent=new CpfRuntimeControlAgent(port,registration(),List.of(),
                new CpfRuntimeInstanceInboxStore(tempDir.resolve("deferred-registration")));

        agent.start();
        assertEquals(1,registrations.get());
        assertEquals(0,heartbeats.get());
        agent.poll();
        assertEquals(2,registrations.get());
        assertEquals(1,heartbeats.get());
        agent.stop();
    }


    @Test
    void heartbeatFenceDefersReregistrationWhenReplacementLeaseIsStillActive(@TempDir Path tempDir) {
        AtomicInteger registrations = new AtomicInteger();
        AtomicInteger heartbeats = new AtomicInteger();
        AtomicInteger claims = new AtomicInteger();
        CpfRuntimeAgentPort port = new CpfRuntimeAgentPort() {
            @Override
            public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration value) {
                int attempt = registrations.getAndIncrement();
                if (attempt == 1) {
                    throw new CpfRuntimeFenceException("replacement lease is active");
                }
                long token = attempt == 0 ? 7L : 9L;
                return new CpfRuntimeInstanceLease(
                        value.instanceId(), token, 0L, 0L, null, null,
                        "IN_SYNC", Instant.now().plusSeconds(60));
            }

            @Override
            public CpfRuntimeInstanceLease heartbeat(
                    String instanceId, long fence, String hash, long version) {
                if (heartbeats.getAndIncrement() == 0) {
                    throw new CpfRuntimeFenceException("stale writer");
                }
                return new CpfRuntimeInstanceLease(
                        instanceId, fence, 0L, version, null, hash,
                        "IN_SYNC", Instant.now().plusSeconds(60));
            }

            @Override
            public List<CpfRuntimeDelivery> claim(String instanceId, long fence, int limit) {
                claims.incrementAndGet();
                return List.of();
            }

            @Override
            public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack) {
                return null;
            }
        };
        CpfRuntimeControlAgent agent = new CpfRuntimeControlAgent(
                port, registration(), List.of(),
                new CpfRuntimeInstanceInboxStore(tempDir.resolve("fence-reclaim")));

        agent.start();
        agent.poll();
        assertEquals(2, registrations.get());
        assertEquals(1, heartbeats.get());
        assertEquals(0, claims.get());

        agent.poll();
        assertEquals(3, registrations.get());
        assertEquals(2, heartbeats.get());
        assertEquals(1, claims.get());
        agent.stop();
    }

    @Test
    void registrationLeaseIsRetainedWhenInboxReconciliationTemporarilyFails(@TempDir Path tempDir) {
        CpfRuntimeInstanceInboxStore inbox=new CpfRuntimeInstanceInboxStore(tempDir.resolve("reconcile-inbox"));
        CpfRuntimePayload payload=CpfRuntimePayload.parse("{\"enabled\":true}");
        CpfRuntimeDelivery delivery=new CpfRuntimeDelivery(
                "delivery-1","change-1","TEST","instance-1",3L,7L,
                "request-hash",CpfRuntimeCanonicalHash.sha256(payload),1,payload,1,Instant.now().plusSeconds(60));
        inbox.prepare(delivery);
        inbox.markApplied(delivery,"actual-3");

        AtomicInteger registrations=new AtomicInteger();
        AtomicInteger reconciliations=new AtomicInteger();
        AtomicInteger claims=new AtomicInteger();
        CpfRuntimeAgentPort port=new CpfRuntimeAgentPort(){
            @Override public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration value){
                registrations.incrementAndGet();
                return new CpfRuntimeInstanceLease(value.instanceId(),7L,0L,0L,null,null,"IN_SYNC",Instant.now().plusSeconds(60));
            }
            @Override public CpfRuntimeInstanceLease heartbeat(String instanceId,long fence,String hash,long version){
                return new CpfRuntimeInstanceLease(instanceId,fence,0L,version,null,hash,"IN_SYNC",Instant.now().plusSeconds(60));
            }
            @Override public void reconcileActualState(String instanceId,long fence,List<CpfRuntimeActualState> states){
                if(reconciliations.getAndIncrement()==0) throw new IllegalStateException("temporary outage");
            }
            @Override public List<CpfRuntimeDelivery> claim(String instanceId,long fence,int limit){claims.incrementAndGet();return List.of();}
            @Override public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack){return null;}
        };
        CpfRuntimeControlAgent agent=new CpfRuntimeControlAgent(port,registration(),List.of(),inbox);
        agent.start();
        assertEquals(1,registrations.get());
        assertEquals(1,reconciliations.get());
        agent.poll();
        assertEquals(1,registrations.get());
        assertEquals(2,reconciliations.get());
        assertEquals(1,claims.get());
        agent.stop();
    }


    @Test
    void failedAckIsRetriedBeforeAnotherClaimWithoutReapplying(@TempDir Path tempDir) {
        CpfRuntimePayload payload=CpfRuntimePayload.parse("{\"enabled\":true}");
        CpfRuntimeDelivery delivery=new CpfRuntimeDelivery(
                "delivery-1","change-1","TEST","instance-1",3L,7L,
                "request-hash",CpfRuntimeCanonicalHash.sha256(payload),1,payload,1,Instant.now().plusSeconds(60));
        AtomicInteger claims=new AtomicInteger();
        AtomicInteger applies=new AtomicInteger();
        AtomicInteger acknowledgements=new AtomicInteger();
        CpfRuntimeAgentPort port=new CpfRuntimeAgentPort(){
            @Override public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration value){
                return new CpfRuntimeInstanceLease(value.instanceId(),7L,0L,0L,null,null,"IN_SYNC",Instant.now().plusSeconds(60));
            }
            @Override public CpfRuntimeInstanceLease heartbeat(String instanceId,long fence,String hash,long version){
                return new CpfRuntimeInstanceLease(instanceId,fence,0L,version,null,hash,"IN_SYNC",Instant.now().plusSeconds(60));
            }
            @Override public List<CpfRuntimeDelivery> claim(String instanceId,long fence,int limit){
                return claims.getAndIncrement()==0?List.of(delivery):List.of();
            }
            @Override public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack){
                if(acknowledgements.getAndIncrement()<2) throw new IllegalStateException("response lost");
                return null;
            }
        };
        CpfRuntimeChangeApplier applier=new CpfRuntimeChangeApplier(){
            @Override public String changeType(){return "TEST";}
            @Override public boolean supportsIdempotentReplay(){return true;}
            @Override public CpfRuntimeApplyResult apply(CpfRuntimeDelivery value){
                applies.incrementAndGet();
                return CpfRuntimeApplyResult.success("actual-3");
            }
        };
        CpfRuntimeControlAgent agent=new CpfRuntimeControlAgent(port,registration(),List.of(applier),
                new CpfRuntimeInstanceInboxStore(tempDir.resolve("ack-retry-inbox")));
        agent.start();
        agent.poll();
        agent.poll();
        assertEquals(1,applies.get());
        assertEquals(2,acknowledgements.get());
        assertEquals(1,claims.get());
        agent.poll();
        assertEquals(1,applies.get());
        assertEquals(3,acknowledgements.get());
        assertEquals(2,claims.get());
        agent.stop();
    }


    @Test
    void registrationRefreshesAgentTimeInsteadOfReusingBeanCreationTime(@TempDir Path tempDir) {
        java.util.concurrent.atomic.AtomicReference<Instant> sentTime=new java.util.concurrent.atomic.AtomicReference<>();
        CpfRuntimeAgentPort port=new CpfRuntimeAgentPort(){
            @Override public CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration value){
                sentTime.set(value.agentTime());
                return new CpfRuntimeInstanceLease(value.instanceId(),1L,0L,0L,null,null,"IN_SYNC",Instant.now().plusSeconds(60));
            }
            @Override public CpfRuntimeInstanceLease heartbeat(String instanceId,long fence,String hash,long version){return null;}
            @Override public List<CpfRuntimeDelivery> claim(String instanceId,long fence,int limit){return List.of();}
            @Override public CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack){return null;}
        };
        CpfRuntimeInstanceRegistration stale=new CpfRuntimeInstanceRegistration(
                "instance-1","service-1","endpoint-1","test","zone","cell","http://localhost",
                "1","commit","APPLICATION","SELF","1","hash",Map.of(),Map.of(),Instant.EPOCH,60);
        CpfRuntimeControlAgent agent=new CpfRuntimeControlAgent(port,stale,List.of(),
                new CpfRuntimeInstanceInboxStore(tempDir.resolve("fresh-registration")));
        Instant before=Instant.now().minusSeconds(1);
        agent.start();
        assertTrue(!sentTime.get().isBefore(before));
        agent.stop();
    }

    private static CpfRuntimeInstanceRegistration registration(){
        return new CpfRuntimeInstanceRegistration("instance-1","service-1","endpoint-1","test","zone","cell",
                "http://localhost","1","commit","APPLICATION","SELF","1","hash",Map.of(),Map.of(),Instant.now(),60);
    }
}

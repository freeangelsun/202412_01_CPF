<template>
      <section class="panel">
        <div class="panel-title">
          <h2>Service Call Registry</h2>
          <button type="button" @click="loadServiceRegistry">Refresh</button>
        </div>
        <div class="filters">
          <label>Service ID <input v-model="serviceRegistrySearch.serviceId" type="text" placeholder="MBR"></label>
          <label>Endpoint Code <input v-model="serviceRegistrySearch.endpointCode" type="text" placeholder="MBR_API"></label>
          <label>Instance Status <input v-model="serviceRegistrySearch.instanceStatus" type="text" placeholder="UP"></label>
          <label>Transaction ID <input v-model="serviceRegistrySearch.transactionId" type="text"></label>
          <label>Limit <input v-model.number="serviceRegistrySearch.limit" type="number"></label>
        </div>
        <div class="grid two">
          <section>
            <h3>Services / Endpoints</h3>
            <pre class="detail">{{ pretty({ services: serviceRegistryResult.services, endpoints: serviceRegistryResult.endpoints }) }}</pre>
          </section>
          <section>
            <h3>Instances / Health</h3>
            <pre class="detail">{{ pretty({ instances: serviceRegistryResult.instances, health: serviceRegistryResult.health }) }}</pre>
          </section>
          <section>
            <h3>Routing / Circuit</h3>
            <pre class="detail">{{ pretty({ routingPolicies: serviceRegistryResult.routingPolicies, circuits: serviceRegistryResult.circuits }) }}</pre>
          </section>
          <section>
            <h3>Call History</h3>
            <pre class="detail">{{ pretty(serviceRegistryResult.callHistory) }}</pre>
          </section>
        </div>
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "ServiceRegistryPage",
  mixins: [admConsoleMixin]
});
</script>

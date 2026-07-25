<template>
      <section class="panel">
        <div class="panel-title">
          <h2>거래 메타</h2>
          <div class="actions">
            <button type="button" @click="loadTransactions">조회</button>
            <button type="button" v-if="canWrite('TRANSACTION_META')" @click="scanTransactions">재스캔</button>
            <button type="button" v-if="canWrite('TRANSACTION_META')" @click="inactivateTransaction">비활성화</button>
          </div>
        </div>
        <div class="filters">
          <label>모듈 <input v-model="transactionSearch.moduleCode" type="text" placeholder="ADM"></label>
          <label>활성 <select v-model="transactionSearch.activeYn"><option>Y</option><option>N</option><option value="">전체</option></select></label>
          <label>거래 ID <input v-model="transactionSearch.transactionId" type="text"></label>
          <label>선택 거래 ID <input v-model="transactionSearch.selectedTransactionId" type="text"></label>
          <label>사유 <input v-model="transactionSearch.reason" type="text"></label>
        </div>
        <pre class="detail">{{ pretty(transactionResult) }}</pre>
      </section>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { admConsoleMixin } from "../../app/admConsoleMixin";

export default defineComponent({
  name: "TransactionsPage",
  mixins: [admConsoleMixin]
});
</script>

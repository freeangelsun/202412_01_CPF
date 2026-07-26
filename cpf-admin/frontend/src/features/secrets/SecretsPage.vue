<script setup lang="ts">
import { onMounted, ref } from "vue"; import { admApi } from "../../shared/cpfApi";
const providers=ref<any[]>([]),provider=ref("ENV"),key=ref(""),reason=ref(""),metadata=ref<any>(null),message=ref("");
async function loadProviders(){providers.value=await admApi<any[]>("/adm/api/secrets/providers")}
async function loadMetadata(){message.value="";metadata.value=await admApi<any>(`/adm/api/secrets/metadata?provider=${encodeURIComponent(provider.value)}&key=${encodeURIComponent(key.value)}`)}
async function rotate(){message.value="";metadata.value=await admApi<any>("/adm/api/secrets/rotate",{method:"POST",body:JSON.stringify({provider:provider.value,key:key.value,reason:reason.value})});message.value="Rotation 요청 완료"}
onMounted(loadProviders);
</script>
<template><section><h2>Secret / Key 관리</h2><p>원문 Secret은 화면과 API에 표시되지 않습니다. Provider reference와 metadata만 조회합니다.</p><div><select v-model="provider"><option v-for="p in providers" :key="p.providerId">{{p.providerId}}</option></select><input v-model="key" placeholder="Secret key/reference"><button @click="loadMetadata">Metadata 조회</button></div><pre v-if="metadata">{{JSON.stringify(metadata,null,2)}}</pre><div><input v-model="reason" placeholder="Rotation 사유"><button @click="rotate">Rotation</button></div><p>{{message}}</p></section></template>

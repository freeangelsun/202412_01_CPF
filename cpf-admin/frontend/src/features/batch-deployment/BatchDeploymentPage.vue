<script setup lang="ts">import BatchViewPage from '../batch-runtime-control/BatchViewPage.vue';import DeploymentPage from '../batch-runtime-control/DeploymentPage.vue'</script>
<template><section><BatchViewPage view="deployments" title="Deployment History" description="Cell별 배포·Rollback 실행과 실패 Stage를 조회합니다."/><hr/><DeploymentPage/></section></template>

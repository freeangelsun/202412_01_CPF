<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { admApprovalRequest, admSecretFindMetadata, admSecretFindProviders } from "../../generated/cpf-api";
import { useAdmSessionStore } from "../../stores/admSessionStore";

interface SecretProvider {
  providerId: string;
  rotatable: boolean;
}

interface SecretMetadata {
  reference?: { provider?: string; key?: string };
  version?: string;
  createdAt?: string;
  expiresAt?: string;
  rotatable?: boolean;
  attributes?: Record<string, string>;
}

const session = useAdmSessionStore();
const providers = ref<SecretProvider[]>([]);
const provider = ref("ENV");
const key = ref("");
const reason = ref("");
const metadata = ref<SecretMetadata | null>(null);
const message = ref("");
const error = ref("");
const attributeRows = computed(() => Object.entries(metadata.value?.attributes ?? {}));
const selectedProvider = computed(() => providers.value.find(
  (item) => item.providerId.toUpperCase() === provider.value.toUpperCase()
));
const canRotate = computed(() => session.hasButton("SECRET_ROTATE") && Boolean(selectedProvider.value?.rotatable && metadata.value?.rotatable));

function requireReference(): void {
  if (!provider.value.trim()) throw new Error("Provider를 선택하세요.");
  if (!key.value.trim()) throw new Error("Secret key/reference를 입력하세요.");
}

async function loadProviders(): Promise<void> {
  error.value = "";
  try {
    providers.value = await admSecretFindProviders<SecretProvider[]>();
    if (providers.value.length && !providers.value.some((item) => item.providerId === provider.value)) {
      provider.value = providers.value[0].providerId;
    }
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : String(failure);
  }
}

async function loadMetadata(): Promise<void> {
  message.value = "";
  error.value = "";
  try {
    requireReference();
    metadata.value = await admSecretFindMetadata<SecretMetadata>({
      query: { provider: provider.value, key: key.value.trim() }
    });
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : String(failure);
  }
}

async function rotate(): Promise<void> {
  message.value = "";
  error.value = "";
  try {
    requireReference();
    if (!session.hasButton("SECRET_ROTATE")) throw new Error("SECRET_ROTATE 권한이 없습니다.");
    if (!reason.value.trim()) throw new Error("Rotation 사유를 입력하세요.");
    if (!canRotate.value) throw new Error("선택한 Provider 또는 Secret은 Rotation을 지원하지 않습니다.");
    if (!metadata.value?.version) throw new Error("현재 Secret version을 먼저 조회하세요.");
    const providerId = provider.value.trim();
    const secretKey = key.value.trim();
    const result = await admApprovalRequest<Record<string, unknown>>({
      data: {
        requestKey: `secret-rotate-${providerId.slice(0,20)}-${crypto.randomUUID()}`,
        actionType: "SECRET_ROTATE", ownerModule: "CPF-SECURITY", ownerCommand: "SECRET_ROTATE",
        targetType: "SECRET_REFERENCE", targetId: `${providerId}:${secretKey}`,
        payloadSnapshot: JSON.stringify({ provider: providerId, key: secretKey, expectedVersion: metadata.value.version }),
        reason: reason.value.trim()
      }
    });
    const id = String(result.approvalRequestId ?? result.requestId ?? result.id ?? "");
    message.value = id ? `Rotation 승인 요청 ${id}가 생성되었습니다. 독립 승인 후 승인 화면에서 실행하세요.` : "Rotation 승인 요청이 생성되었습니다.";
  } catch (failure) {
    error.value = failure instanceof Error ? failure.message : String(failure);
  }
}

onMounted(loadProviders);
</script>

<template>
  <section class="panel">
    <div class="panel-title">
      <div>
        <h2>Secret / Key 관리</h2>
        <p>원문 Secret은 화면과 API에 표시되지 않습니다. Provider reference와 metadata만 조회합니다.</p>
      </div>
    </div>
    <div class="filters">
      <label>Provider
        <select v-model="provider">
          <option v-for="item in providers" :key="item.providerId" :value="item.providerId">
            {{ item.providerId }}{{ item.rotatable ? " (Rotation 지원)" : "" }}
          </option>
        </select>
      </label>
      <label>Secret key/reference
        <input v-model.trim="key" autocomplete="off" />
      </label>
      <button type="button" @click="loadMetadata">Metadata 조회</button>
    </div>
    <p v-if="error" class="error-banner" role="alert">{{ error }}</p>

    <div v-if="metadata" class="metadata-section">
      <div class="table-wrap">
        <table>
          <thead><tr><th>Provider</th><th>Reference</th><th>Version</th><th>생성</th><th>만료</th><th>Rotation</th></tr></thead>
          <tbody>
            <tr>
              <td>{{ metadata.reference?.provider ?? provider }}</td>
              <td>{{ metadata.reference?.key ?? key }}</td>
              <td>{{ metadata.version ?? "-" }}</td>
              <td>{{ metadata.createdAt ?? "-" }}</td>
              <td>{{ metadata.expiresAt ?? "-" }}</td>
              <td>{{ metadata.rotatable ? "지원" : "미지원" }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="attributeRows.length" class="table-wrap">
        <table>
          <thead><tr><th>Metadata 속성</th><th>값</th></tr></thead>
          <tbody><tr v-for="[name, value] in attributeRows" :key="name"><td>{{ name }}</td><td>{{ value }}</td></tr></tbody>
        </table>
      </div>
    </div>

    <div class="filters">
      <label>Rotation 사유
        <input v-model.trim="reason" placeholder="감사 가능한 변경 사유" />
      </label>
      <button type="button" :disabled="!canRotate" @click="rotate">Rotation 승인 요청</button>
    </div>
    <p v-if="message" class="success-banner" role="status">{{ message }}</p>
  </section>
</template>

<style scoped>
.metadata-section { display: grid; gap: 14px; margin: 16px 18px; }
</style>

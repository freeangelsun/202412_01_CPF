<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import CpfIcon from "../../components/CpfIcon.vue";
import CpfTreeNode, { type TreeNode } from "../../components/CpfTreeNode.vue";
import { bzaBackofficeSaveOrganization } from "../../generated/orval/cpf-api";
import { bzaApi, hasBzaPermission } from "../auth/session";

type Row = Record<string, any>;
const rows = ref<Row[]>([]);
const selected = ref<TreeNode | null>(null);
const error = ref("");
const query = ref("");
const includeDisabled = ref(true);
const issues = ref<string[]>([]);
const saving = ref(false);
const edit = ref({
  parentOrganizationCode: "",
  organizationName: "",
  organizationType: "DEPARTMENT",
  sortOrder: 0,
  useYn: "Y",
  reason: "",
});

function buildTree(source: Row[]): TreeNode[] {
  const byId = new Map<string, Row>();
  source.forEach((r) => byId.set(String(r.organizationCode), r));
  const children = new Map<string, Row[]>();
  const roots: Row[] = [];
  issues.value = [];
  for (const row of source) {
    const id = String(row.organizationCode);
    const parent = String(row.parentOrganizationCode || "");
    if (!parent) roots.push(row);
    else if (!byId.has(parent)) {
      roots.push(row);
      issues.value.push(`고아 조직: ${id} → ${parent}`);
    } else {
      const list = children.get(parent) || [];
      list.push(row);
      children.set(parent, list);
    }
  }
  const walk = (row: Row, path: Set<string>): TreeNode => {
    const id = String(row.organizationCode);
    if (path.has(id)) {
      issues.value.push(`순환 조직: ${[...path, id].join(" → ")}`);
      return { id, label: String(row.organizationName || id), subtitle: id, status: row.useYn, children: [], raw: row, cycle: true };
    }
    const next = new Set(path);
    next.add(id);
    return {
      id,
      label: String(row.organizationName || id),
      subtitle: `${id} · ${row.organizationType || "-"}`,
      status: String(row.useYn || "Y"),
      children: (children.get(id) || [])
        .sort((a, b) => String(a.organizationName).localeCompare(String(b.organizationName)))
        .map((child) => walk(child, next)),
      raw: row,
      orphan: !!row.parentOrganizationCode && !byId.has(String(row.parentOrganizationCode)),
    };
  };
  return roots
    .sort((a, b) => String(a.organizationName).localeCompare(String(b.organizationName)))
    .map((row) => walk(row, new Set()));
}

const tree = computed(() => buildTree(rows.value.filter((r) => includeDisabled.value || r.useYn !== "N")));
const filtered = computed(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return tree.value;
  const filter = (node: TreeNode): TreeNode | null => {
    const children = node.children.map(filter).filter(Boolean) as TreeNode[];
    return `${node.label} ${node.subtitle}`.toLowerCase().includes(q) || children.length ? { ...node, children } : null;
  };
  return tree.value.map(filter).filter(Boolean) as TreeNode[];
});

async function load() {
  error.value = "";
  try {
    rows.value = await bzaApi("/api/bza/backoffice/organizations?limit=5000");
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
  }
}

function selectNode(node: TreeNode) {
  selected.value = node;
  edit.value = {
    parentOrganizationCode: String(node.raw.parentOrganizationCode || ""),
    organizationName: String(node.raw.organizationName || ""),
    organizationType: String(node.raw.organizationType || "DEPARTMENT"),
    sortOrder: Number(node.raw.sortOrder || 0),
    useYn: String(node.raw.useYn || "Y"),
    reason: "",
  };
}

async function saveOrganization() {
  if (!selected.value || !hasBzaPermission("ORGANIZATION", "WRITE")) return;
  if (!edit.value.organizationName.trim()) {
    error.value = "조직명은 필수입니다.";
    return;
  }
  if (!edit.value.reason.trim()) {
    error.value = "변경 사유는 필수입니다.";
    return;
  }
  saving.value = true;
  error.value = "";
  try {
    const raw = selected.value.raw;
    await bzaBackofficeSaveOrganization({
      organizationCode: String(raw.organizationCode),
      parentOrganizationCode: edit.value.parentOrganizationCode.trim() || null,
      organizationName: edit.value.organizationName.trim(),
      organizationType: edit.value.organizationType.trim() || "DEPARTMENT",
      sortOrder: Number(edit.value.sortOrder || 0),
      effectiveFrom: raw.effectiveFrom ?? null,
      effectiveTo: raw.effectiveTo ?? null,
      useYn: edit.value.useYn,
      expectedVersion: raw.version ?? raw.expectedVersion ?? null,
      requestUser: null,
      reason: edit.value.reason.trim(),
    });
    await load();
    const refreshed = rows.value.find((row) => String(row.organizationCode) === String(raw.organizationCode));
    if (refreshed) selectNode({ ...selected.value, raw: refreshed, label: String(refreshed.organizationName || raw.organizationCode) });
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : String(cause);
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div class="page-stack">
    <div class="cpf-page-heading">
      <div><p class="eyebrow">ORGANIZATION DIRECTORY</p><h2>조직도</h2><p>전체 깊이 조직 계층을 검색하고 고아·순환 구조를 즉시 식별합니다.</p></div>
      <button class="ghost" @click="load"><CpfIcon name="refresh" /> 새로고침</button>
    </div>
    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="issues.length" class="error-banner">{{ issues.join(" / ") }}</p>
    <div class="cpf-toolbar">
      <input v-model="query" placeholder="조직명·코드 검색" />
      <label><input v-model="includeDisabled" type="checkbox" /> 중지 조직 포함</label>
      <span class="count-pill">{{ rows.length }} nodes</span>
    </div>
    <div class="bza-org-layout">
      <section class="cpf-card">
        <div class="cpf-card-head"><h2>조직 Tree</h2></div>
        <div class="cpf-card-body org-tree">
          <CpfTreeNode v-for="node in filtered" :key="node.id" :node="node" :selected-id="selected?.id" @select="selectNode" />
          <div v-if="!filtered.length" class="cpf-empty">조직 데이터가 없습니다.</div>
        </div>
      </section>
      <aside class="cpf-card bza-detail-drawer">
        <div class="cpf-card-head"><h2>조직 상세</h2></div>
        <div v-if="selected" class="detail-list">
          <dl>
            <dt>조직 코드</dt><dd>{{ selected.raw.organizationCode }}</dd>
            <dt>상위 조직</dt><dd>{{ selected.raw.parentOrganizationCode || "ROOT" }}</dd>
            <dt>하위 조직</dt><dd>{{ selected.children.length }}</dd>
          </dl>
          <template v-if="hasBzaPermission('ORGANIZATION', 'WRITE')">
            <label>조직명<input v-model="edit.organizationName" /></label>
            <label>상위 조직 코드<input v-model="edit.parentOrganizationCode" /></label>
            <label>조직 유형<input v-model="edit.organizationType" /></label>
            <label>정렬 순서<input v-model.number="edit.sortOrder" type="number" /></label>
            <label>사용 여부<select v-model="edit.useYn"><option value="Y">Y</option><option value="N">N</option></select></label>
            <label>변경 사유<textarea v-model="edit.reason" rows="3" placeholder="감사 사유를 입력하세요" /></label>
            <button :disabled="saving" @click="saveOrganization">{{ saving ? "저장 중..." : "조직 저장" }}</button>
            <p class="cpf-note">저장은 expectedVersion 기반 CAS와 감사 사유를 적용합니다.</p>
          </template>
          <p v-else class="cpf-note">조직 변경에는 ORGANIZATION / WRITE 권한이 필요합니다.</p>
        </div>
        <div v-else class="cpf-empty">Tree에서 조직을 선택하세요.</div>
      </aside>
    </div>
  </div>
</template>

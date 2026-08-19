<script setup lang="ts">
import { h } from 'vue'
import type { ColumnDef } from '@tanstack/vue-table'
import CpfDataTable from '../../../components/ui/CpfDataTable.vue'
import type { ManagedServerRow } from '../model/serverManagementModel'

const props = defineProps<{ rows: ManagedServerRow[]; total: number; page: number; size: number; loading: boolean }>()
const emit = defineEmits<{ row: [ManagedServerRow]; page: [number]; size: [number] }>()

const columns: ColumnDef<ManagedServerRow, unknown>[] = [
  { id: 'name', header: 'Server', cell: context => h('button', { class: 'link-button', onClick: () => emit('row', context.row.original) }, String(context.row.original.displayName || context.row.original.serverName || context.row.original.managedServerId)) },
  { accessorKey: 'environment', header: 'Environment' },
  { accessorKey: 'status', header: 'Status' },
  { accessorKey: 'hostname', header: 'Hostname' },
  { accessorKey: 'runtimeCount', header: 'Runtimes' },
  { accessorKey: 'activeRuntimeCount', header: 'Active' },
  { accessorKey: 'serverGroup', header: 'Group' },
]
</script>
<template>
  <CpfDataTable
    :rows="props.rows" :columns="columns" :total="props.total" :page="props.page" :size="props.size"
    :loading="props.loading" :row-key="row => String(row.managedServerId)"
    @page="emit('page', $event)" @size="emit('size', $event)" @row="emit('row', $event)"
  />
</template>
<style scoped>.link-button{border:0;background:transparent;text-decoration:underline;color:var(--el-color-primary,#409eff)}</style>

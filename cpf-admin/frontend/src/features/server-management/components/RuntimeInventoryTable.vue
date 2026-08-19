<script setup lang="ts">
import { h } from 'vue'
import type { ColumnDef } from '@tanstack/vue-table'
import CpfDataTable from '../../../components/ui/CpfDataTable.vue'
import type { RuntimeInventoryRow } from '../model/serverManagementModel'

const props = defineProps<{ rows: RuntimeInventoryRow[]; total: number; page: number; size: number; loading: boolean }>()
const emit = defineEmits<{ page: [number]; size: [number] }>()
const columns: ColumnDef<RuntimeInventoryRow, unknown>[] = [
  { accessorKey: 'instanceId', header: 'Instance' },
  { accessorKey: 'serverName', header: 'Server', cell: context => h('span', String(context.row.original.serverName || context.row.original.managedServerId || '미연결')) },
  { accessorKey: 'systemCode', header: 'System' },
  { accessorKey: 'applicationName', header: 'Application' },
  { accessorKey: 'applicationRole', header: 'Role' },
  { accessorKey: 'status', header: 'Status' },
  { accessorKey: 'runtimeHostname', header: 'Hostname' },
  { accessorKey: 'lastSeenAt', header: 'Last Seen' },
]
</script>
<template>
  <CpfDataTable
    :rows="props.rows" :columns="columns" :total="props.total" :page="props.page" :size="props.size"
    :loading="props.loading" :row-key="row => String(row.instanceId)"
    @page="emit('page', $event)" @size="emit('size', $event)"
  />
</template>

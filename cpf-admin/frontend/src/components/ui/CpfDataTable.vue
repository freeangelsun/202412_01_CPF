<script setup lang="ts" generic="T extends Record<string, unknown>">
import { computed } from "vue";
import { FlexRender, getCoreRowModel, useVueTable, type ColumnDef, type PaginationState, type SortingState } from "@tanstack/vue-table";
const props = withDefaults(defineProps<{ rows:T[]; columns:ColumnDef<T,unknown>[]; total:number; page:number; size:number; loading?:boolean; sorting?:SortingState; rowKey:(row:T)=>string; }>(), { loading:false, sorting:()=>[] });
const emit=defineEmits<{page:[number]; size:[number]; sort:[SortingState]; row:[T]}>();
const pagination=computed<PaginationState>(()=>({pageIndex:Math.max(0,props.page-1),pageSize:props.size}));
const table=useVueTable({get data(){return props.rows},get columns(){return props.columns},state:{get pagination(){return pagination.value},get sorting(){return props.sorting}},manualPagination:true,manualSorting:true,pageCount:computed(()=>Math.max(1,Math.ceil(props.total/props.size))).value,getCoreRowModel:getCoreRowModel(),onSortingChange:updater=>emit('sort',typeof updater==='function'?updater(props.sorting):updater)});
</script>
<template>
  <el-table v-loading="loading" :data="table.getRowModel().rows" row-key="id" stripe border height="100%" @row-click="(r:any)=>emit('row',r.original)">
    <el-table-column v-for="header in table.getHeaderGroups()[0]?.headers||[]" :key="header.id" :label="String(header.column.columnDef.header||'')">
      <template #default="scope"><FlexRender :render="header.column.columnDef.cell" :props="scope.row.getVisibleCells().find((c:any)=>c.column.id===header.column.id)?.getContext()" /></template>
    </el-table-column>
    <template #empty><el-empty description="조회 결과가 없습니다." /></template>
  </el-table>
  <el-pagination background layout="total, sizes, prev, pager, next" :total="total" :current-page="page" :page-size="size" :page-sizes="[20,50,100,200]" @current-change="v=>emit('page',v)" @size-change="v=>emit('size',v)" />
</template>

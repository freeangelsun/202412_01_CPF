<script setup lang="ts">
import { computed, ref } from "vue";
export interface TreeNode { id:string; label:string; subtitle?:string; status?:string; children:TreeNode[]; raw:Record<string,unknown>; cycle?:boolean; orphan?:boolean }
const props=withDefaults(defineProps<{node:TreeNode;level?:number;selectedId?:string}>(),{level:0,selectedId:""});
const emit=defineEmits<{select:[TreeNode]}>();
const expanded=ref(props.level<2);
const hasChildren=computed(()=>props.node.children.length>0);
</script>
<template>
  <div class="cpf-tree-node" :style="{ '--tree-level': String(level) }">
    <button type="button" class="cpf-tree-row" :class="{selected:selectedId===node.id,danger:node.cycle||node.orphan}" @click="emit('select',node)">
      <span class="cpf-tree-toggle" @click.stop="expanded=!expanded">{{hasChildren?(expanded?'▾':'▸'):'·'}}</span>
      <span class="cpf-tree-copy"><strong>{{node.label}}</strong><small>{{node.subtitle}}</small></span>
      <span v-if="node.cycle" class="cpf-status danger">순환</span><span v-else-if="node.orphan" class="cpf-status warning">고아</span>
      <span v-else-if="node.status" class="cpf-status" :class="node.status==='N'?'danger':'success'">{{node.status==='N'?'중지':'사용'}}</span>
    </button>
    <div v-if="expanded&&hasChildren" class="cpf-tree-children">
      <CpfTreeNode v-for="child in node.children" :key="child.id" :node="child" :level="level+1" :selected-id="selectedId" @select="emit('select',$event)" />
    </div>
  </div>
</template>

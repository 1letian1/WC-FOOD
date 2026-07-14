<script setup>
import{computed}from'vue';import{imageUrl}from'../api/request';const props=defineProps({src:String,name:String,size:{type:String,default:'160rpx'},radius:{type:String,default:'18rpx'}});const url=computed(()=>imageUrl(props.src));const emoji=computed(()=>/茶|饮|奶/.test(props.name||'')?'🥤':/鸡|肉/.test(props.name||'')?'🍗':'🍛')
</script>
<template><image v-if="url" class="image" :src="url" mode="aspectFill" :style="{width:size,height:size,borderRadius:radius}"/><view v-else class="fallback" :style="{width:size,height:size,borderRadius:radius}">{{emoji}}</view></template>
<style scoped>.image{flex-shrink:0;background:#fff2e8}.fallback{flex-shrink:0;display:flex;align-items:center;justify-content:center;background:linear-gradient(135deg,#fff5ea,#ffdcbc);font-size:64rpx}</style>

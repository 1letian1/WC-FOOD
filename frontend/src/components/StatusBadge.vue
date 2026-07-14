<script setup>
import{statusClass}from'../utils/format';defineProps({status:{type:Object,default:()=>({})}})
</script>
<template><text class="badge" :class="statusClass(status.code)">{{status.description||'未知'}}</text></template>
<style scoped>
.badge{display:inline-block;padding:6rpx 18rpx;border-radius:22rpx;font-size:22rpx;font-weight:700}.pending{background:#fff2e8;color:#ff8a34}.accepted{background:#e8f4ff;color:#1677ff}.cooking{background:#fffbe6;color:#ffb020}.pickup{background:#f9f0ff;color:#722ed1}.delivering{background:#e6fffb;color:#13c2c2}.done{background:#f6ffed;color:#34c759}.closed{background:#f5f5f5;color:#999}.rejected{background:#fff1f0;color:#ff4d4f}
</style>

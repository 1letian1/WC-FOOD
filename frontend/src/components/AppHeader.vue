<script setup>
defineProps({ title: String, subtitle: String })
</script>
<template>
  <view class="safe-header"><view class="header-main"><view><text class="title">{{ title }}</text><text v-if="subtitle" class="subtitle">{{ subtitle }}</text></view><slot /></view></view>
</template>
<style scoped>
.safe-header{padding:calc(env(safe-area-inset-top) + 26rpx) 30rpx 22rpx;background:#fff}.header-main{display:flex;align-items:center;justify-content:space-between}.title{display:block;font-size:36rpx;font-weight:800}.subtitle{display:block;margin-top:6rpx;color:#34c759;font-size:22rpx}
</style>

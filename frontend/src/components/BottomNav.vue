<script setup>
const props=defineProps({active:String,merchant:Boolean})
const userTabs=[{id:'home',icon:'⌂',label:'首页',url:'/pages/home/index'},{id:'orders',icon:'▣',label:'订单',url:'/pages/orders/index'},{id:'profile',icon:'♙',label:'我的',url:'/pages/profile/index'}]
const merchantTabs=[{id:'dashboard',icon:'▦',label:'工作台',url:'/pages/merchant/dashboard'},{id:'orders',icon:'▤',label:'订单',url:'/pages/merchant/orders'},{id:'products',icon:'▢',label:'商品',url:'/pages/merchant/products'},{id:'profile',icon:'♙',label:'我的',url:'/pages/merchant/profile'}]
function go(tab){if(tab.id!==props.active)uni.redirectTo({url:tab.url})}
</script>
<template><view class="bottom-nav"><view v-for="tab in (merchant?merchantTabs:userTabs)" :key="tab.id" class="tab" :class="{active:active===tab.id}" @tap="go(tab)"><text class="icon">{{tab.icon}}</text><text>{{tab.label}}</text></view></view></template>
<style scoped>
.bottom-nav{position:fixed;z-index:80;left:0;right:0;bottom:0;display:flex;height:calc(112rpx + env(safe-area-inset-bottom));padding-bottom:env(safe-area-inset-bottom);background:#fff;border-top:2rpx solid #eee}.tab{flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center;gap:4rpx;color:#999;font-size:21rpx}.icon{height:42rpx;font-size:38rpx;line-height:42rpx}.active{color:#ff8a34;font-weight:700}
</style>

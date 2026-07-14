<script setup>
import{ref}from'vue';import{onPullDownRefresh,onShow}from'@dcloudio/uni-app';import BottomNav from'../../components/BottomNav.vue';import OrderCard from'../../components/OrderCard.vue';import{api}from'../../api'
const active=ref(null),orders=ref([]);const tabs=[{label:'全部',value:null},{label:'待接单',value:1},{label:'制作中',value:3},{label:'待取餐',value:4},{label:'配送中',value:5}]
onShow(load);onPullDownRefresh(async()=>{await load();uni.stopPullDownRefresh()})
async function load(){try{const data=await api.userOrders({current:1,size:100,status:active.value||undefined});orders.value=data?.records||[]}catch(_){}}
async function choose(value){active.value=value;await load()}
function open(order){uni.navigateTo({url:`/pages/order-detail/index?id=${order.id}`})}
</script>
<template><view class="page page-with-nav"><scroll-view class="tabs" scroll-x><text v-for="tab in tabs" :key="String(tab.value)" :class="{active:active===tab.value}" @tap="choose(tab.value)">{{tab.label}}</text></scroll-view><view class="content"><OrderCard v-for="order in orders" :key="order.id" :order="order" @open="open"/><view v-if="!orders.length" class="empty"><text class="empty-icon">▣</text>暂无相关订单</view></view><BottomNav active="orders"/></view></template>
<style scoped>.tabs{white-space:nowrap;height:90rpx;background:#fff;border-bottom:2rpx solid #eee}.tabs text{position:relative;display:inline-block;padding:0 32rpx;line-height:90rpx;color:#777;font-size:25rpx}.tabs .active{color:#ff7b20;font-weight:700}.tabs .active::after{content:'';position:absolute;left:32rpx;right:32rpx;bottom:0;height:5rpx;border-radius:3rpx;background:#ff8a34}</style>

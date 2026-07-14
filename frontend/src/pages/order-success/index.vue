<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { money } from '../../utils/format'
const order = ref({})
onLoad(query => { order.value = query })
</script>
<template><view class="success-page"><view class="success-icon">✓</view><text class="title">订单提交成功</text><text class="order-no">订单号：{{order.no}}</text><view class="summary card"><view class="between"><text>{{Number(order.type)===1?'堂食订单':'配送订单'}}</text><text class="brand">待接单</text></view><view class="between muted"><text>实付金额</text><text class="price">¥{{money(order.amount)}}</text></view><view class="between muted"><text>预计用餐时间</text><text>尽快为您准备</text></view></view><text class="tip">商家接单后将开始制作，请留意订单状态</text><button class="primary-btn" @tap="uni.redirectTo({url:`/pages/order-detail/index?id=${order.id}`})">查看订单</button><button class="outline-btn" @tap="uni.reLaunch({url:'/pages/home/index'})">返回首页</button></view></template>
<style scoped>.success-page{min-height:100vh;padding:calc(env(safe-area-inset-top) + 120rpx) 34rpx 50rpx;display:flex;flex-direction:column;align-items:center;background:linear-gradient(#fff8f0,#fff)}.success-icon{width:112rpx;height:112rpx;border-radius:50%;background:#34c759;color:#fff;text-align:center;line-height:112rpx;font-size:70rpx;font-weight:800}.title{margin-top:32rpx;font-size:38rpx;font-weight:800}.order-no{margin-top:14rpx;color:#999;font-size:23rpx}.summary{width:100%;margin-top:48rpx;padding:28rpx}.summary>view{margin-bottom:24rpx}.summary>view:last-child{margin:0}.tip{margin:34rpx 0 58rpx;color:#777;font-size:24rpx}.success-page button{width:100%;margin-bottom:20rpx}</style>

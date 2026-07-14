<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api } from '../../api'

const agreed = ref(false)
const loading = ref(false)

onShow(() => {
  if (uni.getStorageSync('userToken')) uni.reLaunch({ url: '/pages/home/index' })
})

async function login() {
  if (!agreed.value || loading.value) return
  loading.value = true
  try {
    let code = import.meta.env.VITE_WECHAT_MOCK_CODE || ''
    // #ifdef MP-WEIXIN
    const result = await uni.login({ provider: 'weixin' })
    code = result.code
    // #endif
    if (!code) throw new Error('请在微信小程序中登录，或配置本地模拟登录 code')
    const data = await api.userLogin(code)
    uni.setStorageSync('userToken', data.token)
    uni.setStorageSync('userProfile', data.user)
    uni.reLaunch({ url: '/pages/home/index' })
  } catch (error) {
    uni.showToast({ title: error.message || '登录失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="login-page">
    <view class="login-main">
      <view class="logo">🍜</view>
      <text class="name">食刻小馆</text>
      <text class="slogan">堂食配送，轻松点餐</text>
      <view class="agreement" @tap="agreed = !agreed">
        <view class="check" :class="{ checked: agreed }">{{ agreed ? '✓' : '' }}</view>
        <text>已阅读并同意 <text class="brand">《用户协议》</text> 和 <text class="brand">《隐私政策》</text></text>
      </view>
      <button class="primary-btn login-btn" :disabled="!agreed || loading" @tap="login">{{ loading ? '登录中…' : '微信快捷登录' }}</button>
      <text class="login-tip">微信授权后自动登录，快捷安全</text>
      <view class="merchant-link" @tap="uni.navigateTo({ url: '/pages/merchant/login' })">商家人员登录 <text>›</text></view>
    </view>
    <text class="bottom-tip">普通用户请使用微信快捷登录</text>
  </view>
</template>

<style scoped>
.login-page{min-height:100vh;display:flex;flex-direction:column;background:#fff;padding:env(safe-area-inset-top) 56rpx 42rpx}.login-main{flex:1;display:flex;flex-direction:column;align-items:center;justify-content:center}.logo{width:176rpx;height:176rpx;border-radius:48rpx;display:flex;align-items:center;justify-content:center;background:#fff2e8;font-size:88rpx;margin-bottom:30rpx}.name{font-size:52rpx;font-weight:800}.slogan{margin-top:12rpx;margin-bottom:80rpx;color:#666;font-size:28rpx}.agreement{width:100%;display:flex;gap:18rpx;align-items:flex-start;color:#666;font-size:24rpx;line-height:1.7;margin-bottom:36rpx}.check{width:36rpx;height:36rpx;flex-shrink:0;border-radius:50%;border:3rpx solid #ddd;text-align:center;line-height:32rpx;color:#fff}.checked{background:#ff8a34;border-color:#ff8a34}.login-btn{width:100%}.login-tip{margin-top:20rpx;color:#999;font-size:23rpx}.merchant-link{margin-top:48rpx;padding:18rpx;color:#999;font-size:26rpx}.bottom-tip{text-align:center;color:#aaa;font-size:22rpx}
</style>

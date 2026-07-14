<script setup>
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import ProductImage from '../../components/ProductImage.vue'
import { api } from '../../api'
import { money } from '../../utils/format'

const cart = ref({ items: [], totalQuantity: 0, totalAmount: 0 })
const mode = ref(uni.getStorageSync('orderMode') || 'dine')
const updating = ref(false)
const selectedIds = ref([])
const selectedItems = computed(() => cart.value.items.filter(item => selectedIds.value.includes(item.id) && item.available))
const selectedAmount = computed(() => selectedItems.value.reduce((sum, item) => sum + Number(item.amount), 0))

onShow(load)
async function load() {
  try {
    cart.value = await api.cart()
    selectedIds.value = cart.value.items.filter(item => item.available).map(item => item.id)
  } catch (_) {}
}
async function change(item, delta) {
  if (updating.value) return
  const quantity = item.quantity + delta
  updating.value = true
  try {
    if (quantity <= 0) {
      await api.deleteCart(item.id)
      await load()
    } else {
      cart.value = await api.updateCart(item.id, quantity)
    }
  } catch (_) {} finally { updating.value = false }
}
function toggle(id) {
  selectedIds.value = selectedIds.value.includes(id) ? selectedIds.value.filter(item => item !== id) : [...selectedIds.value, id]
}
function checkout() {
  if (!selectedItems.value.length) return uni.showToast({ title: '请选择可结算商品', icon: 'none' })
  uni.setStorageSync('checkoutItemIds', selectedItems.value.map(item => item.id))
  uni.setStorageSync('orderMode', mode.value)
  uni.navigateTo({ url: `/pages/checkout/index?mode=${mode.value}` })
}
function clear() {
  uni.showModal({ title: '清空购物车', content: '确定删除购物车中的全部商品吗？', success: async res => { if (res.confirm) { await api.clearCart(); await load() } } })
}
</script>

<template>
  <view class="page cart-page">
    <view class="cart-head between"><view class="mode"><text :class="{active:mode==='dine'}" @tap="mode='dine'">堂食</text><text :class="{active:mode==='delivery'}" @tap="mode='delivery'">配送</text></view><text class="clear" @tap="clear">清空</text></view>
    <view v-if="cart.items.length" class="list">
      <view v-for="item in cart.items" :key="item.id" class="cart-item" :class="{disabled:!item.available}">
        <view class="select" :class="{checked:selectedIds.includes(item.id)}" @tap="item.available && toggle(item.id)">{{selectedIds.includes(item.id)?'✓':''}}</view>
        <ProductImage :src="item.productImageUrl" :name="item.productName" size="126rpx" />
        <view class="item-info"><text class="item-name ellipsis">{{item.productName}}</text><text class="spec">{{[item.specificationName,item.tasteName].filter(Boolean).join(' · ')||'默认'}}</text><view class="between"><text class="price">¥{{money(item.unitPrice)}}</text><view class="stepper"><button @tap="change(item,-1)">−</button><text>{{item.quantity}}</text><button @tap="change(item,1)">＋</button></view></view><text v-if="!item.available" class="unavailable">商品状态已变化，暂不可结算</text></view>
      </view>
    </view>
    <view v-else class="empty"><text class="empty-icon">🛒</text><text>购物车还是空的</text><button class="outline-btn" @tap="uni.reLaunch({url:'/pages/home/index'})">去选商品</button></view>
    <view class="settle-bar"><view><text class="label">实付</text><text class="total">¥{{money(selectedAmount)}}</text><small>最终金额以下单接口为准</small></view><button class="primary-btn" @tap="checkout">去结算</button></view>
  </view>
</template>

<style scoped>
.cart-page{padding-bottom:150rpx}.cart-head{height:96rpx;padding:0 30rpx;background:#fff;border-bottom:2rpx solid #eee}.mode{width:330rpx;height:64rpx;display:flex;padding:6rpx;border-radius:32rpx;background:#f3f3f3}.mode text{flex:1;text-align:center;line-height:52rpx;color:#666;font-size:24rpx}.mode .active{border-radius:26rpx;background:#ff8a34;color:#fff;font-weight:700}.clear{color:#999;font-size:25rpx}.list{padding:22rpx 28rpx}.cart-item{display:flex;align-items:center;gap:18rpx;padding:26rpx 20rpx;margin-bottom:20rpx;border-radius:22rpx;background:#fff}.disabled{opacity:.55}.select{width:34rpx;height:34rpx;flex-shrink:0;border-radius:50%;border:2rpx solid #ccc;text-align:center;color:#fff;font-size:22rpx;line-height:31rpx}.checked{background:#ff8a34;border-color:#ff8a34}.item-info{min-width:0;flex:1}.item-name{display:block;font-weight:700}.spec{display:block;margin:9rpx 0 16rpx;color:#999;font-size:22rpx}.stepper{display:flex;align-items:center;gap:16rpx}.stepper button{width:48rpx;height:48rpx;padding:0;border-radius:50%;background:#f2f2f2;line-height:46rpx}.unavailable{display:block;margin-top:10rpx;color:#ff4d4f;font-size:20rpx}.empty{display:flex;flex-direction:column;align-items:center;gap:28rpx}.empty .outline-btn{margin-top:10rpx}.settle-bar{position:fixed;z-index:50;left:0;right:0;bottom:0;height:calc(124rpx + env(safe-area-inset-bottom));padding:16rpx 28rpx calc(16rpx + env(safe-area-inset-bottom));display:flex;align-items:center;background:#fff;border-top:2rpx solid #eee}.settle-bar>view{flex:1}.label{font-size:24rpx}.total{margin-left:10rpx;color:#ff6b18;font-size:36rpx;font-weight:800}.settle-bar small{display:block;color:#999;font-size:19rpx}.settle-bar .primary-btn{width:230rpx}
</style>

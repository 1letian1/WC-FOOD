<script setup>
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import ProductImage from '../../components/ProductImage.vue'
import { api } from '../../api'
import { money, randomKey } from '../../utils/format'

const mode = ref('dine')
const cart = ref({ items: [] })
const shop = ref({})
const addresses = ref([])
const addressId = ref(null)
const form = ref({ contactName: '', contactPhone: '', tableNo: '', noSeatYet: false, remark: '' })
const submitting = ref(false)
const ids = ref([])
const items = computed(() => cart.value.items.filter(item => ids.value.includes(item.id)))
const goodsAmount = computed(() => items.value.reduce((sum, item) => sum + Number(item.amount), 0))
const payAmount = computed(() => goodsAmount.value + Number(shop.value.packageFee || 0) + (mode.value === 'delivery' ? Number(shop.value.deliveryFee || 0) : 0))
const selectedAddress = computed(() => addresses.value.find(item => item.id === addressId.value))

onLoad(async query => {
  mode.value = query.mode || uni.getStorageSync('orderMode') || 'dine'
  ids.value = uni.getStorageSync('checkoutItemIds') || []
  try {
    const tasks = [api.cart(), api.shop()]
    if (mode.value === 'delivery') tasks.push(api.addresses())
    const result = await Promise.all(tasks)
    cart.value = result[0]
    shop.value = result[1]
    if (result[2]) {
      addresses.value = result[2]
      addressId.value = addresses.value.find(item => item.isDefault)?.id || addresses.value[0]?.id || null
    }
  } catch (_) {}
})

function validate() {
  if (!items.value.length) return '结算商品为空'
  if (mode.value === 'delivery') return addressId.value ? '' : '请先选择收货地址'
  if (!form.value.contactName.trim()) return '请输入联系人'
  if (!/^1[3-9]\d{9}$/.test(form.value.contactPhone)) return '请输入正确的联系电话'
  if (!form.value.noSeatYet && !form.value.tableNo.trim()) return '请输入桌号或选择暂未入座'
  return ''
}

async function submit() {
  const message = validate()
  if (message) return uni.showToast({ title: message, icon: 'none' })
  if (submitting.value) return
  submitting.value = true
  const delivery = mode.value === 'delivery'
  try {
    const order = await api.createOrder({
      orderType: delivery ? 2 : 1,
      cartItemIds: ids.value,
      contactName: delivery ? null : form.value.contactName,
      contactPhone: delivery ? null : form.value.contactPhone,
      tableNo: delivery || form.value.noSeatYet ? null : form.value.tableNo,
      noSeatYet: delivery ? false : form.value.noSeatYet,
      addressId: delivery ? addressId.value : null,
      remark: form.value.remark || null,
    }, randomKey())
    uni.removeStorageSync('checkoutItemIds')
    uni.redirectTo({ url: `/pages/order-success/index?id=${order.id}&no=${order.orderNo}&amount=${order.payAmount}&type=${order.orderType.code}` })
  } catch (_) { submitting.value = false }
}

function chooseAddress() {
  if (!addresses.value.length) return uni.navigateTo({ url: '/pages/addresses/index' })
  uni.showActionSheet({ itemList: addresses.value.map(item => `${item.contactName}  ${item.phone}\n${item.area}${item.detail}${item.houseNumber || ''}`), success: res => { addressId.value = addresses.value[res.tapIndex].id } })
}
</script>

<template>
  <view class="page checkout-page">
    <view class="switcher"><text :class="{active:mode==='dine'}">堂食</text><text :class="{active:mode==='delivery'}">配送</text></view>
    <view class="content">
      <view v-if="mode==='delivery'" class="card address-card" @tap="chooseAddress">
        <template v-if="selectedAddress"><view class="address-title"><text class="pin">⌖</text><text>{{selectedAddress.contactName}}</text><text class="muted">{{selectedAddress.phone}}</text></view><text class="address-text">{{selectedAddress.area}}{{selectedAddress.detail}}{{selectedAddress.houseNumber||''}}</text></template>
        <template v-else><text class="brand">＋ 添加收货地址</text></template><text class="arrow">›</text>
      </view>
      <view v-else class="card form-card">
        <text class="section-title">就餐信息</text>
        <view class="line"><text>桌号</text><input v-model="form.tableNo" :disabled="form.noSeatYet" placeholder="请输入桌号 例如 A06" /></view>
        <view class="line check-line" @tap="form.noSeatYet=!form.noSeatYet"><view class="round" :class="{checked:form.noSeatYet}">{{form.noSeatYet?'✓':''}}</view><text>暂未入座，到店后告知商家</text></view>
        <view class="line"><text>联系人</text><input v-model="form.contactName" placeholder="请输入联系人" /></view>
        <view class="line"><text>联系电话</text><input v-model="form.contactPhone" type="number" maxlength="11" placeholder="请输入手机号" /></view>
      </view>
      <view v-if="mode==='delivery'" class="card delivery-info"><view class="between"><text>配送方式</text><text>商家配送</text></view><view class="between"><text>预计送达时间</text><text>{{shop.estimatedDeliveryMinutes||30}}分钟</text></view><text class="hint">{{shop.deliveryRange||'订单由商家自行配送，请保持电话畅通'}}</text></view>
      <view class="card remark-card"><text class="section-title">订单备注</text><textarea v-model="form.remark" maxlength="500" placeholder="口味偏好、餐具需求等（选填）"/><text>{{form.remark.length}}/500</text></view>
      <view class="card goods-card"><text class="section-title">商品清单</text><view v-for="item in items" :key="item.id" class="goods"><ProductImage :src="item.productImageUrl" :name="item.productName" size="92rpx"/><view><text>{{item.productName}}</text><small>{{[item.specificationName,item.tasteName].filter(Boolean).join(' · ')}}</small></view><text class="qty">×{{item.quantity}}</text><text>¥{{money(item.amount)}}</text></view></view>
      <view class="card fee-card"><view class="between"><text>商品总额</text><text>¥{{money(goodsAmount)}}</text></view><view class="between"><text>包装费</text><text>¥{{money(shop.packageFee)}}</text></view><view v-if="mode==='delivery'" class="between"><text>配送费</text><text>¥{{money(shop.deliveryFee)}}</text></view></view>
    </view>
    <view class="submit-bar"><view>实付金额 <text>¥{{money(payAmount)}}</text></view><button class="primary-btn" :disabled="submitting" @tap="submit">{{submitting?'提交中…':'提交订单'}}</button></view>
  </view>
</template>

<style scoped>
.checkout-page{padding-bottom:150rpx}.switcher{height:92rpx;padding:12rpx 34rpx;display:flex;background:#fff}.switcher text{flex:1;border-radius:12rpx;background:#f2f2f2;color:#666;text-align:center;line-height:68rpx}.switcher .active{background:#ff7c22;color:#fff;font-weight:700}.content{padding-top:22rpx}.card{position:relative;padding:28rpx;margin-bottom:22rpx}.address-title{display:flex;align-items:center;gap:14rpx;font-weight:700}.pin{color:#ff8a34}.address-text{display:block;margin:14rpx 40rpx 0;font-size:29rpx;font-weight:700}.arrow{position:absolute;right:24rpx;top:50%;transform:translateY(-50%);font-size:48rpx;color:#aaa}.form-card .section-title,.remark-card .section-title,.goods-card .section-title{display:block;margin-bottom:18rpx}.line{min-height:86rpx;display:flex;align-items:center;border-top:2rpx solid #f1f1f1}.line>text{width:150rpx;color:#666;font-size:25rpx}.line input{flex:1;text-align:right;font-size:26rpx}.check-line{gap:12rpx}.check-line>text{width:auto}.round{width:34rpx;height:34rpx;border:2rpx solid #ccc;border-radius:50%;color:#fff;text-align:center;line-height:30rpx}.round.checked{background:#ff8a34;border-color:#ff8a34}.delivery-info .between,.fee-card .between{margin-bottom:22rpx}.hint{display:block;margin-top:18rpx;padding:20rpx;border-radius:12rpx;background:#f7f8fa;color:#777;font-size:23rpx;line-height:1.6}.remark-card textarea{width:100%;height:120rpx;padding:18rpx;border-radius:14rpx;background:#f7f8fa;font-size:25rpx}.remark-card>text:last-child{display:block;text-align:right;color:#aaa;font-size:20rpx}.goods{display:flex;align-items:center;gap:16rpx;padding:18rpx 0;border-top:2rpx solid #f3f3f3;font-size:24rpx}.goods>view:nth-child(2){min-width:0;flex:1;display:flex;flex-direction:column}.goods small{margin-top:6rpx;color:#999}.qty{color:#999}.submit-bar{position:fixed;z-index:50;left:0;right:0;bottom:0;height:calc(126rpx + env(safe-area-inset-bottom));padding:18rpx 28rpx calc(18rpx + env(safe-area-inset-bottom));display:flex;align-items:center;background:#fff;border-top:2rpx solid #eee}.submit-bar>view{flex:1}.submit-bar>view text{margin-left:8rpx;color:#ff6b18;font-size:36rpx;font-weight:800}.submit-bar button{width:250rpx}
</style>

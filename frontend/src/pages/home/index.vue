<script setup>
import { computed, ref } from 'vue'
import { onPullDownRefresh, onShow } from '@dcloudio/uni-app'
import AppHeader from '../../components/AppHeader.vue'
import BottomNav from '../../components/BottomNav.vue'
import ProductImage from '../../components/ProductImage.vue'
import { api } from '../../api'
import { money } from '../../utils/format'

const shop = ref({})
const categories = ref([])
const products = ref([])
const activeCategory = ref(null)
const mode = ref(uni.getStorageSync('orderMode') || 'dine')
const cart = ref({ items: [], totalQuantity: 0, totalAmount: 0 })
const detail = ref(null)
const specificationId = ref(null)
const tasteId = ref(null)
const quantity = ref(1)
const keyword = ref('')

const available = computed(() => mode.value === 'dine' ? shop.value.dineInStatus?.code === 1 : shop.value.deliveryStatus?.code === 1)
const visibleProducts = computed(() => products.value.filter(item => !keyword.value || `${item.name}${item.description || ''}`.includes(keyword.value)))

onShow(load)
onPullDownRefresh(async () => { await load(); uni.stopPullDownRefresh() })

async function load() {
  try {
    const [shopData, categoryData] = await Promise.all([api.shop(), api.categories()])
    shop.value = shopData || {}
    categories.value = categoryData || []
    if (!activeCategory.value && categories.value.length) activeCategory.value = categories.value[0].id
    await loadProducts()
    if (uni.getStorageSync('userToken')) cart.value = await api.cart()
  } catch (_) {}
}

async function loadProducts() {
  const data = await api.products({ current: 1, size: 100, categoryId: activeCategory.value || undefined })
  products.value = data?.records || []
}

async function chooseCategory(id) {
  activeCategory.value = id
  await loadProducts()
}

function setMode(value) {
  mode.value = value
  uni.setStorageSync('orderMode', value)
}

async function openProduct(product) {
  try {
    detail.value = await api.product(product.id)
    specificationId.value = detail.value.specifications?.[0]?.id || null
    tasteId.value = null
    quantity.value = 1
  } catch (_) {}
}

async function quickAdd(product) {
  if (!uni.getStorageSync('userToken')) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }
  if (product.hasSpecifications) return openProduct(product)
  try {
    cart.value = await api.addCart({ productId: product.id, specificationId: null, tasteId: null, quantity: 1 })
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch (_) {}
}

async function addSelected() {
  if (!uni.getStorageSync('userToken')) return uni.reLaunch({ url: '/pages/login/index' })
  if (detail.value.specifications?.length && !specificationId.value) return uni.showToast({ title: '请选择规格', icon: 'none' })
  try {
    cart.value = await api.addCart({ productId: detail.value.id, specificationId: specificationId.value, tasteId: tasteId.value, quantity: quantity.value })
    detail.value = null
    uni.showToast({ title: '已加入购物车', icon: 'success' })
  } catch (_) {}
}

function goCart() {
  if (!uni.getStorageSync('userToken')) return uni.reLaunch({ url: '/pages/login/index' })
  uni.navigateTo({ url: '/pages/cart/index' })
}
</script>

<template>
  <view class="page page-with-nav">
    <AppHeader :title="shop.name || '食刻小馆'" :subtitle="shop.businessStatus?.description || '加载中'">
      <text class="notice-icon">◉</text>
    </AppHeader>
    <view class="search-wrap"><text>⌕</text><input v-model="keyword" placeholder="搜索商品" /></view>
    <view class="mode-tabs"><view :class="{ active: mode === 'dine' }" @tap="setMode('dine')">堂食</view><view :class="{ active: mode === 'delivery' }" @tap="setMode('delivery')">配送</view></view>
    <view class="banner">
      <image src="/static/home-banner.jpg" mode="aspectFill" />
      <view class="banner-copy"><text>好好吃饭</text><text>用心生活</text><small>{{ shop.notice || '新鲜现做 · 暖心到家' }}</small></view>
    </view>
    <view v-if="!available" class="closed-tip">当前{{ mode === 'dine' ? '堂食' : '配送' }}暂停服务</view>
    <view class="menu-area">
      <scroll-view class="category-list" scroll-y>
        <view v-for="category in categories" :key="category.id" class="category" :class="{ active: activeCategory === category.id }" @tap="chooseCategory(category.id)">{{ category.name }}</view>
      </scroll-view>
      <scroll-view class="product-list" scroll-y>
        <view v-for="product in visibleProducts" :key="product.id" class="product" @tap="openProduct(product)">
          <ProductImage :src="product.imageUrl" :name="product.name" size="142rpx" />
          <view class="product-info">
            <text class="product-name">{{ product.name }}</text>
            <text class="product-desc ellipsis">{{ product.description || '新鲜现做，欢迎品尝' }}</text>
            <text class="sales">库存 {{ product.stock }}</text>
            <view class="between"><view><text class="price">¥{{ money(product.price) }}</text><text v-if="product.originalPrice" class="original">¥{{ money(product.originalPrice) }}</text></view><button class="plus" :disabled="product.status?.code !== 1 || !available" @tap.stop="quickAdd(product)">+</button></view>
          </view>
        </view>
        <view v-if="!visibleProducts.length" class="empty"><text class="empty-icon">🍽</text>暂无商品</view>
      </scroll-view>
    </view>
    <view class="cart-bar" @tap="goCart">
      <view class="cart-icon">🛒<text v-if="cart.totalQuantity" class="count">{{ cart.totalQuantity }}</text></view>
      <view class="cart-price"><text>¥{{ money(cart.totalAmount) }}</text><small>{{ mode === 'delivery' ? `另需配送费 ¥${money(shop.deliveryFee)}` : '到店享用更美味' }}</small></view>
      <button>去结算</button>
    </view>
    <BottomNav active="home" />

    <view v-if="detail" class="mask" @tap.self="detail = null">
      <view class="sheet">
        <view class="sheet-image"><ProductImage :src="detail.imageUrl" :name="detail.name" size="100%" radius="0"/><text class="close" @tap="detail = null">×</text></view>
        <view class="sheet-content">
          <view class="between"><text class="detail-name">{{ detail.name }}</text><text class="price">¥{{ money(detail.price) }}</text></view>
          <text class="detail-desc">{{ detail.description }}</text>
          <view v-if="detail.specifications?.length" class="option-group"><text class="option-title">规格选择</text><view class="options"><text v-for="item in detail.specifications" :key="item.id" :class="{ selected: specificationId === item.id }" @tap="specificationId = item.id">{{ item.name }}<template v-if="Number(item.priceDelta)"> +¥{{ money(item.priceDelta) }}</template></text></view></view>
          <view v-if="detail.tastes?.length" class="option-group"><text class="option-title">口味选择</text><view class="options"><text :class="{ selected: !tasteId }" @tap="tasteId = null">默认</text><text v-for="item in detail.tastes" :key="item.id" :class="{ selected: tasteId === item.id }" @tap="tasteId = item.id">{{ item.name }}</text></view></view>
          <view class="quantity between"><text>数量</text><view><button @tap="quantity = Math.max(1, quantity - 1)">−</button><text>{{ quantity }}</text><button @tap="quantity++">＋</button></view></view>
          <button class="primary-btn" @tap="addSelected">加入购物车</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.notice-icon{font-size:38rpx;color:#666}.search-wrap{margin:0 28rpx 18rpx;height:68rpx;padding:0 22rpx;display:flex;align-items:center;gap:12rpx;border-radius:34rpx;background:#f3f3f3;color:#999}.search-wrap input{flex:1;font-size:25rpx}.mode-tabs{margin:0 124rpx 20rpx;height:62rpx;display:flex;padding:6rpx;border-radius:32rpx;background:#eee}.mode-tabs view{flex:1;text-align:center;border-radius:26rpx;line-height:50rpx;color:#666;font-size:24rpx}.mode-tabs .active{background:linear-gradient(135deg,#ffa138,#ff741d);color:#fff;font-weight:700}.banner{position:relative;height:188rpx;margin:0 28rpx 22rpx;border-radius:22rpx;overflow:hidden}.banner image{width:100%;height:100%}.banner-copy{position:absolute;left:30rpx;top:28rpx;display:flex;flex-direction:column;color:#9b4f1d;font-family:serif;font-size:34rpx;font-weight:800;line-height:1.35}.banner-copy small{margin-top:12rpx;font-family:inherit;font-size:19rpx;font-weight:400}.closed-tip{margin:0 28rpx 18rpx;padding:16rpx;border-radius:12rpx;background:#fff1f0;color:#ff4d4f;text-align:center;font-size:24rpx}.menu-area{display:flex;height:calc(100vh - 535rpx - env(safe-area-inset-top));min-height:500rpx;background:#fff}.category-list{width:174rpx;background:#f6f6f6}.category{position:relative;padding:34rpx 14rpx;text-align:center;color:#777;font-size:25rpx}.category.active{background:#fff;color:#222;font-weight:700}.category.active::before{content:'';position:absolute;left:0;top:28rpx;width:7rpx;height:42rpx;border-radius:4rpx;background:#ff8a34}.product-list{flex:1;padding:8rpx 22rpx 200rpx}.product{display:flex;gap:20rpx;padding:22rpx 0;border-bottom:2rpx solid #f3f3f3}.product-info{min-width:0;flex:1}.product-name{display:block;font-weight:700;font-size:28rpx}.product-desc,.sales{display:block;margin-top:8rpx;color:#999;font-size:21rpx}.product-info .between{margin-top:13rpx}.original{margin-left:10rpx;color:#aaa;text-decoration:line-through;font-size:20rpx}.plus{width:48rpx;height:48rpx;border-radius:50%;padding:0;background:#ff8a34;color:#fff;line-height:46rpx;font-size:34rpx}.plus[disabled]{background:#ccc}.cart-bar{position:fixed;z-index:85;left:28rpx;right:28rpx;bottom:calc(122rpx + env(safe-area-inset-bottom));height:94rpx;display:flex;align-items:center;border-radius:47rpx;background:#222;color:#fff;box-shadow:0 8rpx 24rpx rgba(0,0,0,.2)}.cart-icon{position:relative;width:94rpx;height:94rpx;margin-left:4rpx;border-radius:50%;display:flex;align-items:center;justify-content:center;background:#ff8a34;font-size:44rpx}.count{position:absolute;right:-3rpx;top:-5rpx;min-width:32rpx;height:32rpx;padding:0 7rpx;border-radius:16rpx;background:#ff5f18;font-size:19rpx;text-align:center;line-height:32rpx}.cart-price{flex:1;display:flex;flex-direction:column;padding-left:18rpx;font-size:31rpx;font-weight:700}.cart-price small{color:#f7a155;font-size:19rpx;font-weight:400}.cart-bar button{height:72rpx;margin-right:10rpx;padding:0 32rpx;border-radius:36rpx;background:#ff711d;color:#fff;font-size:26rpx;line-height:72rpx}.mask{position:fixed;z-index:200;inset:0;display:flex;align-items:flex-end;background:rgba(0,0,0,.55)}.sheet{width:100%;max-height:88vh;overflow:auto;border-radius:34rpx 34rpx 0 0;background:#fff}.sheet-image{position:relative;width:100%;height:360rpx;overflow:hidden}.close{position:absolute;right:22rpx;top:22rpx;width:56rpx;height:56rpx;border-radius:50%;background:rgba(255,255,255,.8);font-size:45rpx;text-align:center;line-height:52rpx}.sheet-content{padding:30rpx 30rpx calc(34rpx + env(safe-area-inset-bottom))}.detail-name{font-size:38rpx;font-weight:800}.detail-desc{display:block;margin:12rpx 0 30rpx;color:#777;font-size:25rpx}.option-group{margin-bottom:28rpx}.option-title{display:block;margin-bottom:16rpx;font-weight:700}.options{display:flex;flex-wrap:wrap;gap:14rpx}.options text{padding:15rpx 24rpx;border-radius:12rpx;background:#f6f6f6;font-size:24rpx}.options .selected{background:#fff5ee;color:#ff741d;box-shadow:inset 0 0 0 2rpx #ff8a34}.quantity{margin:26rpx 0}.quantity view{display:flex;align-items:center;gap:22rpx}.quantity button{width:54rpx;height:54rpx;padding:0;border-radius:50%;background:#f2f2f2;line-height:52rpx}.sheet-content .primary-btn{width:100%}
</style>

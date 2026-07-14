const BASE_URL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

function tokenFor(url) {
  if (url.startsWith('/api/merchant')) return uni.getStorageSync('merchantToken')
  if (url.startsWith('/api/user')) return uni.getStorageSync('userToken')
  return ''
}

function goLogin(url) {
  const merchant = url.startsWith('/api/merchant')
  uni.removeStorageSync(merchant ? 'merchantToken' : 'userToken')
  setTimeout(() => uni.reLaunch({ url: merchant ? '/pages/merchant/login' : '/pages/login/index' }), 300)
}

export function request({ url, method = 'GET', data, header = {}, hideLoading = false }) {
  const token = tokenFor(url)
  if (!hideLoading) uni.showLoading({ title: '加载中', mask: true })
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...header,
      },
      success(res) {
        const body = res.data || {}
        if (res.statusCode === 401) {
          goLogin(url)
          reject(new Error(body.message || '登录已过期'))
          return
        }
        if (res.statusCode >= 200 && res.statusCode < 300 && body.code === 0) {
          resolve(body.data)
          return
        }
        const error = new Error(body.message || '请求失败，请稍后重试')
        error.code = body.code
        error.statusCode = res.statusCode
        uni.showToast({ title: error.message, icon: 'none' })
        reject(error)
      },
      fail(err) {
        uni.showToast({ title: '无法连接服务器', icon: 'none' })
        reject(err)
      },
      complete() {
        if (!hideLoading) uni.hideLoading()
      },
    })
  })
}

export function imageUrl(path) {
  if (!path) return ''
  if (/^https?:\/\//.test(path)) return path
  return `${BASE_URL}${path.startsWith('/') ? '' : '/'}${path}`
}

export { BASE_URL }

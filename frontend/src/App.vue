<script>
export default {
  onLaunch() {},
}
</script>

<style>
page {
  --brand: #ff8a34;
  --brand-deep: #ff6b18;
  --brand-light: #fff2e8;
  --success: #34c759;
  --warning: #ffb020;
  --danger: #ff4d4f;
  --blue: #1677ff;
  --purple: #722ed1;
  --cyan: #13c2c2;
  --text: #222;
  --text-2: #666;
  --text-3: #999;
  --bg: #f7f8fa;
  --line: #eee;
  background: var(--bg);
  color: var(--text);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", sans-serif;
  font-size: 28rpx;
}

view, text, image, input, textarea, button, scroll-view, switch {
  box-sizing: border-box;
}

button::after { border: 0; }
button { margin: 0; }
.page { min-height: 100vh; background: var(--bg); }
.page-with-nav { padding-bottom: calc(130rpx + env(safe-area-inset-bottom)); }
.content { padding: 24rpx 28rpx; }
.card { background: #fff; border-radius: 24rpx; box-shadow: 0 2rpx 12rpx rgba(0,0,0,.04); }
.section-title { font-size: 30rpx; font-weight: 700; }
.muted { color: var(--text-3); }
.brand { color: var(--brand); }
.price { color: var(--brand-deep); font-weight: 700; }
.primary-btn {
  height: 88rpx; border-radius: 44rpx; background: linear-gradient(135deg, #ff961f, #ff6817);
  color: #fff; font-size: 30rpx; font-weight: 700; line-height: 88rpx;
}
.primary-btn[disabled] { background: #ccc; color: #fff; }
.outline-btn {
  height: 72rpx; padding: 0 30rpx; border-radius: 36rpx; border: 2rpx solid var(--brand);
  background: #fff; color: var(--brand); font-size: 26rpx; line-height: 68rpx;
}
.danger-outline { border-color: var(--danger); color: var(--danger); }
.field { margin-bottom: 26rpx; }
.field-label { display: block; margin-bottom: 12rpx; color: var(--text-2); font-size: 26rpx; font-weight: 600; }
.input, .textarea {
  width: 100%; border: 2rpx solid var(--line); border-radius: 18rpx; background: #fff;
  padding: 0 24rpx; font-size: 28rpx;
}
.input { height: 88rpx; }
.textarea { height: 150rpx; padding-top: 20rpx; }
.empty { padding: 120rpx 30rpx; text-align: center; color: var(--text-3); }
.empty-icon { display: block; margin-bottom: 18rpx; font-size: 88rpx; opacity: .55; }
.divider { height: 2rpx; background: var(--line); }
.row { display: flex; align-items: center; }
.between { display: flex; align-items: center; justify-content: space-between; }
.ellipsis { overflow: hidden; white-space: nowrap; text-overflow: ellipsis; }
</style>

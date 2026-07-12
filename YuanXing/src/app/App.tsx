import React, { useState } from "react";
import {
  ShoppingCart, Home, ClipboardList, User, ChevronRight, Plus, Minus, X,
  Check, 手机, MapPin, 搜索, Bell, ChevronLeft,
  Package, 设置, Store, 编辑,
  FileText, Info, Grid3X3, Lock, Eye, EyeOff, KeyRound, Shield
} from "lucide-react";

/* ─── BRAND TOKENS ─────────────────────────────── */
const P = "#FF8A34";
const PL = "#FFF2E8";
const SG = "#34C759";
const WY = "#FFB020";
const ER = "#FF4D4F";
const BL = "#1677FF";
const PU = "#722ED1";
const TE = "#13C2C2";
const T1 = "#222222";
const T2 = "#666666";
const T3 = "#999999";
const BG = "#F7F8FA";
const WH = "#FFFFFF";
const DV = "#EEEEEE";
const BD = "#E5E5E5";

/* ─── STATUS ────────────────────────────────────── */
const STATUS: Record<string, { bg: string; color: string }> = {
  待接单: { bg: "#FFF2E8", color: P },
  已接单: { bg: "#E8F4FF", color: BL },
  制作中: { bg: "#FFFBE6", color: WY },
  待取餐: { bg: "#F9F0FF", color: PU },
  配送中: { bg: "#E6FFFB", color: TE },
  已送达: { bg: "#F6FFED", color: SG },
  已完成: { bg: "#F6FFED", color: "#52C41A" },
  已取消: { bg: "#F5F5F5", color: T3 },
  已拒单: { bg: "#FFF1F0", color: ER },
};

/* ─── DATA ──────────────────────────────────────── */
const CATS = ["热门推荐", "主食", "小吃", "饮品", "套餐"];
const PRODS = [
  { id: 1, name: "招牌牛肉饭", desc: "精选黄牛肉，鲜嫩多汁", price: 26, origPrice: 30, sales: 328, cat: "热门推荐", em: "🍛", hasSpecs: false },
  { id: 2, name: "香辣鸡腿饭", desc: "酥脆鸡腿，秘制辣酱", price: 22, sales: 256, cat: "主食", em: "🍗", hasSpecs: true },
  { id: 3, name: "番茄鸡蛋面", desc: "酸甜番茄，滑嫩鸡蛋", price: 18, sales: 189, cat: "主食", em: "🍜", hasSpecs: false },
  { id: 4, name: "黄金鸡排", desc: "外酥里嫩，现炸现卖", price: 12, sales: 445, cat: "小吃", em: "🥩", hasSpecs: true },
  { id: 5, name: "柠檬冰茶", desc: "新鲜柠檬，现泡现调", price: 8, sales: 312, cat: "饮品", em: "🧋", hasSpecs: true },
  { id: 6, name: "双人套餐", desc: "两份主食+两份饮品", price: 58, origPrice: 68, sales: 167, cat: "套餐", em: "🍱", hasSpecs: false },
];
interface CartItem { id: number; name: string; em: string; price: number; qty: number; spec?: string }
interface Order { id: string; type: "dine-in" | "delivery"; status: string; time: string; amount: number; items: string[]; table?: string; address?: string; contact: string; phone: string; note?: string }
const ORDERS: Order[] = [
  { id: "202607090001", type: "dine-in", status: "待接单", time: "10分钟前", amount: 28, items: ["招牌牛肉饭×1"], table: "A06", contact: "小李", phone: "138 0000 0000", note: "不要香菜，少辣" },
  { id: "202607090002", type: "delivery", status: "配送中", time: "35分钟前", amount: 51, items: ["香辣鸡腿饭×1", "柠檬冰茶×2"], address: "阳光花园2栋1203室", contact: "小李", phone: "138 0000 0000", note: "到了请电话联系" },
  { id: "202607090005", type: "dine-in", status: "制作中", time: "20分钟前", amount: 36, items: ["番茄鸡蛋面×1", "黄金鸡排×2"], table: "C02", contact: "小赵", phone: "135 1111 2222" },
  { id: "202607090006", type: "delivery", status: "待取餐", time: "50分钟前", amount: 30, items: ["香辣鸡腿饭×1", "柠檬冰茶×1"], address: "幸福里小区4栋301室", contact: "小陈", phone: "136 3333 4444" },
  { id: "202607090003", type: "dine-in", status: "已完成", time: "昨天 12:30", amount: 60, items: ["双人套餐×1"], table: "B03", contact: "小王", phone: "139 1234 5678" },
  { id: "202607090004", type: "delivery", status: "已取消", time: "昨天 11:00", amount: 22, items: ["香辣鸡腿饭×1"], address: "城市广场3号楼501", contact: "小张", phone: "137 8888 9999" },
];

/* ─── MICRO COMPONENTS ──────────────────────────── */
function SBadge({ s }: { s: string }) {
  const st = STATUS[s] ?? { bg: "#F5F5F5", color: T3 };
  return <span style={{ background: st.bg, color: st.color, fontSize: 11, padding: "2px 8px", borderRadius: 10, fontWeight: 600 }}>{s}</span>;
}
function Div({ ml }: { ml?: number }) {
  return <div style={{ height: 1, background: DV, marginLeft: ml ?? 0 }} />;
}
function PBtn({ label, onClick, disabled, small }: { label: string; onClick?: () => void; disabled?: boolean; small?: boolean }) {
  return (
    <button onClick={onClick} disabled={disabled} style={{
      height: small ? 36 : 44, borderRadius: small ? 18 : 22, border: "none",
      background: disabled ? "#CCCCCC" : P, color: WH, fontWeight: 600,
      fontSize: small ? 13 : 15, cursor: disabled ? "not-allowed" : "pointer", width: "100%",
    }}>{label}</button>
  );
}
function OBtn({ label, onClick, danger }: { label: string; onClick?: () => void; danger?: boolean }) {
  return (
    <button onClick={onClick} style={{
      height: 36, borderRadius: 18, border: `1.5px solid ${danger ? ER : P}`,
      background: WH, color: danger ? ER : P, fontWeight: 500, fontSize: 13,
      cursor: "pointer", padding: "0 16px",
    }}>{label}</button>
  );
}
function Toggle({ on, onChange }: { on: boolean; onChange: () => void }) {
  return (
    <div onClick={onChange} style={{
      width: 44, height: 24, borderRadius: 12, background: on ? SG : "#CCCCCC",
      position: "relative", cursor: "pointer", flexShrink: 0, transition: "background .2s",
    }}>
      <div style={{ width: 18, height: 18, borderRadius: 9, background: WH, position: "absolute", top: 3, left: on ? 23 : 3, transition: "left .2s" }} />
    </div>
  );
}
function NavBar({ title, onBack, right }: { title: string; onBack?: () => void; right?: React.ReactNode }) {
  return (
    <div style={{ height: 52, background: WH, borderBottom: `1px solid ${DV}`, display: "flex", alignItems: "center", padding: "0 16px", gap: 4, flexShrink: 0 }}>
      {onBack && <button onClick={onBack} style={{ background: "none", border: "none", cursor: "pointer", padding: 6, color: T1 }}><ChevronLeft size={22} /></button>}
      <span style={{ flex: 1, fontWeight: 700, fontSize: 17, color: T1, textAlign: onBack ? "center" : "left" }}>{title}</span>
      {right}
    </div>
  );
}
function StatusBar({ dark }: { dark?: boolean }) {
  return (
    <div style={{ height: 44, background: "transparent", display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 20px", fontSize: 12, fontWeight: 700, color: dark ? WH : T1, flexShrink: 0 }}>
      <span>9:41</span>
      <div style={{ display: "flex", gap: 5, alignItems: "center" }}>
        <span style={{ letterSpacing: 1 }}>●●●</span><span>Wi-Fi</span><span>100%</span>
      </div>
    </div>
  );
}
function UserTabs({ active, nav }: { active: string; nav: (s: string) => void }) {
  const tabs = [{ id: "home", Icon: Home, label: "首页" }, { id: "orders", Icon: ClipboardList, label: "订单" }, { id: "my", Icon: User, label: "我的" }];
  return (
    <div style={{ background: WH, borderTop: `1px solid ${DV}`, display: "flex", paddingBottom: 16, flexShrink: 0 }}>
      {tabs.map(({ id, Icon, label }) => (
        <button key={id} onClick={() => nav(id)} style={{ flex: 1, background: "none", border: "none", cursor: "pointer", display: "flex", flexDirection: "column", alignItems: "center", paddingTop: 10, gap: 3, color: active === id ? P : T3 }}>
          <Icon size={22} /><span style={{ fontSize: 10, fontWeight: active === id ? 700 : 400 }}>{label}</span>
        </button>
      ))}
    </div>
  );
}
function MerchantTabs({ active, nav }: { active: string; nav: (s: string) => void }) {
  const tabs = [{ id: "dashboard", Icon: Home, label: "工作台" }, { id: "orders", Icon: ClipboardList, label: "订单" }, { id: "products", Icon: Package, label: "商品" }, { id: "profile", Icon: User, label: "我的" }];
  return (
    <div style={{ background: WH, borderTop: `1px solid ${DV}`, display: "flex", paddingBottom: 16, flexShrink: 0 }}>
      {tabs.map(({ id, Icon, label }) => (
        <button key={id} onClick={() => nav(id)} style={{ flex: 1, background: "none", border: "none", cursor: "pointer", display: "flex", flexDirection: "column", alignItems: "center", paddingTop: 10, gap: 3, color: active === id ? P : T3 }}>
          <Icon size={20} /><span style={{ fontSize: 10, fontWeight: active === id ? 700 : 400 }}>{label}</span>
        </button>
      ))}
    </div>
  );
}

/* ─── SCREEN: LOGIN ─────────────────────────────── */
function LoginScreen({ nav }: { nav: (s: string) => void }) {
  const [agreed, setAgreed] = useState(false);
  return (
    <div style={{ height: "100%", background: WH, display: "flex", flexDirection: "column" }}>
      <StatusBar />
      <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "40px 32px" }}>
        <div style={{ width: 88, height: 88, borderRadius: 24, background: PL, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 44, marginBottom: 16 }}>🍜</div>
        <div style={{ fontWeight: 700, fontSize: 26, color: T1, marginBottom: 6 }}>食刻小馆</div>
        <div style={{ fontSize: 14, color: T2, marginBottom: 40 }}>堂食配送，轻松点餐</div>
        <div onClick={() => setAgreed(!agreed)} style={{ display: "flex", alignItems: "flex-start", gap: 10, marginBottom: 20, alignSelf: "stretch", cursor: "pointer" }}>
          <div style={{ width: 18, height: 18, borderRadius: 9, border: `1.5px solid ${agreed ? P : BD}`, background: agreed ? P : WH, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0, marginTop: 2 }}>
            {agreed && <Check size={11} color={WH} strokeWidth={3} />}
          </div>
          <span style={{ fontSize: 12, color: T2, lineHeight: 1.7 }}>已阅读并同意 <span style={{ color: P }}>《用户协议》</span> 和 <span style={{ color: P }}>《隐私政策》</span></span>
        </div>
        <PBtn label="微信快捷登录" disabled={!agreed} onClick={() => nav("user-home")} />
        <div style={{ fontSize: 12, color: T3, marginTop: 12, textAlign: "center", marginBottom: 28 }}>微信授权后自动登录，快捷安全</div>
        {/* Merchant secondary entry */}
        <div onClick={() => nav("merchant-login")} style={{ display: "flex", alignItems: "center", gap: 6, cursor: "pointer", padding: "8px 0" }}>
          <span style={{ fontSize: 13, color: T3 }}>商家人员登录</span>
          <ChevronRight size={14} color={T3} />
        </div>
      </div>
      <div style={{ textAlign: "center", padding: "12px 32px 28px", fontSize: 11, color: T3 }}>普通用户请使用微信快捷登录</div>
    </div>
  );
}

/* ─── SCREEN: MERCHANT LOGIN ────────────────────── */
function MerchantLoginScreen({ nav }: { nav: (s: string) => void }) {
  const [account, setAccount] = useState("");
  const [password, setPassword] = useState("");
  const [showPwd, setShowPwd] = useState(false);
  const [remember, setRemember] = useState(false);
  const [loading, setLoading] = useState(false);
  const [accountErr, setAccountErr] = useState("");
  const [passwordErr, setPasswordErr] = useState("");
  const [showForgot, setShowForgot] = useState(false);

  const canSubmit = account.trim() !== "" && password !== "" && !loading;

  const handleLogin = () => {
    setAccountErr("");
    setPasswordErr("");
    if (!account.trim()) { setAccountErr("请输入商家账号"); return; }
    if (!password) { setPasswordErr("请输入登录密码"); return; }
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      if (account === "merchant001" && password === "merchant123") {
        nav("merchant-dashboard");
      } else {
        setPasswordErr("账号或密码错误，请重新输入");
        setPassword("");
      }
    }, 1200);
  };

  return (
    <div style={{ height: "100%", background: WH, display: "flex", flexDirection: "column" }}>
      <StatusBar />
      <NavBar title="商家登录" onBack={() => nav("user-login")} />
      <div style={{ flex: 1, overflowY: "auto", padding: "28px 28px 24px" }}>
        {/* Logo & title */}
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", marginBottom: 32 }}>
          <div style={{ width: 72, height: 72, borderRadius: 20, background: PL, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 36, marginBottom: 12 }}>🍜</div>
          <div style={{ fontWeight: 700, fontSize: 20, color: T1, marginBottom: 4 }}>商家管理端</div>
          <div style={{ fontSize: 12, color: T3, textAlign: "center", lineHeight: 1.7 }}>登录后可处理订单、管理商品和设置店铺</div>
        </div>
        {/* Account field */}
        <div style={{ marginBottom: 16 }}>
          <div style={{ fontSize: 13, color: T2, marginBottom: 6, fontWeight: 500 }}>商家账号</div>
          <div style={{ position: "relative", display: "flex", alignItems: "center" }}>
            <Store size={16} color={accountErr ? ER : T3} style={{ position: "absolute", left: 13, flexShrink: 0 }} />
            <input
              value={account}
              onChange={e => { setAccount(e.target.value); setAccountErr(""); }}
              placeholder="请输入商家账号"
              style={{ width: "100%", height: 46, borderRadius: 10, border: `1.5px solid ${accountErr ? ER : DV}`, padding: "0 12px 0 38px", fontSize: 14, boxSizing: "border-box", fontFamily: "inherit", outline: "none" }}
            />
          </div>
          {accountErr && <div style={{ fontSize: 11, color: ER, marginTop: 5 }}>{accountErr}</div>}
        </div>
        {/* Password field */}
        <div style={{ marginBottom: 16 }}>
          <div style={{ fontSize: 13, color: T2, marginBottom: 6, fontWeight: 500 }}>登录密码</div>
          <div style={{ position: "relative", display: "flex", alignItems: "center" }}>
            <Lock size={16} color={passwordErr ? ER : T3} style={{ position: "absolute", left: 13, flexShrink: 0 }} />
            <input
              value={password}
              onChange={e => { setPassword(e.target.value); setPasswordErr(""); }}
              placeholder="请输入登录密码"
              type={showPwd ? "text" : "password"}
              style={{ width: "100%", height: 46, borderRadius: 10, border: `1.5px solid ${passwordErr ? ER : DV}`, padding: "0 44px 0 38px", fontSize: 14, boxSizing: "border-box", fontFamily: "inherit", outline: "none" }}
            />
            <button onClick={() => setShowPwd(!showPwd)} style={{ position: "absolute", right: 12, background: "none", border: "none", cursor: "pointer", display: "flex", alignItems: "center" }}>
              {showPwd ? <EyeOff size={17} color={T3} /> : <Eye size={17} color={T3} />}
            </button>
          </div>
          {passwordErr && <div style={{ fontSize: 11, color: ER, marginTop: 5 }}>{passwordErr}</div>}
        </div>
        {/* Remember account */}
        <div onClick={() => setRemember(!remember)} style={{ display: "flex", alignItems: "center", gap: 8, marginBottom: 28, cursor: "pointer" }}>
          <div style={{ width: 16, height: 16, borderRadius: 8, border: `1.5px solid ${remember ? P : BD}`, background: remember ? P : WH, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
            {remember && <Check size={9} color={WH} strokeWidth={3} />}
          </div>
          <span style={{ fontSize: 12, color: T2 }}>记住账号</span>
        </div>
        {/* Login button */}
        <button
          onClick={handleLogin}
          disabled={!canSubmit}
          style={{ width: "100%", height: 46, borderRadius: 23, border: "none", background: canSubmit ? P : "#CCCCCC", color: WH, fontWeight: 700, fontSize: 15, cursor: canSubmit ? "pointer" : "not-allowed", marginBottom: 20 }}
        >
          {loading ? "登录中…" : "登录商家端"}
        </button>
        {/* Forgot password */}
        <div style={{ textAlign: "center", marginBottom: 24 }}>
          <span onClick={() => setShowForgot(true)} style={{ fontSize: 13, color: T3, cursor: "pointer", textDecoration: "underline" }}>忘记密码？</span>
        </div>
        {/* Hint */}
        <div style={{ background: BG, borderRadius: 10, padding: "10px 14px", fontSize: 11, color: T3, lineHeight: 1.7 }}>
          💡 演示账号：merchant001 · 密码：merchant123
        </div>
      </div>
      {/* Bottom link */}
      <div style={{ textAlign: "center", padding: "12px 28px 28px" }}>
        <span onClick={() => nav("user-login")} style={{ fontSize: 13, color: T3, cursor: "pointer" }}>返回用户登录</span>
      </div>
      {/* Forgot password modal */}
      {showForgot && (
        <div style={{ position: "absolute", inset: 0, background: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 300, padding: 28 }} onClick={() => setShowForgot(false)}>
          <div style={{ background: WH, borderRadius: 16, padding: "28px 24px", width: "100%", textAlign: "center" }} onClick={e => e.stopPropagation()}>
            <div style={{ fontSize: 36, marginBottom: 12 }}>🔑</div>
            <div style={{ fontWeight: 700, fontSize: 16, color: T1, marginBottom: 8 }}>忘记密码</div>
            <div style={{ fontSize: 13, color: T2, lineHeight: 1.7, marginBottom: 24 }}>请联系店铺管理员重置密码</div>
            <button onClick={() => setShowForgot(false)} style={{ width: "100%", height: 44, borderRadius: 22, border: "none", background: P, color: WH, fontWeight: 700, fontSize: 15, cursor: "pointer" }}>我知道了</button>
          </div>
        </div>
      )}
    </div>
  );
}

/* ─── SCREEN: USER HOME ─────────────────────────── */
function UserHomeScreen({ cart, addToCart, nav, mode, setMode }: {
  cart: CartItem[]; addToCart: (i: CartItem) => void;
  nav: (s: string) => void; mode: "dine-in" | "delivery"; setMode: (m: "dine-in" | "delivery") => void;
}) {
  const [activeCat, setActiveCat] = useState("热门推荐");
  const [detailProd, setDetailProd] = useState<typeof PRODS[0] | null>(null);
  const cartTotal = cart.reduce((s, i) => s + i.price * i.qty, 0);
  const cartCount = cart.reduce((s, i) => s + i.qty, 0);
  const qty = (id: number) => cart.find(i => i.id === id)?.qty ?? 0;
  const filtered = PRODS.filter(p => activeCat === "热门推荐" || p.cat === activeCat);

  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      {/* Header */}
      <div style={{ background: WH, padding: "10px 16px 12px", borderBottom: `1px solid ${DV}` }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10 }}>
          <div>
            <div style={{ fontWeight: 700, fontSize: 17, color: T1 }}>食刻小馆</div>
            <div style={{ fontSize: 11, color: SG, marginTop: 1 }}>● 营业中</div>
          </div>
          <Bell size={20} color={T2} style={{ cursor: "pointer" }} />
        </div>
        {/* Mode toggle */}
        <div style={{ background: BG, borderRadius: 20, padding: 3, display: "flex", marginBottom: 10 }}>
          {(["dine-in", "delivery"] as const).map(m => (
            <button key={m} onClick={() => setMode(m)} style={{
              flex: 1, height: 32, borderRadius: 17, border: "none",
              background: mode === m ? P : "transparent", color: mode === m ? WH : T2,
              fontWeight: mode === m ? 700 : 400, fontSize: 14, cursor: "pointer",
            }}>{m === "dine-in" ? "🍽 堂食" : "🛵 配送"}</button>
          ))}
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 8, background: BG, borderRadius: 20, padding: "8px 14px" }}>
          <Search size={14} color={T3} />
          <span style={{ fontSize: 13, color: T3 }}>搜索菜品</span>
        </div>
        {mode === "delivery" && (
          <div style={{ marginTop: 8, background: PL, borderRadius: 8, padding: "6px 10px", fontSize: 11, color: P }}>
            📍 阳光花园2栋1203室 · 商家配送 · 预计30-45分钟
          </div>
        )}
      </div>
      {/* Body */}
      <div style={{ flex: 1, display: "flex", overflow: "hidden" }}>
        {/* Category sidebar */}
        <div style={{ width: 76, background: WH, overflowY: "auto", borderRight: `1px solid ${DV}`, flexShrink: 0 }}>
          {CATS.map(cat => (
            <button key={cat} onClick={() => setActiveCat(cat)} style={{
              width: "100%", padding: "13px 6px", border: "none", cursor: "pointer",
              background: activeCat === cat ? PL : WH,
              borderLeft: `3px solid ${activeCat === cat ? P : "transparent"}`,
              color: activeCat === cat ? P : T2, fontSize: 12,
              fontWeight: activeCat === cat ? 700 : 400, textAlign: "center", lineHeight: 1.4,
            }}>{cat}</button>
          ))}
        </div>
        {/* Product list */}
        <div style={{ flex: 1, overflowY: "auto", padding: "8px 10px" }}>
          {filtered.map(p => (
            <div key={p.id} onClick={() => setDetailProd(p)} style={{
              background: WH, borderRadius: 12, padding: 11, marginBottom: 8,
              display: "flex", gap: 10, cursor: "pointer", boxShadow: "0 1px 4px rgba(0,0,0,0.05)",
            }}>
              <div style={{ width: 68, height: 68, borderRadius: 10, background: BG, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 30, flexShrink: 0 }}>{p.em}</div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontWeight: 600, fontSize: 13, color: T1, marginBottom: 2 }}>{p.name}</div>
                <div style={{ fontSize: 11, color: T3, marginBottom: 3, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>{p.desc}</div>
                <div style={{ fontSize: 10, color: T3, marginBottom: 6 }}>月售 {p.sales}</div>
                <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
                  <div style={{ display: "flex", alignItems: "baseline", gap: 4 }}>
                    <span style={{ color: P, fontWeight: 700, fontSize: 15 }}>¥{p.price}</span>
                    {p.origPrice && <span style={{ color: T3, fontSize: 10, textDecoration: "line-through" }}>¥{p.origPrice}</span>}
                  </div>
                  {qty(p.id) > 0 ? (
                    <div style={{ display: "flex", alignItems: "center", gap: 5 }} onClick={e => e.stopPropagation()}>
                      <button onClick={() => addToCart({ ...p, qty: -1 })} style={{ width: 22, height: 22, borderRadius: 11, border: `1.5px solid ${P}`, background: WH, display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer" }}><Minus size={11} color={P} /></button>
                      <span style={{ fontSize: 13, fontWeight: 700, minWidth: 14, textAlign: "center" }}>{qty(p.id)}</span>
                      <button onClick={() => addToCart({ ...p, qty: 1 })} style={{ width: 22, height: 22, borderRadius: 11, border: "none", background: P, display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer" }}><Plus size={11} color={WH} /></button>
                    </div>
                  ) : (
                    <button onClick={e => { e.stopPropagation(); p.hasSpecs ? setDetailProd(p) : addToCart({ ...p, qty: 1 }); }} style={{ width: 26, height: 26, borderRadius: 13, border: "none", background: P, display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer" }}><Plus size={15} color={WH} /></button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
      {/* Cart bar */}
      <div style={{ padding: "8px 12px", background: WH, borderTop: `1px solid ${DV}` }}>
        <div style={{ background: "#2C2C2C", borderRadius: 32, padding: "7px 7px 7px 14px", display: "flex", alignItems: "center", gap: 10 }}>
          <div style={{ position: "relative", cursor: "pointer" }} onClick={() => nav("cart")}>
            <ShoppingCart size={24} color={WH} />
            {cartCount > 0 && <div style={{ position: "absolute", top: -6, right: -6, background: ER, color: WH, borderRadius: 9, minWidth: 16, height: 16, fontSize: 9, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center", padding: "0 3px" }}>{cartCount}</div>}
          </div>
          <div style={{ flex: 1 }}>
            {cartTotal > 0 ? <div style={{ color: WH, fontWeight: 700, fontSize: 15 }}>¥{cartTotal}</div> : <div style={{ color: "#888", fontSize: 12 }}>{mode === "delivery" ? "¥20起送" : "选好了就结算吧"}</div>}
            {mode === "delivery" && cartTotal > 0 && cartTotal < 20 && <div style={{ color: "#888", fontSize: 10 }}>还差¥{20 - cartTotal}起送</div>}
          </div>
          <button onClick={() => cartTotal > 0 && nav(mode === "dine-in" ? "checkout-dine-in" : "checkout-delivery")} style={{ background: cartTotal > 0 ? P : "#555", color: WH, borderRadius: 24, padding: "9px 18px", border: "none", fontWeight: 700, fontSize: 13, cursor: "pointer" }}>
            {cartTotal > 0 ? "去结算" : "去点餐"}
          </button>
        </div>
      </div>
      <UserTabs active="home" nav={(t) => { if (t === "orders") nav("order-list"); if (t === "my") nav("my"); }} />
      {/* Product detail overlay */}
      {detailProd && (
        <ProdDetailSheet prod={detailProd} onClose={() => setDetailProd(null)} onAdd={(item) => { addToCart(item); setDetailProd(null); }} />
      )}
    </div>
  );
}

/* ─── PRODUCT DETAIL BOTTOM SHEET ──────────────── */
function ProdDetailSheet({ prod, onClose, onAdd }: { prod: typeof PRODS[0]; onClose: () => void; onAdd: (i: CartItem) => void }) {
  const [qty, setQty] = useState(1);
  const [spec, setSpec] = useState<string | null>(prod.hasSpecs ? null : "标准");
  const [flavor, setFlavor] = useState<string | null>(null);
  const specs = ["小份", "中份", "大份"];
  const flavors = ["不辣", "微辣", "中辣", "特辣"];
  return (
    <div style={{ position: "absolute", inset: 0, background: "rgba(0,0,0,0.55)", zIndex: 200, display: "flex", flexDirection: "column", justifyContent: "flex-end" }} onClick={onClose}>
      <div style={{ background: WH, borderRadius: "16px 16px 0 0", maxHeight: "88%", overflowY: "auto" }} onClick={e => e.stopPropagation()}>
        <div style={{ height: 200, background: `linear-gradient(135deg, ${PL}, #FFE0C5)`, borderRadius: "16px 16px 0 0", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 80, position: "relative" }}>
          {prod.em}
          <button onClick={onClose} style={{ position: "absolute", top: 12, right: 12, width: 28, height: 28, borderRadius: 14, background: "rgba(0,0,0,0.3)", border: "none", display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer" }}><X size={15} color={WH} /></button>
        </div>
        <div style={{ padding: "16px 16px 24px" }}>
          <div style={{ fontWeight: 700, fontSize: 20, color: T1, marginBottom: 4 }}>{prod.name}</div>
          <div style={{ fontSize: 12, color: T3, marginBottom: 6 }}>{prod.desc}</div>
          <div style={{ fontSize: 11, color: T3, marginBottom: 12 }}>月售 {prod.sales}</div>
          <div style={{ display: "flex", alignItems: "baseline", gap: 6, marginBottom: 16 }}>
            <span style={{ color: P, fontWeight: 700, fontSize: 24 }}>¥{prod.price}</span>
            {prod.origPrice && <span style={{ color: T3, fontSize: 13, textDecoration: "line-through" }}>¥{prod.origPrice}</span>}
          </div>
          <Div /><div style={{ marginTop: 14 }} />
          {prod.hasSpecs && <>
            <div style={{ fontWeight: 600, fontSize: 14, color: T1, marginBottom: 10 }}>规格</div>
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 16 }}>
              {specs.map(s => <button key={s} onClick={() => setSpec(s)} style={{ padding: "6px 16px", borderRadius: 20, border: `1.5px solid ${spec === s ? P : DV}`, background: spec === s ? PL : WH, color: spec === s ? P : T2, fontSize: 13, cursor: "pointer", fontWeight: spec === s ? 700 : 400 }}>{s}</button>)}
            </div>
            <div style={{ fontWeight: 600, fontSize: 14, color: T1, marginBottom: 10 }}>口味</div>
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 16 }}>
              {flavors.map(f => <button key={f} onClick={() => setFlavor(f)} style={{ padding: "6px 16px", borderRadius: 20, border: `1.5px solid ${flavor === f ? P : DV}`, background: flavor === f ? PL : WH, color: flavor === f ? P : T2, fontSize: 13, cursor: "pointer", fontWeight: flavor === f ? 700 : 400 }}>{f}</button>)}
            </div>
          </>}
          <div style={{ display: "flex", alignItems: "center", gap: 14, marginTop: 8 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 12 }}>
              <button onClick={() => setQty(Math.max(1, qty - 1))} style={{ width: 32, height: 32, borderRadius: 16, border: `1.5px solid ${P}`, background: WH, display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer" }}><Minus size={15} color={P} /></button>
              <span style={{ fontWeight: 700, fontSize: 18, minWidth: 24, textAlign: "center" }}>{qty}</span>
              <button onClick={() => setQty(qty + 1)} style={{ width: 32, height: 32, borderRadius: 16, border: "none", background: P, display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer" }}><Plus size={15} color={WH} /></button>
            </div>
            <button onClick={() => onAdd({ id: prod.id, name: prod.name, em: prod.em, price: prod.price, qty, spec: spec ?? undefined })} style={{ flex: 1, height: 44, borderRadius: 22, border: "none", background: P, color: WH, fontWeight: 700, fontSize: 15, cursor: "pointer" }}>
              加入购物车 ¥{prod.price * qty}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

/* ─── SCREEN: CART ──────────────────────────────── */
function CartScreen({ cart, setCart, mode, nav }: { cart: CartItem[]; setCart: (c: CartItem[]) => void; mode: "dine-in" | "delivery"; nav: (s: string) => void }) {
  const total = cart.reduce((s, i) => s + i.price * i.qty, 0);
  const deliveryFee = mode === "delivery" ? 3 : 0;
  const packFee = 2;
  const canGo = mode === "dine-in" || total >= 20;
  const upd = (id: number, d: number) => setCart(cart.map(i => i.id === id ? { ...i, qty: i.qty + d } : i).filter(i => i.qty > 0));
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <NavBar title="购物车" onBack={() => nav("user-home")} right={cart.length > 0 ? <button onClick={() => setCart([])} style={{ border: "none", background: "none", color: ER, fontSize: 13, cursor: "pointer", fontWeight: 500 }}>清空</button> : undefined} />
      <div style={{ padding: "8px 16px 4px" }}>
        <span style={{ background: PL, color: P, fontSize: 11, padding: "3px 10px", borderRadius: 10, fontWeight: 700 }}>{mode === "dine-in" ? "🍽 堂食" : "🛵 配送"}</span>
      </div>
      {cart.length === 0 ? (
        <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: 14 }}>
          <div style={{ fontSize: 60 }}>🛒</div>
          <div style={{ fontSize: 15, color: T2, fontWeight: 500 }}>购物车还是空的</div>
          <OBtn label="去点餐" onClick={() => nav("user-home")} />
        </div>
      ) : <>
        <div style={{ flex: 1, overflowY: "auto", padding: "8px 16px" }}>
          {cart.map(item => (
            <div key={item.id} style={{ background: WH, borderRadius: 12, padding: 12, marginBottom: 8, display: "flex", gap: 10, alignItems: "center", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
              <div style={{ width: 52, height: 52, borderRadius: 8, background: BG, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 26 }}>{item.em}</div>
              <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 600, fontSize: 14, color: T1 }}>{item.name}</div>
                {item.spec && <div style={{ fontSize: 11, color: T3 }}>{item.spec}</div>}
                <div style={{ color: P, fontWeight: 700, fontSize: 15 }}>¥{item.price}</div>
              </div>
              <div style={{ display: "flex", alignItems: "center", gap: 7 }}>
                <button onClick={() => upd(item.id, -1)} style={{ width: 24, height: 24, borderRadius: 12, border: `1.5px solid ${P}`, background: WH, display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer" }}><Minus size={12} color={P} /></button>
                <span style={{ fontWeight: 700, fontSize: 14, minWidth: 16, textAlign: "center" }}>{item.qty}</span>
                <button onClick={() => upd(item.id, 1)} style={{ width: 24, height: 24, borderRadius: 12, border: "none", background: P, display: "flex", alignItems: "center", justifyContent: "center", cursor: "pointer" }}><Plus size={12} color={WH} /></button>
              </div>
            </div>
          ))}
        </div>
        <div style={{ background: WH, padding: "12px 16px 20px", borderTop: `1px solid ${DV}` }}>
          {[["商品合计", `¥${total}`], ...(mode === "delivery" ? [["配送费", `¥${deliveryFee}`]] : []), ["包装费", `¥${packFee}`]].map(([l, v]) => (
            <div key={l} style={{ display: "flex", justifyContent: "space-between", marginBottom: 6, fontSize: 13, color: T2 }}><span>{l}</span><span>{v}</span></div>
          ))}
          <Div /><div style={{ marginTop: 10, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
            <div><span style={{ fontSize: 13, color: T2 }}>实付 </span><span style={{ color: P, fontWeight: 700, fontSize: 20 }}>¥{total + deliveryFee + packFee}</span></div>
            <button onClick={() => canGo && nav(mode === "dine-in" ? "checkout-dine-in" : "checkout-delivery")} style={{ background: canGo ? P : "#CCC", color: WH, borderRadius: 22, padding: "10px 22px", border: "none", fontWeight: 700, fontSize: 14, cursor: canGo ? "pointer" : "not-allowed" }}>
              {canGo ? "去结算" : `还差¥${20 - total}起送`}
            </button>
          </div>
        </div>
      </>}
    </div>
  );
}

/* ─── SCREEN: CHECKOUT DINE-IN ──────────────────── */
function CheckoutDineInScreen({ cart, nav }: { cart: CartItem[]; nav: (s: string) => void }) {
  const [table, setTable] = useState("");
  const [noSeat, setNoSeat] = useState(false);
  const [phone, setPhone] = useState("138 0000 0000");
  const [note, setNote] = useState("");
  const total = cart.reduce((s, i) => s + i.price * i.qty, 0);
  const tags = ["少辣", "不要香菜", "加餐具", "到达后电话联系"];
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <NavBar title="确认订单" onBack={() => nav("cart")} />
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        <Card mb={12}>
          <Row icon="🍽️" label="就餐方式：堂食" />
          <div style={{ fontSize: 11, color: T3, marginTop: 4, marginLeft: 28 }}>到店后请向商家出示订单编号</div>
        </Card>
        <Card mb={12}>
          <FieldLabel label="桌号" required />
          <input value={noSeat ? "" : table} onChange={e => { setTable(e.target.value); setNoSeat(false); }} placeholder="请输入桌号，例如A06" disabled={noSeat} style={inputStyle(noSeat)} />
          <div onClick={() => setNoSeat(!noSeat)} style={{ display: "flex", alignItems: "center", gap: 8, marginTop: 10, cursor: "pointer" }}>
            <div style={{ width: 18, height: 18, borderRadius: 9, border: `1.5px solid ${noSeat ? P : BD}`, background: noSeat ? P : WH, display: "flex", alignItems: "center", justifyContent: "center" }}>{noSeat && <Check size={11} color={WH} strokeWidth={3} />}</div>
            <span style={{ fontSize: 12, color: T2 }}>暂未入座，到店后告知商家</span>
          </div>
        </Card>
        <Card mb={12}>
          <FieldLabel label="联系电话" required />
          <input value={phone} onChange={e => setPhone(e.target.value)} style={inputStyle()} />
        </Card>
        <Card mb={12}>
          <FieldLabel label="商品清单" />
          {cart.map(i => <div key={i.id} style={{ display: "flex", justifyContent: "space-between", fontSize: 13, color: T1, marginTop: 8 }}><span>{i.name} ×{i.qty}</span><span>¥{i.price * i.qty}</span></div>)}
        </Card>
        <Card mb={12}>
          <FieldLabel label="订单备注" />
          <div style={{ display: "flex", flexWrap: "wrap", gap: 7, marginTop: 4, marginBottom: 10 }}>
            {tags.map(t => <button key={t} onClick={() => setNote(n => n ? `${n}，${t}` : t)} style={{ padding: "4px 12px", borderRadius: 14, border: `1px solid ${DV}`, background: BG, color: T2, fontSize: 11, cursor: "pointer" }}>{t}</button>)}
          </div>
          <textarea value={note} onChange={e => setNote(e.target.value)} rows={2} placeholder="特殊要求请在此备注" style={{ width: "100%", borderRadius: 8, border: `1px solid ${DV}`, padding: "8px 10px", fontSize: 13, resize: "none", boxSizing: "border-box" }} />
        </Card>
        <Card mb={12}>
          <FeeRow label="商品金额" val={`¥${total}`} />
          <FeeRow label="包装费" val="¥2" />
          <Div /><div style={{ marginTop: 8, display: "flex", justifyContent: "space-between", fontWeight: 700, fontSize: 15 }}>
            <span style={{ color: T1 }}>实付金额</span><span style={{ color: P }}>¥{total + 2}</span>
          </div>
        </Card>
      </div>
      <div style={{ padding: "12px 16px 24px", background: WH, borderTop: `1px solid ${DV}`, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div><span style={{ fontSize: 13, color: T2 }}>实付 </span><span style={{ color: P, fontWeight: 700, fontSize: 20 }}>¥{total + 2}</span></div>
        <button onClick={() => nav("order-success")} style={{ background: P, color: WH, borderRadius: 22, padding: "12px 26px", border: "none", fontWeight: 700, fontSize: 15, cursor: "pointer" }}>提交订单</button>
      </div>
    </div>
  );
}

/* ─── SCREEN: CHECKOUT DELIVERY ─────────────────── */
function CheckoutDeliveryScreen({ cart, nav }: { cart: CartItem[]; nav: (s: string) => void }) {
  const [phone, setPhone] = useState("138 0000 0000");
  const [note, setNote] = useState("");
  const total = cart.reduce((s, i) => s + i.price * i.qty, 0);
  const tags = ["少辣", "不要香菜", "加餐具", "到达后电话联系"];
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <NavBar title="确认订单" onBack={() => nav("cart")} />
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        <div onClick={() => nav("address-list")} style={{ background: WH, borderRadius: 12, padding: 14, marginBottom: 12, display: "flex", alignItems: "center", gap: 10, cursor: "pointer", boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
          <MapPin size={18} color={P} style={{ flexShrink: 0 }} />
          <div style={{ flex: 1 }}>
            <div style={{ fontWeight: 600, fontSize: 14, color: T1, marginBottom: 2 }}>小李 138 0000 0000</div>
            <div style={{ fontSize: 12, color: T2 }}>阳光花园2栋1203室</div>
          </div>
          <ChevronRight size={16} color={T3} />
        </div>
        <Card mb={12}>
          <Row icon="🛵" label="配送方式：商家配送" />
          <div style={{ display: "flex", justifyContent: "space-between", fontSize: 12, color: T3, marginTop: 6, marginLeft: 28 }}>
            <span>预计送达：30-45分钟</span><span>配送费 ¥3</span>
          </div>
          <div style={{ fontSize: 11, color: T3, marginTop: 4, marginLeft: 28 }}>订单由商家自行配送，请保持电话畅通</div>
        </Card>
        <Card mb={12}>
          <FieldLabel label="联系电话" required />
          <input value={phone} onChange={e => setPhone(e.target.value)} style={inputStyle()} />
        </Card>
        <Card mb={12}>
          <FieldLabel label="商品清单" />
          {cart.map(i => <div key={i.id} style={{ display: "flex", justifyContent: "space-between", fontSize: 13, color: T1, marginTop: 8 }}><span>{i.name} ×{i.qty}</span><span>¥{i.price * i.qty}</span></div>)}
        </Card>
        <Card mb={12}>
          <FieldLabel label="订单备注" />
          <div style={{ display: "flex", flexWrap: "wrap", gap: 7, marginTop: 4, marginBottom: 10 }}>
            {tags.map(t => <button key={t} onClick={() => setNote(n => n ? `${n}，${t}` : t)} style={{ padding: "4px 12px", borderRadius: 14, border: `1px solid ${DV}`, background: BG, color: T2, fontSize: 11, cursor: "pointer" }}>{t}</button>)}
          </div>
          <textarea value={note} onChange={e => setNote(e.target.value)} rows={2} placeholder="特殊要求请在此备注" style={{ width: "100%", borderRadius: 8, border: `1px solid ${DV}`, padding: "8px 10px", fontSize: 13, resize: "none", boxSizing: "border-box" }} />
        </Card>
        <Card mb={12}>
          <FeeRow label="商品金额" val={`¥${total}`} />
          <FeeRow label="配送费" val="¥3" />
          <FeeRow label="包装费" val="¥2" />
          <Div /><div style={{ marginTop: 8, display: "flex", justifyContent: "space-between", fontWeight: 700, fontSize: 15 }}>
            <span style={{ color: T1 }}>实付金额</span><span style={{ color: P }}>¥{total + 5}</span>
          </div>
        </Card>
      </div>
      <div style={{ padding: "12px 16px 24px", background: WH, borderTop: `1px solid ${DV}`, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <div><span style={{ fontSize: 13, color: T2 }}>实付 </span><span style={{ color: P, fontWeight: 700, fontSize: 20 }}>¥{total + 5}</span></div>
        <button onClick={() => nav("order-success")} style={{ background: P, color: WH, borderRadius: 22, padding: "12px 26px", border: "none", fontWeight: 700, fontSize: 15, cursor: "pointer" }}>提交订单</button>
      </div>
    </div>
  );
}

/* ─── SCREEN: ORDER SUCCESS ─────────────────────── */
function OrderSuccessScreen({ nav }: { nav: (s: string) => void }) {
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <NavBar title="订单提交成功" onBack={() => nav("user-home")} />
      <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "0 24px" }}>
        <div style={{ width: 72, height: 72, borderRadius: 36, background: SG, display: "flex", alignItems: "center", justifyContent: "center", marginBottom: 16 }}><Check size={36} color={WH} strokeWidth={3} /></div>
        <div style={{ fontWeight: 700, fontSize: 22, color: T1, marginBottom: 6 }}>订单提交成功！</div>
        <div style={{ fontSize: 13, color: T3, marginBottom: 28, textAlign: "center" }}>商家接单后将开始制作，请留意订单状态</div>
        <div style={{ background: WH, borderRadius: 12, padding: 16, width: "100%", marginBottom: 24, boxShadow: "0 2px 8px rgba(0,0,0,0.06)" }}>
          {[["订单编号", "202607090001"], ["订单类型", null], ["实付金额", null], ["当前状态", null]].map(([l]) => (
            <div key={l as string} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 10, fontSize: 13 }}>
              <span style={{ color: T2 }}>{l}</span>
              {l === "订单编号" && <span style={{ color: T1, fontWeight: 500 }}>202607090001</span>}
              {l === "订单类型" && <span style={{ background: PL, color: P, padding: "2px 10px", borderRadius: 8, fontSize: 11, fontWeight: 700 }}>堂食</span>}
              {l === "实付金额" && <span style={{ color: P, fontWeight: 700, fontSize: 16 }}>¥28</span>}
              {l === "当前状态" && <SBadge s="待接单" />}
            </div>
          ))}
        </div>
        <div style={{ width: "100%", display: "flex", flexDirection: "column", gap: 10 }}>
          <PBtn label="查看订单详情" onClick={() => nav("order-detail-dine-in")} />
          <OBtn label="返回首页" onClick={() => nav("user-home")} />
        </div>
      </div>
    </div>
  );
}

/* ─── SCREEN: ORDER LIST ────────────────────────── */
function OrderListScreen({ nav }: { nav: (s: string) => void }) {
  const [tab, setTab] = useState("全部");
  const tabs = ["全部", "待接单", "制作中", "配送中", "已完成"];
  const filtered = tab === "全部" ? ORDERS : ORDERS.filter(o => o.status === tab);
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <NavBar title="我的订单" />
      <div style={{ background: WH, display: "flex", borderBottom: `1px solid ${DV}`, overflowX: "auto" }}>
        {tabs.map(t => (
          <button key={t} onClick={() => setTab(t)} style={{ flexShrink: 0, padding: "11px 14px", border: "none", background: "none", cursor: "pointer", fontSize: 13, fontWeight: tab === t ? 700 : 400, color: tab === t ? P : T2, borderBottom: `2px solid ${tab === t ? P : "transparent"}` }}>{t}</button>
        ))}
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        {filtered.length === 0 ? (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", paddingTop: 80, gap: 12 }}>
            <div style={{ fontSize: 50 }}>📋</div>
            <div style={{ fontSize: 14, color: T2 }}>暂无订单</div>
            <OBtn label="去点餐" onClick={() => nav("user-home")} />
          </div>
        ) : filtered.map(o => (
          <div key={o.id} onClick={() => nav(o.type === "dine-in" ? "order-detail-dine-in" : "order-detail-delivery")} style={{ background: WH, borderRadius: 12, padding: 14, marginBottom: 10, cursor: "pointer", boxShadow: "0 1px 4px rgba(0,0,0,0.06)" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
              <div style={{ display: "flex", gap: 6, alignItems: "center" }}>
                <span style={{ background: o.type === "dine-in" ? "#E8F4FF" : PL, color: o.type === "dine-in" ? BL : P, fontSize: 10, padding: "2px 8px", borderRadius: 8, fontWeight: 700 }}>{o.type === "dine-in" ? "堂食" : "配送"}</span>
                <span style={{ fontSize: 11, color: T3 }}>#{o.id.slice(-4)}</span>
              </div>
              <SBadge s={o.status} />
            </div>
            <div style={{ fontSize: 13, color: T1, marginBottom: 5, fontWeight: 500 }}>{o.items.join("、")}</div>
            <div style={{ fontSize: 11, color: T3, marginBottom: 8 }}>{o.type === "dine-in" ? `桌号：${o.table}` : `地址：${o.address}`}</div>
            <Div /><div style={{ marginTop: 8, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
              <div><span style={{ color: P, fontWeight: 700, fontSize: 15 }}>¥{o.amount}</span><span style={{ fontSize: 11, color: T3, marginLeft: 8 }}>{o.time}</span></div>
              <div style={{ display: "flex", gap: 7 }}>
                {o.status === "待接单" && <OBtn label="取消" danger onClick={() => {}} />}
                {(o.status === "已完成" || o.status === "已取消") && <OBtn label="再来一单" onClick={() => nav("user-home")} />}
                <button onClick={() => nav(o.type === "dine-in" ? "order-detail-dine-in" : "order-detail-delivery")} style={{ padding: "6px 13px", borderRadius: 14, border: `1px solid ${DV}`, background: WH, color: T2, fontSize: 12, cursor: "pointer" }}>查看详情</button>
              </div>
            </div>
          </div>
        ))}
      </div>
      <UserTabs active="orders" nav={(t) => { if (t === "home") nav("user-home"); if (t === "my") nav("my"); }} />
    </div>
  );
}

/* ─── SCREEN: ORDER DETAIL DINE-IN ─────────────── */
function OrderDetailDineInScreen({ nav }: { nav: (s: string) => void }) {
  const o = ORDERS[0];
  const steps = ["待接单", "已接单", "制作中", "待取餐", "已完成"];
  const cur = 0;
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar dark />
      <div style={{ background: P, padding: "0 16px 20px", flexShrink: 0 }}>
        <div style={{ height: 52, display: "flex", alignItems: "center", gap: 4 }}>
          <button onClick={() => nav("order-list")} style={{ background: "none", border: "none", cursor: "pointer", color: WH, padding: 6 }}><ChevronLeft size={22} /></button>
          <span style={{ flex: 1, fontWeight: 700, fontSize: 17, color: WH, textAlign: "center" }}>订单详情</span>
          <div style={{ width: 34 }} />
        </div>
        <div style={{ fontWeight: 700, fontSize: 22, color: WH, marginBottom: 4 }}>待商家接单</div>
        <div style={{ fontSize: 12, color: "rgba(255,255,255,0.8)", marginBottom: 18 }}>等待商家接单，接单后开始制作</div>
        <div style={{ display: "flex", alignItems: "center" }}>
          {steps.map((step, i) => (
            <div key={step} style={{ display: "flex", alignItems: "center", flex: i < steps.length - 1 ? 1 : 0 }}>
              <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                <div style={{ width: 20, height: 20, borderRadius: 10, background: i <= cur ? "rgba(255,255,255,0.95)" : "rgba(255,255,255,0.35)", display: "flex", alignItems: "center", justifyContent: "center" }}>
                  {i < cur && <Check size={12} color={P} strokeWidth={3} />}
                  {i === cur && <div style={{ width: 6, height: 6, borderRadius: 3, background: P }} />}
                </div>
                <div style={{ fontSize: 9, color: "rgba(255,255,255,0.85)", marginTop: 4, whiteSpace: "nowrap" }}>{step}</div>
              </div>
              {i < steps.length - 1 && <div style={{ flex: 1, height: 1.5, background: i < cur ? "rgba(255,255,255,0.8)" : "rgba(255,255,255,0.3)", marginBottom: 14 }} />}
            </div>
          ))}
        </div>
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        <Card mb={12}>
          <SectionLabel label="就餐信息" />
          <InfoRow label="就餐方式" val="堂食" />
          <InfoRow label="桌号" val={o.table!} bold />
          <InfoRow label="联系人" val={o.contact} />
          <InfoRow label="联系电话" val={o.phone} />
        </Card>
        <Card mb={12}>
          <SectionLabel label="商品明细" />
          {o.items.map((item, i) => <div key={i} style={{ display: "flex", justifyContent: "space-between", marginTop: 8, fontSize: 13 }}><span style={{ color: T1 }}>{item}</span><span style={{ color: T1, fontWeight: 500 }}>¥26</span></div>)}
        </Card>
        <Card mb={12}>
          <FeeRow label="商品金额" val="¥26" /><FeeRow label="包装费" val="¥2" />
          <Div /><div style={{ marginTop: 8, display: "flex", justifyContent: "space-between", fontWeight: 700, fontSize: 15 }}><span style={{ color: T1 }}>实付金额</span><span style={{ color: P }}>¥28</span></div>
        </Card>
        {o.note && <Card mb={12}><SectionLabel label="订单备注" /><div style={{ fontSize: 13, color: T2, marginTop: 6, background: BG, borderRadius: 8, padding: 10 }}>{o.note}</div></Card>}
        <Card mb={12}>
          <InfoRow label="订单编号" val={o.id} small />
          <InfoRow label="下单时间" val="2026-07-09 10:00:23" small />
        </Card>
      </div>
      <div style={{ padding: "12px 16px 24px", background: WH, borderTop: `1px solid ${DV}` }}>
        <OBtn label="联系商家" onClick={() => {}} />
      </div>
    </div>
  );
}

/* ─── SCREEN: ORDER DETAIL DELIVERY ────────────── */
function OrderDetailDeliveryScreen({ nav }: { nav: (s: string) => void }) {
  const o = ORDERS[1];
  const steps = ["待接单", "已接单", "制作中", "配送中", "已送达", "已完成"];
  const cur = 3;
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar dark />
      <div style={{ background: TE, padding: "0 16px 20px", flexShrink: 0 }}>
        <div style={{ height: 52, display: "flex", alignItems: "center", gap: 4 }}>
          <button onClick={() => nav("order-list")} style={{ background: "none", border: "none", cursor: "pointer", color: WH, padding: 6 }}><ChevronLeft size={22} /></button>
          <span style={{ flex: 1, fontWeight: 700, fontSize: 17, color: WH, textAlign: "center" }}>订单详情</span>
          <div style={{ width: 34 }} />
        </div>
        <div style={{ fontWeight: 700, fontSize: 22, color: WH, marginBottom: 4 }}>配送中</div>
        <div style={{ fontSize: 12, color: "rgba(255,255,255,0.85)", marginBottom: 18 }}>商家正在配送，请保持电话畅通</div>
        <div style={{ display: "flex", alignItems: "center" }}>
          {steps.map((step, i) => (
            <div key={step} style={{ display: "flex", alignItems: "center", flex: i < steps.length - 1 ? 1 : 0 }}>
              <div style={{ display: "flex", flexDirection: "column", alignItems: "center" }}>
                <div style={{ width: 18, height: 18, borderRadius: 9, background: i <= cur ? "rgba(255,255,255,0.95)" : "rgba(255,255,255,0.35)", display: "flex", alignItems: "center", justifyContent: "center" }}>
                  {i < cur && <Check size={10} color={TE} strokeWidth={3} />}
                  {i === cur && <div style={{ width: 6, height: 6, borderRadius: 3, background: TE }} />}
                </div>
                <div style={{ fontSize: 8, color: "rgba(255,255,255,0.85)", marginTop: 3, whiteSpace: "nowrap" }}>{step}</div>
              </div>
              {i < steps.length - 1 && <div style={{ flex: 1, height: 1.5, background: i < cur ? "rgba(255,255,255,0.75)" : "rgba(255,255,255,0.3)", marginBottom: 14 }} />}
            </div>
          ))}
        </div>
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        <Card mb={12}>
          <SectionLabel label="收货信息" />
          <div style={{ display: "flex", gap: 10, marginTop: 10 }}>
            <MapPin size={16} color={P} style={{ flexShrink: 0, marginTop: 1 }} />
            <div>
              <div style={{ fontSize: 14, fontWeight: 600, color: T1, marginBottom: 2 }}>{o.contact} {o.phone}</div>
              <div style={{ fontSize: 13, color: T2 }}>{o.address}</div>
            </div>
          </div>
        </Card>
        <Card mb={12}>
          <InfoRow label="配送方式" val="商家配送" />
          <InfoRow label="预计送达" val="约15分钟" />
        </Card>
        <Card mb={12}>
          <SectionLabel label="商品明细" />
          {o.items.map((item, i) => <div key={i} style={{ display: "flex", justifyContent: "space-between", marginTop: 8, fontSize: 13 }}><span style={{ color: T1 }}>{item}</span><span style={{ color: T1, fontWeight: 500 }}>¥{i === 0 ? 22 : 16}</span></div>)}
        </Card>
        <Card mb={12}>
          <FeeRow label="商品金额" val="¥38" /><FeeRow label="配送费" val="¥3" /><FeeRow label="包装费" val="¥2" />
          <Div /><div style={{ marginTop: 8, display: "flex", justifyContent: "space-between", fontWeight: 700, fontSize: 15 }}><span style={{ color: T1 }}>实付金额</span><span style={{ color: P }}>¥{o.amount}</span></div>
        </Card>
        {o.note && <Card mb={12}><SectionLabel label="订单备注" /><div style={{ fontSize: 13, color: T2, marginTop: 6, background: BG, borderRadius: 8, padding: 10 }}>{o.note}</div></Card>}
      </div>
      <div style={{ padding: "12px 16px 24px", background: WH, borderTop: `1px solid ${DV}`, display: "flex", gap: 10 }}>
        <OBtn label="联系商家" onClick={() => {}} />
        <button style={{ flex: 1, height: 40, borderRadius: 20, border: "none", background: SG, color: WH, fontWeight: 700, fontSize: 14, cursor: "pointer" }}>确认收货</button>
      </div>
    </div>
  );
}

/* ─── SCREEN: MY ────────────────────────────────── */
function MyScreen({ nav }: { nav: (s: string) => void }) {
  const menus = [
    { icon: ClipboardList, label: "我的订单", badge: "2" }, { icon: MapPin, label: "收货地址", badge: "" },
    { icon: Phone, label: "联系商家", badge: "" }, { icon: Store, label: "店铺信息", badge: "" },
    { icon: FileText, label: "用户协议", badge: "" }, { icon: Settings, label: "隐私政策", badge: "" },
    { icon: Info, label: "关于我们", badge: "" },
  ];
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar dark />
      <div style={{ background: `linear-gradient(135deg, ${P}, #FF6B00)`, padding: "0 16px 28px" }}>
        <div style={{ height: 44, display: "flex", alignItems: "center", justifyContent: "flex-end" }}>
          <Edit size={18} color="rgba(255,255,255,0.8)" style={{ cursor: "pointer" }} />
        </div>
        <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <div style={{ width: 60, height: 60, borderRadius: 30, background: "rgba(255,255,255,0.3)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 28 }}>👤</div>
          <div>
            <div style={{ fontWeight: 700, fontSize: 18, color: WH }}>小李</div>
            <div style={{ fontSize: 12, color: "rgba(255,255,255,0.8)", marginTop: 2 }}>138 0000 0000</div>
          </div>
        </div>
      </div>
      <div style={{ display: "flex", background: WH, marginTop: -16, borderRadius: "12px 12px 0 0", padding: "16px 0" }}>
        {["待接单", "制作中", "配送中", "已完成"].map((s, i) => (
          <div key={s} onClick={() => nav("order-list")} style={{ flex: 1, textAlign: "center", cursor: "pointer" }}>
            <div style={{ fontWeight: 700, fontSize: 20, color: T1 }}>{[1, 0, 1, 3][i]}</div>
            <div style={{ fontSize: 11, color: T3, marginTop: 2 }}>{s}</div>
          </div>
        ))}
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "10px 16px" }}>
        <div style={{ background: WH, borderRadius: 12, overflow: "hidden", marginBottom: 10 }}>
          {menus.map(({ icon: Icon, label, badge }, i) => (
            <div key={label}>
              <div style={{ display: "flex", alignItems: "center", padding: "13px 14px", cursor: "pointer" }}>
                <Icon size={17} color={T2} style={{ marginRight: 12 }} />
                <span style={{ flex: 1, fontSize: 14, color: T1 }}>{label}</span>
                {badge && <span style={{ background: ER, color: WH, fontSize: 10, padding: "1px 6px", borderRadius: 8, marginRight: 8 }}>{badge}</span>}
                <ChevronRight size={14} color={T3} />
              </div>
              {i < menus.length - 1 && <div style={{ height: 1, background: DV, marginLeft: 43 }} />}
            </div>
          ))}
        </div>
        <button style={{ width: "100%", padding: 13, borderRadius: 12, border: "none", background: WH, color: ER, fontSize: 14, fontWeight: 500, cursor: "pointer" }}>退出登录</button>
        <div onClick={() => nav("merchant-login")} style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 6, padding: "12px 0 4px", cursor: "pointer" }}>
          <Store size={13} color={T3} />
          <span style={{ fontSize: 12, color: T3 }}>商家人员登录</span>
          <ChevronRight size={12} color={T3} />
        </div>
      </div>
      <UserTabs active="my" nav={(t) => { if (t === "home") nav("user-home"); if (t === "orders") nav("order-list"); }} />
    </div>
  );
}

/* ─── SCREEN: ADDRESS LIST ──────────────────────── */
function AddressListScreen({ nav }: { nav: (s: string) => void }) {
  const addrs = [
    { id: 1, name: "小李", phone: "138 0000 0000", addr: "阳光花园2栋1203室", isDefault: true },
    { id: 2, name: "小李（公司）", phone: "138 0000 0000", addr: "城市广场3号楼501室", isDefault: false },
  ];
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <NavBar title="选择收货地址" onBack={() => nav("checkout-delivery")} />
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        {addrs.map(a => (
          <div key={a.id} onClick={() => nav("checkout-delivery")} style={{ background: WH, borderRadius: 12, padding: 14, marginBottom: 10, cursor: "pointer", boxShadow: "0 1px 3px rgba(0,0,0,0.05)", border: a.isDefault ? `2px solid ${P}` : "none" }}>
            <div style={{ display: "flex", alignItems: "flex-start", justifyContent: "space-between" }}>
              <div style={{ flex: 1 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 6, marginBottom: 4 }}>
                  <span style={{ fontWeight: 600, fontSize: 14, color: T1 }}>{a.name}</span>
                  <span style={{ fontSize: 13, color: T2 }}>{a.phone}</span>
                  {a.isDefault && <span style={{ background: PL, color: P, fontSize: 10, padding: "1px 6px", borderRadius: 6, fontWeight: 700 }}>默认</span>}
                </div>
                <div style={{ fontSize: 13, color: T2 }}>{a.addr}</div>
              </div>
              <Edit size={15} color={T3} style={{ marginLeft: 10, cursor: "pointer", marginTop: 2 }} />
            </div>
          </div>
        ))}
      </div>
      <div style={{ padding: "12px 16px 24px", background: WH, borderTop: `1px solid ${DV}` }}>
        <button style={{ width: "100%", height: 44, borderRadius: 22, border: `1.5px dashed ${P}`, background: PL, color: P, fontWeight: 600, fontSize: 15, cursor: "pointer", display: "flex", alignItems: "center", justifyContent: "center", gap: 8 }}>
          <Plus size={16} /> 新增收货地址
        </button>
      </div>
    </div>
  );
}

/* ─── SCREEN: MERCHANT DASHBOARD ───────────────── */
function MerchantDashboardScreen({ nav }: { nav: (s: string) => void }) {
  const [isOpen, setIsOpen] = useState(true);
  const [dineIn, setDineIn] = useState(true);
  const [delivery, setDelivery] = useState(true);
  const stats = [
    { label: "今日订单", val: "12", em: "📋", color: BL },
    { label: "今日营业额", val: "¥568", em: "💰", color: P },
    { label: "待接单", val: "1", em: "⏰", color: ER },
    { label: "制作中", val: "2", em: "👨‍🍳", color: WY },
    { label: "堂食待取", val: "1", em: "🍽️", color: PU },
    { label: "配送中", val: "1", em: "🛵", color: TE },
  ];
  const quick = [
    { label: "待接单", em: "⏰", badge: 1, to: "merchant-orders" },
    { label: "堂食订单", em: "🍽️", badge: 0, to: "merchant-orders" },
    { label: "配送订单", em: "🛵", badge: 0, to: "merchant-orders" },
    { label: "商品管理", em: "📦", badge: 0, to: "merchant-products" },
    { label: "店铺设置", em: "⚙️", badge: 0, to: "merchant-settings" },
  ];
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <div style={{ background: WH, padding: "10px 16px 14px", borderBottom: `1px solid ${DV}` }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
          <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
            <div style={{ width: 44, height: 44, borderRadius: 22, background: PL, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 22 }}>👨‍🍳</div>
            <div>
              <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
                <div style={{ fontWeight: 700, fontSize: 16, color: T1 }}>上午好，店主小王</div>
                <span style={{ background: PL, color: P, fontSize: 9, padding: "1px 6px", borderRadius: 6, fontWeight: 700 }}>商家端</span>
              </div>
              <div style={{ fontSize: 11, color: T3, marginTop: 1 }}>食刻小馆 · 商家端</div>
            </div>
          </div>
          <div style={{ position: "relative" }}>
            <Bell size={22} color={T2} style={{ cursor: "pointer" }} />
            <div style={{ position: "absolute", top: -3, right: -3, width: 14, height: 14, borderRadius: 7, background: ER, display: "flex", alignItems: "center", justifyContent: "center", color: WH, fontSize: 8, fontWeight: 700 }}>1</div>
          </div>
        </div>
        <div style={{ background: isOpen ? "#F6FFED" : "#F5F5F5", borderRadius: 10, padding: "10px 14px", display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div>
            <div style={{ fontWeight: 700, fontSize: 14, color: isOpen ? "#52C41A" : T3 }}>{isOpen ? "● 营业中" : "● 休息中"}</div>
            {isOpen && <div style={{ fontSize: 11, color: T3, marginTop: 2 }}>堂食 {dineIn ? "✓" : "✗"} · 配送 {delivery ? "✓" : "✗"}</div>}
          </div>
          <button onClick={() => setIsOpen(!isOpen)} style={{ padding: "6px 14px", borderRadius: 16, border: "none", background: isOpen ? ER : SG, color: WH, fontSize: 12, fontWeight: 700, cursor: "pointer" }}>
            {isOpen ? "暂停营业" : "开始营业"}
          </button>
        </div>
        {isOpen && (
          <div style={{ display: "flex", gap: 16, marginTop: 10 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <span style={{ fontSize: 12, color: T2 }}>堂食</span><Toggle on={dineIn} onChange={() => setDineIn(!dineIn)} />
            </div>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <span style={{ fontSize: 12, color: T2 }}>配送</span><Toggle on={delivery} onChange={() => setDelivery(!delivery)} />
            </div>
          </div>
        )}
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        {/* New order alert */}
        <div style={{ background: WH, borderRadius: 12, padding: 14, marginBottom: 12, border: `2px solid ${P}`, boxShadow: `0 2px 12px ${PL}` }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
            <div style={{ display: "flex", alignItems: "center", gap: 6 }}>
              <div style={{ width: 8, height: 8, borderRadius: 4, background: ER }} />
              <span style={{ fontWeight: 700, fontSize: 14, color: ER }}>新订单提醒</span>
            </div>
            <span style={{ fontSize: 11, color: T3 }}>刚刚</span>
          </div>
          <div style={{ fontSize: 13, color: T2, marginBottom: 12 }}>堂食 · A06桌 · 招牌牛肉饭×1 · <span style={{ color: P, fontWeight: 700 }}>¥28</span></div>
          <PBtn small label="立即处理" onClick={() => nav("merchant-order-detail")} />
        </div>
        {/* Stats grid */}
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 10, marginBottom: 12 }}>
          {stats.map(s => (
            <div key={s.label} style={{ background: WH, borderRadius: 12, padding: 14, display: "flex", alignItems: "center", gap: 10, boxShadow: "0 1px 4px rgba(0,0,0,0.05)" }}>
              <div style={{ fontSize: 28 }}>{s.em}</div>
              <div><div style={{ fontWeight: 700, fontSize: 20, color: s.color }}>{s.val}</div><div style={{ fontSize: 11, color: T3, marginTop: 1 }}>{s.label}</div></div>
            </div>
          ))}
        </div>
        {/* Quick links */}
        <div style={{ background: WH, borderRadius: 12, padding: 14, boxShadow: "0 1px 4px rgba(0,0,0,0.05)" }}>
          <div style={{ fontWeight: 700, fontSize: 15, color: T1, marginBottom: 12 }}>快捷入口</div>
          <div style={{ display: "flex", flexWrap: "wrap", gap: 10 }}>
            {quick.map(q => (
              <button key={q.label} onClick={() => nav(q.to)} style={{ background: BG, borderRadius: 10, padding: "12px 14px", border: "none", cursor: "pointer", display: "flex", flexDirection: "column", alignItems: "center", gap: 6, position: "relative" }}>
                <span style={{ fontSize: 26 }}>{q.em}</span>
                <span style={{ fontSize: 11, color: T1, fontWeight: 500 }}>{q.label}</span>
                {q.badge > 0 && <div style={{ position: "absolute", top: 6, right: 6, width: 16, height: 16, borderRadius: 8, background: ER, display: "flex", alignItems: "center", justifyContent: "center", color: WH, fontSize: 9, fontWeight: 700 }}>{q.badge}</div>}
              </button>
            ))}
          </div>
        </div>
      </div>
      <MerchantTabs active="dashboard" nav={(t) => { if (t === "orders") nav("merchant-orders"); if (t === "products") nav("merchant-products"); if (t === "profile") nav("merchant-profile"); }} />
    </div>
  );
}

/* ─── SCREEN: MERCHANT ORDERS ───────────────────── */
function MerchantOrdersScreen({ nav }: { nav: (s: string) => void }) {
  const [typeTab, setTypeTab] = useState("全部");
  const [statusTab, setStatusTab] = useState("待接单");
  const filtered = ORDERS.filter(o => {
    const tm = typeTab === "全部" || (typeTab === "堂食" && o.type === "dine-in") || (typeTab === "配送" && o.type === "delivery");
    const sm = o.status === statusTab || statusTab === "全部";
    return tm && sm;
  });
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <div style={{ background: WH, padding: "0 16px", borderBottom: `1px solid ${DV}` }}>
        <div style={{ height: 52, display: "flex", alignItems: "center" }}>
          <span style={{ fontWeight: 700, fontSize: 17, color: T1 }}>订单管理</span>
          <div style={{ flex: 1 }} />
          <Search size={20} color={T2} style={{ cursor: "pointer" }} />
        </div>
        <div style={{ display: "flex", marginBottom: -1 }}>
          {["全部", "堂食", "配送"].map(t => (
            <button key={t} onClick={() => setTypeTab(t)} style={{ padding: "8px 14px", border: "none", background: "none", cursor: "pointer", fontSize: 13, fontWeight: typeTab === t ? 700 : 400, color: typeTab === t ? P : T2, borderBottom: `2px solid ${typeTab === t ? P : "transparent"}` }}>{t}</button>
          ))}
        </div>
      </div>
      <div style={{ background: WH, overflowX: "auto", display: "flex", borderBottom: `1px solid ${DV}` }}>
        {["待接单", "制作中", "待取餐", "配送中", "已完成", "已取消"].map(s => (
          <button key={s} onClick={() => setStatusTab(s)} style={{ flexShrink: 0, padding: "8px 12px", border: "none", background: "none", cursor: "pointer", fontSize: 11, fontWeight: statusTab === s ? 700 : 400, color: statusTab === s ? P : T3, borderBottom: `2px solid ${statusTab === s ? P : "transparent"}` }}>{s}</button>
        ))}
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        {filtered.length === 0 ? (
          <div style={{ display: "flex", flexDirection: "column", alignItems: "center", paddingTop: 60, gap: 12 }}>
            <div style={{ fontSize: 50 }}>📋</div>
            <div style={{ fontSize: 14, color: T2 }}>暂无订单</div>
          </div>
        ) : filtered.map(o => (
          <div key={o.id} onClick={() => nav("merchant-order-detail")} style={{ background: WH, borderRadius: 12, padding: 14, marginBottom: 10, border: o.status === "待接单" ? `2px solid ${P}` : `1px solid ${DV}`, cursor: "pointer", boxShadow: "0 1px 4px rgba(0,0,0,0.05)" }}>
            {o.status === "待接单" && <div style={{ background: PL, color: P, fontSize: 11, fontWeight: 700, padding: "3px 8px", borderRadius: 6, display: "inline-block", marginBottom: 8 }}>🔔 新订单</div>}
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
              <div style={{ display: "flex", gap: 6, alignItems: "center" }}>
                <span style={{ background: o.type === "dine-in" ? "#E8F4FF" : PL, color: o.type === "dine-in" ? BL : P, fontSize: 10, padding: "2px 8px", borderRadius: 8, fontWeight: 700 }}>{o.type === "dine-in" ? "堂食" : "配送"}</span>
                <span style={{ fontSize: 11, color: T3 }}>#{o.id.slice(-4)}</span>
              </div>
              <SBadge s={o.status} />
            </div>
            <div style={{ fontWeight: 600, fontSize: 14, color: T1, marginBottom: 3 }}>{o.contact}</div>
            <div style={{ fontSize: 12, color: T3, marginBottom: 6 }}>{o.type === "dine-in" ? `桌号：${o.table}` : `配送：${o.address?.slice(0, 14)}...`}</div>
            <div style={{ fontSize: 12, color: T2, marginBottom: 8 }}>{o.items.join("、")}</div>
            <Div />
            <div style={{ marginTop: 8, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div><span style={{ color: P, fontWeight: 700, fontSize: 16 }}>¥{o.amount}</span><span style={{ fontSize: 11, color: T3, marginLeft: 8 }}>{o.time}</span></div>
              {o.status === "待接单" && (
                <div style={{ display: "flex", gap: 8 }}>
                  <OBtn label="拒单" danger />
                  <button style={{ padding: "6px 14px", borderRadius: 14, border: "none", background: P, color: WH, fontSize: 12, fontWeight: 700, cursor: "pointer" }}>接单</button>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>
      <MerchantTabs active="orders" nav={(t) => { if (t === "dashboard") nav("merchant-dashboard"); if (t === "products") nav("merchant-products"); }} />
    </div>
  );
}

/* ─── SCREEN: MERCHANT ORDER DETAIL ────────────── */
function MerchantOrderDetailScreen({ nav }: { nav: (s: string) => void }) {
  const o = ORDERS[0];
  const [showReject, setShowReject] = useState(false);
  const [accepted, setAccepted] = useState(false);
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <NavBar title="订单详情" onBack={() => nav("merchant-orders")} />
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        <Card mb={12}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 6 }}>
            <div style={{ display: "flex", gap: 6, alignItems: "center" }}>
              <span style={{ background: "#E8F4FF", color: BL, fontSize: 10, padding: "2px 8px", borderRadius: 8, fontWeight: 700 }}>堂食</span>
              <span style={{ fontSize: 11, color: T3 }}>#{o.id}</span>
            </div>
            <SBadge s={accepted ? "已接单" : o.status} />
          </div>
          <div style={{ fontSize: 11, color: T3, marginTop: 4 }}>下单时间：2026-07-09 10:00:23</div>
        </Card>
        <Card mb={12}>
          <SectionLabel label="顾客信息" />
          <div style={{ display: "flex", justifyContent: "space-between", marginTop: 10, fontSize: 13 }}><span style={{ color: T2 }}>联系人</span><span style={{ color: T1, fontWeight: 500 }}>{o.contact}</span></div>
          <div style={{ display: "flex", justifyContent: "space-between", marginTop: 8, fontSize: 13, alignItems: "center" }}>
            <span style={{ color: T2 }}>联系电话</span>
            <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <span style={{ color: T1 }}>{o.phone}</span>
              <button style={{ border: "none", background: PL, color: P, padding: "3px 8px", borderRadius: 8, fontSize: 11, cursor: "pointer" }}>📞 拨打</button>
            </div>
          </div>
          <div style={{ display: "flex", justifyContent: "space-between", marginTop: 8, fontSize: 13 }}>
            <span style={{ color: T2 }}>桌号</span>
            <span style={{ color: T1, fontWeight: 700, fontSize: 16 }}>{o.table}</span>
          </div>
        </Card>
        <Card mb={12}>
          <SectionLabel label="商品明细" />
          {o.items.map((item, i) => (
            <div key={i} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 10 }}>
              <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                <div style={{ width: 36, height: 36, borderRadius: 8, background: BG, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 18 }}>🍛</div>
                <div><div style={{ fontSize: 13, color: T1, fontWeight: 500 }}>{item.split("×")[0]}</div><div style={{ fontSize: 11, color: T3 }}>×{item.split("×")[1]}</div></div>
              </div>
              <span style={{ fontSize: 13, color: T1, fontWeight: 500 }}>¥26</span>
            </div>
          ))}
        </Card>
        <Card mb={12}>
          <FeeRow label="商品金额" val="¥26" /><FeeRow label="包装费" val="¥2" />
          <Div /><div style={{ marginTop: 8, display: "flex", justifyContent: "space-between", fontWeight: 700, fontSize: 15 }}><span style={{ color: T1 }}>实付金额</span><span style={{ color: P }}>¥28</span></div>
        </Card>
        <Card mb={12}>
          <SectionLabel label="顾客备注" />
          <div style={{ marginTop: 8, background: BG, borderRadius: 8, padding: 10, fontSize: 13, color: T2 }}>{o.note}</div>
        </Card>
      </div>
      <div style={{ padding: "12px 16px 24px", background: WH, borderTop: `1px solid ${DV}` }}>
        {!accepted ? (
          <div style={{ display: "flex", gap: 10 }}>
            <button onClick={() => setShowReject(true)} style={{ flex: 1, height: 44, borderRadius: 22, border: `1.5px solid ${ER}`, background: WH, color: ER, fontWeight: 700, fontSize: 14, cursor: "pointer" }}>拒单</button>
            <button onClick={() => setAccepted(true)} style={{ flex: 2, height: 44, borderRadius: 22, border: "none", background: P, color: WH, fontWeight: 700, fontSize: 14, cursor: "pointer" }}>接单</button>
          </div>
        ) : (
          <button style={{ width: "100%", height: 44, borderRadius: 22, border: "none", background: WY, color: WH, fontWeight: 700, fontSize: 14, cursor: "pointer" }}>开始制作</button>
        )}
      </div>
      {showReject && (
        <div style={{ position: "absolute", inset: 0, background: "rgba(0,0,0,0.55)", display: "flex", flexDirection: "column", justifyContent: "flex-end", zIndex: 200 }} onClick={() => setShowReject(false)}>
          <div style={{ background: WH, borderRadius: "16px 16px 0 0", padding: "20px 16px 32px" }} onClick={e => e.stopPropagation()}>
            <div style={{ fontWeight: 700, fontSize: 16, color: T1, marginBottom: 16 }}>请选择拒单原因</div>
            {["商品售罄", "店铺繁忙", "超出配送范围", "无法联系用户", "其他原因"].map(r => (
              <div key={r} style={{ padding: "12px 0", borderBottom: `1px solid ${DV}`, fontSize: 14, color: T1, cursor: "pointer" }}>{r}</div>
            ))}
            <div style={{ marginTop: 16, display: "flex", gap: 10 }}>
              <button onClick={() => setShowReject(false)} style={{ flex: 1, height: 40, borderRadius: 20, border: `1px solid ${DV}`, background: WH, color: T2, fontSize: 14, cursor: "pointer" }}>取消</button>
              <button onClick={() => setShowReject(false)} style={{ flex: 1, height: 40, borderRadius: 20, border: "none", background: ER, color: WH, fontSize: 14, fontWeight: 700, cursor: "pointer" }}>确认拒单</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ─── SCREEN: MERCHANT PRODUCTS ─────────────────── */
function MerchantProductsScreen({ nav }: { nav: (s: string) => void }) {
  const [statTab, setStatTab] = useState("全部");
  const [toggles, setToggles] = useState<Record<number, boolean>>({ 1: true, 2: false, 3: true, 4: true, 5: true, 6: true });
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <div style={{ background: WH, padding: "0 16px", borderBottom: `1px solid ${DV}` }}>
        <div style={{ height: 52, display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <span style={{ fontWeight: 700, fontSize: 17, color: T1 }}>商品管理</span>
          <button onClick={() => nav("merchant-product-edit")} style={{ background: P, color: WH, border: "none", borderRadius: 16, padding: "6px 14px", fontSize: 13, fontWeight: 700, cursor: "pointer", display: "flex", alignItems: "center", gap: 4 }}><Plus size={13} /> 新增商品</button>
        </div>
        <div style={{ display: "flex", marginBottom: -1 }}>
          {["全部", "已上架", "已下架", "已售罄"].map(s => (
            <button key={s} onClick={() => setStatTab(s)} style={{ flex: 1, padding: "8px 6px", border: "none", background: "none", cursor: "pointer", fontSize: 11, fontWeight: statTab === s ? 700 : 400, color: statTab === s ? P : T2, borderBottom: `2px solid ${statTab === s ? P : "transparent"}` }}>{s}</button>
          ))}
        </div>
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        {PRODS.map(p => (
          <div key={p.id} style={{ background: WH, borderRadius: 12, padding: "12px 12px", marginBottom: 8, display: "flex", alignItems: "center", gap: 12, boxShadow: "0 1px 3px rgba(0,0,0,0.04)" }}>
            {/* Product image — fixed size so emoji size never shifts position */}
            <div style={{ width: 64, height: 64, borderRadius: 10, background: BG, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 30, flexShrink: 0 }}>{p.em}</div>
            {/* Info block */}
            <div style={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: 3 }}>
              <div style={{ fontWeight: 600, fontSize: 13, color: T1 }}>{p.name}</div>
              <div style={{ fontSize: 11, color: T3 }}>{p.cat} · 月售{p.sales}</div>
              <div style={{ display: "flex", alignItems: "baseline", gap: 4 }}>
                <span style={{ color: P, fontWeight: 700, fontSize: 15 }}>¥{p.price}</span>
                {p.origPrice && <span style={{ color: T3, fontSize: 10, textDecoration: "line-through", fontWeight: 400 }}>¥{p.origPrice}</span>}
              </div>
            </div>
            {/* Actions — vertically centered in card */}
            <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 8, flexShrink: 0 }}>
              <Toggle on={!!toggles[p.id]} onChange={() => setToggles(t => ({ ...t, [p.id]: !t[p.id] }))} />
              <div style={{ display: "flex", gap: 6 }}>
                <button style={{ border: "none", background: BG, borderRadius: 6, padding: "4px 10px", fontSize: 11, color: T2, cursor: "pointer", height: 24 }}>编辑</button>
                <button style={{ border: "none", background: "#FFF1F0", borderRadius: 6, padding: "4px 10px", fontSize: 11, color: ER, cursor: "pointer", height: 24 }}>删除</button>
              </div>
            </div>
          </div>
        ))}
      </div>
      <MerchantTabs active="products" nav={(t) => { if (t === "dashboard") nav("merchant-dashboard"); if (t === "orders") nav("merchant-orders"); if (t === "profile") nav("merchant-profile"); }} />
    </div>
  );
}

/* ─── SCREEN: MERCHANT PRODUCT EDIT ────────────── */
function MerchantProductEditScreen({ nav }: { nav: (s: string) => void }) {
  const [name, setName] = useState("");
  const [cat, setCat] = useState("热门推荐");
  const [price, setPrice] = useState("");
  const [onSale, setOnSale] = useState(true);
  const [recommended, setRecommended] = useState(false);
  const [specs, setSpecs] = useState(["小份", "中份", "大份"]);
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <NavBar title="新增商品" onBack={() => nav("merchant-products")} />
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        <Card mb={12}>
          <SectionLabel label="商品图片" />
          <div style={{ display: "flex", gap: 10, marginTop: 10 }}>
            <div style={{ width: 80, height: 80, borderRadius: 10, border: `2px dashed ${DV}`, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", cursor: "pointer", gap: 4 }}>
              <Plus size={20} color={T3} /><span style={{ fontSize: 10, color: T3 }}>上传图片</span>
            </div>
          </div>
        </Card>
        <Card mb={12}>
          <SectionLabel label="基本信息" />
          <div style={{ marginTop: 12 }}><FieldLabel label="商品名称" required /><input value={name} onChange={e => setName(e.target.value)} placeholder="请输入商品名称" style={inputStyle()} /></div>
          <div style={{ marginTop: 12 }}>
            <FieldLabel label="所属分类" required />
            <div style={{ display: "flex", flexWrap: "wrap", gap: 7, marginTop: 6 }}>
              {CATS.map(c => <button key={c} onClick={() => setCat(c)} style={{ padding: "5px 14px", borderRadius: 16, border: `1.5px solid ${cat === c ? P : DV}`, background: cat === c ? PL : WH, color: cat === c ? P : T2, fontSize: 12, cursor: "pointer", fontWeight: cat === c ? 700 : 400 }}>{c}</button>)}
            </div>
          </div>
          <div style={{ marginTop: 12 }}><FieldLabel label="商品简介" /><input placeholder="简要描述商品特色" style={inputStyle()} /></div>
          <div style={{ display: "flex", gap: 10, marginTop: 12 }}>
            <div style={{ flex: 1 }}><FieldLabel label="售价（元）" required /><input value={price} onChange={e => setPrice(e.target.value)} placeholder="0.00" type="number" style={inputStyle()} /></div>
            <div style={{ flex: 1 }}><FieldLabel label="原价（元）" /><input placeholder="0.00" type="number" style={inputStyle()} /></div>
          </div>
        </Card>
        <Card mb={12}>
          <SectionLabel label="规格配置" />
          {specs.map((s, i) => (
            <div key={i} style={{ display: "flex", alignItems: "center", gap: 8, marginTop: 10 }}>
              <input defaultValue={s} style={{ ...inputStyle(), flex: 1 }} />
              <input defaultValue={String(12 + i * 4)} style={{ ...inputStyle(), width: 60 }} />
              <button onClick={() => setSpecs(specs.filter((_, j) => j !== i))} style={{ border: "none", background: "#FFF1F0", borderRadius: 6, padding: "6px 8px", cursor: "pointer" }}><X size={13} color={ER} /></button>
            </div>
          ))}
          <button onClick={() => setSpecs([...specs, ""])} style={{ marginTop: 10, border: `1.5px dashed ${P}`, background: PL, color: P, borderRadius: 8, padding: "6px 14px", fontSize: 12, fontWeight: 700, cursor: "pointer", display: "flex", alignItems: "center", gap: 6 }}><Plus size={13} /> 添加规格</button>
        </Card>
        <Card mb={12}>
          {[{ label: "立即上架", val: onSale, set: () => setOnSale(!onSale) }, { label: "设为推荐", val: recommended, set: () => setRecommended(!recommended) }].map(item => (
            <div key={item.label} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
              <span style={{ fontSize: 14, color: T1 }}>{item.label}</span>
              <Toggle on={item.val} onChange={item.set} />
            </div>
          ))}
        </Card>
      </div>
      <div style={{ padding: "12px 16px 24px", background: WH, borderTop: `1px solid ${DV}` }}>
        <PBtn label="保存商品" onClick={() => nav("merchant-products")} />
      </div>
    </div>
  );
}

/* ─── SCREEN: MERCHANT SETTINGS ─────────────────── */
function MerchantSettingsScreen({ nav }: { nav: (s: string) => void }) {
  const [dineIn, setDineIn] = useState(true);
  const [delivery, setDelivery] = useState(true);
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <NavBar title="店铺设置" onBack={() => nav("merchant-dashboard")} />
      <div style={{ flex: 1, overflowY: "auto", padding: "12px 16px" }}>
        <Card mb={12}>
          <SectionLabel label="基本信息" />
          {[{ l: "店铺名称", v: "食刻小馆" }, { l: "联系电话", v: "028-88888888" }, { l: "店铺地址", v: "某市某区某街道123号" }].map(f => (
            <div key={f.l} style={{ marginTop: 12 }}><FieldLabel label={f.l} /><input defaultValue={f.v} style={inputStyle()} /></div>
          ))}
          <div style={{ marginTop: 12 }}><FieldLabel label="店铺公告" /><textarea defaultValue="欢迎光临食刻小馆！本店主营家常便饭，食材新鲜，现点现做。" rows={3} style={{ width: "100%", borderRadius: 8, border: `1px solid ${DV}`, padding: "8px 10px", fontSize: 13, resize: "none", boxSizing: "border-box", marginTop: 6 }} /></div>
        </Card>
        <Card mb={12}>
          <SectionLabel label="营业设置" />
          {[{ label: "堂食开放", desc: "关闭后用户无法提交堂食订单", val: dineIn, set: () => setDineIn(!dineIn) }, { label: "配送开放", desc: "关闭后用户无法提交配送订单", val: delivery, set: () => setDelivery(!delivery) }].map(item => (
            <div key={item.label} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: 14 }}>
              <div><div style={{ fontSize: 14, color: T1, fontWeight: 500 }}>{item.label}</div><div style={{ fontSize: 11, color: T3 }}>{item.desc}</div></div>
              <Toggle on={item.val} onChange={item.set} />
            </div>
          ))}
        </Card>
        <Card mb={12}>
          <SectionLabel label="配送设置" />
          {[{ l: "起送金额（元）", v: "20" }, { l: "配送费（元）", v: "3" }, { l: "预计配送时间（分钟）", v: "30" }].map(f => (
            <div key={f.l} style={{ marginTop: 12 }}><FieldLabel label={f.l} /><input defaultValue={f.v} type="number" style={inputStyle()} /></div>
          ))}
          <div style={{ marginTop: 12 }}><FieldLabel label="配送范围说明" /><textarea defaultValue="本店配送范围：3公里以内，超出范围请联系商家确认。" rows={2} style={{ width: "100%", borderRadius: 8, border: `1px solid ${DV}`, padding: "8px 10px", fontSize: 13, resize: "none", boxSizing: "border-box", marginTop: 6 }} /></div>
        </Card>
      </div>
      <div style={{ padding: "12px 16px 24px", background: WH, borderTop: `1px solid ${DV}` }}>
        <PBtn label="保存设置" onClick={() => nav("merchant-dashboard")} />
      </div>
    </div>
  );
}

/* ─── SCREEN: MERCHANT PROFILE ──────────────────── */
function MerchantProfileScreen({ nav }: { nav: (s: string) => void }) {
  const [showLogout, setShowLogout] = useState(false);
  const menus = [
    { icon: Settings, label: "店铺设置", to: "merchant-settings" },
    { icon: Package, label: "商品管理", to: "merchant-products" },
    { icon: ClipboardList, label: "营业数据", to: "merchant-dashboard" },
    { icon: KeyRound, label: "修改密码", to: "merchant-change-password" },
    { icon: Shield, label: "账号与安全", to: "" },
    { icon: Phone, label: "联系技术支持", to: "" },
  ];
  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar dark />
      <div style={{ background: `linear-gradient(135deg, ${P}, #FF6B00)`, padding: "0 16px 28px" }}>
        <div style={{ height: 44 }} />
        <div style={{ display: "flex", alignItems: "center", gap: 14 }}>
          <div style={{ width: 60, height: 60, borderRadius: 30, background: "rgba(255,255,255,0.3)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 28 }}>👨‍🍳</div>
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: 7, marginBottom: 3 }}>
              <div style={{ fontWeight: 700, fontSize: 18, color: WH }}>店主小王</div>
              <span style={{ background: "rgba(255,255,255,0.25)", color: WH, fontSize: 10, padding: "1px 7px", borderRadius: 8, fontWeight: 700 }}>店铺管理员</span>
            </div>
            <div style={{ fontSize: 12, color: "rgba(255,255,255,0.8)" }}>merchant001 · 食刻小馆</div>
          </div>
        </div>
      </div>
      <div style={{ flex: 1, overflowY: "auto", padding: "10px 16px", marginTop: -16 }}>
        <div style={{ background: WH, borderRadius: 12, padding: "2px 0", marginBottom: 10 }}>
          {menus.map(({ icon: Icon, label, to }, i) => (
            <div key={label}>
              <div onClick={() => to && nav(to)} style={{ display: "flex", alignItems: "center", padding: "13px 16px", cursor: "pointer" }}>
                <Icon size={17} color={T2} style={{ marginRight: 12, flexShrink: 0 }} />
                <span style={{ flex: 1, fontSize: 14, color: T1 }}>{label}</span>
                <ChevronRight size={14} color={T3} />
              </div>
              {i < menus.length - 1 && <div style={{ height: 1, background: DV, marginLeft: 45 }} />}
            </div>
          ))}
        </div>
        <button onClick={() => setShowLogout(true)} style={{ width: "100%", padding: 14, borderRadius: 12, border: `1.5px solid ${ER}`, background: WH, color: ER, fontSize: 14, fontWeight: 600, cursor: "pointer" }}>退出商家登录</button>
      </div>
      <MerchantTabs active="profile" nav={(t) => { if (t === "dashboard") nav("merchant-dashboard"); if (t === "orders") nav("merchant-orders"); if (t === "products") nav("merchant-products"); }} />
      {/* Logout confirm */}
      {showLogout && (
        <div style={{ position: "absolute", inset: 0, background: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 300, padding: 28 }} onClick={() => setShowLogout(false)}>
          <div style={{ background: WH, borderRadius: 16, padding: "28px 24px", width: "100%" }} onClick={e => e.stopPropagation()}>
            <div style={{ fontWeight: 700, fontSize: 17, color: T1, marginBottom: 10, textAlign: "center" }}>确认退出登录？</div>
            <div style={{ fontSize: 13, color: T2, lineHeight: 1.7, textAlign: "center", marginBottom: 24 }}>退出后需要重新输入商家账号和密码。</div>
            <div style={{ display: "flex", gap: 10 }}>
              <button onClick={() => setShowLogout(false)} style={{ flex: 1, height: 44, borderRadius: 22, border: `1px solid ${DV}`, background: WH, color: T2, fontSize: 14, fontWeight: 500, cursor: "pointer" }}>取消</button>
              <button onClick={() => { setShowLogout(false); nav("merchant-login"); }} style={{ flex: 1, height: 44, borderRadius: 22, border: "none", background: ER, color: WH, fontSize: 14, fontWeight: 700, cursor: "pointer" }}>确认退出</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ─── SCREEN: MERCHANT CHANGE PASSWORD ──────────── */
function MerchantChangePasswordScreen({ nav }: { nav: (s: string) => void }) {
  const [cur, setCur] = useState("");
  const [nw, setNw] = useState("");
  const [confirm, setConfirm] = useState("");
  const [showCur, setShowCur] = useState(false);
  const [showNw, setShowNw] = useState(false);
  const [showCfm, setShowCfm] = useState(false);
  const [curErr, setCurErr] = useState("");
  const [nwErr, setNwErr] = useState("");
  const [cfmErr, setCfmErr] = useState("");
  const [toastMsg, setToastMsg] = useState("");

  const validate = () => {
    let ok = true;
    setCurErr(""); setNwErr(""); setCfmErr("");
    if (!cur) { setCurErr("请输入当前密码"); ok = false; }
    else if (cur !== "merchant123") { setCurErr("当前密码错误"); ok = false; }
    if (!nw) { setNwErr("请输入新密码"); ok = false; }
    else if (nw.length < 8 || nw.length > 20) { setNwErr("新密码长度需为8至20位"); ok = false; }
    else if (!/[a-zA-Z]/.test(nw) || !/[0-9]/.test(nw)) { setNwErr("新密码至少包含字母和数字"); ok = false; }
    else if (nw === cur) { setNwErr("新密码不能与当前密码相同"); ok = false; }
    if (!cfmErr && nw && confirm !== nw) { setCfmErr("两次输入的密码不一致"); ok = false; }
    return ok;
  };

  const handleSave = () => {
    if (!validate()) return;
    setToastMsg("密码修改成功，请重新登录");
    setTimeout(() => { setToastMsg(""); nav("merchant-login"); }, 1800);
  };

  const PwdField = ({ label, val, set, show, setShow, err }: { label: string; val: string; set: (v: string) => void; show: boolean; setShow: (v: boolean) => void; err: string }) => (
    <div style={{ marginBottom: 16 }}>
      <div style={{ fontSize: 13, color: T2, marginBottom: 6, fontWeight: 500 }}>{label}</div>
      <div style={{ position: "relative", display: "flex", alignItems: "center" }}>
        <Lock size={16} color={err ? ER : T3} style={{ position: "absolute", left: 13 }} />
        <input value={val} onChange={e => set(e.target.value)} type={show ? "text" : "password"} placeholder={`请输入${label}`}
          style={{ width: "100%", height: 46, borderRadius: 10, border: `1.5px solid ${err ? ER : DV}`, padding: "0 44px 0 38px", fontSize: 14, boxSizing: "border-box", fontFamily: "inherit", outline: "none" }} />
        <button onClick={() => setShow(!show)} style={{ position: "absolute", right: 12, background: "none", border: "none", cursor: "pointer", display: "flex", alignItems: "center" }}>
          {show ? <EyeOff size={17} color={T3} /> : <Eye size={17} color={T3} />}
        </button>
      </div>
      {err && <div style={{ fontSize: 11, color: ER, marginTop: 5 }}>{err}</div>}
    </div>
  );

  return (
    <div style={{ height: "100%", display: "flex", flexDirection: "column", background: BG }}>
      <StatusBar />
      <NavBar title="修改密码" onBack={() => nav("merchant-profile")} />
      <div style={{ flex: 1, overflowY: "auto", padding: "16px 16px" }}>
        <div style={{ background: WH, borderRadius: 12, padding: 16, marginBottom: 12, boxShadow: "0 1px 4px rgba(0,0,0,0.05)" }}>
          <PwdField label="当前密码" val={cur} set={v => { setCur(v); setCurErr(""); }} show={showCur} setShow={setShowCur} err={curErr} />
          <PwdField label="新密码" val={nw} set={v => { setNw(v); setNwErr(""); }} show={showNw} setShow={setShowNw} err={nwErr} />
          <PwdField label="确认新密码" val={confirm} set={v => { setConfirm(v); setCfmErr(""); }} show={showCfm} setShow={setShowCfm} err={cfmErr} />
          <div style={{ background: BG, borderRadius: 8, padding: "10px 12px", fontSize: 11, color: T3, lineHeight: 1.8 }}>
            密码规则：8-20位字符，至少包含字母和数字
          </div>
        </div>
      </div>
      <div style={{ padding: "12px 16px 24px", background: WH, borderTop: `1px solid ${DV}` }}>
        <PBtn label="保存新密码" onClick={handleSave} />
      </div>
      {toastMsg && (
        <div style={{ position: "absolute", bottom: 100, left: "50%", transform: "translateX(-50%)", background: "rgba(0,0,0,0.75)", color: WH, padding: "10px 20px", borderRadius: 24, fontSize: 13, whiteSpace: "nowrap", zIndex: 400 }}>{toastMsg}</div>
      )}
    </div>
  );
}

/* ─── REUSABLE HELPERS ──────────────────────────── */
function Card({ children, mb }: { children: React.ReactNode; mb?: number }) {
  return <div style={{ background: WH, borderRadius: 12, padding: 14, marginBottom: mb ?? 0, boxShadow: "0 1px 4px rgba(0,0,0,0.05)" }}>{children}</div>;
}
function SectionLabel({ label }: { label: string }) {
  return <div style={{ fontWeight: 700, fontSize: 15, color: T1 }}>{label}</div>;
}
function FieldLabel({ label, required }: { label: string; required?: boolean }) {
  return <div style={{ fontSize: 13, color: T2, marginBottom: 6 }}>{label}{required && <span style={{ color: ER, marginLeft: 3 }}>*</span>}</div>;
}
function InfoRow({ label, val, bold, small }: { label: string; val: string; bold?: boolean; small?: boolean }) {
  return <div style={{ display: "flex", justifyContent: "space-between", marginTop: 8, fontSize: small ? 12 : 13 }}><span style={{ color: T2 }}>{label}</span><span style={{ color: T1, fontWeight: bold ? 700 : 500 }}>{val}</span></div>;
}
function FeeRow({ label, val }: { label: string; val: string }) {
  return <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 6, fontSize: 13, color: T2 }}><span>{label}</span><span>{val}</span></div>;
}
function Row({ icon, label }: { icon: string; label: string }) {
  return <div style={{ display: "flex", alignItems: "center", gap: 8 }}><span style={{ fontSize: 16 }}>{icon}</span><span style={{ fontWeight: 600, fontSize: 14, color: T1 }}>{label}</span></div>;
}
function inputStyle(disabled?: boolean): React.CSSProperties {
  return { width: "100%", height: 44, borderRadius: 8, border: `1px solid ${DV}`, padding: "0 12px", fontSize: 14, boxSizing: "border-box", background: disabled ? BG : WH, marginTop: 4, fontFamily: "inherit" };
}

/* ─── PHONE FRAME ───────────────────────────────── */
function PhoneFrame({ children, scale = 1 }: { children: React.ReactNode; scale?: number }) {
  return (
    <div style={{ width: 375 * scale, height: 812 * scale, borderRadius: 46 * scale, overflow: "hidden", background: WH, boxShadow: "0 20px 60px rgba(0,0,0,0.3), 0 0 0 1px rgba(0,0,0,0.1)", border: `${8 * scale}px solid #1C1C1E`, flexShrink: 0, position: "relative" }}>
      <div style={{ width: 375, height: 812, transformOrigin: "top left", transform: `scale(${scale})`, position: "relative" }}>
        {children}
      </div>
    </div>
  );
}

/* ─── SCREEN ROUTER ─────────────────────────────── */
const SCREENS = [
  { id: "user-login", label: "登录", group: "用户端" },
  { id: "merchant-login", label: "商家登录", group: "用户端" },
  { id: "user-home", label: "首页（堂食）", group: "用户端" },
  { id: "user-home-delivery", label: "首页（配送）", group: "用户端" },
  { id: "cart", label: "购物车", group: "用户端" },
  { id: "checkout-dine-in", label: "确认订单（堂食）", group: "用户端" },
  { id: "checkout-delivery", label: "确认订单（配送）", group: "用户端" },
  { id: "order-success", label: "提交成功", group: "用户端" },
  { id: "order-list", label: "订单列表", group: "用户端" },
  { id: "order-detail-dine-in", label: "订单详情（堂食）", group: "用户端" },
  { id: "order-detail-delivery", label: "订单详情（配送）", group: "用户端" },
  { id: "my", label: "我的", group: "用户端" },
  { id: "address-list", label: "收货地址", group: "用户端" },
  { id: "merchant-dashboard", label: "商家工作台", group: "商家端" },
  { id: "merchant-orders", label: "订单管理", group: "商家端" },
  { id: "merchant-order-detail", label: "订单详情", group: "商家端" },
  { id: "merchant-products", label: "商品管理", group: "商家端" },
  { id: "merchant-product-edit", label: "新增商品", group: "商家端" },
  { id: "merchant-settings", label: "店铺设置", group: "商家端" },
  { id: "merchant-profile", label: "商家个人中心", group: "商家端" },
  { id: "merchant-change-password", label: "修改密码", group: "商家端" },
];

/* ─── APP ROOT ──────────────────────────────────── */
export default function App() {
  const [screen, setScreen] = useState("user-login");
  const [viewMode, setViewMode] = useState<"overview" | "interactive">("overview");
  const [cart, setCart] = useState<CartItem[]>([]);
  const [mode, setMode] = useState<"dine-in" | "delivery">("dine-in");

  const addToCart = (item: CartItem) => {
    setCart(prev => {
      const existing = prev.find(i => i.id === item.id && i.spec === item.spec);
      if (existing) return prev.map(i => (i.id === item.id && i.spec === item.spec) ? { ...i, qty: Math.max(0, i.qty + item.qty) } : i).filter(i => i.qty > 0);
      if (item.qty > 0) return [...prev, item];
      return prev;
    });
  };

  const nav = (s: string) => {
    if (s === "user-home-delivery") { setMode("delivery"); setScreen("user-home"); }
    else setScreen(s);
  };

  const renderScreen = (sid: string) => {
    const n = (s: string) => { setScreen(s); };
    const effectiveSid = sid === "user-home-delivery" ? "user-home" : sid;
    switch (effectiveSid) {
      case "user-login": return <LoginScreen nav={n} />;
      case "merchant-login": return <MerchantLoginScreen nav={n} />;
      case "user-home": return <UserHomeScreen cart={cart} addToCart={addToCart} nav={n} mode={sid === "user-home-delivery" ? "delivery" : mode} setMode={setMode} />;
      case "cart": return <CartScreen cart={cart} setCart={setCart} mode={mode} nav={n} />;
      case "checkout-dine-in": return <CheckoutDineInScreen cart={cart.length > 0 ? cart : [{ id: 1, name: "招牌牛肉饭", em: "🍛", price: 26, qty: 1 }]} nav={n} />;
      case "checkout-delivery": return <CheckoutDeliveryScreen cart={cart.length > 0 ? cart : [{ id: 2, name: "香辣鸡腿饭", em: "🍗", price: 22, qty: 1 }]} nav={n} />;
      case "order-success": return <OrderSuccessScreen nav={n} />;
      case "order-list": return <OrderListScreen nav={n} />;
      case "order-detail-dine-in": return <OrderDetailDineInScreen nav={n} />;
      case "order-detail-delivery": return <OrderDetailDeliveryScreen nav={n} />;
      case "my": return <MyScreen nav={n} />;
      case "address-list": return <AddressListScreen nav={n} />;
      case "merchant-dashboard": return <MerchantDashboardScreen nav={n} />;
      case "merchant-orders": return <MerchantOrdersScreen nav={n} />;
      case "merchant-order-detail": return <MerchantOrderDetailScreen nav={n} />;
      case "merchant-products": return <MerchantProductsScreen nav={n} />;
      case "merchant-product-edit": return <MerchantProductEditScreen nav={n} />;
      case "merchant-settings": return <MerchantSettingsScreen nav={n} />;
      case "merchant-profile": return <MerchantProfileScreen nav={n} />;
      case "merchant-change-password": return <MerchantChangePasswordScreen nav={n} />;
      default: return <LoginScreen nav={n} />;
    }
  };

  // ── Interactive mode ──────────────────────────────
  if (viewMode === "interactive") {
    const curScreen = SCREENS.find(s => s.id === screen);
    return (
      <div style={{ minHeight: "100vh", background: "#1A1A2E", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: 24, fontFamily: "'Noto Sans SC', system-ui, sans-serif" }}>
        {/* Toolbar */}
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 24, width: "100%", maxWidth: 500 }}>
          <button onClick={() => setViewMode("overview")} style={{ display: "flex", alignItems: "center", gap: 6, background: "rgba(255,255,255,0.1)", border: "none", borderRadius: 10, padding: "8px 14px", color: WH, fontSize: 13, cursor: "pointer" }}>
            <Grid3X3 size={15} /> 总览
          </button>
          <div style={{ flex: 1, background: "rgba(255,255,255,0.08)", borderRadius: 10, padding: "8px 14px", color: "rgba(255,255,255,0.7)", fontSize: 13 }}>
            {curScreen?.group} · {curScreen?.label ?? screen}
          </div>
        </div>
        {/* Phone */}
        <PhoneFrame>
          <div style={{ width: 375, height: 812, position: "relative", overflow: "hidden", fontFamily: "'Noto Sans SC', system-ui, sans-serif" }}>
            {renderScreen(screen)}
          </div>
        </PhoneFrame>
        {/* Screen nav */}
        <div style={{ display: "flex", gap: 8, marginTop: 20, flexWrap: "wrap", justifyContent: "center", maxWidth: 640 }}>
          {SCREENS.map(s => (
            <button key={s.id} onClick={() => { nav(s.id); }} style={{ padding: "5px 12px", borderRadius: 16, border: "none", background: screen === s.id ? P : "rgba(255,255,255,0.12)", color: screen === s.id ? WH : "rgba(255,255,255,0.6)", fontSize: 11, cursor: "pointer", fontWeight: screen === s.id ? 700 : 400 }}>{s.label}</button>
          ))}
        </div>
      </div>
    );
  }

  // ── Overview mode ─────────────────────────────────
  const userScreens = SCREENS.filter(s => s.group === "用户端");
  const merchantScreens = SCREENS.filter(s => s.group === "商家端");
  const SCALE = 0.29;

  return (
    <div style={{ minHeight: "100vh", background: "#0F0F1A", fontFamily: "'Noto Sans SC', system-ui, sans-serif", padding: "32px 32px 60px" }}>
      {/* Header */}
      <div style={{ textAlign: "center", marginBottom: 40 }}>
        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 14, marginBottom: 12 }}>
          <div style={{ fontSize: 40 }}>🍜</div>
          <div>
            <div style={{ fontSize: 28, fontWeight: 700, color: WH, letterSpacing: 1 }}>食刻到家</div>
            <div style={{ fontSize: 13, color: P, marginTop: 2, letterSpacing: 2 }}>WeChat Mini-Program · 高保真原型</div>
          </div>
        </div>
        <div style={{ fontSize: 12, color: "rgba(255,255,255,0.4)", marginBottom: 20 }}>点击任意屏幕进入交互模式 · 包含 {SCREENS.length} 个界面</div>
        <button onClick={() => { setScreen("user-login"); setViewMode("interactive"); }} style={{ background: P, color: WH, border: "none", borderRadius: 24, padding: "12px 32px", fontSize: 15, fontWeight: 700, cursor: "pointer", boxShadow: `0 4px 20px ${PL}60` }}>
          ▶ 进入交互原型
        </button>
      </div>

      {/* User screens */}
      <div style={{ marginBottom: 40 }}>
        <div style={{ fontSize: 13, fontWeight: 700, color: P, letterSpacing: 3, textTransform: "uppercase", marginBottom: 20, paddingLeft: 4 }}>用户端 · User Side</div>
        <div style={{ display: "flex", flexWrap: "wrap", gap: 20 }}>
          {userScreens.map(s => (
            <div key={s.id} onClick={() => { nav(s.id); setViewMode("interactive"); }} style={{ cursor: "pointer", display: "flex", flexDirection: "column", alignItems: "center", gap: 10 }}>
              <div style={{ transition: "transform .2s", transform: "scale(1)" }} onMouseEnter={e => (e.currentTarget.style.transform = "scale(1.04)")} onMouseLeave={e => (e.currentTarget.style.transform = "scale(1)")}>
                <PhoneFrame scale={SCALE}>
                  <div style={{ width: 375, height: 812, position: "relative", overflow: "hidden", fontFamily: "'Noto Sans SC', system-ui, sans-serif" }}>
                    {renderScreen(s.id)}
                  </div>
                </PhoneFrame>
              </div>
              <div style={{ fontSize: 11, color: "rgba(255,255,255,0.55)", textAlign: "center", maxWidth: 375 * SCALE }}>{s.label}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Merchant screens */}
      <div>
        <div style={{ fontSize: 13, fontWeight: 700, color: "#13C2C2", letterSpacing: 3, textTransform: "uppercase", marginBottom: 20, paddingLeft: 4 }}>商家端 · Merchant Side</div>
        <div style={{ display: "flex", flexWrap: "wrap", gap: 20 }}>
          {merchantScreens.map(s => (
            <div key={s.id} onClick={() => { nav(s.id); setViewMode("interactive"); }} style={{ cursor: "pointer", display: "flex", flexDirection: "column", alignItems: "center", gap: 10 }}>
              <div style={{ transition: "transform .2s" }} onMouseEnter={e => (e.currentTarget.style.transform = "scale(1.04)")} onMouseLeave={e => (e.currentTarget.style.transform = "scale(1)")}>
                <PhoneFrame scale={SCALE}>
                  <div style={{ width: 375, height: 812, position: "relative", overflow: "hidden", fontFamily: "'Noto Sans SC', system-ui, sans-serif" }}>
                    {renderScreen(s.id)}
                  </div>
                </PhoneFrame>
              </div>
              <div style={{ fontSize: 11, color: "rgba(255,255,255,0.55)", textAlign: "center", maxWidth: 375 * SCALE }}>{s.label}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Design system legend */}
      <div style={{ marginTop: 60, background: "rgba(255,255,255,0.04)", borderRadius: 16, padding: "24px 28px" }}>
        <div style={{ fontSize: 13, fontWeight: 700, color: "rgba(255,255,255,0.5)", letterSpacing: 2, marginBottom: 20 }}>DESIGN SYSTEM</div>
        <div style={{ display: "flex", flexWrap: "wrap", gap: 20 }}>
          {[["主色", P], ["浅橙", PL], ["成功", SG], ["警告", WY], ["错误", ER], ["蓝色", BL], ["紫色", PU], ["青色", TE]].map(([label, color]) => (
            <div key={label as string} style={{ display: "flex", alignItems: "center", gap: 8 }}>
              <div style={{ width: 20, height: 20, borderRadius: 6, background: color as string }} />
              <div><div style={{ fontSize: 10, color: "rgba(255,255,255,0.35)", marginBottom: 1 }}>{label}</div><div style={{ fontSize: 10, color: "rgba(255,255,255,0.5)" }}>{color}</div></div>
            </div>
          ))}
        </div>
        <div style={{ display: "flex", flexWrap: "wrap", gap: 10, marginTop: 20 }}>
          {Object.entries(STATUS).map(([key, val]) => (
            <span key={key} style={{ background: val.bg, color: val.color, fontSize: 11, padding: "3px 10px", borderRadius: 10, fontWeight: 600 }}>{key}</span>
          ))}
        </div>
      </div>
    </div>
  );
}

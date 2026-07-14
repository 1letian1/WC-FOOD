export const money = value => Number(value || 0).toFixed(2).replace(/\.00$/, '')
export const dateTime = value => value ? value.replace('T', ' ').slice(0, 16) : '-'
export const randomKey = () => `order_${Date.now()}_${Math.random().toString(36).slice(2, 10)}`
export const statusClass = code => ({
  1: 'pending', 2: 'accepted', 3: 'cooking', 4: 'pickup', 5: 'delivering',
  6: 'done', 7: 'done', 8: 'closed', 9: 'rejected',
}[code] || 'closed')

export function formatCurrency(value) {
  return new Intl.NumberFormat('ko-KR').format(Number(value || 0))
}

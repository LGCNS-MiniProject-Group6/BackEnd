function Loading({ label = '불러오는 중입니다.' }) {
  return (
    <div className="loading" role="status" aria-live="polite">
      <span className="loading__spinner" aria-hidden="true" />
      <span>{label}</span>
    </div>
  )
}

export default Loading

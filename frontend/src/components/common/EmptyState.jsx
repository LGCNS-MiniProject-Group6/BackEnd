function EmptyState({ title, description }) {
  return (
    <section className="empty-state">
      <span aria-hidden="true">⌕</span>
      <h2>{title}</h2>
      <p>{description}</p>
    </section>
  )
}

export default EmptyState

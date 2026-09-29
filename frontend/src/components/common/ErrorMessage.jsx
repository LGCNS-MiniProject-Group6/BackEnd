import Button from './Button'

function ErrorMessage({
  title = '정보를 불러오지 못했습니다.',
  description,
  actionLabel = '다시 시도',
  onRetry,
}) {
  return (
    <section className="error-state" role="alert">
      <div>
        <h2>{title}</h2>
        {description && <p>{description}</p>}
      </div>
      {onRetry && <Button onClick={onRetry}>{actionLabel}</Button>}
    </section>
  )
}

export default ErrorMessage

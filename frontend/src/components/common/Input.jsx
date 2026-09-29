import { useId } from 'react'

function Input({ label, helperText, error, className = '', ...props }) {
  const generatedId = useId()
  const inputId = props.id || generatedId
  const messageId = `${inputId}-message`

  return (
    <div className={`field ${className}`.trim()}>
      {label && <label htmlFor={inputId}>{label}</label>}
      <input
        id={inputId}
        className={error ? 'input input--error' : 'input'}
        aria-describedby={helperText || error ? messageId : undefined}
        {...props}
      />
      {(error || helperText) && (
        <p id={messageId} className={error ? 'field__message field__message--error' : 'field__message'}>
          {error || helperText}
        </p>
      )}
    </div>
  )
}

export default Input

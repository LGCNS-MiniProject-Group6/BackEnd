import { useEffect } from 'react'
import Button from './Button'

function Modal({ open, title, description, confirmLabel = '확인', onConfirm, onClose }) {
  useEffect(() => {
    if (!open) return undefined
    const onKeyDown = (event) => event.key === 'Escape' && onClose?.()
    window.addEventListener('keydown', onKeyDown)
    return () => window.removeEventListener('keydown', onKeyDown)
  }, [open, onClose])

  if (!open) return null

  return (
    <div className="modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <h2 id="modal-title">{title}</h2>
        <p>{description}</p>
        <div className="modal__actions">
          <Button variant="secondary" onClick={onClose}>취소</Button>
          <Button onClick={onConfirm}>{confirmLabel}</Button>
        </div>
      </section>
    </div>
  )
}

export default Modal

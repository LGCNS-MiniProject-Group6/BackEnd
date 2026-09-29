import { useEffect, useState } from 'react'
import { authApi } from '../../api/authApi'
import Button from '../common/Button'
import Input from '../common/Input'
import { formatTimer, getApiErrorMessage } from '../../utils/authUtils'

const VERIFICATION_SECONDS = 180

function PhoneVerification({ email, phone, onVerified }) {
  const [code, setCode] = useState('')
  const [remainingSeconds, setRemainingSeconds] = useState(VERIFICATION_SECONDS)
  const [requestState, setRequestState] = useState('idle')
  const [error, setError] = useState('')
  const [verified, setVerified] = useState(false)
  const [timerVersion, setTimerVersion] = useState(0)

  useEffect(() => {
    if (verified) return undefined

    const timerId = window.setInterval(() => {
      setRemainingSeconds((current) => {
        if (current <= 1) {
          window.clearInterval(timerId)
          return 0
        }
        return current - 1
      })
    }, 1000)

    return () => window.clearInterval(timerId)
  }, [verified, timerVersion])

  const expired = remainingSeconds <= 0 && !verified

  const handleCodeChange = (event) => {
    setCode(event.target.value.replace(/\D/g, '').slice(0, 6))
    setError('')
  }

  const verifyCode = async (event) => {
    event.preventDefault()

    if (expired) {
      setError('인증시간이 만료되었습니다. 인증번호를 다시 요청해주세요.')
      return
    }
    if (code.length !== 6) {
      setError('6자리 인증번호를 입력해주세요.')
      return
    }

    setRequestState('verifying')
    setError('')

    try {
      const { data } = await authApi.verifyPasswordResetCode({
        email,
        phone: phone.replace(/\D/g, ''),
        code,
      })
      setVerified(true)
      onVerified(data.resetToken)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '인증번호를 확인하지 못했습니다.'))
    } finally {
      setRequestState('idle')
    }
  }

  const resendCode = async () => {
    if (requestState !== 'idle') return

    setRequestState('resending')
    setError('')

    try {
      await authApi.sendPasswordResetCode({
        email,
        phone: phone.replace(/\D/g, ''),
      })
      setCode('')
      setVerified(false)
      setRemainingSeconds(VERIFICATION_SECONDS)
      setTimerVersion((current) => current + 1)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '인증번호를 다시 발송하지 못했습니다.'))
    } finally {
      setRequestState('idle')
    }
  }

  return (
    <form className="verification-form" onSubmit={verifyCode}>
      <div className="auth-notice" role="status">인증번호가 발송되었습니다.</div>
      <Input
        label="인증번호"
        value={code}
        onChange={handleCodeChange}
        inputMode="numeric"
        autoComplete="one-time-code"
        placeholder="6자리 숫자"
        maxLength={6}
        disabled={verified}
        error={error}
      />
      <div className="verification-meta">
        <span className={expired ? 'timer timer--expired' : 'timer'} role="timer">
          남은 시간 {formatTimer(remainingSeconds)}
        </span>
        <Button
          size="small"
          variant="secondary"
          onClick={resendCode}
          disabled={requestState !== 'idle'}
        >
          {requestState === 'resending' ? '재전송 중...' : '인증번호 재전송'}
        </Button>
      </div>
      {expired && <p className="form-error" role="alert">인증시간이 만료되었습니다. 인증번호를 다시 요청해주세요.</p>}
      {verified ? (
        <div className="verification-complete" role="status">
          <p>✓ 인증이 완료되었습니다.</p>
        </div>
      ) : (
        <Button type="submit" size="large" disabled={requestState !== 'idle' || expired}>
          {requestState === 'verifying' ? '확인 중...' : '인증번호 확인'}
        </Button>
      )}
    </form>
  )
}

export default PhoneVerification

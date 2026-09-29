import { useState } from 'react'
import { Link } from 'react-router-dom'
import { authApi } from '../api/authApi'
import supportUpLogo from '../assets/support-up-logo.png'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import ErrorMessage from '../components/common/ErrorMessage'
import Input from '../components/common/Input'
import { ROUTES } from '../constants/routes'
import {
  getApiErrorMessage,
  isValidPhone,
} from '../utils/authUtils'

function normalizePhone(value) {
  return String(value ?? '').replace(/\D/g, '')
}

function FindIdPage() {
  const [phone, setPhone] = useState('')
  const [code, setCode] = useState('')
  const [step, setStep] = useState('phone')
  const [foundEmail, setFoundEmail] = useState('')
  const [requestState, setRequestState] = useState('idle')
  const [error, setError] = useState('')

  const sendCode = async (event) => {
    event?.preventDefault()
    if (!isValidPhone(phone)) {
      setError('올바른 휴대폰 번호를 입력해주세요.')
      return
    }

    setRequestState('sending')
    setError('')
    try {
      await authApi.sendFindEmailCode({ phone: normalizePhone(phone) })
      setCode('')
      setStep('verify')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '인증번호를 발송하지 못했습니다.'))
    } finally {
      setRequestState('idle')
    }
  }

  const verifyCode = async (event) => {
    event.preventDefault()
    if (!/^\d{6}$/.test(code)) {
      setError('6자리 인증번호를 입력해주세요.')
      return
    }

    setRequestState('verifying')
    setError('')
    try {
      const { data } = await authApi.verifyFindEmailCode({
        phone: normalizePhone(phone),
        code,
      })
      setFoundEmail(data.email)
      setStep('complete')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '인증번호를 확인하지 못했습니다.'))
    } finally {
      setRequestState('idle')
    }
  }

  return (
    <main className="auth-page">
      <div className="auth-page__shell">
        <Link className="brand auth-brand" to={ROUTES.LOGIN}><img className="brand__logo" src={supportUpLogo} alt="" />지원UP</Link>
        <Link className="back-link" to={ROUTES.LOGIN}>← 로그인으로 돌아가기</Link>
        <Card className="auth-card">
          {step === 'complete' ? (
            <div className="auth-result">
              <div className="auth-result__icon" aria-hidden="true">✓</div>
              <span className="eyebrow">아이디 찾기 완료</span>
              <h1>회원님의 아이디를 찾았습니다.</h1>
              <strong className="found-email">{foundEmail}</strong>
              <p>가입할 때 사용한 이메일입니다.</p>
              <div className="auth-actions auth-actions--stacked">
                <Link className="button button--primary button--large" to={ROUTES.LOGIN}>로그인하러 가기</Link>
                <Link className="button button--secondary button--large" to={ROUTES.FORGOT_PASSWORD}>비밀번호 찾기</Link>
              </div>
            </div>
          ) : (
            <form onSubmit={step === 'phone' ? sendCode : verifyCode} noValidate>
              <span className="eyebrow">계정 정보 확인</span>
              <h1>아이디 찾기</h1>
              <p className="auth-card__description">
                {step === 'phone'
                  ? '가입할 때 등록한 휴대폰 번호를 입력해주세요.'
                  : '문자로 받은 인증번호를 입력해주세요.'}
              </p>
              <div className="auth-fields">
                <Input
                  label="휴대폰 번호"
                  type="tel"
                  value={phone}
                  onChange={(event) => {
                    setPhone(event.target.value)
                    setError('')
                  }}
                  autoComplete="tel"
                  placeholder="010-1234-5678"
                  disabled={step === 'verify' || requestState !== 'idle'}
                />
                {step === 'verify' && (
                  <div className="find-id-verification-row">
                    <Input
                      label="인증번호"
                      inputMode="numeric"
                      maxLength={6}
                      value={code}
                      onChange={(event) => {
                        setCode(event.target.value.replace(/\D/g, ''))
                        setError('')
                      }}
                      autoComplete="one-time-code"
                      placeholder="인증번호 6자리"
                      disabled={requestState !== 'idle'}
                    />
                    <Button
                      type="button"
                      variant="secondary"
                      onClick={sendCode}
                      disabled={requestState !== 'idle'}
                    >
                      {requestState === 'sending' ? '재전송 중...' : '인증번호 재전송'}
                    </Button>
                  </div>
                )}
                {error && <ErrorMessage title={error} />}
              </div>
              <Button type="submit" size="large" disabled={requestState !== 'idle'}>
                {requestState === 'sending'
                  ? '인증번호 발송 중...'
                  : requestState === 'verifying'
                    ? '확인 중...'
                    : step === 'phone' ? '인증번호 받기' : '인증번호 확인'}
              </Button>
            </form>
          )}
        </Card>
      </div>
    </main>
  )
}

export default FindIdPage

import { useState } from 'react'
import { Link } from 'react-router-dom'
import { authApi } from '../api/authApi'
import supportUpLogo from '../assets/support-up-logo.png'
import PhoneVerification from '../components/auth/PhoneVerification'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import ErrorMessage from '../components/common/ErrorMessage'
import Input from '../components/common/Input'
import { ROUTES } from '../constants/routes'
import { getApiErrorMessage, getPasswordValidationError, isValidEmail, isValidPhone } from '../utils/authUtils'

const initialAccount = { email: '', phone: '' }
const initialPasswords = { password: '', passwordConfirm: '' }

function ForgotPasswordPage() {
  const [step, setStep] = useState('account')
  const [account, setAccount] = useState(initialAccount)
  const [passwords, setPasswords] = useState(initialPasswords)
  const [errors, setErrors] = useState({})
  const [requestState, setRequestState] = useState('idle')
  const [verificationCompleted, setVerificationCompleted] = useState(false)
  const [resetToken, setResetToken] = useState('')
  const [requestError, setRequestError] = useState('')

  const setAccountField = (field, value) => {
    setAccount((current) => ({ ...current, [field]: value }))
    setErrors((current) => ({ ...current, [field]: '' }))
    setRequestError('')
  }

  const setPasswordField = (field, value) => {
    setPasswords((current) => ({ ...current, [field]: value }))
    setErrors((current) => ({ ...current, [field]: '' }))
  }

  const sendVerification = async (event) => {
    event.preventDefault()
    const email = account.email.trim()
    const phone = account.phone.trim()
    const nextErrors = {}

    if (!email) nextErrors.email = '이메일을 입력해주세요.'
    else if (!isValidEmail(email)) nextErrors.email = '올바른 이메일 형식을 입력해주세요.'
    if (!phone) nextErrors.phone = '휴대폰 번호를 입력해주세요.'
    else if (!isValidPhone(phone)) nextErrors.phone = '올바른 휴대폰 번호를 입력해주세요.'

    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setRequestState('sending')
    setRequestError('')

    try {
      await authApi.sendPasswordResetCode({
        email,
        phone: phone.replace(/\D/g, ''),
      })
      setAccount({ email, phone: phone.replace(/\D/g, '') })
      setStep('verification')
    } catch (error) {
      setRequestError(getApiErrorMessage(error, '인증번호를 발송하지 못했습니다.'))
    } finally {
      setRequestState('idle')
    }
  }

  const openPasswordReset = (token) => {
    setResetToken(token)
    setVerificationCompleted(true)
    setStep('reset')
  }

  const resetPassword = async (event) => {
    event.preventDefault()

    if (!verificationCompleted) {
      setStep('account')
      return
    }

    const nextErrors = {}
    const passwordError = getPasswordValidationError(passwords.password)
    if (passwordError) nextErrors.password = passwordError
    if (!passwords.passwordConfirm) nextErrors.passwordConfirm = '비밀번호 확인을 입력해주세요.'
    else if (passwords.password !== passwords.passwordConfirm) nextErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.'

    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setRequestState('resetting')
    setRequestError('')

    try {
      await authApi.resetPassword({
        resetToken,
        newPassword: passwords.password,
        newPasswordConfirm: passwords.passwordConfirm,
      })
      setPasswords(initialPasswords)
      setStep('complete')
    } catch (error) {
      setRequestError(getApiErrorMessage(error, '비밀번호를 변경하지 못했습니다.'))
    } finally {
      setRequestState('idle')
    }
  }

  const stepNumber = step === 'account' ? 1 : step === 'verification' ? 2 : step === 'reset' ? 3 : 3

  return (
    <main className="auth-page">
      <div className="auth-page__shell">
        <Link className="brand auth-brand" to={ROUTES.LOGIN}><img className="brand__logo" src={supportUpLogo} alt="" />지원UP</Link>
        <Link className="back-link" to={ROUTES.LOGIN}>← 로그인으로 돌아가기</Link>
        <Card className="auth-card">
          <div className="auth-progress" aria-label={`비밀번호 재설정 ${stepNumber}/3 단계`}>
            <span>STEP {stepNumber} / 3</span>
            <div><i className={`auth-progress__fill auth-progress__fill--${stepNumber}`} /></div>
          </div>

          {step === 'complete' ? (
            <div className="auth-result">
              <div className="auth-result__icon" aria-hidden="true">✓</div>
              <span className="eyebrow">변경 완료</span>
              <h1>비밀번호가 변경되었습니다.</h1>
              <p>새 비밀번호로 로그인해주세요.</p>
              <Link className="button button--primary button--large" to={ROUTES.LOGIN}>로그인하러 가기</Link>
            </div>
          ) : step === 'verification' ? (
            <>
              <span className="eyebrow">본인 인증</span>
              <h1>인증번호 확인</h1>
              <p className="auth-card__description">{account.phone}로 전송된 인증번호를 입력해주세요.</p>
              <PhoneVerification email={account.email} phone={account.phone} onVerified={openPasswordReset} />
            </>
          ) : step === 'reset' && verificationCompleted ? (
            <form onSubmit={resetPassword} noValidate>
              <span className="eyebrow">새 비밀번호 설정</span>
              <h1>비밀번호 재설정</h1>
              <p className="auth-card__description">회원가입과 동일하게 8자 이상의 비밀번호를 사용해주세요.</p>
              <div className="auth-fields">
                <Input
                  label="새 비밀번호"
                  type="password"
                  autoComplete="new-password"
                  value={passwords.password}
                  onChange={(event) => setPasswordField('password', event.target.value)}
                  placeholder="8자 이상 입력해주세요"
                  error={errors.password}
                />
                <Input
                  label="새 비밀번호 확인"
                  type="password"
                  autoComplete="new-password"
                  value={passwords.passwordConfirm}
                  onChange={(event) => setPasswordField('passwordConfirm', event.target.value)}
                  placeholder="비밀번호를 다시 입력해주세요"
                  error={errors.passwordConfirm}
                />
              </div>
              {requestError && <ErrorMessage title={requestError} description="잠시 후 다시 시도해주세요." />}
              <Button type="submit" size="large" disabled={requestState !== 'idle'}>
                {requestState === 'resetting' ? '변경 중...' : '비밀번호 변경'}
              </Button>
            </form>
          ) : (
            <form onSubmit={sendVerification} noValidate>
              <span className="eyebrow">계정 확인</span>
              <h1>비밀번호 찾기</h1>
              <p className="auth-card__description">가입한 아이디와 휴대폰 번호를 입력해주세요.</p>
              <div className="auth-fields">
                <Input
                  label="아이디(이메일)"
                  type="email"
                  autoComplete="email"
                  value={account.email}
                  onChange={(event) => setAccountField('email', event.target.value)}
                  placeholder="example@email.com"
                  error={errors.email}
                />
                <Input
                  label="휴대폰 번호"
                  type="tel"
                  autoComplete="tel"
                  value={account.phone}
                  onChange={(event) => setAccountField('phone', event.target.value)}
                  placeholder="010-1234-5678"
                  error={errors.phone}
                />
              </div>
              {requestError && <ErrorMessage title={requestError} description="입력한 계정 정보를 확인해주세요." />}
              <Button type="submit" size="large" disabled={requestState !== 'idle'}>
                {requestState === 'sending' ? '인증번호 발송 중...' : '인증번호 받기'}
              </Button>
            </form>
          )}
        </Card>
      </div>
    </main>
  )
}

export default ForgotPasswordPage

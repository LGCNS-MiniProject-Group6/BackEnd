import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../api/authApi'
import supportUpLogo from '../assets/support-up-logo.png'
import BusinessCategoryField from '../components/business/BusinessCategoryField'
import Button from '../components/common/Button'
import Input from '../components/common/Input'
import SignupStepCard from '../components/signup/SignupStepCard'
import { BUSINESS_TYPES } from '../constants/businessCategories'
import { ROUTES } from '../constants/routes'
import { useAuth } from '../hooks/useAuth'
import {
  formatTimer,
  getApiErrorMessage,
  getPasswordValidationError,
  isValidEmail,
  isValidPhone,
} from '../utils/authUtils'
import { formatCurrency } from '../utils/formatUtils'

const initialForm = {
  email: '',
  password: '',
  passwordConfirm: '',
  name: '',
  phone: '',
  category: '',
  region: '',
  industry: '',
  openingDate: '',
  employeeCount: 0,
  annualRevenue: '',
  businessType: '',
}

// [추가] 휴대폰 인증 상태 초기값 / 인증 제한시간(초)
const initialVerification = { sent: false, code: '', verified: false }
const VERIFY_TIME_LIMIT = 180

const stepCopy = [
  ['이메일 입력', '회원가입에 사용할 이메일을 입력해주세요.'],
  ['비밀번호 설정', '영문, 숫자, 특수문자를 포함한 8자 이상의 비밀번호를 설정해주세요.'],
  ['이름 입력', '서비스 이용 시 표시될 이름을 입력해주세요.'],
  ['휴대폰 번호 인증', '휴대폰 번호를 입력하고 문자로 받은 인증번호를 확인해주세요.'],
  ['기본정보 입력 완료', '이제 사업정보를 입력하면 맞춤 공고와 AI 검수를 더 정확하게 이용할 수 있어요.'],
  ['사업장 지역 입력', '주 사업장이 위치한 시·도 및 시·군·구를 입력해주세요.'],
  ['업종 입력', '사업자등록증의 주업종 또는 분류 코드를 입력해주세요.'],
  ['개업일 입력', '사업자등록증에 기재된 개업연월일을 입력해주세요.'],
  ['상시근로자 수 입력', '대표자를 제외한 상시근로자 수를 입력해주세요.'],
  ['연 평균 매출액 입력', '직전 사업연도 기준 연 평균 매출액을 입력해주세요.'],
  ['사업자 유형 선택', '사업자등록증에 기재된 사업자 유형을 선택해주세요.'],
  ['입력정보 확인', '입력한 정보를 확인하고 가입을 완료해주세요.'],
]

function SignupPage() {
  const [step, setStep] = useState(0)
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState('')
  const [requestState, setRequestState] = useState('idle')
  // [추가] 휴대폰 인증 상태와 남은 시간
  const [verification, setVerification] = useState(initialVerification)
  const [timeLeft, setTimeLeft] = useState(0)
  const { register } = useAuth()
  const navigate = useNavigate()

  const setField = (field, value) => setForm((current) => ({ ...current, [field]: value }))
  const progressStep = step <= 3 ? step + 1 : step >= 5 && step <= 10 ? step : null

  // [추가] 인증번호 발송 후 1초마다 남은 시간 감소 (인증 완료 or 0초가 되면 멈춤)
  useEffect(() => {
    if (!verification.sent || verification.verified || timeLeft <= 0) return undefined
    const timerId = setTimeout(() => setTimeLeft((current) => current - 1), 1000)
    return () => clearTimeout(timerId)
  }, [verification.sent, verification.verified, timeLeft])

  // [추가] 번호가 바뀌면 인증을 처음부터 다시 받도록 초기화
  const handlePhoneChange = (value) => {
    setField('phone', value.replace(/\D/g, '').slice(0, 11)) // [수정] 숫자만, 최대 11자리
    setVerification(initialVerification)
    setTimeLeft(0)
  }

  // [추가] 인증번호 받기 / 재전송
  const sendVerificationCode = async () => {
    if (!isValidPhone(form.phone)) {
      setError('올바른 휴대폰 번호를 입력해주세요.')
      return
    }
    setError('')
    setRequestState('sending-code')
    try {
      await authApi.sendPhoneVerification(form.phone.replace(/\D/g, ''))
      setVerification({ sent: true, code: '', verified: false })
      setTimeLeft(VERIFY_TIME_LIMIT)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '인증번호 발송 중 문제가 발생했습니다.'))
    } finally {
      setRequestState('idle')
    }
  }

  // [추가] 인증번호 확인
  const verifyCode = async () => {
    if (timeLeft <= 0) {
      setError('인증 시간이 만료되었습니다. 인증번호를 다시 받아주세요.')
      return
    }
    if (!/^\d{6}$/.test(verification.code)) {
      setError('6자리 인증번호를 입력해주세요.')
      return
    }
    setError('')
    setRequestState('verifying-code')
    try {
      const { data } = await authApi.verifyPhoneVerification(
        form.phone.replace(/\D/g, ''),
        verification.code,
      )
      if (!data?.isVerified) {
        setError('인증번호가 일치하지 않습니다.')
        return
      }
      setVerification((current) => ({ ...current, verified: true }))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '인증번호 확인 중 문제가 발생했습니다.'))
    } finally {
      setRequestState('idle')
    }
  }

  const validateCurrentStep = () => {
    const requiredByStep = ['email', 'password', 'name', 'phone', null, 'region', 'industry', 'openingDate', 'employeeCount', 'annualRevenue', 'businessType']
    const field = requiredByStep[step]
    if (field && form[field] === '') return '필수 정보를 입력해주세요.'
    if (step === 0 && !isValidEmail(form.email)) return '올바른 이메일 형식을 입력해주세요.'
    if (step === 1) {
      const passwordError = getPasswordValidationError(form.password)
      if (passwordError) return passwordError
    }
    if (step === 1 && form.password !== form.passwordConfirm) return '비밀번호가 일치하지 않습니다.'
    if (step === 3 && !isValidPhone(form.phone)) return '올바른 휴대폰 번호를 입력해주세요.'
    // [추가] 인증을 마쳐야 다음 단계로 이동
    if (step === 3 && !verification.verified) return '휴대폰 인증을 완료해주세요.'
    return ''
  }

  const goNext = async () => {
    const validationError = validateCurrentStep()
    if (validationError) {
      setError(validationError)
      return
    }
    setError('')

    if (step === 0) {
      setRequestState('checking-email')
      try {
        const { data } = await authApi.checkEmail({ email: form.email.trim() })
        if (!data?.available) {
          setError('이미 가입된 이메일입니다.')
          return
        }
      } catch (requestError) {
        setError(getApiErrorMessage(
          requestError,
          '이메일 중복확인 중 문제가 발생했습니다.',
        ))
        return
      } finally {
        setRequestState('idle')
      }
    }

    setStep((current) => Math.min(current + 1, stepCopy.length - 1))
  }

  // [추가] 엔터 키로 다음 입력칸 / 다음 단계로 이동
  const handleEnter = (e) => {
    if (e.key !== 'Enter' || e.nativeEvent.isComposing) return // 한글 조합 중이면 무시
    if (e.target.tagName === 'BUTTON') return // 버튼 위에서는 원래 동작대로

    e.preventDefault()
    // [수정] 비활성화된 칸(인증 완료된 인증번호 칸)은 건너뜀
    const inputs = [...e.currentTarget.querySelectorAll('input:not(:disabled)')]
    const index = inputs.indexOf(e.target)

    // [추가] 휴대폰 단계: 번호 칸에서 엔터 → 인증번호 받기, 인증번호 칸에서 엔터 → 확인
    if (step === 3 && !verification.verified) {
      if (index === 0 && !verification.sent) {
        sendVerificationCode()
        return
      }
      if (index === 1) {
        verifyCode()
        return
      }
    }

    const nextInput = inputs[index + 1]
    if (nextInput) nextInput.focus() // 같은 단계에 다음 칸이 있으면 이동 (비밀번호 → 비밀번호 확인)
    else goNext() // 마지막 칸이면 다음 단계로
  }

  const finishSignup = async (includeBusiness = true) => {
    const business = includeBusiness
      ? {
          region: form.region,
          industry: form.industry,
          openingDate: form.openingDate,
          businessType: form.businessType,
          employeeCount: Number(form.employeeCount),
          annualRevenue: Number(form.annualRevenue),
        }
      : undefined

    setRequestState('signing-up')
    try {
      await register({
        account: {
          email: form.email.trim(),
          password: form.password,
          name: form.name.trim(),
          phone: form.phone.replace(/\D/g, ''),
        },
        business,
      })
      navigate(ROUTES.HOME, { replace: true })
    } catch (requestError) {
      if (requestError.accountCreated) {
        navigate(ROUTES.LOGIN, {
          replace: true,
          state: {
            signupSuccess: true,
            email: form.email.trim(),
            followUpError: '회원가입은 완료됐지만 로그인 또는 사업정보 저장을 완료하지 못했습니다. 로그인 후 다시 입력해주세요.',
          },
        })
        return
      }
      setError(getApiErrorMessage(
        requestError,
        '회원가입 중 문제가 발생했습니다. 입력 정보를 확인해주세요.',
      ))
    } finally {
      setRequestState('idle')
    }
  }

  // [추가] 휴대폰 번호 + 인증번호 입력 영역
  const renderPhoneVerification = () => {
    const rowStyle = { display: 'flex', gap: 8, alignItems: 'flex-end' }
    const codeHelperText = verification.verified
      ? '인증이 완료되었습니다.'
      : timeLeft > 0
        ? `남은 시간 ${formatTimer(timeLeft)}`
        : '인증 시간이 만료되었습니다. 인증번호를 다시 받아주세요.'

    return (
      <div className="stack">
        <div style={rowStyle}>
          <div style={{ flex: 1 }}>
            {/* [수정] 숫자만 입력, 최대 11자리, 하이픈 없는 예시 */}
            <Input label="휴대폰 번호" type="tel" inputMode="numeric" maxLength={11} value={form.phone} onChange={(e) => handlePhoneChange(e.target.value)} placeholder="01012345678" autoFocus />
          </div>
          <Button variant="secondary" onClick={sendVerificationCode} disabled={requestState !== 'idle' || verification.verified}>
            {requestState === 'sending-code' ? '발송 중...' : verification.sent ? '재전송' : '인증번호 받기'}
          </Button>
        </div>

        {verification.sent && (
          <div className="field">
            <div style={rowStyle}>
              <div style={{ flex: 1 }}>
                <Input
                  label="인증번호"
                  inputMode="numeric"
                  maxLength={6}
                  value={verification.code}
                  onChange={(e) => setVerification((current) => ({ ...current, code: e.target.value.replace(/\D/g, '') }))}
                  placeholder="6자리 숫자"
                  disabled={verification.verified}
                  autoFocus
                />
              </div>
              <Button variant="secondary" onClick={verifyCode} disabled={requestState !== 'idle' || verification.verified || timeLeft <= 0}>
                {requestState === 'verifying-code' ? '확인 중...' : verification.verified ? '인증 완료' : '확인'}
              </Button>
            </div>
            <p className="field__message" role="status">{codeHelperText}</p>
          </div>
        )}
      </div>
    )
  }

  const renderField = () => {
    if (step === 0) return <Input label="이메일" type="email" value={form.email} onChange={(e) => setField('email', e.target.value)} placeholder="example@email.com" autoFocus />
    if (step === 1) return <div className="stack"><Input label="비밀번호" type="password" value={form.password} onChange={(e) => setField('password', e.target.value)} placeholder="비밀번호를 입력해주세요" autoFocus /><Input label="비밀번호 확인" type="password" value={form.passwordConfirm} onChange={(e) => setField('passwordConfirm', e.target.value)} placeholder="비밀번호를 다시 입력해주세요" /></div>
    if (step === 2) return <Input label="이름" value={form.name} onChange={(e) => setField('name', e.target.value)} placeholder="홍길동" autoFocus />
    // [수정] 휴대폰 번호 단계에 인증번호 입력란 추가
    if (step === 3) return renderPhoneVerification()
    if (step === 5) return <Input label="주요 사업장 소재지" value={form.region} onChange={(e) => setField('region', e.target.value)} placeholder="서울특별시 강남구" autoFocus />
    if (step === 6) return <><Input label="주업종 또는 분류 코드" value={form.industry} onChange={(e) => setField('industry', e.target.value)} placeholder="온라인 소매업" autoFocus /><div className="signup-suggestions"><span>주요 추천 분야</span>{['제조업', '정보통신업', '도소매업', '전문 서비스업'].map((item) => <button type="button" key={item} onClick={() => setField('industry', item)}>{item}</button>)}</div><BusinessCategoryField selected={form.category} onSelect={(category) => setField('category', category)} disabled={requestState !== 'idle'} /></>
    if (step === 7) return <Input label="개업연월일" type="date" value={form.openingDate} onChange={(e) => setField('openingDate', e.target.value)} autoFocus />
    if (step === 8) return <div className="number-stepper"><Button variant="secondary" onClick={() => setField('employeeCount', Math.max(0, Number(form.employeeCount) - 1))}>−</Button><strong>{form.employeeCount}<small>명</small></strong><Button variant="secondary" onClick={() => setField('employeeCount', Number(form.employeeCount) + 1)}>＋</Button></div>
    if (step === 9) return <Input label="연 평균 매출액" type="number" min="0" value={form.annualRevenue} onChange={(e) => setField('annualRevenue', e.target.value)} helperText={form.annualRevenue ? `${formatCurrency(form.annualRevenue)}원` : '숫자로 입력해주세요.'} placeholder="120000000" autoFocus />
    if (step === 10) return (
      <div className="chip-group" aria-label="사업자 유형 선택">
        {BUSINESS_TYPES.map((option) => (
          <button
            type="button"
            key={option}
            className={form.businessType === option ? 'chip chip--active' : 'chip'}
            aria-pressed={form.businessType === option}
            onClick={() => setField('businessType', option)}
          >
            {option}
          </button>
        ))}
      </div>
    )
    return null
  }

  if (step === 4) {
    return (
      <main className="signup-page">
        <Link className="brand signup-brand" to={ROUTES.LOGIN}><img className="brand__logo" src={supportUpLogo} alt="" />지원UP</Link>
        <SignupStepCard title={stepCopy[step][0]} description={stepCopy[step][1]}>
          <div className="signup-complete-icon" aria-hidden="true">✓</div>
          {error && <p className="form-error" role="alert">{error}</p>}
          <div className="signup-actions signup-actions--stacked">
            <Button size="large" onClick={goNext} disabled={requestState !== 'idle'}>사업정보 입력 시작하기</Button>
            <Button variant="secondary" onClick={() => finishSignup(false)} disabled={requestState !== 'idle'}>
              {requestState === 'signing-up' ? '가입 처리 중...' : '나중에 작성하기'}
            </Button>
          </div>
        </SignupStepCard>
      </main>
    )
  }

  if (step === 11) {
    const summary = [
      ['이메일', form.email], ['이름', form.name], ['휴대폰', form.phone],
      ['사업장', form.region], ['업종', form.industry], ['관심 카테고리', form.category || '미선택'], ['개업일', form.openingDate],
      ['상시근로자', `${form.employeeCount}명`],
      ['연 매출', `${formatCurrency(form.annualRevenue)}원`],
      ['사업자 유형', form.businessType],
    ]
    return (
      <main className="signup-page">
        <Link className="brand signup-brand" to={ROUTES.LOGIN}><img className="brand__logo" src={supportUpLogo} alt="" />지원UP</Link>
        <SignupStepCard title={stepCopy[step][0]} description={stepCopy[step][1]}>
          <dl className="signup-summary">{summary.map(([label, value]) => <div key={label}><dt>{label}</dt><dd>{value}</dd></div>)}</dl>
          {error && <p className="form-error" role="alert">{error}</p>}
          <div className="signup-actions"><Button variant="secondary" onClick={() => setStep(10)} disabled={requestState !== 'idle'}>수정하기</Button><Button onClick={() => finishSignup(true)} disabled={requestState !== 'idle'}>{requestState === 'signing-up' ? '가입 처리 중...' : '가입 완료하고 시작하기'}</Button></div>
        </SignupStepCard>
      </main>
    )
  }

  return (
    <main className="signup-page">
      <Link className="brand signup-brand" to={ROUTES.LOGIN}><img className="brand__logo" src={supportUpLogo} alt="" />지원UP</Link>
      <SignupStepCard current={progressStep} total={10} title={stepCopy[step][0]} description={stepCopy[step][1]}>
        {/* [수정] onKeyDown={handleEnter} 추가 */}
        <div className="signup-card__body" onKeyDown={handleEnter}>{renderField()}{error && <p className="form-error" role="alert">{error}</p>}</div>
        <div className="signup-actions">
          <Button
            variant="secondary"
            disabled={requestState !== 'idle'}
            onClick={() =>
              step === 0
                ? navigate(ROUTES.LOGIN)
                : setStep((current) => Math.max(0, current - 1))
            }
          >
            {step === 0 ? '로그인으로' : '이전'}
          </Button>
          <Button onClick={goNext} disabled={requestState !== 'idle'}>
            {requestState === 'checking-email' ? '확인 중...' : '다음'}
          </Button>
        </div>
      </SignupStepCard>
    </main>
  )
}

export default SignupPage

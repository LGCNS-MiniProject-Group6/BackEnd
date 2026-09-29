import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { businessApi } from '../api/businessApi'
import BusinessCategoryField from '../components/business/BusinessCategoryField'
import AppLayout from '../components/common/AppLayout'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import Input from '../components/common/Input'
import { BUSINESS_TYPES } from '../constants/businessCategories'
import { ROUTES } from '../constants/routes'
import { useAuth } from '../hooks/useAuth'
import { getApiErrorMessage, isValidPhone } from '../utils/authUtils'

function createInitialForm(user, business) {
  return {
    name: user?.name || '',
    email: user?.email || '',
    phone: user?.phone || '',
    category: '',
    region: business?.region || '',
    industry: business?.industry || '',
    openingDate: business?.openingDate || '',
    businessType: business?.businessType || '',
    employeeCount: business?.employeeCount ?? 0,
    annualRevenue: business?.annualRevenue ?? '',
  }
}

function ProfileEditPage() {
  const { user, business, refreshSession, updateProfile } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState(() => createInitialForm(user, business))
  const [requestState, setRequestState] = useState('idle')
  const [error, setError] = useState('')

  const setField = (field, value) => {
    setForm((current) => ({ ...current, [field]: value }))
    setError('')
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (!form.name.trim()) {
      setError('이름을 입력해주세요.')
      return
    }
    if (!isValidPhone(form.phone)) {
      setError('올바른 휴대폰 번호를 입력해주세요.')
      return
    }

    const businessValues = [form.region, form.industry, form.openingDate, form.businessType, form.annualRevenue]
    const hasBusinessInput = businessValues.some((value) => String(value).trim() !== '')
    if (hasBusinessInput && businessValues.some((value) => String(value).trim() === '')) {
      setError('사업정보를 저장하려면 지역, 업종, 개업일, 사업자 유형, 연 매출을 모두 입력해주세요.')
      return
    }

    setRequestState('saving')
    setError('')
    try {
      await updateProfile({
        name: form.name.trim(),
        phone: form.phone.replace(/\D/g, ''),
      })

      if (hasBusinessInput) {
        const businessPayload = {
          industry: form.industry.trim(),
          region: form.region.trim(),
          openingDate: form.openingDate,
          businessType: form.businessType,
          employeeCount: Number(form.employeeCount),
          annualRevenue: Number(form.annualRevenue),
        }
        if (business) await businessApi.updateBusinessInfo(businessPayload)
        else await businessApi.createBusinessInfo(businessPayload)
        await refreshSession()
      }

      navigate(ROUTES.MY_PAGE)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '정보를 저장하지 못했습니다.'))
    } finally {
      setRequestState('idle')
    }
  }

  return (
    <AppLayout>
      <button className="back-link" type="button" onClick={() => navigate(-1)}>← 마이페이지로</button>
      <header className="page-heading">
        <span className="eyebrow">계정 및 사업정보</span>
        <h1>프로필 수정</h1>
        <p>맞춤 공고와 AI 검수 정확도를 위해 최신 정보를 유지해주세요.</p>
      </header>

      <form onSubmit={handleSubmit}>
        <Card className="profile-edit-card">
          <h2>기본정보</h2>
          <div className="form-grid">
            <Input label="이름" value={form.name} onChange={(event) => setField('name', event.target.value)} disabled={requestState !== 'idle'} />
            <Input label="이메일" type="email" value={form.email} disabled helperText="이메일은 변경할 수 없습니다." />
            <Input label="휴대폰번호" value={form.phone} onChange={(event) => setField('phone', event.target.value)} disabled={requestState !== 'idle'} />
          </div>
        </Card>

        <Card className="profile-edit-card">
          <h2>사업정보</h2>
          {!business && <p className="field__message">사업정보를 모두 입력하면 새로 등록됩니다.</p>}
          <div className="form-grid">
            <Input label="사업장 지역" value={form.region} onChange={(event) => setField('region', event.target.value)} disabled={requestState !== 'idle'} />
            <Input label="업종" value={form.industry} onChange={(event) => setField('industry', event.target.value)} disabled={requestState !== 'idle'} />
            <Input label="개업일" type="date" value={form.openingDate} onChange={(event) => setField('openingDate', event.target.value)} disabled={requestState !== 'idle'} />
            <Input label="상시근로자 수" type="number" min="0" value={form.employeeCount} onChange={(event) => setField('employeeCount', event.target.value)} disabled={requestState !== 'idle'} />
            <Input label="연 매출" type="number" min="0" value={form.annualRevenue} onChange={(event) => setField('annualRevenue', event.target.value)} disabled={requestState !== 'idle'} />
          </div>
          <div className="field">
            <strong>사업자 유형</strong>
            <div className="chip-group" aria-label="사업자 유형 선택">
              {BUSINESS_TYPES.map((option) => (
                <button
                  type="button"
                  key={option}
                  className={form.businessType === option ? 'chip chip--active' : 'chip'}
                  aria-pressed={form.businessType === option}
                  disabled={requestState !== 'idle'}
                  onClick={() => setField('businessType', option)}
                >
                  {option}
                </button>
              ))}
            </div>
          </div>
          <BusinessCategoryField selected={form.category} onSelect={(category) => setField('category', category)} disabled={requestState !== 'idle'} />
        </Card>

        {error && <p className="form-error" role="alert">{error}</p>}
        <div className="form-actions">
          <Button variant="secondary" onClick={() => navigate(-1)} disabled={requestState !== 'idle'}>취소</Button>
          <Button type="submit" disabled={requestState !== 'idle'}>
            {requestState === 'saving' ? '저장 중...' : '수정 완료'}
          </Button>
        </div>
      </form>

    </AppLayout>
  )
}

export default ProfileEditPage

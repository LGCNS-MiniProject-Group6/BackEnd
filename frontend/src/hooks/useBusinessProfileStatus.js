import { useCallback, useEffect, useState } from 'react'
import { businessApi } from '../api/businessApi'
import { isBusinessProfileComplete } from '../utils/businessUtils'

export function useBusinessProfileStatus() {
  const [requestVersion, setRequestVersion] = useState(0)
  const [profileState, setProfileState] = useState('loading')
  const [business, setBusiness] = useState(null)

  useEffect(() => {
    let active = true

    businessApi.getBusinessInfo()
      .then(({ data }) => {
        if (!active) return
        setBusiness(data)
        setProfileState(isBusinessProfileComplete(data) ? 'complete' : 'incomplete')
      })
      .catch((error) => {
        if (!active) return
        setBusiness(null)
        setProfileState(error.response?.status === 404 ? 'incomplete' : 'error')
      })

    return () => {
      active = false
    }
  }, [requestVersion])

  const refresh = useCallback(() => {
    setProfileState('loading')
    setRequestVersion((current) => current + 1)
  }, [])

  return {
    business,
    profileState,
    profileComplete: profileState === 'complete',
    refresh,
  }
}

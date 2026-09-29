import { useCallback, useEffect, useMemo, useState } from 'react'
import { authApi } from '../api/authApi'
import { businessApi } from '../api/businessApi'
import { readStoredAuth, writeStoredAuth } from '../utils/authStorage'
import { AuthContext } from './authContext'

function createTokenAuth(tokens) {
  if (!tokens?.accessToken || !tokens?.refreshToken) {
    throw new Error('로그인 응답에 인증 토큰이 없습니다.')
  }

  return {
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    tokenType: tokens.tokenType || 'Bearer',
    accessTokenExpiresIn: tokens.accessTokenExpiresIn,
    refreshTokenExpiresIn: tokens.refreshTokenExpiresIn,
  }
}

async function getBusinessOrNull() {
  try {
    const { data } = await businessApi.getBusinessInfo()
    return data
  } catch (error) {
    if (error.response?.status === 404) return null
    throw error
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const storedAuth = readStoredAuth()
    if (storedAuth?.accessToken?.startsWith('mock-')) {
      writeStoredAuth(null)
      return null
    }
    return storedAuth
  })

  const saveAuth = useCallback((nextAuth) => {
    setAuth(nextAuth)
    writeStoredAuth(nextAuth)
  }, [])

  const loadSession = useCallback(async (tokenAuth = readStoredAuth()) => {
    if (!tokenAuth?.accessToken) throw new Error('로그인 정보가 없습니다.')

    const [{ data: profile }, business] = await Promise.all([
      authApi.getMyInfo(),
      getBusinessOrNull(),
    ])
    const nextAuth = {
      ...tokenAuth,
      business,
      user: { ...profile, business },
    }
    saveAuth(nextAuth)
    return nextAuth
  }, [saveAuth])

  const login = useCallback(async ({ email, password }) => {
    const { data: tokens } = await authApi.login({ email, password })
    const tokenAuth = createTokenAuth(tokens)

    // 뒤따르는 Profile/Business 요청에서 Access Token을 사용할 수 있게 먼저 저장합니다.
    writeStoredAuth(tokenAuth)
    try {
      return await loadSession(tokenAuth)
    } catch (error) {
      saveAuth(null)
      throw error
    }
  }, [loadSession, saveAuth])

  const register = useCallback(async ({ account, business }) => {
    let accountCreated = false
    try {
      const { data: signupResult } = await authApi.signup(account)
      accountCreated = true

      const { data: tokens } = await authApi.login({
        email: account.email,
        password: account.password,
      })
      const tokenAuth = createTokenAuth(tokens)
      writeStoredAuth(tokenAuth)

      if (business) await businessApi.createBusinessInfo(business)
      const session = await loadSession(tokenAuth)
      return { signupResult, session }
    } catch (error) {
      saveAuth(null)
      if (accountCreated) error.accountCreated = true
      throw error
    }
  }, [loadSession, saveAuth])

  const updateProfile = useCallback(async (payload) => {
    await authApi.updateMyInfo(payload)
    return loadSession()
  }, [loadSession])

  const refreshSession = useCallback(() => loadSession(), [loadSession])

  const logout = useCallback(async () => {
    try {
      if (auth?.refreshToken) {
        await authApi.logout({ refreshToken: auth.refreshToken })
      }
    } finally {
      saveAuth(null)
    }
  }, [auth?.refreshToken, saveAuth])

  useEffect(() => {
    const syncTokens = (event) => setAuth(event.detail)
    const expireSession = () => setAuth(null)
    window.addEventListener('auth:tokens-updated', syncTokens)
    window.addEventListener('auth:session-expired', expireSession)
    return () => {
      window.removeEventListener('auth:tokens-updated', syncTokens)
      window.removeEventListener('auth:session-expired', expireSession)
    }
  }, [])

  const value = useMemo(
    () => ({
      isAuthenticated: Boolean(auth?.accessToken),
      user: auth?.user ?? null,
      business: auth?.business ?? auth?.user?.business ?? null,
      login,
      register,
      logout,
      refreshSession,
      updateProfile,
    }),
    [auth, login, logout, refreshSession, register, updateProfile],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

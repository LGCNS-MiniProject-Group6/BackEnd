export const AUTH_STORAGE_KEY = 'support-check-auth'

export function readStoredAuth() {
  try {
    return JSON.parse(localStorage.getItem(AUTH_STORAGE_KEY)) ?? null
  } catch {
    localStorage.removeItem(AUTH_STORAGE_KEY)
    return null
  }
}

export function writeStoredAuth(auth) {
  if (auth) localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth))
  else localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function updateStoredTokens(tokens) {
  const current = readStoredAuth()
  const next = {
    ...(current ?? {}),
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    tokenType: tokens.tokenType || current?.tokenType || 'Bearer',
    accessTokenExpiresIn: tokens.accessTokenExpiresIn ?? current?.accessTokenExpiresIn,
    refreshTokenExpiresIn: tokens.refreshTokenExpiresIn ?? current?.refreshTokenExpiresIn,
  }
  writeStoredAuth(next)
  return next
}

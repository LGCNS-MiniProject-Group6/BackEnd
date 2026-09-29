import axios from 'axios'
import {
  readStoredAuth,
  updateStoredTokens,
  writeStoredAuth,
} from '../utils/authStorage'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000/api';
console.log('test API_ BASE_URL : ' + API_BASE_URL);
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

const refreshClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' },
})

const REFRESH_EXCLUDED_PATHS = [
  '/auth/login',
  '/auth/reissue',
  '/auth/signup',
  '/auth/check-email',
  '/auth/phone-verification/',
  '/auth/find-email/',
  '/auth/password-reset',
  '/auth/logout',
]

let refreshPromise = null

axiosInstance.interceptors.request.use((config) => {
  const auth = readStoredAuth()
  if (auth?.accessToken) config.headers.Authorization = `Bearer ${auth.accessToken}`
  return config
})

axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config
    const requestPath = String(originalRequest?.url ?? '')
    const isRefreshExcluded = REFRESH_EXCLUDED_PATHS.some((path) => requestPath.startsWith(path))

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      isRefreshExcluded
    ) {
      return Promise.reject(error)
    }

    const storedAuth = readStoredAuth()
    if (!storedAuth?.refreshToken) return Promise.reject(error)

    originalRequest._retry = true

    try {
      if (!refreshPromise) {
        refreshPromise = refreshClient
          .post('/auth/reissue', { refreshToken: storedAuth.refreshToken })
          .then(({ data }) => {
            const nextAuth = updateStoredTokens(data)
            window.dispatchEvent(new CustomEvent('auth:tokens-updated', { detail: nextAuth }))
            return data.accessToken
          })
          .finally(() => {
            refreshPromise = null
          })
      }

      const accessToken = await refreshPromise
      originalRequest.headers.Authorization = `Bearer ${accessToken}`
      return axiosInstance(originalRequest)
    } catch (refreshError) {
      writeStoredAuth(null)
      window.dispatchEvent(new Event('auth:session-expired'))
      return Promise.reject(refreshError)
    }
  },
)

export default axiosInstance

import axiosInstance from './axiosInstance'

export const authApi = {
  signup: (payload) => axiosInstance.post('/auth/signup', payload),
  checkEmail: (params) => axiosInstance.get('/auth/check-email', { params }),
  sendPhoneVerification: (phoneNumber) =>
    axiosInstance.post('/auth/phone-verification/send', null, {
      params: { phoneNumber },
    }),
  verifyPhoneVerification: (phoneNumber, code) =>
    axiosInstance.post('/auth/phone-verification/verify', null, {
      params: { phoneNumber, code },
    }),
  login: (payload) => axiosInstance.post('/auth/login', payload),
  logout: (payload) => axiosInstance.post('/auth/logout', payload),
  reissue: (payload) => axiosInstance.post('/auth/reissue', payload),
  sendFindEmailCode: (payload) => axiosInstance.post('/auth/find-email/send-code', payload),
  verifyFindEmailCode: (payload) => axiosInstance.post('/auth/find-email/verify', payload),
  sendPasswordResetCode: (payload) => axiosInstance.post('/auth/password-reset/send-code', payload),
  verifyPasswordResetCode: (payload) => axiosInstance.post('/auth/password-reset/verify', payload),
  resetPassword: (payload) => axiosInstance.post('/auth/password-reset', payload),
  getMyInfo: () => axiosInstance.get('/auth/users/me'),
  updateMyInfo: (payload) => axiosInstance.put('/auth/users/me', payload),
}

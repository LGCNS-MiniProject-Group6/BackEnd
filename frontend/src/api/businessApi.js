import axiosInstance from './axiosInstance'

export const businessApi = {
  getBusinessInfo: () => axiosInstance.get('/users/me/business-info'),
  createBusinessInfo: (payload) =>
    axiosInstance.post('/users/me/business-info', payload),
  updateBusinessInfo: (payload) =>
    axiosInstance.put('/users/me/business-info', payload),
  deleteBusinessInfo: () => axiosInstance.delete('/users/me/business-info'),
}

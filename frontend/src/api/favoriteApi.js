import axiosInstance from './axiosInstance'

export const favoriteApi = {
  getFavorites: () => axiosInstance.get('/favorites'),
  addFavorite: (pblancId) => axiosInstance.post(`/favorites/${pblancId}`),
  removeFavorite: (pblancId) => axiosInstance.delete(`/favorites/${pblancId}`),
}

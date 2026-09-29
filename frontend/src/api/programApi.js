import axiosInstance from './axiosInstance'

export const programApi = {
  getPrograms: (params, config = {}) => axiosInstance.get('/programs', { params, ...config }),
  getProgramDetail: (pblancId) => axiosInstance.get(`/programs/${pblancId}`),
  // 카테고리 후보가 많을 때는 AI가 전체 목록을 한 번에 비교하므로 응답까지 시간이 걸릴 수 있습니다.
  getRecommendations: (params, config = {}) =>
    axiosInstance.get('/programs/recommendations', { params, timeout: 90000, ...config }),
}

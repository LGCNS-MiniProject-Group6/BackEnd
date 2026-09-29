import axiosInstance from './axiosInstance'

export const reviewApi = {
  // 원문이 긴 공고는 AI 응답까지 시간이 걸릴 수 있어 기본 타임아웃보다 넉넉하게 잡습니다.
  createReview: (pblancId) =>
    axiosInstance.post(`/reviews/${pblancId}`, undefined, { timeout: 120000 }),
  getReviews: (params) => axiosInstance.get('/reviews', { params }),
  getReviewDetail: (reviewId) => axiosInstance.get(`/reviews/${reviewId}`),
}

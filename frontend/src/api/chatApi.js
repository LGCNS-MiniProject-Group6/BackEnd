import axiosInstance from './axiosInstance'

export const chatApi = {
  // OpenAI 응답 대기 시간이 있어 기본 타임아웃보다 넉넉하게 잡습니다.
  sendChatMessage: (pblancId, payload) =>
    axiosInstance.post(`/programs/${pblancId}/chat`, payload, { timeout: 45000 }),
}

import { useState } from 'react'
import Button from '../common/Button'
import ChatMessage from './ChatMessage'
import { chatApi } from '../../api/chatApi'
import { getApiErrorMessage } from '../../utils/authUtils'

const suggestedQuestions = [
  '왜 추가 확인이 필요해?',
  '필요한 서류는?',
  '신청방법 알려줘',
  '공고 쉽게 설명해줘',
  '자부담이 있나요?',
]

const INITIAL_MESSAGE = {
  role: 'assistant',
  text: '안녕하세요. 이 공고와 관련해 궁금한 점을 물어보세요.',
}

function ChatPanel({ pblancId, programTitle }) {
  const [input, setInput] = useState('')
  const [messages, setMessages] = useState([INITIAL_MESSAGE])
  const [sending, setSending] = useState(false)

  const sendMessage = async (text) => {
    const normalized = text.trim()
    if (!normalized || sending || !pblancId) return

    // 백엔드에 보낼 이전 대화 이력은 방금 보낼 질문을 추가하기 전 상태 기준입니다.
    const history = messages
      .slice(-10)
      .map((message) => ({ role: message.role, content: message.text }))

    setMessages((current) => [...current, { role: 'user', text: normalized }])
    setInput('')
    setSending(true)

    try {
      const { data } = await chatApi.sendChatMessage(pblancId, { message: normalized, history })
      setMessages((current) => [...current, { role: 'assistant', text: data.answer }])
    } catch (error) {
      setMessages((current) => [
        ...current,
        { role: 'assistant', text: getApiErrorMessage(error, '답변을 가져오지 못했습니다. 잠시 후 다시 시도해주세요.') },
      ])
    } finally {
      setSending(false)
    }
  }

  return (
    <aside className="chat-panel" aria-label="공고 AI 도우미">
      <div className="chat-panel__header">
        <div><span className="eyebrow">실시간</span><h2>공고 AI 도우미</h2></div>
        <p>{programTitle}</p>
        <small>공고문과 사업정보를 기준으로 답변합니다.</small>
      </div>
      <div className="chat-panel__suggestions">
        <strong>추천 질문</strong>
        <div>
          {suggestedQuestions.map((question) => (
            <button type="button" key={question} onClick={() => sendMessage(question)} disabled={sending}>
              {question}
            </button>
          ))}
        </div>
      </div>
      <div className="chat-panel__messages" aria-live="polite">
        {messages.map((message, index) => (
          <ChatMessage key={`${message.role}-${index}`} role={message.role}>{message.text}</ChatMessage>
        ))}
        {sending && <ChatMessage role="assistant">답변을 작성하고 있어요...</ChatMessage>}
      </div>
      <form
        className="chat-panel__composer"
        onSubmit={(event) => {
          event.preventDefault()
          sendMessage(input)
        }}
      >
        <label className="sr-only" htmlFor="chat-input">질문 입력</label>
        <input
          id="chat-input"
          value={input}
          onChange={(event) => setInput(event.target.value)}
          placeholder="질문을 입력하세요"
          disabled={sending}
        />
        <Button type="submit" aria-label="질문 보내기" disabled={sending}>↑</Button>
      </form>
    </aside>
  )
}

export default ChatPanel

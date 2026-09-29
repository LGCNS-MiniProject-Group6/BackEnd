function ChatMessage({ role, children }) {
  return (
    <div className={`chat-message chat-message--${role}`}>
      {role === 'assistant' && <span className="chat-message__avatar" aria-hidden="true">AI</span>}
      <p>{children}</p>
    </div>
  )
}

export default ChatMessage

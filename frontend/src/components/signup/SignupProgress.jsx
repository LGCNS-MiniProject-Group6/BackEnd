function SignupProgress({ current, total }) {
  const progress = Math.min(100, (current / total) * 100)

  return (
    <div className="signup-progress" aria-label={`가입 진행 ${current}/${total}`}>
      <div className="signup-progress__meta">
        <span>가입 정보 입력</span>
        <strong>{current} / {total}</strong>
      </div>
      <div className="signup-progress__track">
        <span style={{ width: `${progress}%` }} />
      </div>
    </div>
  )
}

export default SignupProgress

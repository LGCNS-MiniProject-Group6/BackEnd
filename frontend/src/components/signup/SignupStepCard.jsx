import Card from '../common/Card'
import SignupProgress from './SignupProgress'

function SignupStepCard({ current, total, title, description, children }) {
  return (
    <Card className="signup-card">
      {current && <SignupProgress current={current} total={total} />}
      <div className="signup-card__heading">
        <h1>{title}</h1>
        {description && <p>{description}</p>}
      </div>
      {children}
    </Card>
  )
}

export default SignupStepCard

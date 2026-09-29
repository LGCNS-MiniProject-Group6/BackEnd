function Card({ children, className = '', as: Element = 'section' }) {
  return <Element className={`card ${className}`.trim()}>{children}</Element>
}

export default Card

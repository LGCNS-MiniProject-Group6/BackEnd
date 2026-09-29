import { useEffect, useRef, useState } from 'react'

function Reveal({
  as: Element = 'div',
  children,
  className = '',
  variant = 'up',
  delay = 0,
}) {
  const elementRef = useRef(null)
  const [isVisible, setIsVisible] = useState(false)

  useEffect(() => {
    const element = elementRef.current

    if (!element || !('IntersectionObserver' in window)) {
      setIsVisible(true)
      return undefined
    }

    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true)
          observer.unobserve(entry.target)
        }
      },
      { threshold: 0.14, rootMargin: '0px 0px -48px' },
    )

    observer.observe(element)
    return () => observer.disconnect()
  }, [])

  return (
    <Element
      ref={elementRef}
      className={`reveal reveal--${variant} reveal--delay-${delay} ${isVisible ? 'is-visible' : ''} ${className}`.trim()}
    >
      {children}
    </Element>
  )
}

export default Reveal

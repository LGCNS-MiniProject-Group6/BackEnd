function Button({
  children,
  variant = 'primary',
  size = 'medium',
  className = '',
  ...props
}) {
  return (
    <button
      type="button"
      className={`button button--${variant} button--${size} ${className}`.trim()}
      {...props}
    >
      {children}
    </button>
  )
}

export default Button

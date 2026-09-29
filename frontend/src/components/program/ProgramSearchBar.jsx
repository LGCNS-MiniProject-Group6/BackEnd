import Button from '../common/Button'

function ProgramSearchBar({ value, onChange, onSubmit, placeholder }) {
  return (
    <form className="search-bar" onSubmit={onSubmit} role="search">
      <label className="sr-only" htmlFor="program-search">지원사업 검색</label>
      <span className="search-bar__icon" aria-hidden="true">⌕</span>
      <input
        id="program-search"
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder || '지원사업명이나 키워드를 검색해보세요'}
      />
      <Button type="submit">검색</Button>
    </form>
  )
}

export default ProgramSearchBar

import { BUSINESS_CATEGORIES } from '../../constants/businessCategories'

function BusinessCategoryField({ selected, onSelect, disabled = false }) {
  return (
    <div className="business-category-field">
      <strong>관심 카테고리</strong>
      <div className="chip-group" aria-label="관심 카테고리 목록">
        {BUSINESS_CATEGORIES.map((category) => (
          <button
            className={selected === category ? 'chip chip--active' : 'chip'}
            type="button"
            key={category}
            aria-pressed={selected === category}
            onClick={() => onSelect(category)}
            disabled={disabled}
          >
            {category}
          </button>
        ))}
      </div>
      <p className="field__message">
        현재 Backend에 저장 필드가 없어 선택값은 서버에 저장되지 않습니다.
      </p>
    </div>
  )
}

export default BusinessCategoryField

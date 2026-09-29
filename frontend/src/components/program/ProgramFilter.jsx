import { PROGRAM_CATEGORIES } from '../../constants/programCategories'

function ProgramFilter({ selected, onSelect, compact = false }) {
  return (
    <div className={compact ? 'chip-group chip-group--compact' : 'chip-group'}>
      {PROGRAM_CATEGORIES.map((filter) => (
        <button
          type="button"
          key={filter}
          className={selected === filter ? 'chip chip--active' : 'chip'}
          onClick={() => onSelect(filter)}
        >
          {filter}
        </button>
      ))}
    </div>
  )
}

export default ProgramFilter

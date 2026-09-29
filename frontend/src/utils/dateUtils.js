const DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000

function parseDate(value) {
  if (!value) return null

  if (typeof value === 'string') {
    const dateOnly = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value)

    if (dateOnly) {
      const [, year, month, day] = dateOnly.map(Number)
      const timestamp = Date.UTC(year, month - 1, day)
      const parsed = new Date(timestamp)

      if (
        parsed.getUTCFullYear() !== year ||
        parsed.getUTCMonth() !== month - 1 ||
        parsed.getUTCDate() !== day
      ) {
        return null
      }

      return parsed
    }
  }

  const parsed = value instanceof Date ? new Date(value) : new Date(value)
  return Number.isNaN(parsed.getTime()) ? null : parsed
}

function formatDatePart(date) {
  const year = date.getUTCFullYear()
  const month = String(date.getUTCMonth() + 1).padStart(2, '0')
  const day = String(date.getUTCDate()).padStart(2, '0')
  return `${year}.${month}.${day}`
}

export function getDateTimestamp(value) {
  return parseDate(value)?.getTime() ?? null
}

export function formatDate(value) {
  if (!value) return '상시 접수'
  const parsed = parseDate(value)
  return parsed ? formatDatePart(parsed) : '날짜 정보 없음'
}

export function formatPeriod(start, end, rawApplyPeriod) {
  const rawPeriod = String(rawApplyPeriod ?? '').trim()

  if (!start && !end) return rawPeriod || '상시 접수'

  const parsedStart = parseDate(start)
  const parsedEnd = parseDate(end)

  if ((start && !parsedStart) || (end && !parsedEnd)) {
    return rawPeriod || '신청기간 정보 없음'
  }

  if (parsedStart && parsedEnd) {
    return `${formatDatePart(parsedStart)} ~ ${formatDatePart(parsedEnd)}`
  }

  if (parsedStart) return `${formatDatePart(parsedStart)} ~ 상시 모집`
  return `~ ${formatDatePart(parsedEnd)}`
}

export function getDday(end, today = new Date()) {
  if (!end) return '상시모집'

  const parsedEnd = parseDate(end)
  const parsedToday = parseDate(today)

  if (!parsedEnd || !parsedToday) return '마감일 미정'

  const todayTimestamp = Date.UTC(
    parsedToday.getFullYear(),
    parsedToday.getMonth(),
    parsedToday.getDate(),
  )
  const endTimestamp = Date.UTC(
    parsedEnd.getUTCFullYear(),
    parsedEnd.getUTCMonth(),
    parsedEnd.getUTCDate(),
  )
  const remainingDays = Math.round(
    (endTimestamp - todayTimestamp) / DAY_IN_MILLISECONDS,
  )

  if (remainingDays < 0) return '마감'
  if (remainingDays === 0) return 'D-Day'
  return `D-${remainingDays}`
}

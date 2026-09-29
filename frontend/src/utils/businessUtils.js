export function isBusinessProfileComplete(business) {
  if (!business) return false

  const requiredTextFields = [
    business.industry,
    business.region,
    business.openingDate,
  ]
  const hasRequiredText = requiredTextFields.every(
    (value) => value !== null && value !== undefined && String(value).trim() !== '',
  )
  const hasNonNegativeNumber = (value) => (
    value !== null &&
    value !== undefined &&
    value !== '' &&
    Number.isFinite(Number(value)) &&
    Number(value) >= 0
  )

  return (
    hasRequiredText &&
    hasNonNegativeNumber(business.employeeCount) &&
    hasNonNegativeNumber(business.annualRevenue)
  )
}

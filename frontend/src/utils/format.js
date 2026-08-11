// Pinned to en-US rather than the viewer's browser locale, so every HR Manager sees the same
// number formatting regardless of OS/browser settings - and so this stays deterministic in tests.
const LOCALE = "en-US";

export function formatMoney(amount, currencyCode) {
  if (amount == null || !currencyCode) return "—";
  try {
    return new Intl.NumberFormat(LOCALE, { style: "currency", currency: currencyCode }).format(amount);
  } catch {
    return `${amount} ${currencyCode}`;
  }
}

export function formatUsd(amount) {
  if (amount == null) return "—";
  return new Intl.NumberFormat(LOCALE, { style: "currency", currency: "USD", maximumFractionDigits: 0 }).format(
    amount,
  );
}

export function formatCompactUsd(amount) {
  if (amount == null) return "—";
  return new Intl.NumberFormat(LOCALE, {
    style: "currency",
    currency: "USD",
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(amount);
}

export function formatCompactNumber(value) {
  if (value == null) return "—";
  return new Intl.NumberFormat(LOCALE, { notation: "compact", maximumFractionDigits: 1 }).format(value);
}

export function formatDate(isoDate) {
  if (!isoDate) return "—";
  return new Date(isoDate).toLocaleDateString(LOCALE, { year: "numeric", month: "short", day: "numeric" });
}

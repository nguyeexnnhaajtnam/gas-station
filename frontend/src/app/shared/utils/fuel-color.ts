// Fixed fuel colors used on every screen: gasoline = text color, diesel = neutral-400.
function normalize(value: string | null | undefined) {
  return (value ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/gi, 'd').toUpperCase();
}

export function isDiesel(fuel: string | null | undefined) {
  const n = normalize(fuel);
  return /(^|[^A-Z])DO([^A-Z]|$)/.test(n) || n.includes('DAU') || n.includes('DIESEL');
}

export function fuelColor(fuel: string | null | undefined) {
  if (!normalize(fuel).trim()) return 'var(--color-neutral-300)';
  return isDiesel(fuel) ? 'var(--color-neutral-400)' : 'var(--color-text)';
}

/** Key used to match the same fuel across sources ("DO 0,05S-II" ≈ "DO 0,05S"). */
export function fuelKey(fuel: string | null | undefined) {
  return normalize(fuel).replace(/-I+$/, '').replace(/[^A-Z0-9]/g, '');
}

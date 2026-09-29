const LEGACY_PREFIX = 'CỬA HÀNG XĂNG DẦU ';

/** Display-only short name: strip the legacy prefix and title-case the rest. */
export function stationShortName(name: string | undefined | null): string {
  if (!name) return '';
  const stripped = name.toUpperCase().startsWith(LEGACY_PREFIX) ? name.slice(LEGACY_PREFIX.length) : name;
  return stripped
    .toLowerCase()
    .split(' ')
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}

export function initials(name: string | undefined | null, fallback = ''): string {
  return name?.trim().charAt(0).toUpperCase() || fallback;
}

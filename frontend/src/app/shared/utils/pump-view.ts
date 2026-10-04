import { PumpRealtime } from '../../core/api.models';
import { fuelColor } from './fuel-color';

/** Nozzle state derived from backend statuses — the raw strings are never shown. */
export type NozzleState = 'PUMPING' | 'IDLE' | 'OFFLINE' | 'UNKNOWN';

export const NOZZLE_STATES: NozzleState[] = ['PUMPING', 'IDLE', 'OFFLINE', 'UNKNOWN'];

export const NOZZLE_LABELS: Record<NozzleState, { label: string; color: string }> = {
  PUMPING: { label: 'Đang bơm', color: 'var(--color-success)' },
  IDLE: { label: 'Đã gác vòi', color: 'var(--color-text)' },
  OFFLINE: { label: 'Không kết nối', color: 'var(--color-accent-700)' },
  UNKNOWN: { label: 'Không xác định', color: 'var(--color-neutral-600)' },
};

/** Labels for the status filter chips / legend. */
export const STATE_FILTER_LABELS: Record<NozzleState, { label: string; color: string }> = {
  PUMPING: { label: 'Đang bơm', color: 'var(--color-success)' },
  IDLE: { label: 'Đã gác vòi', color: 'var(--color-neutral-500)' },
  OFFLINE: { label: 'Offline', color: 'var(--color-accent)' },
  UNKNOWN: { label: 'Không rõ', color: 'var(--color-neutral-400)' },
};

export const CONNECTION_LABELS: Record<PumpRealtime['connectionStatus'], { label: string; color: string }> = {
  ONLINE: { label: 'Online', color: 'var(--color-success)' },
  OFFLINE: { label: 'Offline', color: 'var(--color-accent-700)' },
  UNKNOWN: { label: 'Không rõ', color: 'var(--color-neutral-600)' },
};

export interface PumpView {
  id: string;
  name: string;
  fuel: string;
  fuelColor: string;
  state: NozzleState;
  stateLabel: string;
  stateColor: string;
  conn: PumpRealtime['connectionStatus'];
  connLabel: string;
  connColor: string;
  /** Color of the 4px band under the dispenser cap. */
  strip: string;
  /** Mini screen color on the pump glyph. */
  screen: string;
  /** Digit color on the dispenser screen. */
  digit: string;
  money: number | null;
  liters: number | null;
  price: number | null;
}

export function deriveState(p: PumpRealtime): NozzleState {
  if (p.connectionStatus === 'OFFLINE') return 'OFFLINE';
  if (p.operationalStatus === 'FUELING') return 'PUMPING';
  if (p.operationalStatus === 'IDLE') return 'IDLE';
  return 'UNKNOWN';
}

export function toPumpView(p: PumpRealtime): PumpView {
  const state = deriveState(p);
  const hasValue = state === 'PUMPING' || state === 'IDLE';
  const fuel = p.fuelType && p.fuelType.toUpperCase() !== 'UNKNOWN' ? p.fuelType : '';
  // Prefer the backend amount; only compute liters × price when it is missing.
  const money = p.money ?? (p.liters != null && p.unitPrice != null ? Math.round(p.liters * p.unitPrice) : null);
  return {
    id: p.id,
    name: p.name || p.number,
    fuel: fuel || 'Chưa xác định',
    fuelColor: fuelColor(fuel),
    state,
    stateLabel: NOZZLE_LABELS[state].label,
    stateColor: NOZZLE_LABELS[state].color,
    conn: p.connectionStatus,
    connLabel: CONNECTION_LABELS[p.connectionStatus].label,
    connColor: CONNECTION_LABELS[p.connectionStatus].color,
    strip: state === 'OFFLINE' ? 'var(--color-accent)' : state === 'PUMPING' ? 'var(--color-success)' : 'var(--color-neutral-600)',
    screen: state === 'PUMPING' ? 'var(--color-success)' : state === 'IDLE' ? 'var(--color-neutral-700)' : state === 'OFFLINE' ? 'var(--color-accent)' : 'var(--color-neutral-300)',
    digit: state === 'PUMPING' ? 'var(--color-neutral-100)' : 'var(--color-neutral-500)',
    money: money ?? (hasValue ? 0 : null),
    liters: p.liters ?? (hasValue ? 0 : null),
    price: p.unitPrice,
  };
}

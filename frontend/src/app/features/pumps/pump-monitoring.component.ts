import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, EMPTY, exhaustMap, finalize, merge, Subject, timer } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { CompanyContextService } from '../../core/company-context.service';
import { PumpRealtime } from '../../core/api.models';
import { SearchFieldComponent } from '../../shared/components/search-field/search-field.component';
import { LucideDroplet, LucideFuel, LucideRefreshCw, LucideWifi, LucideWifiOff } from '../../shared/icons';

/** Trạng thái vòi bơm đã quy đổi — không hiển thị chuỗi thô từ backend. */
type NozzleState = 'PUMPING' | 'IDLE' | 'OFFLINE' | 'UNKNOWN';
type ViewMode = 'grid' | 'table';

interface NozzleLabel { label: string; sub: string; color: string }
const NOZZLE_LABELS: Record<NozzleState, NozzleLabel> = {
  PUMPING: { label: 'Đang bơm', sub: 'Vòi đã nhấc', color: 'var(--success)' },
  IDLE: { label: 'Đã gác vòi', sub: 'Sẵn sàng bơm', color: 'var(--text)' },
  OFFLINE: { label: 'Không kết nối', sub: 'Không đọc được vòi', color: 'var(--danger)' },
  UNKNOWN: { label: 'Không xác định', sub: 'Chưa có tín hiệu', color: 'var(--unknown)' },
};
const CONN_LABELS: Record<PumpRealtime['connectionStatus'], { label: string; color: string }> = {
  ONLINE: { label: 'Online', color: 'var(--success)' },
  OFFLINE: { label: 'Offline', color: 'var(--danger)' },
  UNKNOWN: { label: 'Không rõ', color: 'var(--unknown)' },
};

export interface PumpView {
  id: string; number: string; fuel: string;
  state: NozzleState; stateLabel: string; stateSub: string; stateColor: string;
  conn: PumpRealtime['connectionStatus']; connLabel: string; connSub: string; connColor: string;
  strip: string; digit: string;
  money: number | null; moneyKnown: boolean;
  liters: number | null; litersKnown: boolean;
  price: number | null; total: number | null;
  matches(q: string): boolean;
}

function deriveState(p: PumpRealtime): NozzleState {
  if (p.connectionStatus === 'OFFLINE') return 'OFFLINE';
  if (p.operationalStatus === 'FUELING') return 'PUMPING';
  if (p.operationalStatus === 'IDLE') return 'IDLE';
  return 'UNKNOWN';
}

@Component({
  standalone: true,
  imports: [DatePipe, DecimalPipe, SearchFieldComponent, LucideDroplet, LucideFuel, LucideRefreshCw, LucideWifi, LucideWifiOff],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pump-monitoring.component.html',
  styleUrl: './pump-monitoring.component.scss',
})
export class PumpMonitoringComponent {
  private api = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly manualRefresh = new Subject<void>();
  readonly context = inject(CompanyContextService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly pumps = signal<PumpRealtime[]>([]);
  readonly updated = signal<Date | null>(null);
  readonly now = signal(new Date());

  readonly query = signal('');
  readonly filter = signal<NozzleState | 'ALL'>('ALL');
  readonly view = signal<ViewMode>('grid');

  readonly live = computed(() => !this.error());

  readonly rows = computed<PumpView[]>(() => this.pumps().map((p) => {
    const state = deriveState(p);
    const sl = NOZZLE_LABELS[state];
    const cl = CONN_LABELS[p.connectionStatus];
    const hasVal = state === 'PUMPING' || state === 'IDLE';
    const money = p.money ?? (p.liters != null && p.unitPrice != null ? p.liters * p.unitPrice : null);
    const fuel = p.fuelType && p.fuelType.toUpperCase() !== 'UNKNOWN' ? p.fuelType : 'Chưa xác định';
    return {
      id: p.id, number: p.number, fuel,
      state, stateLabel: sl.label, stateSub: sl.sub, stateColor: sl.color,
      conn: p.connectionStatus, connLabel: cl.label, connColor: cl.color,
      connSub: p.connectionStatus === 'OFFLINE'
        ? 'Mất tín hiệu từ ' + (this.updated() ? formatClock(this.updated()!) : '—')
        : p.connectionStatus === 'ONLINE' ? 'Tín hiệu ổn định' : 'Chưa nhận dữ liệu',
      strip: state === 'OFFLINE' ? 'var(--accent)' : state === 'PUMPING' ? 'var(--success)' : 'var(--unknown)',
      digit: state === 'PUMPING' ? 'var(--surface)' : 'var(--text-muted)',
      money, moneyKnown: money != null || hasVal,
      liters: p.liters, litersKnown: p.liters != null || hasVal,
      price: p.unitPrice, total: p.totalizer,
      matches: (q: string) => !q || (p.number + ' ' + fuel).toLocaleLowerCase('vi').includes(q),
    };
  }));

  readonly counts = computed(() => {
    const rows = this.rows();
    const of = (s: NozzleState) => rows.filter((r) => r.state === s).length;
    return { all: rows.length, PUMPING: of('PUMPING'), IDLE: of('IDLE'), OFFLINE: of('OFFLINE'), UNKNOWN: of('UNKNOWN') };
  });

  readonly visible = computed(() => {
    const q = this.query().trim().toLocaleLowerCase('vi');
    const f = this.filter();
    return this.rows().filter((r) => (f === 'ALL' || f === r.state) && r.matches(q));
  });

  constructor() {
    merge(timer(0, 5_000), this.manualRefresh).pipe(
      exhaustMap(() => {
        this.loading.set(this.pumps().length === 0);
        this.error.set('');
        return this.api.pumpsRealtime().pipe(
          catchError(() => {
            this.error.set('Không thể tải trạng thái trụ bơm. Vui lòng thử lại.');
            return EMPTY;
          }),
          finalize(() => this.loading.set(false)),
        );
      }),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((p) => { this.pumps.set(p); this.updated.set(new Date()); });

    timer(0, 1_000).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.now.set(new Date()));
  }

  load() { this.manualRefresh.next(); }
}

function formatClock(d: Date) {
  return d.toLocaleTimeString('vi-VN', { hour12: false, hour: '2-digit', minute: '2-digit' });
}

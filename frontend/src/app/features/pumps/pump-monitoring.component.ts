import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { catchError, EMPTY, exhaustMap, finalize, merge, Subject, timer } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { PumpRealtime } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { PumpDispenserComponent } from '../../shared/ui/pump-dispenser.component';
import { LucideSearch } from '../../shared/icons';
import { NOZZLE_STATES, NozzleState, PumpView, STATE_FILTER_LABELS, toPumpView } from '../../shared/utils/pump-view';

@Component({
  standalone: true,
  imports: [DatePipe, PageHeaderComponent, PumpDispenserComponent, LucideSearch],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pump-monitoring.component.html',
  styleUrl: './pump-monitoring.component.scss',
})
export class PumpMonitoringComponent {
  private readonly api = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly manualRefresh = new Subject<void>();
  /** Previous view per pump id: reused when nothing changed so OnPush skips that card. */
  private viewCache = new Map<string, { key: string; view: PumpView }>();

  readonly loading = signal(true);
  readonly refreshing = signal(false);
  readonly error = signal('');
  readonly pumps = signal<PumpRealtime[]>([]);
  readonly updated = signal<Date | null>(null);
  readonly now = signal(new Date());

  readonly query = signal('');
  readonly filter = signal<NozzleState | 'ALL'>('ALL');

  readonly views = computed<PumpView[]>(() => {
    const next = new Map<string, { key: string; view: PumpView }>();
    const views = this.pumps().map((p) => {
      const key = JSON.stringify(p, (k, v) => (k === 'observedAt' ? undefined : v));
      const cached = this.viewCache.get(p.id);
      const view = cached && cached.key === key ? cached.view : toPumpView(p);
      next.set(p.id, { key, view });
      return view;
    });
    this.viewCache = next;
    return views;
  });

  readonly segments = computed(() => {
    const views = this.views();
    const f = this.filter();
    return NOZZLE_STATES.map((state) => ({
      state,
      ...STATE_FILTER_LABELS[state],
      count: views.filter((v) => v.state === state).length,
      active: f === state,
    }));
  });

  readonly visible = computed(() => {
    const q = this.query().trim().toLocaleLowerCase('vi');
    const f = this.filter();
    return this.views().filter(
      (v) => (f === 'ALL' || v.state === f) && (!q || `${v.name} ${v.fuel}`.toLocaleLowerCase('vi').includes(q)),
    );
  });

  constructor() {
    const initial = inject(ActivatedRoute).snapshot.queryParamMap.get('filter');
    if (initial && (NOZZLE_STATES as string[]).includes(initial)) this.filter.set(initial as NozzleState);

    merge(timer(0, 5_000), this.manualRefresh)
      .pipe(
        exhaustMap(() =>
          this.api.pumpsRealtime().pipe(
            catchError(() => {
              this.error.set('Dữ liệu trụ bơm chưa tải được. Kiểm tra kết nối mạng của trạm rồi thử lại.');
              return EMPTY;
            }),
            finalize(() => {
              this.loading.set(false);
              this.refreshing.set(false);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((p) => {
        this.error.set('');
        this.pumps.set(p);
        this.updated.set(new Date());
      });

    timer(0, 1_000).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.now.set(new Date()));
  }

  toggle(state: NozzleState) {
    this.filter.set(this.filter() === state ? 'ALL' : state);
  }

  load() {
    this.refreshing.set(true);
    this.manualRefresh.next();
  }
}

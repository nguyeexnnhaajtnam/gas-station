import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { catchError, EMPTY, exhaustMap, finalize, merge, Observable, Subject, timer } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { DashboardSummary, FuelPrice, RevenueReport, Tank } from '../../core/api.models';
import { CompanyContextService } from '../../core/company-context.service';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { PanelComponent } from '../../shared/ui/panel.component';
import { PumpGlyphComponent } from '../../shared/ui/pump-glyph.component';
import { TankGaugeComponent } from '../../shared/ui/tank-gauge.component';
import { LucideTriangleAlert } from '../../shared/icons';
import { stationShortName } from '../../shared/utils/station-name';
import { fuelColor } from '../../shared/utils/fuel-color';
import { toPumpView } from '../../shared/utils/pump-view';
import { addDays, isoDate } from '../../shared/utils/dates';

/** Loading/error/data holder for one independent source. */
class Source<T> {
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly data = signal<T | null>(null);

  load(request: Observable<T>) {
    this.loading.set(this.data() === null);
    request.pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (value) => {
        this.error.set(false);
        this.data.set(value);
      },
      error: () => this.error.set(true),
    });
  }
}

@Component({
  standalone: true,
  imports: [
    DatePipe,
    DecimalPipe,
    RouterLink,
    PageHeaderComponent,
    ErrorStateComponent,
    EmptyStateComponent,
    SkeletonComponent,
    PanelComponent,
    PumpGlyphComponent,
    TankGaugeComponent,
    LucideTriangleAlert,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  private readonly api = inject(ApiService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly manualRefresh = new Subject<void>();
  readonly context = inject(CompanyContextService);

  readonly loading = signal(true);
  readonly refreshing = signal(false);
  readonly error = signal(false);
  readonly summary = signal<DashboardSummary | null>(null);
  readonly now = signal(new Date());
  readonly today = new Date();
  readonly mixFrom = addDays(this.today, -9);

  readonly tanks = new Source<Tank[]>();
  readonly prices = new Source<FuelPrice[]>();
  readonly report = new Source<RevenueReport>();

  readonly subtitle = computed(() => {
    const station = this.context.selectedStation();
    const parts = [
      stationShortName(station?.name) || '—',
      station?.code ? `Mã trạm ${station.code}` : '',
      `Hôm nay ${this.today.toLocaleDateString('vi-VN')}`,
    ];
    return parts.filter(Boolean).join(' · ');
  });

  readonly pumps = computed(() => (this.summary()?.pumpOverview ?? []).map(toPumpView));

  readonly alerts = computed(() => {
    const items: { label: string; link: string; params: Record<string, string> | null }[] = [];
    const negative = (this.tanks.data() ?? []).filter((t) => t.estimatedVolumeLiters < 0).length;
    const offline = this.pumps().filter((p) => p.state === 'OFFLINE').length;
    const unknown = this.pumps().filter((p) => p.state === 'UNKNOWN').length;
    if (negative) items.push({ label: `${negative} bồn tồn kho âm`, link: '/tanks', params: null });
    if (offline) items.push({ label: `${offline} trụ mất kết nối`, link: '/pumps', params: { filter: 'OFFLINE' } });
    if (unknown) items.push({ label: `${unknown} trụ chưa có tín hiệu`, link: '/pumps', params: { filter: 'UNKNOWN' } });
    return items;
  });

  readonly tankViews = computed(() =>
    (this.tanks.data() ?? []).map((t) => ({ ...t, negative: t.estimatedVolumeLiters < 0, color: fuelColor(t.fuelName) })),
  );
  readonly negativeTanks = computed(() => this.tankViews().filter((t) => t.negative).length);

  readonly priceViews = computed(() => (this.prices.data() ?? []).map((p) => ({ ...p, color: fuelColor(p.fuelName) })));

  readonly mix = computed(() => {
    const fuels = (this.report.data()?.fuels ?? []).filter((f) => f.liters > 0);
    const total = fuels.reduce((sum, f) => sum + f.liters, 0);
    return {
      total,
      rows: fuels.map((f) => ({ name: f.name, liters: f.liters, share: total ? (f.liters / total) * 100 : 0, color: fuelColor(f.name) })),
    };
  });

  constructor() {
    merge(timer(0, 5_000), this.manualRefresh)
      .pipe(
        exhaustMap(() =>
          this.api.dashboard().pipe(
            catchError(() => {
              this.error.set(true);
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
      .subscribe((response) => {
        this.error.set(false);
        this.summary.set(response);
      });

    timer(0, 1_000).pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => this.now.set(new Date()));
    this.loadSecondary();
  }

  load() {
    this.refreshing.set(true);
    this.manualRefresh.next();
    this.loadSecondary();
  }

  private loadSecondary() {
    this.tanks.load(this.api.tanks());
    this.prices.load(this.api.fuelPrices());
    this.report.load(this.api.revenueReport(isoDate(this.mixFrom), isoDate(this.today)));
  }
}

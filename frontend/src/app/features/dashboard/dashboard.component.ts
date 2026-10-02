import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, EMPTY, exhaustMap, finalize, merge, Subject, timer } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { DashboardSummary } from '../../core/api.models';
import { CompanyContextService } from '../../core/company-context.service';
import { KpiCellComponent } from '../../shared/components/kpi-cell/kpi-cell.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { stationShortName } from '../../shared/utils/station-name';
import { LucideRefreshCw } from '../../shared/icons';

@Component({
  standalone: true,
  imports: [DatePipe, DecimalPipe, KpiCellComponent, ErrorStateComponent, SkeletonComponent, LucideRefreshCw],
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
  readonly error = signal('');
  readonly summary = signal<DashboardSummary | null>(null);
  readonly lastRefresh = signal<Date | null>(null);
  readonly today = new Date();

  constructor() {
    merge(timer(0, 5_000), this.manualRefresh)
      .pipe(
        exhaustMap(() => {
          this.loading.set(this.summary() === null);
          this.error.set('');
          return this.api.dashboard().pipe(
            catchError(() => {
              this.error.set('Không thể tải dữ liệu tổng quan.');
              return EMPTY;
            }),
            finalize(() => this.loading.set(false)),
          );
        }),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((response) => {
        this.summary.set(response);
        this.lastRefresh.set(new Date());
      });
  }

  load() {
    this.manualRefresh.next();
  }

  shortStationName(name: string | undefined) {
    return stationShortName(name);
  }
}

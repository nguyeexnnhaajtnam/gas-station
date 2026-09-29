import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { DashboardSummary } from '../../core/api.models';
import { CompanyContextService } from '../../core/company-context.service';
import { KpiCellComponent } from '../../shared/components/kpi-cell/kpi-cell.component';
import { SectionHeaderComponent } from '../../shared/components/section-header/section-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { stationShortName } from '../../shared/utils/station-name';
import { LucideRefreshCw } from '../../shared/icons';

type ChartRange = 'today' | '7d' | '30d';

@Component({
  standalone: true,
  imports: [DatePipe, KpiCellComponent, SectionHeaderComponent, EmptyStateComponent, ErrorStateComponent, SkeletonComponent, LucideRefreshCw],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent {
  private readonly api = inject(ApiService);
  readonly context = inject(CompanyContextService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly summary = signal<DashboardSummary | null>(null);
  readonly range = signal<ChartRange>('today');
  readonly today = new Date();

  constructor() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.api
      .dashboard()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => this.summary.set(r),
        error: () => this.error.set('Không thể tải dữ liệu tổng quan.'),
      });
  }

  shortStationName(name: string | undefined) {
    return stationShortName(name);
  }
}

import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { catchError, EMPTY, exhaustMap, finalize, merge, Subject, timer } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { FuelPrice } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { PanelComponent } from '../../shared/ui/panel.component';
import { StatusTagComponent, TagTone } from '../../shared/ui/status-tag.component';
import { fuelColor } from '../../shared/utils/fuel-color';

const STATUS: Record<FuelPrice['updateStatus'], { label: string; tone: TagTone }> = {
  UPDATED: { label: 'Đang áp dụng', tone: 'success' },
  SCHEDULED: { label: 'Đã đặt lịch', tone: 'neutral' },
  UNKNOWN: { label: 'Chưa rõ', tone: 'neutral' },
};

@Component({
  standalone: true,
  imports: [DatePipe, DecimalPipe, PageHeaderComponent, EmptyStateComponent, ErrorStateComponent, SkeletonComponent, PanelComponent, StatusTagComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './fuel-prices.component.html',
  styleUrl: './fuel-prices.component.scss',
})
export class FuelPricesComponent {
  private readonly api = inject(ApiService);
  private readonly refresh$ = new Subject<void>();

  readonly loading = signal(true);
  readonly refreshing = signal(false);
  readonly error = signal('');
  readonly prices = signal<FuelPrice[]>([]);
  readonly updatedAt = signal<Date | null>(null);

  readonly views = computed(() =>
    this.prices().map((p) => ({ ...p, color: fuelColor(p.fuelName), status: STATUS[p.updateStatus] ?? STATUS.UNKNOWN })),
  );

  constructor() {
    merge(timer(0, 5000), this.refresh$)
      .pipe(
        exhaustMap(() =>
          this.api.fuelPrices().pipe(
            catchError(() => {
              this.error.set('Không thể tải bảng giá nhiên liệu.');
              return EMPTY;
            }),
            finalize(() => {
              this.loading.set(false);
              this.refreshing.set(false);
            }),
          ),
        ),
        takeUntilDestroyed(inject(DestroyRef)),
      )
      .subscribe((items) => {
        this.error.set('');
        this.prices.set(items);
        this.updatedAt.set(new Date());
      });
  }

  refresh() {
    this.refreshing.set(true);
    this.refresh$.next();
  }
}

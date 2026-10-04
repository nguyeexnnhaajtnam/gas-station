import { DecimalPipe, NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { RevenueBreakdown, RevenueReport } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { PanelComponent } from '../../shared/ui/panel.component';
import { StatItem, StatStripComponent } from '../../shared/ui/stat-strip.component';
import { fuelColor, fuelKey } from '../../shared/utils/fuel-color';
import { displayDate, isoDate } from '../../shared/utils/dates';

const nf = (value: number, digits = 0) =>
  new Intl.NumberFormat('vi-VN', { minimumFractionDigits: digits, maximumFractionDigits: digits }).format(value);

const sum = (rows: RevenueBreakdown[], key: 'liters' | 'revenue' | 'count') => rows.reduce((total, r) => total + (Number(r[key]) || 0), 0);

@Component({
  standalone: true,
  imports: [DecimalPipe, NgTemplateOutlet, PageHeaderComponent, EmptyStateComponent, ErrorStateComponent, SkeletonComponent, PanelComponent, StatStripComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './revenue-report.component.html',
  styleUrl: './revenue-report.component.scss',
})
export class RevenueReportComponent {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly report = signal<RevenueReport | null>(null);
  readonly updatedAt = signal<Date | null>(null);
  readonly showDetail = signal(false);
  readonly from = signal(isoDate(new Date()));
  readonly to = signal(isoDate(new Date()));

  readonly rangeLabel = computed(() => {
    const r = this.report();
    return r ? `${displayDate(r.from)} – ${displayDate(r.to)}` : '';
  });

  readonly headline = computed<StatItem[]>(() => {
    const fuels = this.report()?.fuels ?? [];
    return [
      { label: 'Tổng doanh thu', value: nf(sum(fuels, 'revenue')), unit: '₫' },
      { label: 'Tổng sản lượng', value: nf(sum(fuels, 'liters'), 2), unit: 'L' },
      { label: 'Lượt bơm', value: nf(sum(fuels, 'count')), unit: 'lượt' },
    ];
  });

  readonly isEmpty = computed(() => {
    const r = this.report();
    return !r || (!r.fuels.length && !r.pumps.length && !r.invoices.length);
  });

  readonly mix = computed(() => {
    const fuels = (this.report()?.fuels ?? []).filter((f) => f.liters > 0);
    const total = sum(fuels, 'liters');
    return {
      total,
      rows: fuels.map((f) => ({ name: f.name, liters: f.liters, share: total ? (f.liters / total) * 100 : 0, color: fuelColor(f.name) })),
    };
  });

  readonly pumpBars = computed(() => {
    const pumps = this.report()?.pumps ?? [];
    const max = Math.max(0, ...pumps.map((p) => p.liters));
    return pumps.map((p) => ({
      name: p.name,
      liters: p.liters,
      pct: max > 0 ? (p.liters / max) * 100 : 0,
      top: max > 0 && p.liters === max,
    }));
  });

  /** Share of sold liters that already has an invoice, per fuel and overall. */
  readonly invoice = computed(() => {
    const fuels = this.report()?.fuels ?? [];
    const invoices = this.report()?.invoices ?? [];
    const rows = fuels
      .filter((f) => f.liters > 0)
      .map((f) => {
        const invoiced = sum(invoices.filter((i) => fuelKey(i.name) === fuelKey(f.name)), 'liters');
        return { name: f.name, pct: Math.min(100, (invoiced / f.liters) * 100) };
      });
    const sold = sum(fuels, 'liters');
    return { overall: sold > 0 ? Math.min(100, (sum(invoices, 'liters') / sold) * 100) : null, rows };
  });

  constructor() {
    this.load();
  }

  load() {
    if (!this.from() || !this.to() || this.from() > this.to()) {
      this.error.set('Khoảng thời gian không hợp lệ: ngày bắt đầu phải trước ngày kết thúc.');
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.error.set('');
    this.api
      .revenueReport(this.from(), this.to())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => {
          this.report.set(r);
          this.updatedAt.set(new Date());
        },
        error: () => this.error.set('Không thể tải báo cáo doanh thu. Vui lòng thử lại.'),
      });
  }
}

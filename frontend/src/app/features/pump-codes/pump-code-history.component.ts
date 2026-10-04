import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, finalize, Subject } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { PumpCodeHistory } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { DataListComponent, ListState } from '../../shared/ui/data-list.component';
import { FilterPopoverComponent } from '../../shared/ui/filter-popover.component';
import { StatusTagComponent, TagTone } from '../../shared/ui/status-tag.component';
import { LucideChevronDown } from '../../shared/icons';
import { displayDate, isoDate } from '../../shared/utils/dates';

const SORT_LABELS: Record<string, string> = {
  'finishedAt,desc': 'Mới nhất',
  'finishedAt,asc': 'Cũ nhất',
  'amount,desc': 'Số tiền giảm dần',
};
const INVOICE_LABELS: Record<string, string> = { '': 'Tất cả', ISSUED: 'Đã xuất', NOT_ISSUED: 'Chưa xuất' };

@Component({
  standalone: true,
  imports: [DatePipe, DecimalPipe, PageHeaderComponent, DataListComponent, FilterPopoverComponent, StatusTagComponent, LucideChevronDown],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pump-code-history.component.html',
  styleUrl: './pump-code-history.component.scss',
})
export class PumpCodeHistoryComponent {
  private readonly api = inject(ApiService);
  private readonly today = isoDate(new Date());
  private readonly search$ = new Subject<void>();

  readonly sortLabels = SORT_LABELS;
  readonly invoiceLabels = INVOICE_LABELS;

  readonly loading = signal(true);
  readonly error = signal('');
  readonly histories = signal<PumpCodeHistory[]>([]);
  readonly updatedAt = signal<Date | null>(null);
  readonly fuels = signal<string[]>([]);

  readonly from = signal(this.today);
  readonly to = signal(this.today);
  readonly pumpId = signal('');
  readonly fuelType = signal('');
  readonly customer = signal('');
  readonly amountFilter = signal('');
  readonly volumeFilter = signal('');
  readonly status = signal('');
  readonly sort = signal('finishedAt,desc');
  readonly page = signal(0);
  readonly size = signal(20);
  readonly totalElements = signal(0);
  readonly totalPages = signal(0);

  readonly state = computed<ListState>(() =>
    this.loading() ? 'loading' : this.error() ? 'error' : this.histories().length ? 'ready' : 'empty',
  );
  readonly rangeLabel = computed(() =>
    this.from() === this.to() ? displayDate(this.from()) : `${displayDate(this.from())} – ${displayDate(this.to())}`,
  );
  readonly moreCount = computed(() => [this.customer(), this.amountFilter(), this.volumeFilter()].filter(Boolean).length);

  constructor() {
    this.search$.pipe(debounceTime(400), takeUntilDestroyed(inject(DestroyRef))).subscribe(() => this.load(true));
    this.api.fuelPrices().subscribe({ next: (items) => this.fuels.set(items.map((p) => p.fuelName)), error: () => {} });
    this.load();
  }

  load(resetPage = false) {
    if (resetPage) this.page.set(0);
    this.loading.set(true);
    this.error.set('');
    this.api
      .pumpCodeHistory({
        from: this.from(),
        to: this.to(),
        pumpId: this.pumpId(),
        fuelType: this.fuelType(),
        customer: this.customer(),
        amountFilter: this.amountFilter(),
        volumeFilter: this.volumeFilter(),
        status: this.status(),
        sort: this.sort(),
        page: this.page(),
        size: this.size(),
      })
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => {
          this.histories.set(r.content);
          this.page.set(r.number);
          this.totalElements.set(r.totalElements);
          this.totalPages.set(r.totalPages);
          this.updatedAt.set(new Date());
        },
        error: () => this.error.set('Không thể tải lịch sử mã bơm. Vui lòng thử lại.'),
      });
  }

  onSearch(value: string) {
    this.pumpId.set(value);
    this.search$.next();
  }

  setAndLoad(target: { set(value: string): void }, value: string) {
    target.set(value);
    this.load(true);
  }

  applyPopover(popover: FilterPopoverComponent) {
    popover.close();
    this.load(true);
  }

  clearMore(popover: FilterPopoverComponent) {
    this.customer.set('');
    this.amountFilter.set('');
    this.volumeFilter.set('');
    this.applyPopover(popover);
  }

  changePage(next: number) {
    if (next < 0 || next >= this.totalPages() || next === this.page()) return;
    this.page.set(next);
    this.load();
  }

  tone(state: string): TagTone {
    return state === 'ISSUED' ? 'success' : 'neutral';
  }

  invoiceLabel(state: string) {
    return state === 'ISSUED' ? 'Đã xuất' : state === 'NOT_ISSUED' ? 'Chưa xuất' : 'Chưa rõ';
  }

  track(item: PumpCodeHistory, index: number) {
    return `${item.pumpCode}-${item.finishedAt}-${item.invoiceNumber ?? index}`;
  }
}

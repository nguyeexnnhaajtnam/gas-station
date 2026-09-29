import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Transaction } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { SearchFieldComponent } from '../../shared/components/search-field/search-field.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { StatusBadgeComponent, StatusTone } from '../../shared/components/status-badge/status-badge.component';

@Component({
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    DecimalPipe,
    PageHeaderComponent,
    SearchFieldComponent,
    EmptyStateComponent,
    ErrorStateComponent,
    SkeletonComponent,
    StatusBadgeComponent,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './transactions.component.html',
  styleUrl: './transactions.component.scss',
})
export class TransactionsComponent {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly transactions = signal<Transaction[]>([]);
  readonly query = signal('');
  readonly day = signal('');
  readonly pump = signal('');
  readonly fuel = signal('');
  readonly status = signal('');

  readonly pumpOptions = computed(() => Array.from(new Set(this.transactions().map((t) => t.pumpCode))).sort());
  readonly fuelOptions = computed(() => Array.from(new Set(this.transactions().map((t) => t.fuelName))).sort());
  readonly statusOptions = computed(() => Array.from(new Set(this.transactions().map((t) => t.invoiceState))).sort());

  readonly filtered = computed(() => {
    const q = this.query().trim().toLocaleLowerCase('vi');
    const day = this.day();
    const pump = this.pump();
    const fuel = this.fuel();
    const status = this.status();
    return this.transactions().filter((t) => {
      if (day && !t.completedAt.startsWith(day)) return false;
      if (pump && t.pumpCode !== pump) return false;
      if (fuel && t.fuelName !== fuel) return false;
      if (status && t.invoiceState !== status) return false;
      if (q && ![t.pumpCode, t.fuelName, t.customerName, t.invoiceNumber].some((v) => v?.toLocaleLowerCase('vi').includes(q))) return false;
      return true;
    });
  });

  constructor() {
    this.load();
  }

  statusTone(state: string): StatusTone {
    const s = state.toUpperCase();
    if (['PAID', 'COMPLETED', 'DONE', 'HOAN_TAT'].includes(s)) return 'success';
    if (['PENDING', 'PROCESSING'].includes(s)) return 'warning';
    if (['VOID', 'CANCELLED', 'FAILED'].includes(s)) return 'danger';
    return 'unknown';
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.api
      .transactions()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => this.transactions.set(r.items),
        error: () => this.error.set('Không thể tải danh sách giao dịch.'),
      });
  }
}

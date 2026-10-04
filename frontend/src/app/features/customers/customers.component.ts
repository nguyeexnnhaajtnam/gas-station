import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Customer } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { DataListComponent, ListState } from '../../shared/ui/data-list.component';

const PAGE_SIZE = 20;

@Component({
  standalone: true,
  imports: [PageHeaderComponent, DataListComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './customers.component.html',
  styleUrl: './customers.component.scss',
})
export class CustomersComponent {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly customers = signal<Customer[]>([]);
  readonly updatedAt = signal<Date | null>(null);
  readonly query = signal('');
  readonly page = signal(0);

  readonly filtered = computed(() => {
    const q = this.normalize(this.query());
    return q
      ? this.customers().filter((c) => [c.name, c.id, c.taxCode, c.address, c.email].some((v) => this.normalize(v).includes(q)))
      : this.customers();
  });
  readonly totalPages = computed(() => Math.max(1, Math.ceil(this.filtered().length / PAGE_SIZE)));
  readonly paged = computed(() => this.filtered().slice(this.page() * PAGE_SIZE, (this.page() + 1) * PAGE_SIZE));
  readonly state = computed<ListState>(() =>
    this.loading() ? 'loading' : this.error() ? 'error' : this.filtered().length ? 'ready' : 'empty',
  );
  readonly countLabel = computed(() =>
    this.query().trim() ? `${this.filtered().length} / ${this.customers().length} khách hàng` : `${this.customers().length} khách hàng`,
  );

  constructor() {
    effect(() => {
      this.query();
      this.page.set(0);
    });
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.api
      .customers()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (items) => {
          this.customers.set(items);
          this.updatedAt.set(new Date());
        },
        error: () => this.error.set('Không thể tải danh sách khách hàng. Vui lòng thử lại.'),
      });
  }

  private normalize(value?: string) {
    return (value ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/gi, 'd').toLocaleLowerCase('vi').trim();
  }
}

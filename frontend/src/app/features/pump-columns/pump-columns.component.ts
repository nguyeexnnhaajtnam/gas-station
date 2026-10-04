import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { PumpColumn } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { DataListComponent, ListState } from '../../shared/ui/data-list.component';
import { StatusTagComponent, TagTone } from '../../shared/ui/status-tag.component';
import { LucideChevronDown } from '../../shared/icons';
import { CONNECTION_LABELS } from '../../shared/utils/pump-view';

const PAGE_SIZE = 20;

@Component({
  standalone: true,
  imports: [DecimalPipe, PageHeaderComponent, DataListComponent, StatusTagComponent, LucideChevronDown],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pump-columns.component.html',
  styleUrl: './pump-columns.component.scss',
})
export class PumpColumnsComponent {
  private readonly api = inject(ApiService);
  readonly connLabels = CONNECTION_LABELS;

  readonly loading = signal(true);
  readonly error = signal('');
  readonly columns = signal<PumpColumn[]>([]);
  readonly updatedAt = signal<Date | null>(null);
  readonly query = signal('');
  readonly connection = signal<'' | PumpColumn['connectionStatus']>('');
  readonly fuel = signal('');
  readonly page = signal(0);

  readonly fuels = computed(() => [...new Set(this.columns().map((c) => c.fuelType).filter(Boolean))]);

  readonly filtered = computed(() => {
    const q = this.query().trim().toLocaleLowerCase('vi');
    return this.columns().filter(
      (c) =>
        (!this.connection() || c.connectionStatus === this.connection()) &&
        (!this.fuel() || c.fuelType === this.fuel()) &&
        (!q || [c.id, c.name, c.fuelType, c.deviceAddress, c.serialNumber].some((v) => v?.toLocaleLowerCase('vi').includes(q))),
    );
  });
  readonly totalPages = computed(() => Math.max(1, Math.ceil(this.filtered().length / PAGE_SIZE)));
  readonly paged = computed(() => this.filtered().slice(this.page() * PAGE_SIZE, (this.page() + 1) * PAGE_SIZE));
  readonly state = computed<ListState>(() =>
    this.loading() ? 'loading' : this.error() ? 'error' : this.filtered().length ? 'ready' : 'empty',
  );

  constructor() {
    // Any filter change goes back to the first page.
    effect(() => {
      this.query();
      this.connection();
      this.fuel();
      this.page.set(0);
    });
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.api
      .pumpColumns()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (r) => {
          this.columns.set(r);
          this.updatedAt.set(new Date());
        },
        error: () => this.error.set('Không thể tải danh sách cột bơm. Vui lòng thử lại.'),
      });
  }

  tone(status: PumpColumn['connectionStatus']): TagTone {
    return status === 'ONLINE' ? 'success' : status === 'OFFLINE' ? 'danger' : 'neutral';
  }
}

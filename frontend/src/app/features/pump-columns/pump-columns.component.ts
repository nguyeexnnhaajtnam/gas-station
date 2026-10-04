import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { PumpColumn } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { PanelComponent } from '../../shared/ui/panel.component';
import { LucideChevronDown, LucideSearch, LucideWifi, LucideWifiOff } from '../../shared/icons';
import { CONNECTION_LABELS } from '../../shared/utils/pump-view';
import { fuelColor } from '../../shared/utils/fuel-color';

const STRIP: Record<PumpColumn['connectionStatus'], string> = {
  ONLINE: 'var(--color-success)',
  OFFLINE: 'var(--color-accent)',
  UNKNOWN: 'var(--color-neutral-600)',
};

@Component({
  standalone: true,
  imports: [
    DecimalPipe,
    PageHeaderComponent,
    EmptyStateComponent,
    ErrorStateComponent,
    SkeletonComponent,
    PanelComponent,
    LucideChevronDown,
    LucideSearch,
    LucideWifi,
    LucideWifiOff,
  ],
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

  readonly fuels = computed(() => [...new Set(this.columns().map((c) => c.fuelType).filter(Boolean))]);
  readonly onlineCount = computed(() => this.columns().filter((c) => c.connectionStatus === 'ONLINE').length);

  readonly filtered = computed(() => {
    const q = this.query().trim().toLocaleLowerCase('vi');
    return this.columns()
      .filter(
        (c) =>
          (!this.connection() || c.connectionStatus === this.connection()) &&
          (!this.fuel() || c.fuelType === this.fuel()) &&
          (!q || [c.id, c.name, c.fuelType, c.deviceAddress, c.serialNumber].some((v) => v?.toLocaleLowerCase('vi').includes(q))),
      )
      .map((c) => ({
        ...c,
        strip: STRIP[c.connectionStatus],
        connColor: this.connLabels[c.connectionStatus].color,
        connLabel: this.connLabels[c.connectionStatus].label,
        // Readable status colors on the dark pump screen
        screenText: c.connectionStatus === 'ONLINE' ? 'oklch(0.78 0.14 150)' : c.connectionStatus === 'OFFLINE' ? 'var(--color-accent-400)' : 'var(--color-neutral-400)',
        fuelColor: fuelColor(c.fuelType),
      }));
  });

  readonly countLabel = computed(() => {
    const total = this.columns().length;
    const shown = this.filtered().length;
    return `${shown === total ? total : shown + ' / ' + total} cột bơm · ${this.onlineCount()} đang kết nối`;
  });

  constructor() {
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

  clearFilters() {
    this.query.set('');
    this.connection.set('');
    this.fuel.set('');
  }
}

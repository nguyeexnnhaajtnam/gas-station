import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { StoreDetails } from '../../core/api.models';
import { PageHeaderComponent } from '../../shared/components/page-header/page-header.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { ErrorStateComponent } from '../../shared/components/error-state/error-state.component';
import { SkeletonComponent } from '../../shared/components/skeleton/skeleton.component';
import { PanelComponent } from '../../shared/ui/panel.component';
import { StatusTagComponent } from '../../shared/ui/status-tag.component';
import { LucideBuilding, LucideFuel } from '../../shared/icons';

interface InfoRow {
  label: string;
  value: string | undefined;
}

@Component({
  standalone: true,
  imports: [NgTemplateOutlet, PageHeaderComponent, EmptyStateComponent, ErrorStateComponent, SkeletonComponent, PanelComponent, StatusTagComponent, LucideBuilding, LucideFuel],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './store-info.component.html',
  styleUrl: './store-info.component.scss',
})
export class StoreInfoComponent {
  private readonly api = inject(ApiService);

  readonly loading = signal(true);
  readonly error = signal('');
  readonly info = signal<StoreDetails | null>(null);
  readonly updatedAt = signal<Date | null>(null);

  readonly companyRows = computed<InfoRow[]>(() => {
    const i = this.info();
    return [
      { label: 'Địa chỉ', value: i?.companyAddress },
      { label: 'Mã số thuế', value: i?.taxCode },
      { label: 'Điện thoại', value: i?.companyPhone },
      { label: 'Fax', value: i?.companyFax },
      { label: 'Email', value: i?.companyEmail },
    ];
  });

  readonly storeRows = computed<InfoRow[]>(() => {
    const i = this.info();
    return [
      { label: 'Địa chỉ', value: i?.storeAddress },
      { label: 'Điện thoại', value: i?.storePhone },
      { label: 'Fax', value: i?.storeFax },
      { label: 'Email', value: i?.storeEmail },
    ];
  });

  constructor() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.error.set('');
    this.api
      .storeInfo()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (value) => {
          this.info.set(value);
          this.updatedAt.set(new Date());
        },
        error: () => this.error.set('Không thể tải thông tin cửa hàng.'),
      });
  }
}

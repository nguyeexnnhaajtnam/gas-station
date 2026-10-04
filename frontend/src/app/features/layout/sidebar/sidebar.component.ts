import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CompanyContextService } from '../../../core/company-context.service';
import { LayoutStateService } from '../layout-state.service';
import {
  LucideFuel,
  LucideLock,
  LucideLayoutDashboard,
  LucideActivity,
  LucideBarcode,
  LucideTag,
  LucideCylinder,
  LucideCalendarClock,
  LucideStore,
  LucideUsers,
  LucideChartColumn,
} from '../../../shared/icons';

type IconKey = 'dashboard' | 'activity' | 'barcode' | 'fuel' | 'tag' | 'tank' | 'shift' | 'store' | 'users' | 'chart';

interface NavItem {
  label: string;
  link: string;
  icon: IconKey;
  requiresStation: boolean;
  /** Not available yet: rendered locked with the "SẮP CÓ" tag. */
  soon?: boolean;
}

const ITEMS: NavItem[] = [
  { label: 'Tổng quan', link: '/dashboard', icon: 'dashboard', requiresStation: true },
  { label: 'Theo dõi online', link: '/pumps', icon: 'activity', requiresStation: true },
  { label: 'Mã bơm', link: '/pump-codes', icon: 'barcode', requiresStation: true },
  { label: 'Cột bơm', link: '/pump-columns', icon: 'fuel', requiresStation: true },
  { label: 'Giá nhiên liệu', link: '/fuel-prices', icon: 'tag', requiresStation: true },
  { label: 'Bồn bể', link: '/tanks', icon: 'tank', requiresStation: true },
  { label: 'Ca bán hàng', link: '/shifts', icon: 'shift', requiresStation: true, soon: true },
  { label: 'Thông tin cửa hàng', link: '/store-info', icon: 'store', requiresStation: true },
  { label: 'Khách hàng', link: '/customers', icon: 'users', requiresStation: true },
  { label: 'Báo cáo doanh thu', link: '/reports', icon: 'chart', requiresStation: true },
];

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    NgTemplateOutlet,
    LucideFuel,
    LucideLock,
    LucideLayoutDashboard,
    LucideActivity,
    LucideBarcode,
    LucideTag,
    LucideCylinder,
    LucideCalendarClock,
    LucideStore,
    LucideUsers,
    LucideChartColumn,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  readonly context = inject(CompanyContextService);
  readonly layout = inject(LayoutStateService);
  readonly items = ITEMS;

  hasStation() {
    return !!this.context.selectedStation();
  }

  locked(item: NavItem) {
    return !!item.soon || (item.requiresStation && !this.hasStation());
  }

  lockedTitle(item: NavItem) {
    return item.soon ? `${item.label} · chưa mở cho trạm này` : `${item.label} · cần chọn trạm`;
  }
}

import { ChangeDetectionStrategy, Component, HostListener, inject, signal } from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { CompanyContextService } from '../../../core/company-context.service';
import {
  LucideFuel,
  LucideLock,
  LucideChevronsLeft,
  LucideChevronsRight,
  LucideLayoutDashboard,
  LucideGauge,
  LucideReceipt,
  LucideClipboardList,
  LucideDroplet,
  LucideWarehouse,
  LucideTag,
  LucideUsers,
  LucideWallet,
  LucideFileText,
  LucideBuilding,
  LucideUser,
  LucideSettings,
} from '../../../shared/icons';

type IconKey =
  | 'dashboard' | 'gauge' | 'receipt' | 'shift' | 'tank' | 'warehouse' | 'tag'
  | 'users' | 'fuel' | 'wallet' | 'file' | 'building' | 'user' | 'settings';

interface NavItem {
  label: string;
  link: string;
  icon: IconKey;
  requiresStation: boolean;
}

interface NavGroup {
  label: string;
  items: NavItem[];
}

const GROUPS: NavGroup[] = [
  { label: 'TỔNG QUAN', items: [{ label: 'Tổng quan', link: '/dashboard', icon: 'dashboard', requiresStation: true }] },
  {
    label: 'VẬN HÀNH',
    items: [
      { label: 'Theo dõi trụ bơm', link: '/pumps', icon: 'gauge', requiresStation: true },
      { label: 'Giao dịch', link: '/transactions', icon: 'receipt', requiresStation: true },
      { label: 'Ca bán hàng', link: '/shifts', icon: 'shift', requiresStation: true },
    ],
  },
  {
    label: 'NHIÊN LIỆU',
    items: [
      { label: 'Bồn bể', link: '/tanks', icon: 'tank', requiresStation: true },
      { label: 'Kho nhiên liệu', link: '/fuel-inventory', icon: 'warehouse', requiresStation: true },
      { label: 'Giá nhiên liệu', link: '/fuel-prices', icon: 'tag', requiresStation: true },
    ],
  },
  {
    label: 'KINH DOANH',
    items: [
      { label: 'Khách hàng', link: '/customers', icon: 'users', requiresStation: true },
      { label: 'Cấp nhiên liệu', link: '/fuel-supply', icon: 'fuel', requiresStation: true },
      { label: 'Công nợ', link: '/debts', icon: 'wallet', requiresStation: true },
      { label: 'Hóa đơn', link: '/invoices', icon: 'receipt', requiresStation: true },
    ],
  },
  { label: 'BÁO CÁO', items: [{ label: 'Báo cáo', link: '/reports', icon: 'file', requiresStation: true }] },
  {
    label: 'HỆ THỐNG',
    items: [
      { label: 'Công ty / Đại lý', link: '/companies', icon: 'building', requiresStation: false },
      { label: 'Người dùng', link: '/users', icon: 'user', requiresStation: false },
      { label: 'Cài đặt', link: '/settings', icon: 'settings', requiresStation: false },
    ],
  },
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
    LucideChevronsLeft,
    LucideChevronsRight,
    LucideLayoutDashboard,
    LucideGauge,
    LucideReceipt,
    LucideClipboardList,
    LucideDroplet,
    LucideWarehouse,
    LucideTag,
    LucideUsers,
    LucideWallet,
    LucideFileText,
    LucideBuilding,
    LucideUser,
    LucideSettings,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.scss',
})
export class SidebarComponent {
  readonly context = inject(CompanyContextService);
  readonly groups = GROUPS;

  private readonly manualCollapse = signal<boolean | null>(null);
  private readonly viewportNarrow = signal(typeof window !== 'undefined' && window.innerWidth < 1180);

  readonly collapsed = signal(false);

  constructor() {
    this.syncCollapsed();
  }

  private syncCollapsed() {
    this.collapsed.set(this.manualCollapse() ?? this.viewportNarrow());
  }

  @HostListener('window:resize')
  onResize() {
    if (typeof window === 'undefined') return;
    this.viewportNarrow.set(window.innerWidth < 1180);
    this.syncCollapsed();
  }

  toggleCollapse() {
    this.manualCollapse.set(!this.collapsed());
    this.syncCollapsed();
  }

  hasStation() {
    return !!this.context.selectedStation();
  }

  disabledTitle(item: NavItem) {
    return item.requiresStation && !this.hasStation() ? `${item.label} · cần chọn trạm` : item.label;
  }
}

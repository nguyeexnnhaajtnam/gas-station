import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent) },
  {
    path: '',
    loadComponent: () => import('./features/layout/layout.component').then((m) => m.LayoutComponent),
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'companies', pathMatch: 'full' },
      { path: 'companies', loadComponent: () => import('./features/companies/companies.component').then((m) => m.CompaniesComponent) },
      {
        path: 'companies/:companyId/stations',
        loadComponent: () => import('./features/companies/company-stations.component').then((m) => m.CompanyStationsComponent),
      },
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent) },
      { path: 'transactions', loadComponent: () => import('./features/transactions/transactions.component').then((m) => m.TransactionsComponent) },
      { path: 'tanks', loadComponent: () => import('./features/tanks/tanks.component').then((m) => m.TanksComponent) },

      // Sidebar items without a backend API yet — placeholder pages so no nav link is dead.
      {
        path: 'pumps',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        data: { eyebrow: 'Vận hành', title: 'Theo dõi trụ bơm', subtitle: 'Tình trạng và sản lượng theo từng trụ bơm.' },
      },
      {
        path: 'shifts',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        data: { eyebrow: 'Vận hành', title: 'Ca bán hàng', subtitle: 'Quản lý ca làm việc và đối soát doanh thu.' },
      },
      {
        path: 'fuel-inventory',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        data: { eyebrow: 'Nhiên liệu', title: 'Kho nhiên liệu', subtitle: 'Nhập, xuất và chuyển kho nhiên liệu.' },
      },
      {
        path: 'fuel-prices',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        data: { eyebrow: 'Nhiên liệu', title: 'Giá nhiên liệu', subtitle: 'Bảng giá bán theo từng loại nhiên liệu.' },
      },
      {
        path: 'customers',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        data: { eyebrow: 'Kinh doanh', title: 'Khách hàng', subtitle: 'Danh sách khách hàng và hợp đồng cấp nhiên liệu.' },
      },
      {
        path: 'fuel-supply',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        data: { eyebrow: 'Kinh doanh', title: 'Cấp nhiên liệu', subtitle: 'Lịch sử cấp nhiên liệu cho khách hàng công nợ.' },
      },
      {
        path: 'debts',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        data: { eyebrow: 'Kinh doanh', title: 'Công nợ', subtitle: 'Theo dõi công nợ khách hàng theo kỳ.' },
      },
      {
        path: 'invoices',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        data: { eyebrow: 'Kinh doanh', title: 'Hóa đơn', subtitle: 'Hóa đơn điện tử phát hành cho giao dịch.' },
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        data: { eyebrow: 'Báo cáo', title: 'Báo cáo', subtitle: 'Báo cáo vận hành và kinh doanh theo kỳ.' },
      },
      {
        path: 'users',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        data: { eyebrow: 'Hệ thống', title: 'Người dùng', subtitle: 'Quản lý tài khoản và phân quyền truy cập.' },
      },
      {
        path: 'settings',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        data: { eyebrow: 'Hệ thống', title: 'Cài đặt', subtitle: 'Cấu hình hệ thống và tài khoản của tôi.' },
      },
    ],
  },
  { path: '**', redirectTo: '' },
];

import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { stationGuard } from './core/station.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login.component').then((m) => m.LoginComponent) },
  { path: '', pathMatch: 'full', redirectTo: 'companies' },

  // Onboarding funnel (login -> company -> station): standalone pages with just
  // a brand top bar, deliberately outside the operational app shell/sidebar.
  {
    path: 'companies',
    canActivate: [authGuard],
    loadComponent: () => import('./features/companies/companies.component').then((m) => m.CompaniesComponent),
  },
  {
    path: 'companies/:companyId/stations',
    canActivate: [authGuard],
    loadComponent: () => import('./features/companies/company-stations.component').then((m) => m.CompanyStationsComponent),
  },

  // Operational app shell: only reachable once a station is active (stationGuard).
  {
    path: '',
    loadComponent: () => import('./features/layout/layout.component').then((m) => m.LayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent),
        canActivate: [stationGuard],
      },
      {
        path: 'tanks',
        loadComponent: () => import('./features/tanks/tanks.component').then((m) => m.TanksComponent),
        canActivate: [stationGuard],
      },

      // Sidebar items without a backend API yet — placeholder pages so no nav link is dead.
      {
        path: 'pumps',
        loadComponent: () => import('./features/pumps/pump-monitoring.component').then((m) => m.PumpMonitoringComponent),
        canActivate: [stationGuard],
        data: { eyebrow: 'Vận hành', title: 'Theo dõi online', subtitle: 'Tình trạng và sản lượng theo từng trụ bơm.' },
      },
      {
        path: 'pump-codes',
        loadComponent: () => import('./features/pump-codes/pump-code-history.component').then((m) => m.PumpCodeHistoryComponent),
        canActivate: [stationGuard],
      },
      {
        path: 'pump-columns',
        loadComponent: () => import('./features/pump-columns/pump-columns.component').then((m) => m.PumpColumnsComponent),
        canActivate: [stationGuard],
      },
      {
        path: 'store-info',
        loadComponent: () => import('./features/store-info/store-info.component').then((m) => m.StoreInfoComponent),
        canActivate: [stationGuard],
      },
      {
        path: 'shifts',
        redirectTo: 'dashboard',
        pathMatch: 'full',
      },
      {
        path: 'fuel-inventory',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        canActivate: [stationGuard],
        data: { eyebrow: 'Nhiên liệu', title: 'Kho nhiên liệu', subtitle: 'Nhập, xuất và chuyển kho nhiên liệu.' },
      },
      {
        path: 'fuel-prices',
        loadComponent: () => import('./features/fuel-prices/fuel-prices.component').then((m) => m.FuelPricesComponent),
        canActivate: [stationGuard],
      },
      {
        path: 'customers',
        loadComponent: () => import('./features/customers/customers.component').then((m) => m.CustomersComponent),
        canActivate: [stationGuard],
      },
      {
        path: 'fuel-supply',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        canActivate: [stationGuard],
        data: { eyebrow: 'Kinh doanh', title: 'Cấp nhiên liệu', subtitle: 'Lịch sử cấp nhiên liệu cho khách hàng công nợ.' },
      },
      {
        path: 'debts',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        canActivate: [stationGuard],
        data: { eyebrow: 'Kinh doanh', title: 'Công nợ', subtitle: 'Theo dõi công nợ khách hàng theo kỳ.' },
      },
      {
        path: 'invoices',
        loadComponent: () => import('./features/misc/coming-soon.component').then((m) => m.ComingSoonComponent),
        canActivate: [stationGuard],
        data: { eyebrow: 'Kinh doanh', title: 'Hóa đơn', subtitle: 'Hóa đơn điện tử phát hành cho giao dịch.' },
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/reports/revenue-report.component').then((m) => m.RevenueReportComponent),
        canActivate: [stationGuard],
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

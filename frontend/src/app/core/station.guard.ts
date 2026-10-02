import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { CompanyContextService } from './company-context.service';

/**
 * Enforces the login -> company -> station -> operate funnel: operational
 * routes require an active station in CompanyContextService. Since that
 * context lives only in memory (no backend "current context" endpoint to
 * rehydrate from), a hard reload/deep link without a selected station sends
 * the user back to the step they skipped instead of rendering the page.
 */
export const stationGuard: CanActivateFn = () => {
  const context = inject(CompanyContextService);
  const router = inject(Router);

  if (context.selectedStation()) return true;

  const company = context.selectedCompany();
  if (company) return router.createUrlTree(['/companies', company.id, 'stations']);
  return router.createUrlTree(['/companies']);
};

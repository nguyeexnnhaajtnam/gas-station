import { Injectable, signal } from '@angular/core';

/**
 * UI-only state for the async "open station" flow, so the topbar context
 * switcher can reflect the same in-flight request the Station page shows.
 * Does not touch CompanyContextService or the selection logic itself.
 */
@Injectable({ providedIn: 'root' })
export class StationActivationService {
  readonly activatingStationId = signal<string | null>(null);
}

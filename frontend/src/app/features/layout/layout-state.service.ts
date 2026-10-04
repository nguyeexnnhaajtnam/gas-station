import { BreakpointObserver } from '@angular/cdk/layout';
import { inject, Injectable, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { map } from 'rxjs';

/** Shell state shared by sidebar + topbar: drawer mode under 1024px. */
@Injectable({ providedIn: 'root' })
export class LayoutStateService {
  readonly compact = toSignal(
    inject(BreakpointObserver).observe('(max-width: 1023.98px)').pipe(map((s) => s.matches)),
    { initialValue: typeof window !== 'undefined' && window.innerWidth < 1024 },
  );
  readonly navOpen = signal(false);

  openNav() {
    this.navOpen.set(true);
  }

  closeNav() {
    this.navOpen.set(false);
  }
}

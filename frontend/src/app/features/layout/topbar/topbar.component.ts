import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ContextSwitcherComponent } from '../../../shared/components/context-switcher/context-switcher.component';
import { UserMenuComponent } from '../../../shared/components/user-menu/user-menu.component';
import { LucideBell, LucideMenu } from '../../../shared/icons';
import { LayoutStateService } from '../layout-state.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [ContextSwitcherComponent, UserMenuComponent, LucideBell, LucideMenu],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss',
})
export class TopbarComponent {
  readonly layout = inject(LayoutStateService);
}

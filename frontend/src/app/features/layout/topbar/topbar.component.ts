import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ContextSwitcherComponent } from '../../../shared/components/context-switcher/context-switcher.component';
import { UserMenuComponent } from '../../../shared/components/user-menu/user-menu.component';
import { LucideBell } from '../../../shared/icons';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [ContextSwitcherComponent, UserMenuComponent, LucideBell],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.scss',
})
export class TopbarComponent {}

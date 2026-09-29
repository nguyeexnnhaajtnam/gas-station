import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth.service';
import { LucideFuel } from '../../shared/icons';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, LucideFuel],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly message = signal('');
  readonly isError = signal(false);
  readonly form = this.fb.nonNullable.group({ username: ['', Validators.required], password: ['', Validators.required] });

  submit() {
    if (this.form.invalid || this.submitting()) return;
    this.submitting.set(true);
    this.message.set('');
    this.isError.set(false);
    const { username, password } = this.form.getRawValue();
    this.auth
      .login(username, password)
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: (r) => {
          if (r.status === 'AUTHENTICATED') {
            void this.router.navigate(['/companies']);
            return;
          }
          this.isError.set(r.status === 'REJECTED');
          this.message.set(r.message);
        },
        error: (e) => {
          this.isError.set(true);
          this.message.set(e?.error?.message ?? 'Không thể kết nối tới máy chủ.');
        },
      });
  }
}

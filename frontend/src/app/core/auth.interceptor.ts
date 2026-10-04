import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';
import { CompanyContextService } from './company-context.service';

const LOGIN_URL = '/api/v1/auth/login';

/** Attaches the bearer token to backend API calls and sends the user back to login when it is rejected. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const context = inject(CompanyContextService);
  const router = inject(Router);
  const isApi = req.url.startsWith(`${environment.apiBaseUrl}/api/`);
  const token = auth.token();
  const request = isApi && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(request).pipe(
    catchError((error: unknown) => {
      if (isApi && error instanceof HttpErrorResponse && error.status === 401 && !req.url.endsWith(LOGIN_URL) && auth.authenticated()) {
        auth.clear();
        context.clear();
        void router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};

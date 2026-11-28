import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService, LOGIN_URL } from '../auth/auth.service';
import { API_BASE_URL } from '../services/api';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith(`${API_BASE_URL}/`) || request.url === LOGIN_URL) return next(request);

  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.accessToken();
  const authenticatedRequest = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authenticatedRequest).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && auth.clearIfCurrent(token)) {
        void router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};
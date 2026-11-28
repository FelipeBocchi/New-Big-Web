import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../auth/auth.service';

export const sessionGuard: CanActivateFn = () =>
  inject(AuthService).accessToken() !== null || inject(Router).createUrlTree(['/login']);
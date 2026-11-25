import { ApplicationConfig, LOCALE_ID, provideZoneChangeDetection } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePtBr from '@angular/common/locales/pt';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './core/interceptons/auth.interceptor';

registerLocaleData(localePtBr, 'pt-BR');

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideAnimations(),
    { provide: LOCALE_ID, useValue: 'pt-BR' },
    provideHttpClient(withInterceptors([authInterceptor])),
  ],
};
import { HttpInterceptorFn, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';

export const mockAuthInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.endsWith('/api/login') && req.method === 'POST') {
    // Tipamos o body com segurança para o TypeScript
    const body = req.body as { email?: string; password?: string } | null;
    const email = body?.email;
    const password = body?.password;

    // Simula validação de credenciais
    if (email === 'admin@teste.com' && password === '123456') {
      return of(
        new HttpResponse({
          status: 200,
          body: {
            token: 'mock-jwt-token-xyz123',
            user: { id: 1, name: 'Usuário Teste', email }
          }
        })
      ).pipe(delay(800));
    }

    // Simula erro 401 (Não autorizado)
    return throwError(
      () => new HttpErrorResponse({
        status: 401,
        error: { message: 'E-mail ou senha incorretos' }
      })
    ).pipe(delay(800));
  }

  return next(req);
};
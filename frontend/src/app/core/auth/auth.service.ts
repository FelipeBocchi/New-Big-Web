import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map } from 'rxjs';
import { API_BASE_URL } from '../services/api';

export const LOGIN_URL = `${API_BASE_URL}/auth/login`;
const SESSION_KEY = 'newbig.session';

interface LoginResponse {
  token: string;
  tokenType: string;
  expiresIn: number;
}

interface Session {
  token: string;
  expiresAt: number;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private session = this.restore();

  login(email: string, password: string) {
    return this.http.post<LoginResponse>(LOGIN_URL, { email, password }).pipe(
      map(({ token, tokenType, expiresIn }) => {
        if (!token || tokenType !== 'Bearer' || !Number.isFinite(expiresIn) || expiresIn <= 0) {
          throw new Error('Resposta de login inválida');
        }

        const session = { token, expiresAt: Date.now() + expiresIn * 1000 };
        sessionStorage.setItem(SESSION_KEY, JSON.stringify(session));
        this.session = session;
      }),
    );
  }

  accessToken(): string | null {
    return this.session && this.session.expiresAt > Date.now() ? this.session.token : null;
  }

  logout(): void {
    this.session = null;
    sessionStorage.removeItem(SESSION_KEY);
  }

  clearIfCurrent(token: string | null): boolean {
    if (token !== this.session?.token && !(token === null && !this.accessToken())) return false;
    this.logout();
    return true;
  }

  private restore(): Session | null {
    try {
      const session: Partial<Session> | null = JSON.parse(sessionStorage.getItem(SESSION_KEY) ?? 'null');
      if (typeof session?.token === 'string' && typeof session.expiresAt === 'number' && session.expiresAt > Date.now()) {
        return { token: session.token, expiresAt: session.expiresAt };
      }
      sessionStorage.removeItem(SESSION_KEY);
    } catch {
      return null;
    }
    return null;
  }
}

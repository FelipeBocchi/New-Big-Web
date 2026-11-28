import { HttpErrorResponse } from '@angular/common/http';

export const API_BASE_URL = '/api';

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (!(error instanceof HttpErrorResponse)) return fallback;
  if (error.status === 0) return 'Sem conexão com o servidor.';
  if (error.status === 401) return 'Sua sessão expirou. Entre novamente.';

  const problem = error.error as { detail?: unknown; errors?: Record<string, unknown> } | null;
  const fieldMessages = Object.values(problem?.errors ?? {})
    .filter((message): message is string => typeof message === 'string');

  if (fieldMessages.length) return fieldMessages.join(' ');
  return typeof problem?.detail === 'string' ? problem.detail : fallback;
}

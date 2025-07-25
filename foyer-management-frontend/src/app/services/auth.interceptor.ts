import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  const authReq = req.clone({
    withCredentials: true,
    headers: req.headers.set('X-Requested-With', 'XMLHttpRequest')
  });

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !router.url.startsWith('/sign-in')) {
        router.navigate(['/sign-in'], {
          queryParams: { returnUrl: router.url },
          replaceUrl: true
        });
      }
      return throwError(() => error);
    })
  );
};

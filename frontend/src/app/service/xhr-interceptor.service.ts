import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpErrorResponse, HttpClient, HttpHeaders, HttpSentEvent, HttpHeaderResponse, HttpProgressEvent, HttpResponse, HttpUserEvent } from '@angular/common/http';
import { Observable, of, BehaviorSubject, Subject, throwError, pipe } from 'rxjs';
import { Router } from '@angular/router';
import { catchError, tap, switchMap, finalize, filter, take } from 'rxjs/operators';
import { Constants } from '../constants';
import { environment } from '@src/environments/environment';
import { isArray } from 'util';
// import * as loader from 'sdrc-loader';
// import {LoaderInterceptor } from 'sdrc-loader';

@Injectable()
export class XhrInterceptorService implements HttpInterceptor {

  baseUrl$ = "";
  isRefreshTokenExpired: boolean;
  isRefreshingToken: boolean = false;
  tokenSubject: BehaviorSubject<string> = new BehaviorSubject<string>(null);

  constructor(private router: Router, private http: HttpClient) {

  }

  addToken(req: HttpRequest<any>, token: string): HttpRequest<any> {
    let request = req;
    if (token) {
      let headers: HttpHeaders = request.headers.set('Authorization', 'Bearer ' + token);
      // headers.append("timeout", "300000")
      return request.clone({ headers: headers })
    }
    else
      return request.clone();
  }

  addBaseUrl(req: HttpRequest<any>) {
    let request = !req.url.includes("covid19rmrs") ? req.clone() : req.clone({ url: req.url });
    return request.clone();
  }

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    let request = this.addToken(req, localStorage.getItem('access_token'));
    return next.handle(this.addBaseUrl(request))
      .pipe(catchError(error => {
        if (error instanceof HttpErrorResponse) {
          let defaultMessage = "Something went worng";
          // let option = {
          //   message: error ? (error.error && error.error.message) ? error.error.message : (error.message ? error.message : defaultMessage) : defaultMessage,
          //   status: Constants.toastStatus.ERROR,
          // };
          switch ((<HttpErrorResponse>error).status) {
            case 500:
              break;
            case 401:
              // token expired, try refresh token
              return this.handle401Error(req, next);
            case 403:
              break;
          }
          return throwError(error);
        } else {
          return throwError(error);
        }
      }));
  }

  handle401Error(req: HttpRequest<any>, next: HttpHandler) {
    // if refresh token has been expired then this method is again called,
    // so during refresh token call, the url will be  /covid19rmrs/oauth/token,
    // we log out user if refresh token is also expired.
    if (req.url == "/covid19rmrs/oauth/token") {
      this.logoutUser();
    }
    if (!this.isRefreshingToken) {
      this.isRefreshingToken = true;

      // Reset here so that the following requests wait until the token
      // comes back from the refreshToken call.
      this.tokenSubject.next(null);
      return this.refreshToken()
        .pipe(switchMap((refreshToken: string) => {
          if (refreshToken) {
            this.tokenSubject.next(refreshToken);
            this.isRefreshingToken = false;
            return next.handle(this.addToken(req, refreshToken))

          }
          // If we don't get a new token, we are in trouble so logout.
          return this.logoutUser();
        }))
        .pipe(catchError(error => {
          // If there is an exception calling 'refreshToken', bad news so logout.
          return this.logoutUser();
        }))
        .pipe(finalize(() => {
          this.isRefreshingToken = false;
        }));
      // });

    } else {
      return this.tokenSubject
        .pipe(filter(token => token != null))
        .pipe(take(1))
        .pipe(switchMap(token => {
          return next.handle(this.addToken(req, token));
        }));
    }
  }

  handle400Error(error) {
    if (error && error.status === 400 && error.error && error.error.error === 'invalid_grant') {
      // If we get a 400 and the error message is 'invalid_grant', the token is no longer valid so logout.
      this.deleteCookies();

    }

    return Observable.throw(error);
  }



  logoutUser() {
    // Route to the login page (implementation up to you)
    this.deleteCookies();
    window.location.href = "login"
    return throwError("");
  }

  deleteCookies() {
    localStorage.clear();
  }


  refreshToken(): Observable<string> {
    const tokenObsr = new Subject<string>();
    const token_refreshed = localStorage.getItem("refresh_token");

    if (token_refreshed) {

      let URL: string = Constants.HOME_URL + 'oauth/token'
      const httpOptions = {
        headers: new HttpHeaders({
          'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
        })
      };
      let params = new URLSearchParams();
      params.append('refresh_token', localStorage.getItem('refresh_token'));
      params.append('grant_type', 'refresh_token')

      this.http.post<UserToken>(URL, params.toString(), httpOptions)
        .subscribe(response => {

          localStorage.setItem('access_token', response.access_token);

          tokenObsr.next(response.access_token);

        }, err => {
          this.logoutUser();
        });
    }
    return tokenObsr.asObservable();
  }
}
interface UserToken {
  access_token: string;
  refresh_token: string;
}
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router'
import { Constants } from './constants';
import { Observable, throwError, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AppService {

  userName: string;
  authenticated = false;
  logoutSuccess: boolean = false;
  validationMsg: any;
  _data: any;
  errorCode: any;
  errorMessage: string;
  cmsMenu;
  cmsSubmenu;

  constructor(private http: HttpClient, private router: Router) {
    // console.log(Cookie.get('access_token'));
    // this.userIdle.onTimeout().subscribe(() => console.log('Time is up!'));
  }
  checkLoggedIn(): boolean {
    if (!localStorage.getItem('access_token')) {
      return false
    } else {
      return true
    }
  }

  logout() {
    this.deleteCookies()
    this.router.navigate(['/']);
    this.logoutSuccess = true;
    setTimeout(() => {
      this.logoutSuccess = false;
    }, 2000)

  }
  deleteCookies() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('user_details');
    localStorage.removeItem('JSESSIONID');
    localStorage.clear();
  }

  saveToken(token) {
    var expireDate = new Date().getTime();
    let date = new Date(expireDate);

    localStorage.setItem("access_token", JSON.stringify(token));
    this.router.navigate(['/']);
  }

  getToken(): any {
    return localStorage.getItem("access_token");
  }

  authenticate(credentials, call) {
    this.authenticated = false;
    this.callServer(credentials).subscribe(response => {
      this._data = response;   //store the token
      localStorage.setItem('access_token', this._data.access_token);
      localStorage.setItem('refresh_token', this._data.refresh_token);

      const httpOptions = {
        headers: new HttpHeaders({
          'Authorization': 'Bearer ' + this._data.access_token,
          'Content-type': 'application/json'
        })
      };
      this.http.get(Constants.HOME_URL + 'oauth/user', httpOptions).subscribe(user => {
        localStorage.setItem('user_details', JSON.stringify(user));
        this.router.navigateByUrl('/report');
        this.authenticated = true;
      });
    }, error => {
      if (error == "Your account has been disabled; please contact your administrator for more information.")
        this.validationMsg = "Your account has been disabled, please contact your administrator for more information.";
      else if(error == "Your account is pending for approval."){
        this.validationMsg = "Your account is pending for approval."
      }
      else if(error == "Your registration has been rejected; please contact your administrator for more information."){
        this.validationMsg = "Your registration has been rejected; please contact your administrator for more information.";
      }
      else if (error == "Invalid Credentials !" || error == "Bad credentials")
        this.validationMsg = "The Username/Password you have entered is incorrect.";
      setTimeout(() => {
          this.validationMsg = "";
        }, 3000)
      this.authenticated = true;
    })
  }

  getUsername() {
    if (localStorage.getItem("user_details")) {
      // console.log(JSON.parse(Cookie.get("user_details")))
      return JSON.parse(localStorage.getItem("user_details")).user_name
    }
  }

  callServer(userDetails) {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
      })
    };

    let URL: string = Constants.HOME_URL + 'oauth/token'
    let params = new URLSearchParams();
    let uName = userDetails.username;
    params.append('username', uName.toLowerCase());
    params.append('password', userDetails.password);
    params.append('grant_type', 'password');

    return this.http.post(URL, params.toString(), httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse) {
    let message = "";
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error.message);
    } else {
      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong,
      console.error(
        `Backend returned code ${error.status}, ` +
        `body was: ${error.error.error_description}`);
    }
    // return an observable with a user-facing error message
    return throwError(
      //'Something bad happened; please try again later.');     
      error.error.error_description);
  };

  //handles nav-links which are going to be shown 
  checkUserAuthorization(route) {
    const expectedRoles = route;
    if (localStorage.getItem('access_token')) {
      var token = JSON.parse(localStorage.getItem('user_details')) || {};
    }
    let flag = false;
    if (this.checkLoggedIn() && token.authorities) {
      expectedRoles.forEach(expectedRole => {
        for (let i = 0; i < token.authorities.length; i++) {
          if (token.authorities[i] == expectedRole) {
            flag = true;
          }
        }
      });
    }
    return flag;
  }

  checkUserDesignation(designation) {
    const expectedRoles = designation;
    if (localStorage.getItem('access_token')) {
      var token = JSON.parse(localStorage.getItem('user_details'));
    }
    let flag = false;
    if (this.checkLoggedIn()) {
      expectedRoles.forEach(expectedRole => {
        for (let i = 0; i < token.designationNames.length; i++) {
          if (token.designationNames[i] == expectedRole) {
            flag = true;
          }
        }
      });
    }
    return flag;
  }

  getUserDetails() {
    return JSON.parse(localStorage.getItem('user_details'));
  }

  getUserAuthorities() {
    if (localStorage.getItem('access_token') && localStorage.getItem('user_details')) {
      let userDetails = JSON.parse(localStorage.getItem('user_details'));
      return ((typeof userDetails == "object") && userDetails.authorities) ? userDetails.authorities : [];
    }
    return []
  }

  getVisitorCount() {
    return this.http.get(Constants.HOME_URL + 'bypass/fetchVistorCount');
  }

  // doApiCall(payload) {
  //   let httpOptions = {
  //     headers: this.getHeaders(),
  //     params: payload.params,
  //     body: payload.body
  //   }
  //   return this.http.request(payload.method, payload.url, httpOptions)
  //     .pipe(map(res => {
  //       return of(res);
  //     }))
  //     .pipe(
  //       catchError(this.handleApiError.bind(this))
  //     );
  // }

  // getHeaders() {
  //   const headers = new HttpHeaders({
  //     'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
  //   });
  //   return headers;

  // }

  // private handleApiError(error: HttpErrorResponse) {
  //   let message = "";
  //   if (error.error instanceof ErrorEvent) {
  //     // A client-side or network error occurred. Handle it accordingly.
  //     console.error('An error occurred:', error.error.message);
  //   } else {
  //     // The backend returned an unsuccessful response code.
  //     // The response body may contain clues as to what went wrong,
  //     console.error(
  //       `Backend returned code ${error.status}, ` +
  //       `body was: ${error.error.error_description}`);
  //   }
  //   // return an observable with a user-facing error message
  //   let option = {
  //     message: "Something went worng",
  //     status: "SUCCESS",
  //     toastType: "modal"
  //   }
  //   this.toastService.showToast(option.message, option.status, option.toastType);
  //   return of(null);
  //   // throwError(
  //   //'Something bad happened; please try again later.');     
  //   // error.error.error_description);
  // };


}

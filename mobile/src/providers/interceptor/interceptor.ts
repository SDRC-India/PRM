import { HttpClient, HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AlertController, App } from 'ionic-angular';
import { Observable } from 'rxjs';
import { ConstantProvider } from '../constant/constant';
import { Storage } from '@ionic/storage';
import { UserServiceProvider } from '../user-service/user-service';
import {_throw} from 'rxjs/observable/throw';
/*
  Generated class for the InterceptorProvider provider.
*/
@Injectable()
export class InterceptorProvider implements HttpInterceptor {
  // @ViewChild(Nav) nav: Nav;
  constructor(public http: HttpClient,private storage: Storage,private alertCtrl: AlertController,
              public app: App,private userService: UserServiceProvider){}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    // when not calling /oauth/token, if is executed
    if(!(req.url.includes('oauth/token')))
    {
        let token = this.userService.accessToken
        if (token)
        {
          req = req.clone({ headers: req.headers.set('Authorization', 'Bearer ' + token) })
        }

    }

    // login, form fetch
    return next.handle(req).catch(err => {
      console.log(err);
            if (err.status === 401) {
                if ((err.error.error_description as string).includes("Access token expired")) {
                    //Genrate params for token refreshing
                    let refresh_token = this.userService.refreshToken
                    let URL: string = ConstantProvider.baseUrl + 'oauth/token'
                    const httpOptions = {
                      headers: new HttpHeaders({
                          'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
                      })
                  };
                    let params = new URLSearchParams();
                    params.append('refresh_token',refresh_token );
                    params.append('grant_type', 'refresh_token')
                  return this.http.post(URL, params.toString(), httpOptions).flatMap(
                    (data: any) => {

                      this.userService.accessToken = data.access_token
                      this.userService.refreshToken = data.refresh_token
                      //If reload successful update tokens
                      this.userService.updateTokens(data.access_token, data.refresh_token);
                      //Clone our fieled request ant try to resend it
                      return next.handle(this.addAuthenticationToken(req));

                    },error=>{
                      this.deleteCookies()
                    }
                  );
                }else {
                    //Logout from account or do some other stuff
                    console.log("Not Access token expired ");
                   this.logoutUser();

                }
            }
            return Observable.throw(err);
    });

  }

  addAuthenticationToken(request) {
    // Get access token from Local Storage
    const accessToken = this.userService.accessToken
    // If access token is null this means that user is not logged in
    // And we return the original request
    if (!accessToken) {
        return request;
    }
    // We clone the request, because the original request is immutable
    // return request.clone({
    //     setHeaders: {
    //         Authorization: this.auth.getAccessToken()
    //     }
    // });

    return request.clone({ headers: request.headers.set('Authorization', 'Bearer ' + accessToken) })
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

    let confirm = this.alertCtrl.create({
      enableBackdropDismiss: false,
      title: 'Warning',
      message: "<strong>Your current session has been expired.Please login again to continue</strong>",
      cssClass:'buttonCss',
      buttons: [{
          text: 'OK',
          cssClass: 'exit-button',
          handler: () => {
            this.deleteCookies();
          }
        }
      ]
    });
    confirm.present();

    return _throw("");
  }

  deleteCookies() {
    this.storage.remove(ConstantProvider.dbKeyNames.userAndForm).then(data => {
      let navz=this.app.getActiveNavs()
      navz[0].setRoot('LoginPage');
    });
  }


}

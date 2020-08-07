import { URLSearchParams } from '@angular/http';
import { Injectable } from '@angular/core';
import { ConstantProvider } from '../constant/constant';
import 'rxjs/add/operator/map';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { Storage } from '@ionic/storage'
import { Events } from 'ionic-angular';
import { UserServiceProvider } from '../user-service/user-service';

/*
  Generated class for the LoginServiceProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class LoginServiceProvider {

  grant_type: any;
  headers: any;
  constructor(private http: HttpClient,
    private storage: Storage, public events: Events, private userService: UserServiceProvider) {

  }
  /**
   * This method will authenticate the username and password by calling the rest api for authentication
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param Authorization(username and password are set in header)
   * @since 0.0.1
   */
  authenticate(credentials): Promise < any > {

    let promise = new Promise < any > ((resolve, reject) => {

    const httpOptions = {
      headers: new HttpHeaders({
        'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
      })
    };

    let URL: string = ConstantProvider.baseUrl +'oauth/token'

    let params = new URLSearchParams();
    params.append('username', credentials.username);
    params.append('password', credentials.password);
    params.append('grant_type','password');

    this.http.post(URL, params.toString(), httpOptions)
    .subscribe(res => {
          let  loginResponse = {
              accessToken: res['access_token'],
              tokenType : res['token_type'],
              refreshToken: res['refresh_token'],
              expires: res['expires_in']
            }
            resolve(loginResponse);
          }, (err) => {
            // console.log(err)
            reject(err);
          });
    });
    return promise
  }

  async getUserName(){
    await this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then(userAndForm=>{
      if(userAndForm){
        this.userService.user = userAndForm['user']
        this.events.publish('user',userAndForm['user'])
      }
    });
  }

  addRememberMe() {
    this.storage.set(ConstantProvider.dbKeyNames.REMEMBER_ME, true);
  }

  removeRememberMe() {
    this.storage.remove(ConstantProvider.dbKeyNames.REMEMBER_ME);
  }

  getRememberMe(): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      this.storage.get(ConstantProvider.dbKeyNames.REMEMBER_ME).then((rememberMe) => {
        resolve (rememberMe ? true : false);
      }).catch(() => {
        resolve (false);
      })
    })
  }

}

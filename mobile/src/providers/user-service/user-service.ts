import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Storage } from '@ionic/storage'
import { ConstantProvider } from '../constant/constant';
import { Events  } from 'ionic-angular';

/**
 * This provider will help to keep all the user details in local storage
 * @author Jagat Bandhu (jagat@sdrc.co.in)
 * @since 0.0.1
 */
@Injectable()
export class UserServiceProvider {

  user: ILoginData;
  accessToken:string="";
  refreshToken:string="";
  userTryingToLogout = false;
  constructor(public http: HttpClient,public storage:Storage,public events:Events) {

  }

  /**
   * This method will help to keep all the user details in local storage
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   */
 saveUserFormAndTokensAndPublishUser(userAndForm :IUserAndForm):Promise<any>{
   return new Promise<any> ((resolve,reject) =>{
    this.storage.set(ConstantProvider.dbKeyNames.userAndForm,  userAndForm).then(()=>{
      this.user = userAndForm['user']
      this.events.publish('user',userAndForm['user']);
      resolve( this.user)
      }).catch((error)=>{
          reject(error)
      });
   })
  }
  /**
   * This method will update all forms in local storage
   * @author Sourav Nath
   * @since 0.0.1
   */
  saveNewForms(userAndForm :IUserAndForm):Promise<any>{
    return new Promise<any> ((resolve,reject) =>{
     this.storage.set(ConstantProvider.dbKeyNames.userAndForm,  userAndForm).then(()=>{
      this.events.publish('takeRefresh',true)
       resolve( this.user)
       }).catch((error)=>{
           reject(error)
       });
    })
   }
  /**
   * This method will update all save and finalize form in local storage
   * @author Sourav Nath
   * @since 0.0.1
   */
  updateSaveAndFinalizeForm(key,data):Promise<any>{
    return new Promise<any> ((resolve,reject) =>{
     this.storage.set(key, data).then(()=>{
      this.events.publish('takeRefresh',true)
       resolve(this.user)
       }).catch((error)=>{
           reject(error)
       });
    })
   }
  /**
   * This method will help to get all the user details from local storage
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   */
  async getUserAndForm(){
    return await this.storage.get(ConstantProvider.dbKeyNames.userAndForm)
  }

  async initializeUserInService() {
    await this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then(userAndForm=>{
      this.user = userAndForm['user']
      this.events.publish('user',userAndForm['user']);
      this.accessToken= userAndForm['tokens'].accessToken
      this.refreshToken= userAndForm['tokens'].refreshToken
    })
    }

    /**
   * This method will help to upadete access token through interceptor
   * @author Ratikant (ratikant@sdrc.co.in)
   * @since 1.1.0
   */
  updateTokens(access_token: string, refresh_token: string){
    this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then(userAndForm=>{
      userAndForm['tokens'].accessToken = access_token
      userAndForm['tokens'].refreshToken = refresh_token
      this.storage.set(ConstantProvider.dbKeyNames.userAndForm,userAndForm);
    })

  }
}

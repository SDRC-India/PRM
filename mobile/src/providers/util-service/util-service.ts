import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Platform } from 'ionic-angular';

/*
  Generated class for the UtilServiceProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
declare var navigator: any;
declare var Connection: any;

@Injectable()
export class UtilServiceProvider {

  constructor(public http: HttpClient, private platform: Platform) {
  }

  /**
   * This method will check the internet connection
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 1.0.0
   *
   * @memberof UtilServiceProvider
   */
  checkInternet(): Promise<any>{

    let promise = new Promise < any > ((resolve, reject) => {
      this.platform.ready().then(() => {
        var networkState = navigator.connection.type;
        var states = {};
        states[Connection.UNKNOWN]  = 'Unknown connection';
        states[Connection.ETHERNET] = 'Ethernet connection';
        states[Connection.WIFI]     = 'WiFi connection';
        states[Connection.CELL_2G]  = 'Cell 2G connection';
        states[Connection.CELL_3G]  = 'Cell 3G connection';
        states[Connection.CELL_4G]  = 'Cell 4G connection';
        states[Connection.CELL]     = 'Cell generic connection';
        states[Connection.NONE]     = 'No network connection';
        if(states[networkState] == states[Connection.NONE] || states[networkState] == states[Connection.UNKNOWN]){
          resolve(false)
        }else{
          resolve(true)
        }
      });
    });
    return promise
    // if(isWeb){

    // }
  }

}

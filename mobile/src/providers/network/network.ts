import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

/*
  Generated class for the NetworkProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
export enum ConnectionStatusEnum {
    Online,
    Offline
}
@Injectable()
export class NetworkProvider {

  public static status: ConnectionStatusEnum = ConnectionStatusEnum.Online;
  private static online$: Observable<string>;
  private static offline$: Observable<string>;

  public init() {
    NetworkProvider.online$ = Observable.fromEvent(window, 'online');
    NetworkProvider.offline$ = Observable.fromEvent(window, 'offline');

    NetworkProvider.online$.subscribe(e => {
      console.log('Online');
      NetworkProvider.status = ConnectionStatusEnum.Online;
    });

    NetworkProvider.offline$.subscribe(e => {
      console.log('Offline');
      NetworkProvider.status = ConnectionStatusEnum.Offline;
    });
  }

  constructor() {
    // NetworkProvider.init();
  }

}

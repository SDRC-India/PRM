import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import { Location } from '@angular/common';
import { AppService } from '../app.service';

@Injectable({
  providedIn: 'root'
})
export class PermissionGuard implements CanActivate {

  permissions = {
    USER_MGMT_ALL_API: "USER_MGMT_ALL_API",
    dataentry_HAVING_write: "dataentry_HAVING_write",
    DOWNLOAD_RAWDATA_REPORT: "DOWNLOAD_RAWDATA_REPORT",
    CMS_Admin: "CMS_Admin"
  }
  permissionMapper = {
    "covid19-tracker": [this.permissions.dataentry_HAVING_write,],
    "create-user": [this.permissions.USER_MGMT_ALL_API],
  }
  constructor(private app: AppService, private router: Router, private _location: Location) { }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    let URL = state.url ? state.url.split("/")[1]:null;
    // return true;
    if (URL && this.permissionMapper[URL] && this.app.checkUserAuthorization(this.permissionMapper[URL])) {
      return true;
    } else {
      this.router.navigateByUrl('/');
    }
  }
}

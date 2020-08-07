import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Observable } from 'rxjs';
import { Router } from '@angular/router';
import {Location} from '@angular/common';
import { AppService } from '../app.service';

@Injectable({
  providedIn: 'root'
})
export class LoggedinGuard implements CanActivate {
  constructor(private app: AppService, private router: Router, private _location: Location) { } 

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
      
      if(!this.app.checkLoggedIn()){
        return true;
      }else{
        const userDetails = this.app.getUserDetails();
        if (userDetails.designations[0] === 'ADMIN'){
          this.router.navigateByUrl('/dashboard');
      }
       else {
        this.router.navigateByUrl('/dashboard');
        }
      }
  }
}

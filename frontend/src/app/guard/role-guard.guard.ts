import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AppService } from '../app.service';

@Injectable({
  providedIn: 'root'
})
export class RoleGuardGuard implements CanActivate {  
  constructor(private app: AppService, private router: Router) { }

  canActivate(next: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {

    const expectedRoles = next.data.expectedRoles;

    if(this.app.checkLoggedIn()){
      const token = JSON.parse(localStorage.getItem('user_details'));
      let flag = false;
      expectedRoles.forEach(expectedRole => {
       for(let i=0; i< token.authorities.length; i++){
        if (token.authorities[i] == expectedRole) {
          flag = true;
        }  
       }           
      });
      if(!flag)
      {
        this.app.errorCode=403;
        this.app.errorMessage="You are not authorized to view this page !"
       this.router.navigate(['/exception']);
      }
       return flag;
    }
    else{
      this.app.errorCode=401;
      this.app.errorMessage="Your session has been expired.Please login again !"

      this.router.navigate(['/exception']);
      return false;
    }
  }
}

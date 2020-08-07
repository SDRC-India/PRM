import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { Exception404Component } from './exception404/exception404.component';
import { LoggedinGuard } from '@src/app/guard/loggedin.guard';
import { LoginComponent } from '@src/app/login/login.component';
import { PermissionGuard } from '@src/app/guard/permission.guard';
import { ForgotpassComponent } from './forgotpass/forgotpass.component';
// import { ChangePasswordComponent } from './change-password/change-password.component';
const routes: Routes = [
  {
    path: '',
    component: LoginComponent,
    canActivate: [LoggedinGuard]
  },
  {
    path: 'report',
    loadChildren: './report/report.module#ReportModule',
  },
  {
    path: 'create-user',
    loadChildren: './usermangament/usermangament.module#UsermangamentModule',
    canActivate: [PermissionGuard]
  },
  {
    path: 'exception',
    component: Exception404Component,
    pathMatch: 'full'
  },
  { 
    path: 'login', 
    pathMatch: 'full', 
    component: LoginComponent,
    canActivate: [LoggedinGuard]
  },
  {
    path: 'forgotpass',
    component: ForgotpassComponent,
    pathMatch: 'full'
  },
  {
    path: 'login/forgotpass',
    component: ForgotpassComponent,
    pathMatch: 'full'
  }
  // { 
  //   path: 'covid19-tracker', 
  //   pathMatch: 'full', 
  //   component: Covid19TrackerEntryComponent,
  //   canActivate:[PermissionGuard]
  // }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

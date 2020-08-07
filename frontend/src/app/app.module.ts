import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { NgxPaginationModule } from 'ngx-pagination';
import { MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatSidenavModule, MatExpansionModule, MatListModule, MatSnackBarModule, MatToolbarModule, MatDatepickerModule } from '@angular/material'
import { SdrcLoaderModule } from 'sdrc-loader';
import { DatePipe } from '@angular/common';
import { ToastModule } from 'ng6-toastr';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HeaderComponent } from './fragments/header/header.component';
import { FooterComponent } from './fragments/footer/footer.component';
import { LoginComponent } from './login/login.component';
import { Exception404Component } from './exception404/exception404.component';
import { WorkinProgressComponent } from '@src/app/fragments/workin-progress/workin-progress.component';

import { XhrInterceptorService } from './service/xhr-interceptor.service';
import { AppService } from './app.service';
import { SessionCheckService } from './service/session-check.service';
import { StaticHomeService } from './service/static-home.service';
import { CustomValidatorsService } from './service/custom-validators.service';
import { AuthGuard } from './guard/auth.guard';
import { RoleGuardGuard } from './guard/role-guard.guard';
import { LoggedinGuard } from './guard/loggedin.guard';

import { UsermangamentModule } from './usermangament/usermangament.module';

import { FormService } from '@src/app/service/form.service';
import { PermissionGuard } from '@src/app/guard/permission.guard';
import { ForgotpassComponent } from './forgotpass/forgotpass.component';
@NgModule({
  declarations: [
    AppComponent,
    HeaderComponent,
    FooterComponent,
    Exception404Component,
    LoginComponent,
    WorkinProgressComponent,
    ForgotpassComponent
    // DisableOptionsPipe,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatFormFieldModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatInputModule, ToastModule.forRoot(),
    SdrcLoaderModule,
    MatIconModule,
    MatSidenavModule,
    MatExpansionModule,
    MatSnackBarModule,
    MatListModule,
    MatToolbarModule,
    UsermangamentModule,
    NgxPaginationModule,

  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: XhrInterceptorService, multi: true },
    // [{ provide: DEFAULT_TIMEOUT, useValue: 30000 }],
    XhrInterceptorService,SessionCheckService,
    AppService,
    AuthGuard,
    RoleGuardGuard,
    LoggedinGuard,
    PermissionGuard,
    SessionCheckService,
    StaticHomeService,
    DatePipe,
    CustomValidatorsService,
    FormService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

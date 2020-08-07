import { HttpClient } from '@angular/common/http';
// import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { LoginPage } from './login';
// import { createTranslateLoader } from '../../config/translate';

@NgModule({
  declarations: [
    LoginPage,
  ],
  imports: [
    // TranslateModule.forRoot({
    //   loader: {
    //     provide: TranslateLoader,
    //     useFactory: createTranslateLoader,
    //     deps: [HttpClient],
    //   },
    // }),
    IonicPageModule.forChild(LoginPage),
  ],
})
export class LoginPageModule {}

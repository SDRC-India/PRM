import { HttpClient } from '@angular/common/http';
// import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { HomePage } from './home';
import { PipesModule } from '../../pipes/pipes.module';
import { createTranslateLoader } from '../../config/translate';

@NgModule({
  declarations: [
    HomePage,
  ],
  imports: [
    // TranslateModule.forRoot({
    //   loader: {
    //     provide: TranslateLoader,
    //     useFactory: createTranslateLoader,
    //     deps: [HttpClient],
    //   },
    // }),
    IonicPageModule.forChild(HomePage),PipesModule
  ],
})
export class HomePageModule {}

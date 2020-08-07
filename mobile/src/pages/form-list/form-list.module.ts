import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { FormListPage } from './form-list';
import { PipesModule } from '../../pipes/pipes.module';

@NgModule({
  declarations: [
    FormListPage,
  ],
  imports: [
    IonicPageModule.forChild(FormListPage),PipesModule
  ]
})
export class FormListPageModule {}

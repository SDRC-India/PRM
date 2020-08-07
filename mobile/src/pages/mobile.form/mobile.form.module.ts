import { HttpClient } from '@angular/common/http';
// import { TranslateModule, TranslateLoader } from '@ngx-translate/core';
import { NgModule } from '@angular/core';
import { IonicPageModule } from 'ionic-angular';
import { MobileFormComponent } from './mobile.form';
import { ComponentsModule } from '../../components/components.module';
import { ObjIteratePipe } from '../../pipes/obj-iterate.pipe';
import { SortItemMobilePipe } from '../../pipes/sort-item-mobile/sort-item-mobile';
import { IonicSelectableModule } from 'ionic-selectable';
import { createTranslateLoader } from '../../config/translate';
import { MyDatePickerModule } from 'mydatepicker';
import { DatePickerModule } from 'ionic-calendar-date-picker';
import { MatButtonModule, MatCardModule, MatTabsModule, MatChipsModule, MatIconModule, MatToolbarModule, MatDatepickerModule, MatFormFieldModule, MatNativeDateModule } from "@angular/material";

@NgModule({
  declarations: [
    MobileFormComponent,
    ObjIteratePipe,
    SortItemMobilePipe
  ],
  imports: [
    // TranslateModule.forRoot({
    //   loader: {
    //     provide: TranslateLoader,
    //     useFactory: createTranslateLoader,
    //     deps: [HttpClient],
    //   },
    // }),
    IonicPageModule.forChild(MobileFormComponent),
    ComponentsModule,
    MyDatePickerModule,
    DatePickerModule,
    IonicSelectableModule,
    MatButtonModule,
    MatCardModule,
    MatTabsModule,
    MatChipsModule,
    MatIconModule,
    MatToolbarModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatNativeDateModule

  ]
})
export class FromPageModule {}

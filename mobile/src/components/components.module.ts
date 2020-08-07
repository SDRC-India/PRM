import { NgModule } from '@angular/core';
import { WebFormComponent } from './web/web.form';
import { CommonModule } from '@angular/common';
import { IonicModule } from 'ionic-angular';
import { RemoveExtraKeysPipe } from '../pipes/remove-extra-keys';
import { MyDatePickerModule } from 'mydatepicker';
import { AmazingTimePickerModule } from 'amazing-time-picker';
import { MatDatepickerModule, MatFormFieldModule, MatNativeDateModule, MatInputModule } from '@angular/material';
import { SortItemPipe } from '../pipes/sort-item/sort-item';
import { IonicSelectableModule } from 'ionic-selectable';

export const MY_FORMATS = {
    parse: {
      dateInput: 'MM/YYYY',
    },
    display: {
      dateInput: 'MM/YYYY',
      monthYearLabel: 'MMM YYYY',
      dateA11yLabel: 'LL',
      monthYearA11yLabel: 'MMMM YYYY',
    },
  };


@NgModule({
	declarations: [
        WebFormComponent,
        RemoveExtraKeysPipe,
        SortItemPipe
    ],
	imports: [
        CommonModule,
        IonicModule,
        MyDatePickerModule,
        AmazingTimePickerModule,
        MatDatepickerModule,
        MatFormFieldModule,
        MatNativeDateModule,
        MatInputModule,
        IonicSelectableModule
    ],
	exports: [
        WebFormComponent],


})

export class ComponentsModule {}


import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ReportRoutingModule } from './report-routing.module';
import { RawDataReportComponent } from './raw-data-report/raw-data-report.component';
import { MatFormFieldModule, MatSelectModule, MatDatepickerModule, MatInputModule, MatNativeDateModule, MatDialogModule } from '@angular/material';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { QueryBasedReportComponent } from './query-based-report/query-based-report.component';
import { QueryFiltersComponent } from './query-filters/query-filters.component';
import { ReportComponent } from './report/report.component';
import { SortbyPipe } from './sortby.pipe';
import { TableModule } from 'lib-table/public_api';
import { DialogMessageComponent } from './dialog-message/dialog-message.component';

@NgModule({
  entryComponents: [DialogMessageComponent],
  declarations: [RawDataReportComponent, QueryBasedReportComponent,
     QueryFiltersComponent, ReportComponent, SortbyPipe, DialogMessageComponent],
  imports: [
    CommonModule,
    ReportRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatInputModule,
    TableModule,
    MatDialogModule
  ]
})
export class ReportModule { }

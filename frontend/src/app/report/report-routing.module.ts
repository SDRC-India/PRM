import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { RawDataReportComponent } from './raw-data-report/raw-data-report.component';
import { QueryBasedReportComponent } from './query-based-report/query-based-report.component';
import { ReportComponent } from './report/report.component';

const routes: Routes = [
  {
    path:"",
    component: ReportComponent,
    pathMatch: "full"
  },
  {
    path:"raw-data",
    component: RawDataReportComponent,
    pathMatch: "full"
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportRoutingModule { }

import { Component, OnInit } from '@angular/core';
import { ReportService } from '../report.service';

@Component({
  selector: 'app-raw-data-report',
  templateUrl: './raw-data-report.component.html',
  styleUrls: ['./raw-data-report.component.scss']
})
export class RawDataReportComponent implements OnInit {

  selections: any = {};
  minDate = new Date("12-19-2019");
  todayDate = new Date()

  reportTypes: any = [
    {
      formId: 1,
      name: "Individual Volunteer Report"
    },
    {
      formId: 2,
      name: "Organizational Volunteer Report"
    }
  ]

  constructor(private reportService: ReportService) { }

  ngOnInit() {
  }

  downloadReport() {
    let sDate = this.selections.fromDate.toLocaleDateString().replace(/\//g, "-");;
    let eDate = this.selections.toDate.toLocaleDateString();
    this.reportService.getRawDataReport(this.selections.reportType.formId).subscribe(res => {
      // console.log(res);
      this.downloadFile(res);
    })
  }

  downloadFile(fileName) {
    this.reportService.downloadReport(fileName).subscribe(res => {
      // console.log(res)
    })
  }

}

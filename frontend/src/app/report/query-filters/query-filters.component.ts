import { Component, OnInit, Input } from '@angular/core';
import { ReportService } from '../report.service';
import { saveAs} from 'save-as';
import { DialogMessageComponent } from '../dialog-message/dialog-message.component';
import { MatDialog } from '@angular/material';

@Component({
  selector: 'app-query-filters',
  templateUrl: './query-filters.component.html',
  styleUrls: ['./query-filters.component.scss']
})
export class QueryFiltersComponent implements OnInit {

  filters: any = {};
  minDate = new Date("12-19-2019");
  todayDate = new Date()
  selectedServiceCategories: any;
  selectedLeftTab: string = 'gender';
  @Input() allOptions: any;
  allPreferredDistricts: any = [];
  allPreferredBlocks: any = [];
  allPreferredGP: any = [];
  reportService: ReportService;
  areaLevels = [
    {
      areaLevelId: 1,
      areaLevelName: "District Level"
    },
    {
      areaLevelId: 2,
      areaLevelName: "Block Level"
    },
    {
      areaLevelId: 3,
      areaLevelName: "Gram Panchayat Level"
    }
  ]
  serviceTypes = [
    {
      id: 1,
      name: "Public awareness and Information"
    },
    {
      id: 2,
      name: "On-field support"
    },
    {
      id: 3,
      name: "Essential services"
    }, {
      id: 4,
      name: "Health"
    },
    {
      id: 5,
      name: "Relief Camp Management"
    }
  ]
  constructor(private reportProvider: ReportService, private dialog: MatDialog) {
    this.reportService = reportProvider;
  }

  ngOnInit() {
    this.getAllPreferredDistricts(3, 2)
  }

  selectLeftTab(tab) {
    this.selectedLeftTab = tab;
  }

  getAllPreferredDistricts(areaLevelId, parentAreaId) {
    this.allPreferredBlocks = [];
    this.filters.districts = undefined;
    this.filters.blocks = undefined;
    this.filters.gps = undefined;
    this.reportService.getAreaDetails(areaLevelId, parentAreaId).subscribe(res => {
      this.allPreferredDistricts = res;
    })
  }
  getPreferredBlocks(areaLevelId, parentAreaId) {
    this.allPreferredGP = []
    this.filters.blocks = undefined;
    this.filters.gps = undefined;
    this.reportService.getAreaDetails(areaLevelId, parentAreaId).subscribe(res => {
      this.allPreferredBlocks = res;
    })
  }

  getPreferredGP(areaLevelId, parentAreaId) {
    this.filters.gps = undefined;
    this.reportService.getAreaDetails(areaLevelId, parentAreaId).subscribe(res => {
      this.allPreferredGP = res;
    })
  }

  resetAll() {
    this.filters = {};
    // this.ApplyFilter();
  }

  ApplyFilter() {
    let districts, blocks;
    if (this.filters.areaLevel == 2) {
      if(this.filters.districts) districts = [this.filters.districts];
      blocks = this.filters.blocks
    } else if (this.filters.areaLevel == 3) {
      if(this.filters.districts) districts = [this.filters.districts]
      if(this.filters.blocks) blocks = [this.filters.blocks]
    } else {
      districts = this.filters.districts;
      blocks = this.filters.blocks
    }
    // if(typeof this.filters.areaLevel == 'number')
    // this.filters.areaLevel = [this.filters.areaLevel]
    let data = {
      areaLevel: this.filters.areaLevel ? this.filters.areaLevel : null,
      districts: districts ? districts : [],
      blocks: blocks ? blocks : [],
      gps: this.filters.gps ? this.filters.gps : [],
      genders: this.filters.genders ? this.filters.genders : [],
      qualifications: this.filters.qualifications ? this.filters.qualifications : [],
      professions: this.filters.professions ? this.filters.professions: [],
      vocationals: this.filters.vocationals ? this.filters.vocationals: [],
      // healthCareProfessionals: this.filters.healthCareProfessionals ? this.filters.healthCareProfessionals: [],
      workFrom: [],
      workTime: [],
      awareness: this.filters.awareness ? this.filters.awareness : [],
      fieldSupport: this.filters.fieldSupport ? this.filters.fieldSupport : [],
      essentials: this.filters.essentials ? this.filters.essentials : [],
      health: this.filters.health ? this.filters.health : [],
      reliefCampMgmt: this.filters.reliefCampMgmt ? this.filters.reliefCampMgmt : [],
      availability: this.filters.availability ? this.filters.availability: []
    }
    // this.filters.workFrom = this.filters.fromDate.toLocaleDateString().replace(/\//g, "-");
    // this.filters.workTime = this.filters.toDate.toLocaleDateString().replace(/\//g, "-");
    this.reportService.filterVolunteer(data).subscribe(res => {
      if(res == "nodata") {
        const dialogRef = this.dialog.open(DialogMessageComponent,
          { width: '400px', disableClose: true, data: {header: 'Info', msg: 'No volunteer available with these filters', button: 'Ok' } });
      } else {
        this.downloadFile(res);
      }
      
    })
  }

  getObjectByKey(key, keyValue, arr) {
    return arr.filter(a => a[key] == keyValue)[0]
  }

  downloadFile(fileName) {
    this.reportService.downloadReport(fileName).subscribe(data => {
      // console.log(res)
      saveAs(data, fileName)
    })
  }

}

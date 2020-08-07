import { Component, OnInit } from '@angular/core';
import { AppService } from '@src/app/app.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

  visitorCount:any = 0;
  constructor(private appService: AppService) { }

  ngOnInit() {
    // this.getVisitorsCount();
  }

  getVisitorsCount() {
    this.appService.getVisitorCount().subscribe(data=>{
      this.visitorCount=data;
    })
  }
}

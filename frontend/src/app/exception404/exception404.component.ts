import { Component, OnInit } from '@angular/core';
import { AppService } from '../app.service';

@Component({
  selector: 'app-exception404',
  templateUrl: './exception404.component.html',
  styleUrls: ['./exception404.component.scss']
})
export class Exception404Component implements OnInit {


  constructor(public appService:AppService) { }

  ngOnInit() {
  }

}

import { Component, OnInit, EventEmitter, Output, OnChanges } from '@angular/core';
import { Route, Router } from '@angular/router';
import { AppService } from '../../app.service';
declare var $: any;
@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit, OnChanges {
  @Output() toggleSidenav = new EventEmitter<void>();
  router: Router;
  app: any;
  userName: string;
  // @Input() routerLinkActiveIsDisabled: boolean;
  constructor(router: Router, private appService: AppService) {
    this.router = router;
    this.app = appService;
    this.app.userName = this.app.getUsername();
  }

  ngOnInit() { }

  ngOnChanges() {
    this.app.userName = this.app.getUsername();
  }
  setFontSize(fontSize) {
    $("#bodyMain :not(svg, polygon, g, path, line, text)").css("font-size", fontSize + "px");

  }
  //handles nav-links which are going to be shown 
  checkUserAuthorization(route) {

    const expectedRoles = route;
    if (localStorage.getItem('access_token')) {
      var token = JSON.parse(localStorage.getItem('access_token'));
      this.app.userName = token.username;
    }
    let flag = false;
    if (this.appService.checkLoggedIn()) {
      expectedRoles.forEach(expectedRole => {
        if (token.roles[0] == expectedRole) {
          flag = true;
        }
      });
    }
    return flag;
  }


  logout() {
    this.appService.logout();
    this.app.userName = "";
  }


}

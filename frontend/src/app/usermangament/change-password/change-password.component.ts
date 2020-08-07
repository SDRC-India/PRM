import { Component, OnInit } from '@angular/core';
import { FormGroup, NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router, RoutesRecognized } from '@angular/router';
import { filter } from 'rxjs/operators';

import { Constants } from 'src/app/constants';
import { UsermanagementService } from '../services/usermanagement.service';
declare var $: any;

@Component({
  selector: 'app-change-password',
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.scss']
})
export class ChangePasswordComponent implements OnInit {

  form: FormGroup;
  formFields: any;
  formFieldsAll: any;
  payLoad = '';
  areaDetails: any;
  selectedRole: any;

  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
  userId: any;
  validationMsg: any;
  user: any;
  disableUserId: number;
  selectedSubpartner: any;
  passwordRegex = /^(?=.*[0-9])(?=.*[!@#$%^&*_-])[a-zA-Z0-9!@#$%^&*_-]{7,15}$/;
  passwordDetails: any;


  userManagementService: UsermanagementService;

  constructor(private http: HttpClient, private userManagementProvider: UsermanagementService, private router: Router) {
    this.userManagementService = userManagementProvider;
  }

  ngOnInit() {

    this.router.events
      .pipe(filter((e: any) => e instanceof RoutesRecognized))
      .subscribe((e: any) => {
        // if (this.router.url == "/reset-password" && e.url != '/edit-user') {
        if (this.router.url == "/change-password") {
          this.userManagementService.resetPasswordDetails = {};
          this.userManagementService.searchTexts = "";
        }
      });
    this.userManagementService.e = 1;
    this.userManagementService.resetPasswordDetails = {};
    // if (!this.userManagementService.userRoles)
    this.userManagementService.getUserRoles().subscribe(data => {
      this.userManagementService.userRoles = data;
    })

    // if (!this.userManagementService.allPartners)
    // this.userManagementService.getAllPartners().subscribe(data => {
    //   this.userManagementService.allPartners = data;
    // })

    // if (!this.userManagementService.allSubpartners)
    // this.userManagementService.getAllSubpartners().subscribe(data => {
    //   this.userManagementService.allSubpartners = data;
    // })

    // if ((window.innerWidth) <= 767) {
    //   $(".left-list").attr("style", "display: none !important");
    //   $('.mob-left-list').attr("style", "display: block !important");
    // }
  }
  getUsers() {
    this.userManagementService.getUsersByRoleAndPartner(this.userManagementService.resetPasswordDetails.selectedRole.id).subscribe(res => {
      this.userManagementService.resetPasswordDetails.allUser = res;
      this.passwordDetails =res;
      // console.log(this.userManagementService.resetPasswordDetails.allUser);
    })
  }
  resetModal(user) {
    this.newPassword = "";
    this.confirmPassword = "";
    $("#resetPassModal").modal('show');
    this.user = user;
  }
  submitModal(form: NgForm) {
    let passDetails = {
      'userId': this.user.id,
      'newPassword': this.newPassword
    };

    if (this.newPassword === this.confirmPassword) {
      this.http.post(Constants.HOME_URL + 'resetPassword', passDetails).subscribe((data) => {
        $("#resetPassModal").modal('hide');
        $("#successMatch").modal('show');
        this.newPassword = "";
        this.confirmPassword = "";
      }, err => {
        $("#resetPassModal").modal('hide');
        $("#errorModal").modal('show');
        this.validationMsg = "Error occurred";
      });
    }
  }

  showLists() {
    // $(".left-list").attr("style", "display: block !important");
    // $('.mob-left-list').attr("style", "display: none !important");
  }
  ngAfterViewInit() {
    $("input, textarea, .select-dropdown").focus(function () {
      $(this).closest(".input-holder").parent().find("> label").css({ "color": "#4285F4" })

    })
    $("input, textarea, .select-dropdown").blur(function () {
      $(this).closest(".input-holder").parent().find("> label").css({ "color": "#333" })
    })
    // $('body,html').click(function (e) {
    //   if ((window.innerWidth) <= 767) {
    //     if (e.target.className == "mob-left-list") {
    //       return;
    //     } else {
    //       $(".left-list").attr("style", "display: none !important");
    //       $('.mob-left-list').attr("style", "display: block !important");
    //     }
    //   }
    // });
  }
}

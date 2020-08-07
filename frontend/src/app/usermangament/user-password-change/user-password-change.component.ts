import { Component, OnInit } from '@angular/core';
import { FormGroup, NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Constants } from '../../constants';
import { UsermanagementService } from '../services/usermanagement.service';
import { AppService } from '../../app.service';
declare var $: any;

@Component({
  selector: 'app-user-password-change',
  templateUrl: './user-password-change.component.html',
  styleUrls: ['./user-password-change.component.scss']
})
export class UserPasswordChangeComponent implements OnInit {

  form: FormGroup;
  formFields: any;
  sdrcForm: FormGroup;

  validationMsg: any;
  btnDisable: boolean = false;
  UserForm: FormGroup;

  userName: string;
  password: string;
  currpassword: string;
  confPassword: string;

  userManagementService: UsermanagementService;
  app: AppService;
  passwordRegex = /^(?=.*[0-9])(?=.*[!@#$%^&*_-])[a-zA-Z0-9!@#$%^&*_-]{7,15}$/;

  constructor(private http: HttpClient, private userManagementProvider: UsermanagementService, private router: Router, private appService: AppService) {
    this.userManagementService = userManagementProvider;
    this.app = appService;
  }

  ngOnInit() {
    if (localStorage.getItem('user_details')) {
      var token = JSON.parse(localStorage.getItem('user_details'));
      this.userName = token.user_name;
    }
  }
  /**
   * Change password for individual user
   * @param form 
   */
  changePasswordForm(form: NgForm) {
    let passDetails = {
      'userName': this.userName,
      'oldPassword': this.currpassword,
      'newPassword': this.password,
      'confirmPassword': this.confPassword
    };
    if (this.currpassword != this.password && this.password == this.confPassword) {
      this.http.post(Constants.HOME_URL + 'changePassword', passDetails).subscribe((data) => {
        this.validationMsg = data;
        $("#successMatch").modal('show');
        form.resetForm();
      }, err => {
        $("#oldPassNotMatch").modal('show');
        if (err.status == 409)
          this.validationMsg = err.error.message;
        else
          this.validationMsg = err.error.message;
      });
    }
  }
  successModal() {
    $("#successMatch").modal('hide');
    $("#passNotMatch").modal('hide');
    this.appService.logout();
  }
}

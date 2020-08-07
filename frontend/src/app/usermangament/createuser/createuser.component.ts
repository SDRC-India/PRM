import { Component, OnInit } from '@angular/core';
import { FormGroup, NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Constants } from '../../constants';
import { UsermanagementService } from '../services/usermanagement.service';
import { isArray } from 'util';
declare var $: any;

@Component({
  selector: 'app-createuser',
  templateUrl: './createuser.component.html',
  styleUrls: ['./createuser.component.scss']
})
export class CreateuserComponent implements OnInit {

  form: FormGroup;
  formFields: any;
  sdrcForm: FormGroup;

  payLoad = '';
  todayDate = new Date();

  natAreaDetails: any;
  stateList: any;
  parentAreaId: number;
  paramModal: any;
  validationMsg: any;
  btnDisable: boolean = false;
  UserForm: FormGroup;
  firstFieldVariable: any;
  selectedRole: any;
  selectedStateId: number;
  selectedDistrictId: number;
  selectedBlockId: number;
  selectedUserTypeId: number;
  dob: any;
  // gender: string;
  gender: string = 'Male';
  passwordRegex = /^(?=.*[0-9])(?=.*[!@#$%^&*_-])[a-zA-Z0-9!@#$%^&*_-]{7,15}$/;
  usernameRegex = /^[A-Za-z0-9]+(?:[_-][A-Za-z0-9]+)*$/;
  nameRegex = /^[^-\s][a-zA-Z\s-]{1,50}$/;


  firstName: string;
  middleName: string;
  lastName: string;
  userName: string;
  password: string;
  confPassword: string;
  mobile: number;
  emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
  email: string;
  selectedSubpartner: any;
  selectedPartner: any;
  selectedArea:any;

  userManagementService: UsermanagementService;


  constructor(private http: HttpClient, private userManagementProvider: UsermanagementService) {
    this.userManagementService = userManagementProvider;
    this.userManagementService.getAreaList().subscribe(data => {
      this.userManagementService.areaList = isArray(data) ? data : [];
    })
  }

  ngOnInit() {
    // if(!this.userManagementService.userRoles)
    this.userManagementService.getUserRoles().subscribe(data => {
      this.userManagementService.userRoles = data;
    })

    // if(!this.userManagementService.allPartners)
    // this.userManagementService.getAllPartners().subscribe(data => {
    //   this.userManagementService.allPartners = data;
    // })

    // if(!this.userManagementService.allSubpartners)
    // this.userManagementService.getAllSubpartners().subscribe(data => {
    //   this.userManagementService.allSubpartners = data;
    // })

    // if ((window.innerWidth) <= 767) {
    //   $(".left-list").attr("style", "display: none !important");
    //   $('.mob-left-list').attr("style", "display: block !important");
    // }
  }

  submitForm(role: any, form: NgForm) {
    let uname = this.userName;
    let userDetails = {
      "userName": uname.toLowerCase(),
      "password": this.password,
      "email": this.email,
      "designationIds": [role.id],
      "mobNo": this.mobile,
      "firstName": this.firstName.trim(),
      "middleName": this.middleName ? this.middleName.trim() : '',
      "lastName": this.lastName.trim(),
      "gender": this.gender,
    }
    userDetails["areaId"] = this.selectedRole.slugId === 2 ? [1]: [this.selectedArea];
    this.http.post(Constants.HOME_URL + 'createUser', userDetails).subscribe((data) => {
      // console.log(data);
      this.validationMsg = data;
      $("#successMatch").modal('show');
      form.resetForm();
      setTimeout(() => {
        this.gender = 'Male'
      }, 1000)
    }, err => {
      $("#oldPassNotMatch").modal('show');
      //this.validationMsg = err.error;
      if (err.status == 409) {
        this.validationMsg = err.error.message
      }
      else
        this.validationMsg = "Some server error occured"
    });
  }
  successModal() {
    $("#successMatch").modal('hide');
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

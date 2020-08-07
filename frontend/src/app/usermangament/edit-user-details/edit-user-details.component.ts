import { Component, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Constants } from '../../constants';

import { NgxSpinnerService } from 'ngx-spinner';
import { Router, RoutesRecognized } from '@angular/router';
import { filter, pairwise } from 'rxjs/operators';
import { UsermanagementService } from '../services/usermanagement.service';
import { isArray } from 'util';
declare var $: any;

@Component({
  selector: 'app-edit-user-details',
  templateUrl: './edit-user-details.component.html',
  styleUrls: ['./edit-user-details.component.scss']
})

export class EditUserDetailsComponent implements OnInit {  
  form: FormGroup;
  formFields: any;
  sdrcForm: FormGroup;
  
  payLoad = '';
  genderData = [{ label: 'Male', value: "MALE" }, { label: 'Female', value: "FEMALE" }];
  dob: any;
  validationMsg: any;
  UserForm:FormGroup;
  selectedDistrictId:number;
  selectedBlockId: number;
  selectedAreaId: number;
  selectedGramPanchayatId: number;
  selectedtypeDetailsId: number;
  todayDate = new Date()
  passwordRegex = /^(?=.*[0-9])(?=.*[!@#$%^&*_-])[a-zA-Z0-9!@#$%^&*_-]{7,15}$/;
  usernameRegex = /^[A-Za-z0-9]+(?:[_-][A-Za-z0-9]+)*$/;
  nameRegex = /^[^-\s][a-zA-Z\s-]{1,50}$/;
  emailRegex = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;


  userManagementService: UsermanagementService;

  constructor(private http: HttpClient, private userManagementProvider: UsermanagementService, private spinner: NgxSpinnerService, private router: Router) {
    this.userManagementService = userManagementProvider;
   }

  ngOnInit() {   
    this.spinner.show(); 
    this.router.events
    .pipe(filter((e: any) => e instanceof RoutesRecognized)
    ).subscribe((e: any) => {
        if(this.router.url =="/edit-user" && e.url != '/reset-password'){
          this.userManagementService.resetPasswordDetails ={};
        }
    }); 
    if(!this.userManagementService.editUserDetails){
      this.router.navigateByUrl("edit-user");
    } else {
      this.dob = new Date(this.userManagementService.editUserDetails.dob).toISOString()
      this.selectedAreaId = this.userManagementService.editUserDetails.areaId[0]
    }
    this.userManagementService.getAreaList().subscribe(data => {
      this.userManagementService.areaList = isArray(data) ? data : [];
    })
    
    this.spinner.hide();
    if((window.innerWidth)<= 767){
      $(".left-list").attr("style", "display: none !important"); 
      $('.mob-left-list').attr("style", "display: block !important");
    }
  }

 
  updateUserDetails(roleId:any){ 

    
     let userDetails = {  
      "id": this.userManagementService.editUserDetails.id,
      "areaId": this.selectedAreaId ? [this.selectedAreaId]: [],
      "userName": this.userManagementService.editUserDetails.userName,
      "email": this.userManagementService.editUserDetails.email,
      "designationIds": [this.getObjectByKey('slugId', this.userManagementService.editUserDetails.degSlugId, this.userManagementService.userRoles).id],
      "mobNo": this.userManagementService.editUserDetails.mobileNumber,
      "firstName": this.userManagementService.editUserDetails.name.trim(),
      "middleName": this.userManagementService.editUserDetails.middleName ? this.userManagementService.editUserDetails.middleName.trim() : '',
      "lastName": this.userManagementService.editUserDetails.lastName.trim(),
      "gender": this.userManagementService.editUserDetails.gender,
      "dob": new Date(this.dob).toLocaleDateString()
     }
     this.spinner.show();
     this.http.post(Constants.HOME_URL+'updateUser', userDetails).subscribe((data) => {
       this.spinner.hide();    
       this.validationMsg = data;    
        $("#successMatch").modal('show');     
     }, err=>{
      this.spinner.hide(); 
      $("#oldPassNotMatch").modal('show');
      this.validationMsg = err.error.message;     
    });
  }
  getObjectByKey(key, value, arr) {
    return arr.filter(d => d[key] == value)[0]
  }
  successModal(){
    $("#successMatch").modal('hide');
    this.router.navigateByUrl("edit-user");
  }

  showLists(){    
    $(".left-list").attr("style", "display: block !important"); 
    $('.mob-left-list').attr("style", "display: none !important");
  }
  ngAfterViewInit(){
    $("input, textarea, .select-dropdown").focus(function() {
      $(this).closest(".input-holder").parent().find("> label").css({"color": "#4285F4"})
      
    })
    $("input, textarea, .select-dropdown").blur(function(){
      $(this).closest(".input-holder").parent().find("> label").css({"color": "#333"})
    })
    $('body,html').click(function(e){   
      if((window.innerWidth)<= 767){
      if(e.target.className == "mob-left-list"){
        return;
      } else{ 
          $(".left-list").attr("style", "display: none !important"); 
          $('.mob-left-list').attr("style", "display: block !important");  
      }
     }
    });   
  }

}


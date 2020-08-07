import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Constants } from '../constants';
declare var $:any;
@Component({
  selector: 'app-forgotpass',
  templateUrl: './forgotpass.component.html',
  styleUrls: ['./forgotpass.component.scss']
})
export class ForgotpassComponent implements OnInit {
  userName: string;
  show:boolean = false;
  errorMesg: string;
  successMesg: any;
  otpSuccessMesg: any;
  otpErrorMesg: string;
  showSucessMesg:boolean = false;
  showErrMesg:boolean = false;
  showOtpSucessMesg:boolean = false;
  showOtpErrMesg:boolean = false;
  showOtpSucessImg:boolean =false;
  showOtpErrImg:boolean = false;
  showFields:boolean = false; 
  showBtn:boolean = false;
  validationMsg : any;
  disableVerify: boolean = false;

  credentials: any = {
    emailid: '',
    otp:'',
    newPassword: '',
    confirmPassword: ''
  }
  constructor(private http: HttpClient, private router: Router) { }

  ngOnInit() {
  }

  sendRequest(emailId){
   
    if(emailId === "" || emailId === undefined){
      document.getElementById('emailErr1').innerHTML  = "Please enter username"; 
      this.showErrMesg = false;
    } 
    else {      
      document.getElementById('emailErr1').innerHTML  = "";

    // this.load.show();   
    this.otpErrorMesg = "";
    this.showOtpErrMesg = false;   

    this.http.get(Constants.HOME_URL + "bypass/sendOtp?userName="+emailId + "&valueType=uName" ).subscribe((data)=>{    
      this.show = true;     
      this.showSucessMesg = true;
      this.showErrMesg = false;
      this.successMesg = data;
      this.showBtn = true;
      this.credentials.otp = "";
      this.disableVerify = false;
      // this.spinner.hide();      
    }, err=>{      
      this.showErrMesg = true;
      this.showSucessMesg = false;
      this.errorMesg = "Username doesn't exist !";
      this.disableVerify = false;
      // this.spinner.hide();
    });
  }  
}
checkOTP(emailId, otp){
 
  document.getElementById('otpSec1').innerHTML ="";
  this.successMesg = "";
   if(this.credentials.otp === "" || this.credentials.otp === undefined) {
    document.getElementById('otpSec1').innerHTML  = "Please enter OTP."; 
   } else {
    // this.spinner.show(); 
    this.http.get(Constants.HOME_URL +'bypass/validateOtp?userName='+emailId + '&otp='+otp + "&valueType=uName").subscribe((data)=>{         
      this.showFields=true;
      this.showOtpSucessMesg = true;
      this.showOtpErrMesg = false;
      this.showOtpSucessImg = true;
      this.showOtpErrImg = false;
      this.otpSuccessMesg = data;   
      this.disableVerify = false;
      // this.spinner.hide();    
    }, err=>{      
      this.showOtpSucessMesg = false;
      this.showOtpErrMesg = true;
      this.showOtpSucessImg = false;
      this.showOtpErrImg = true;
      this.otpErrorMesg = err.error;
      if (this.otpErrorMesg.includes('Invalid OTP ! No of attempts exceeded limit')) {
        this.disableVerify = true;
      }
      else{
        this.disableVerify = false;
      }
      // this.spinner.hide();  
    });  
  }
}
sendAllData(email, otp, newPassword, confirmPassword){
  var passPattern: RegExp = /^\S*$/;

   let forgotPasswordDetails: any = {
    'emailId' : email,
    'otp': otp,
    'newPassword': newPassword,
    'confirmPassword': confirmPassword,
    'valueType':'uName'
   }
 
  this.otpSuccessMesg = "";
  this.successMesg = "";

  if(this.credentials.newPassword === "" || this.credentials.newPassword === undefined){
    document.getElementById('newPasserror').innerHTML  = "Please enter new password."; 
  } else if (!passPattern.test(newPassword)) {
    document.getElementById('newPasserror').innerHTML  = "New password will not allow space.";
  } else if(this.credentials.confirmPassword === "" || this.credentials.confirmPassword === undefined){
    document.getElementById('confirmPasserror').innerHTML  = "Please enter confirm password."; 
  } else if(this.credentials.confirmPassword === this.credentials.newPassword){   
    document.getElementById('newPasserror').innerHTML  = "";
    document.getElementById('confirmPasserror').innerHTML  = "";
    
  //  this.spinner.show(); 
   this.http.post(Constants.HOME_URL +'bypass/forgotPassword', forgotPasswordDetails).subscribe((data)=>{  
    this.validationMsg = data;   
    $("#successMatch").modal("show");
    // this.spinner.hide();
   }, err=>{     
    this.validationMsg = err.error;
    $("#passNotMatch").modal("show");
    // this.spinner.hide();
   });
  }
}
pageRefer(){
  this.router.navigateByUrl('/login');    
  $("#successMatch").modal("hide");
  $("#passNotMatch").modal("hide");   
}

newPassError(){
 if(this.credentials.password != undefined || this.credentials.password != "")
  document.getElementById('newPasserror').innerHTML  = "";
  this.otpSuccessMesg = "";
  this.showOtpSucessImg = false;
  this.showOtpErrImg = false;
};

confirmPassError(){
 if(this.credentials.confirmPassword != undefined || this.credentials.confirmPassword != "")
  document.getElementById('confirmPasserror').innerHTML  = "";
  this.otpSuccessMesg = "";
  this.showOtpSucessImg = false;
  this.showOtpErrImg = false;
};

}

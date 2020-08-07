import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { AppService } from '../app.service';
declare var $: any;

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  hide = true;
  credentials: any = {
    username: '',
    password: ''
  }  
  newPassword: any;
  confirmPassword: any;
  form: FormGroup;
  app: AppService;

  constructor(private appService:AppService, private router: Router,private frmbuilder:FormBuilder) { 
    this.app = appService;
  }
  
  ngOnInit() { }

  login(){
    this.app.authenticate(this.credentials, () => {
      if(this.app.authenticated == true){
        this.router.navigateByUrl('report');
      }     
    });
    return false;
  }

}

import { Injectable } from '@angular/core';
import { Validators, FormControl, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { Constants } from '../constants';

@Injectable({
  providedIn: 'root'
})
export class CustomValidatorsService extends Validators {

  constructor() {
    super();
  }


  public static valueMoreThanOne(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      //Validaton code here
      this.numberRange(control, 1, NaN);
      return null;
    }
  }

  public static getNumberRange(minLimit?, maxLimit?): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      //Validaton code here
      this.numberRange(control, minLimit, maxLimit);
      return null;
    }
  }

  public static numberRange(formControl: FormControl | AbstractControl, minLimit?, maxLimit?): any {
    let value = formControl.value;
    let errorResponse = {};
    if (value == null || isNaN(value)) {
      errorResponse['range'] = true
      return errorResponse;
    }
    minLimit && value < minLimit ? errorResponse['minLimit'] = true : '';
    maxLimit && value > maxLimit ? errorResponse['maxLimit'] = true : '';
    return Object.keys(errorResponse).length > 0 ? errorResponse : null;
  }

  public static percentage(formControl: FormControl): any {
    let value = formControl.value;
    let errorResponse = {};
    if (value == null) {
      errorResponse['percentage'] = true
      return errorResponse;
    }
    value < 0 ? errorResponse['minPercentage'] = true : '';
    value > 100 ? errorResponse['maxPercentage'] = true : '';
  }

  public static isEmpty(formControl: FormControl) {
    let data = formControl.value ? formControl.value.toString() : "";
    data = data.trim();
    let emptyspaceRex = Constants.regularExp.usernameRegx;
    return RegExp(emptyspaceRex).test(data) ? { isEmpty: true, "required": true } : null;
  }

  public static password(formControl: FormControl) {
    let data = formControl.value ? formControl.value.toString() : "";
    // let passwordRegx = /^(?!.*[\s])(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[#$^+=!*()@%&]).{6,15}$/;
    let passwordReg = new RegExp(Constants.regularExp.passwordRegx);
    return !RegExp(passwordReg).test(data) ? { password: true } : null;
  }

  public static validateEmail(formControl: FormControl) {
    let email = formControl.value ? formControl.value.toString() : "";
    let regx = new RegExp(Constants.regularExp.email);
    return regx.test(String(email).toLowerCase()) ? null : { email: true };
  }

  public static validateNoWhiteSpace(formControl: FormControl) {
    let value = formControl.value ? formControl.value.toString() : "";
    let regx = new RegExp(Constants.regularExp.noWhiteSpaceAtBegining);
    return regx.test(value) ? null : { whiteSpace: true };
  }

  public static validatePositiveNumber(formControl: FormControl) {
    let value = formControl.value;
    return parseFloat(value) === Math.abs(value) ? null : { positiveNumber: true };
  }


}

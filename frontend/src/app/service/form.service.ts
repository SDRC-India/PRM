import { Injectable } from '@angular/core';
import { Constants } from '../constants';
import { FormBuilder, FormGroup, FormArray } from '@angular/forms';

@Injectable({
  providedIn: 'root'
})
export class FormService {

  constructor(private formBuilder: FormBuilder) { }

  createReactiveForm(formConfig: Array<any>): FormGroup {
    let group = {}
    formConfig.map(eachInput => {
      if (eachInput.controlType == formConstants.formGroup) {
        group[eachInput.columnName] = this.createReactiveForm(eachInput.formInputs)
      } else if (eachInput.controlType == formConstants.formArray) {
        group[eachInput.columnName] = this.formBuilder.array([]);
        eachInput["formInputs"].map(eachFG => {
          let fGroup = this.createReactiveForm([eachFG]).get(eachFG.columnName.toString());
          group[eachInput.columnName].push(fGroup);
        })
      } else {
        // return this.formBuilder.control(eachInput.value || '', eachInput.validators);
        group[eachInput.columnName] = [{value:eachInput.value != null ? eachInput.value  : '', disabled:eachInput.disabled}, eachInput.validators]
      }
    });
    return this.formBuilder.group(group);
  }

  validateForm(formGroup: FormGroup | FormArray) {
    formGroup.updateValueAndValidity();
    Object.keys(formGroup.controls).forEach(eachControl => {
      let fc = formGroup.get(eachControl);
      fc.updateValueAndValidity();
      if (fc instanceof FormGroup || fc instanceof FormArray) {
        this.validateForm(fc);
      }
    });
  }

  markAllAsTouched(formGroup: FormGroup | FormArray) {
    formGroup.markAsTouched();
    Object.keys(formGroup.controls).forEach(eachControl => {
      let fc = formGroup.get(eachControl);
      fc.markAsTouched();
      if (fc instanceof FormGroup || fc instanceof FormArray) {
        this.validateForm(fc);
      }
    });
  }
}

export const formConstants = {
  formArray: "formArray",
  formGroup: "formGroup",
  formControl: "formControl"
}

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

/*
  Generated class for the ValidationsProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class ValidationsProvider {

  constructor(public http: HttpClient) {
  }

  enterToValidations(){

  }
  checkAvailabilityOfCondition(field, dependentConditions, condition){
    // console.log(field);
    // if(dependentConditions == "sum_total,prefetch"){
      for (let i=0; i<dependentConditions.split(",").length; i++) {
        if(dependentConditions.split(",")[i] == condition)
          return true;
      }
    // }

    return false;
  }
}

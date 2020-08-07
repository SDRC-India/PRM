import { Injectable } from '@angular/core';

/*
  Generated class for the DataSharingServiceProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class DataSharingServiceProvider {

  allformData: any;
  selectedSection: string = 'a';
  sectionNames = [];
  questionKeyValueMap : {} ={}
  constructor() {
    this.selectedSection = "a"
  }

  /*
    This functions are used to retrieve keys and type of any objectfrom html
  */
  getKeys(obj): any[]{
    return Object.keys(obj);
  }

  getType(value):string{
    return typeof(value);
  }



}

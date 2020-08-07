import { Pipe, PipeTransform } from '@angular/core';
import { isArray } from 'util';

@Pipe({
  name: 'modifyColumns'
})
export class ModifyColumnsPipe implements PipeTransform {

  /**
   *  
   * */
  transform(input: any, type): any {

    if (isArray(input)) {
      let result = [];
      switch (type) {
        case "data":

          for (let i = 0; i < input.length; i++) {
            result.push(input[i]);
          }
          break;
        case "columns":
          for (let i = 0; i < input.length; i++) {
            result.push(input[i]);
          }
          break;
      }
      return result;
    }
  }

  /** 
   * 
    */
  getAllPermissions(permissions) {
    if (!isArray(permissions)) {
      return "";
    }
    return permissions.reduce((result, eachPermission) => {
      return result + eachPermission.description + ", ";
    }, "");
  }
}

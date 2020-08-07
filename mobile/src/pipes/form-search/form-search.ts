import { Pipe, PipeTransform } from '@angular/core';

/**
 * Generated class for the FormSearchPipe pipe.
 *
 * See https://angular.io/api/core/Pipe for more info on Angular Pipes.
 */
@Pipe({
  name: 'formSearch',
})
export class FormSearchPipe implements PipeTransform {
/**
   * Takes a value and makes it lowercase.
   */
  fromsArray = [];
  transform(
    forms: IDbFormModel[],
    searchText: string,
    type: string
  ): IDbFormModel[] {
    if (forms) {
      this.fromsArray = [];
      if (searchText && type) {
        return forms.filter(
          element =>
          Object.keys(element.formDataHead).map(key => element.formDataHead[key])
           .filter(value=>value.toLocaleLowerCase()
           .includes(searchText.toLocaleLowerCase())).length
               &&
            element.formStatus.toLowerCase() === type.toLowerCase()
        );
        // return this.fromsArray as IDbFormModel[]
      } else if (type) {
        return forms.filter(
          element => element.formStatus.toLowerCase() === type.toLowerCase()
        );
      }
      else
      {
        return forms;
      }
    } else {
      return this.fromsArray;
    }
  }
}

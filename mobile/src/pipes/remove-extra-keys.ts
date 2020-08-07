import { Pipe, PipeTransform } from '@angular/core';

/**
 * Generated class for the RemoveExtraKeysPipe pipe.
 *
 * See https://angular.io/api/core/Pipe for more info on Angular Pipes.
 */
@Pipe({
  name: 'removeExtraKeys',
})
export class RemoveExtraKeysPipe implements PipeTransform {
  /**
   * Takes a value and makes it lowercase.
   */
  transform(values: string[], ...args) {
    let elements: any[] = [];
    for (let value in values) {
      if(args.indexOf(value) == -1){
        elements.push(value)
      }
    }
    return elements;
  }
}

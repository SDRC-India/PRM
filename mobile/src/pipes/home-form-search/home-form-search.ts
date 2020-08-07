import { Pipe, PipeTransform } from '@angular/core';

/**
 * Generated class for the HomeFormSearchPipe pipe.
 *
 * See https://angular.io/api/core/Pipe for more info on Angular Pipes.
 */
@Pipe({
  name: 'homeFormSearch',
})
export class HomeFormSearchPipe implements PipeTransform {
  /**
   * Takes a value and makes it lowercase.
   */
  transform(forms: IHomePageModel[], args:string) {
    if(!args)
    {
      return forms;
    }

    else
  {
    return forms.filter(d=>d.formName.toLowerCase().includes(args.toLowerCase()))
  }
  }
}

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'sortBy'
})
export class SortbyPipe implements PipeTransform {

  transform(value: any[], propertyName: string): any[] {
    if (propertyName)
      return value.sort((a: any, b: any) => b[propertyName].localeCompare(a[propertyName])).reverse();
    else
      return value;
  }

}

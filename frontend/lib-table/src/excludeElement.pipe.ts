import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'excludeElementPipe'
})
export class ExcludeElementPipe implements PipeTransform {
  transform(items: any[], filterOutItems: any[]): any {
    if (items && items.length) {
      const filteredItems = (filterOutItems && filterOutItems.length) ? items.filter(item => !filterOutItems.includes(item)) : items;
      return filteredItems;
    }
    else {
      return [];
    }
  }
}


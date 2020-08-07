import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'tableDataFilter'
})
export class TableDataFilterPipe implements PipeTransform {
  
  transform(data: any, args?: any): any {
    if(data && data.length > 0)
    return data.filter(datas => datas.status == args)
    else 
    return [];
  }

}

import { Pipe, PipeTransform } from '@angular/core';

/**
 * Generated class for the SortItemMobilePipe pipe.
 *
 * See https://angular.io/api/core/Pipe for more info on Angular Pipes.
 */
@Pipe({
  name: 'sortItemMobile',
})
export class SortItemMobilePipe implements PipeTransform {
  transform(areas: any[], ...args): any[] {
    if(areas != undefined && areas != null && areas.length > 0){
    areas.sort((area1: any, area2: any) => {
    var area1Name = area1.value.toLowerCase(), area2Name = area2.value.toLowerCase()
    var patt = /(other|others|Others \(अन्य\))/i;

    if ((area1Name < area2Name || patt.test(area2Name)) && !patt.test(area1Name)) //sort string ascending
    return -1
    if (area1Name > area2Name)
    return 1
    return 0 //default return value (no sorting)
    })
    return areas
    }
    }
}

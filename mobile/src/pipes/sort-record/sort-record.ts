import { Pipe, PipeTransform } from '@angular/core';

/**
 * Generated class for the SortRecordPipe pipe.
 *
 * See https://angular.io/api/core/Pipe for more info on Angular Pipes.
 */
@Pipe({
  name: 'sortRecord',
})
export class SortRecordPipe implements PipeTransform {
  /**
   * Takes a value and makes it lowercase.
   */
  transform(anganwadiList: IDbFormModel[], ...args): IDbFormModel[]{
    if(anganwadiList != undefined && anganwadiList != null && anganwadiList.length > 0){
      anganwadiList.sort((a: IDbFormModel, b: IDbFormModel)=>{

        let day = parseInt(a.updatedDate.split(" ")[0].split('-')[0])
        let month = parseInt(a.updatedDate.split(" ")[0].split('-')[1]) - 1
        let year = parseInt(a.updatedDate.split(" ")[0].split('-')[2])

        let hourOfA = parseInt(a.updatedDate.split(" ")[1].split(':')[0])
        let minuteOfA = parseInt(a.updatedDate.split(" ")[1].split(':')[1])
        let secondOfA = parseInt(a.updatedDate.split(" ")[1].split(':')[2])

        let hourOfB = parseInt(b.updatedDate.split(" ")[1].split(':')[0])
        let minuteOfB = parseInt(b.updatedDate.split(" ")[1].split(':')[1])
        let secondOfB = parseInt(b.updatedDate.split(" ")[1].split(':')[2])

        // passing year, month, day, hourOfA and minuteOfA to Date()
        let dateOfA: Date = new Date(year, month, day, hourOfA, minuteOfA, secondOfA)
        let dateOfB: Date = new Date(year, month, day, hourOfB, minuteOfB, secondOfB)

        if (dateOfA < dateOfB) {
          return 1;
        } else if (dateOfA > dateOfB) {
          return -1;
        } else {
          return 0;
        }
      })
    }
    return anganwadiList;
  }
}

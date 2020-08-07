import { Pipe, PipeTransform } from '@angular/core';
import { pipe } from 'rxjs';

@Pipe({
  name: 'tableName'
})
export class TableNamePipe implements PipeTransform {

  mapper = {
    "Dist Name": "District Name",
    "Dept Name": "Department Name",
    "Dist Code": "District Code",
    "Sl No": "Sl. No.",
    "Data Collection Days": "Data Submission Days",
    "Data Rejection Days": "Data Review Days",
    "Number Of Months": "Number of Months"
  }
  transform(value: any, args?: any): any {
    let formattedValue = pipe(this.converCamelCaseToSpacedCase, this.makeTheFirstLetterCapitalize)(value);
    this.mapper.hasOwnProperty(formattedValue) ? formattedValue = this.mapper[formattedValue] : '';
    return formattedValue
  }

  converCamelCaseToSpacedCase(value): string {
    return value.replace(/([A-Z])/g, (x, y) => { return " " + y.toUpperCase() })
  }

  makeTheFirstLetterCapitalize(value) {
    return value.replace(/^([a-z])/g, (x, y) => { return y.toUpperCase() })
  }
}

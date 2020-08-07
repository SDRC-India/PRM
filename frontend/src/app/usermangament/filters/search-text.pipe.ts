import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'searchText'
})
export class SearchTextPipe implements PipeTransform {

  transform(tableData: any[], searchTexts: string): any[] {
    if(!tableData) return [];
    if(searchTexts == null) return tableData;
    
    searchTexts = searchTexts.toLowerCase();
     
     return tableData.filter(item => {        
      return JSON.stringify(item).toLowerCase().includes(searchTexts);
     });      
  }

}

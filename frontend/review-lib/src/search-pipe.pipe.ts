import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'searchPipe'
})
export class SearchPipePipe implements PipeTransform {

  transform(tableData: any[], searchText: string): any[] {
    if (!tableData) return [];
    if (!searchText) return tableData;
    searchText = searchText.toLowerCase();
    return tableData.filter(item => {   
      let allText: string="";
      for(let i=0; i<Object.keys(item).length;i++){
       if(Object.keys(item)[i] !="formData" && Object.keys(item)[i] !="formDataHead"){
         allText = allText+JSON.stringify(item[Object.keys(item)[i]])
       }
      }  
       return allText.toLowerCase().includes(searchText);
    });     
  }
}

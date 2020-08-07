import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Constants } from '../constants';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  constructor(private httpClient: HttpClient) { }

  getAllOptions() {
    return this.httpClient.get(Constants.HOME_URL + 'options')
  }
  
  getAreaDetails(areaLevelId, parentAreaId) {
    return this.httpClient.get(Constants.HOME_URL + 'area?areaLevelId='+ areaLevelId +'&parentAreaId=' + parentAreaId)
  }

  getStateReportData() {
    return this.httpClient.post(Constants.HOME_URL + 'getStateReportData', {})
  }

  getRawDataReport(formId) {
    return this.httpClient.get(Constants.HOME_URL + 'getRawDataReport?formId='+formId, {responseType: "text"})
  }

  downloadReport(fileName) {
    return this.httpClient.post(Constants.HOME_URL + 'downloadReport?fileName='+fileName, {}, {responseType:"blob"})
  }

  filterVolunteer(data) {
    return this.httpClient.post(Constants.HOME_URL + 'filterVolunteer', data, {responseType: 'text'})
  }

  getReportCount(){
    return this.httpClient.get(Constants.HOME_URL + 'getReportCount')
  }
}

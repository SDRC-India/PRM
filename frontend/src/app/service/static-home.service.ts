import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Constants } from '../constants';
import { Observable } from 'rxjs';

@Injectable()
export class StaticHomeService {
  selectedLang: string = 'english';
  viewName : String ;
  contentDetails: any[] = [];
  contentSection: any = {};
  contentData: any;
  contentList: any[] = [];
  imageCategory: any;
  whatsNewCategory:any;
  viewType: any;
  sectionName: any;
  whatnewdataSection : string;
  menus: any[];
  siteMenus: any[];
 

  constructor(private httpClient: HttpClient) { }

  getMenuList(menu){
    // return this.httpClient.get(Constants.HOME_URL+Constants.CMS_URL + 'getPageContent?viewName=Menu&viewType='+menu);
    return this.httpClient.get("assets/menu.json");
  } 
  getData(viewName){
    // return this.httpClient.get(Constants.HOME_URL+Constants.CMS_URL + 'getPageContent?viewName='+viewName + '&viewType=');
    return this.httpClient.get("assets/language.json");
  } 

  getPhotogalleryData(viewName){
    return this.httpClient.get(Constants.HOME_URL+Constants.CMS_URL + 'fetchPhotoGallery?viewName='+viewName);
  }

 
  reinitializeData(data){
    this.contentData = data;
    this.contentDetails = this.contentData.viewContent[this.selectedLang];
    this.contentSection = {};
    // this.homeHindiDetails = this.homeData.viewContent.hindi;
    for(var i=0;i<this.contentDetails.length;i++){
      this.contentList = this.contentDetails[i].data;
    }
   
    if(this.contentData.viewName == 'Home'){
      for(var i=0;i<this.contentDetails.length;i++)
      {
        this.contentSection[this.contentDetails[i].key] = this.contentDetails[i];
      }
    }
    // if(this.contentData.viewName == 'Photo Gallery'){
    //   for(var i=0;i<this.contentDetails.length;i++)
    //   {
    //     this.contentSection[this.contentDetails[i].section] = this.contentDetails[i];
    //   }
    // }
   
    // for(var i=0;i<this.contentDetails.length;i++){
    //   this.contentGallery = this.contentDetails[i].data;
    // }
  }

}




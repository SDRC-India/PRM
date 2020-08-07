import { Injectable } from '@angular/core';
import { Constants } from '../../constants';
import { HttpClient, HttpEvent, HttpRequest } from '@angular/common/http';
import { MaxLengthValidator } from '@angular/forms';
import { Observable } from 'rxjs';

export interface UserData {
  areaLevel: string;
  areas: any[];
  authorities: any;
  authorityIds: any;
  desgnName: any;
  designation: number;
  devPartner: string;
  dob: any;
  emailId: any;
  firstName: string;

  idProType: string;
  idProofFile: any;
  lastName: string;
  middleName: string;
  mobNo: string;
  name: string;
  orgName: string;
  organization: number;
  othersDevPartner: any;
  othersIdProof: any;
  roleIds: any;
  roles: any;
  userId: string;
}

@Injectable({
  providedIn: 'root'
})
export class UsermanagementService {

  formFieldsAll: any;
  typeDetails: any;
  areaDetails: any;
  gender: any;
  genderData = [{ label: 'Male', value: 1 }, { label: 'Female', value: 2 }];
  allPartners: any;
  allSubpartners: any;
  userRoles: any;
  users: any;

  constructor(private httpClient: HttpClient) { }


  allData: any;
  allTypes: any;
  editUserDetails: any;
  resetPasswordDetails: any = {};
  userDetails: any;
  pendingUsers: any[] = [];
  approveUsers: any[] = [];
  rejectedUsers: any[] = [];
  removeColumnList: any = [];
  dateOfBirth: any;

  // models for update user profile
  updateUserDetails: any = {};
  isAdmin: string;

  // models for user management 
  ifDisclaimerNotSelected: boolean = false;
  hideBtnsIfRejectedusers: boolean = true;
  showpPendingUsersPagination: boolean = true;
  showpAcceptedUsersPagination: boolean = false;
  showpRejectedUsersPagination: boolean = false;
  searchTexts: any;
  p: number = 1;
  a: number = 1;
  r: number = 1;
  e: number = 1;
  selectForRejectionOrApproval: any;
  selectedUserList: any = [];
  selectedUserModelList: any = [];
  aprroveVariable: boolean;
  infoMsg: string;
  successApprovedOrreject: any;
  rejectionModel: any[] = [];
  selectedStateName: string;
  selectedDistrictName: string;
  selectedBlockName: string;
  userLevelName: string;
  organizationName: string;
  designationName: string;
  fetchedDesignationId: any[] = [];
  accessLevelNames: any[] = [];
  fetchedDevPartnerId: string;
  selectedDevPartnerId: string;
  idProofTypeName: string;
  reasonTexts: any[] = [];
  devPartnerName: string;
  accessLevelName: string;
  fetchedEmailId: string;
  adminCreateNewUserDetails = {};
  noData: boolean = false;

  // models for new user registration
  firstName: any;
  middleName: any;
  lastName: any;
  dob: any;
  mobileNumber: number;
  emailId: string;
  otpSuccessMsg: string;
  otpErrorMsg: string;
  enterOTP: any;
  userLevel: any;
  userLevelID: number[] = [];
  userLevelIDFetched: Number[] = [];
  createNewUserDetails: any = {}
  stateLevel: any;
  districtLevel: any;
  blockLevel: any;
  organization: any;
  selectedOrganizationID: number;
  develelopmentPartners: any;
  developmentPartnerID: any;
  designation: any;
  designationAdd: any;
  designationID: number[] = [];
  accessLevel: any;
  userName: any;
  password: any;
  reEnterPassword: any;
  passwordMatched: boolean = false;
  passwordDoesNotMatched: boolean = false;
  idProof: any;
  idProofother: any;
  levelSearch: any;
  uploadIdentityProof: any;
  developmentPartnerOthers: any;
  userlevelDetails: any = {};
  showStateField: boolean = false;
  showDistrictField: boolean = false;
  showBlockField: boolean = false;
  showDevelopmentField: boolean = false;
  showDevelopmentPartnerOthers = false;
  afterEmailVerified: boolean = false;
  enterOTPForValidate: boolean = false;
  showIDProofOtherBlock: boolean = false;
  showIfGenderNotSelected: boolean = false;
  showOTPTickerSuccess: boolean = false;
  showOTPTickerWrong: boolean = false;
  showUploadFileError: boolean = false;
  showErrorIfOTPnotVerified: boolean;
  showUploadIdProofBlock: boolean = false;
  fetchedUploadIdentityProof: string;
  uploadErrorMsg: string;
  checkDuplicateUser: any;
  checkDuplicateEmail: boolean;
  designationLists: any = [];
  organizationLists: any = [];
  developmentPartnerLists: any = [];
  allLevels: any;
  stateLevelLists: any = [];
  districtLevelLists: any = [];
  blockLevelLists: any = [];
  idProofLists: any = [];
  idProofID: string;
  userLevelLists: any;
  accessLevelLists: any;
  accessLevelID: any = [];
  selectedAccessLevelName: string[] = [];
  selectedAccessLevelID: any[] = [];
  errorMessage: any;
  selectedFileName: any;
  emailIDisVerified: string;
  statusVerified: any;
  disclaimer: any = {};
  file: any;
  dataModel: any = {};
  allLocation: any[] = [];
  reasonTextLengthInNumber: number;
  reasonTextLength: number = 200;
  submitSuccessMsg: any;
  areaList: any= [];

  getUserRoles() {
    return this.httpClient.get(Constants.HOME_URL + 'getAllRoles');
  }
  getAreaList() {
    return this.httpClient.get(Constants.HOME_URL + 'area?areaLevelId=2&parentAreaId=1');
  }
  getDesignationByPartner() {
    return this.httpClient.get(Constants.HOME_URL + 'getDesignationByPartner');
  }
  getAllPartners() {
    return this.httpClient.get(Constants.HOME_URL + 'getPartners');
  }
  getAllSubpartners() {
    return this.httpClient.get(Constants.HOME_URL + 'getSubPartners');
  }
  getUsersByRoleAndPartner(roleId) {
    return this.httpClient.get(Constants.HOME_URL + 'getUsersByRole?roleId=' + roleId)
  }
  getElementBySbmId(areaDetails, sbmAreaId) {
    let areaJson;
    for (let i = 0; i < Object.keys(areaDetails).length; i++) {
      const key = Object.keys(areaDetails)[i];
      areaDetails[key].forEach(element => {
        if (element.sbmAreaId == sbmAreaId) {
          areaJson = element;
        }
      });
    }
    return areaJson;
  }
  getUserDetails() {
    return this.httpClient.get(Constants.HOME_URL + 'getAllUser');
  }
  getData(data) {
    this.allData = data;
    let allDataKeys: any = Object.keys(this.allData);
    this.pendingUsers = this.allData[allDataKeys].tableData;
  }

  downloadFile(filePath) {
    return this.httpClient.post(Constants.HOME_URL+"analytics/downloadFile", filePath, {
      responseType: "blob"
    });
  }

  downloadTemplateFile() {
    return this.httpClient.post(Constants.HOME_URL + 'downloadTemplate', {}, {
      responseType: 'blob',
    });
  }
  uploadBulkFile(file: File): Observable<HttpEvent<{}>> {
    const formdata: FormData = new FormData();
    formdata.append('templateFile', file);
    const req = new HttpRequest('POST', Constants.HOME_URL + 'uploadTemplate', formdata, {
      reportProgress: true
    });
    return this.httpClient.request(req);
  }
}

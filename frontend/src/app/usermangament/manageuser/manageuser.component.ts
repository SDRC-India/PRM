import { Component, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material';
import { HttpClient } from '@angular/common/http';

import { Constants } from 'src/app/constants';
import { UsermanagementService, UserData } from '../services/usermanagement.service';
declare var $;

@Component({
  selector: 'app-manageuser',
  templateUrl: './manageuser.component.html',
  styleUrls: ['./manageuser.component.scss']
})
export class ManageuserComponent implements OnInit {


  displayedColumnsForUsers: string[] = [];
  tabIndex: number = 0;
  pendingUsersDetails: MatTableDataSource<UserData>;
  approvedUsersDetails: MatTableDataSource<UserData>;
  rejectedUsersDetails: MatTableDataSource<UserData>;
  // data: UserData[] = ELEMENT_DATA;
  // selection = new SelectionModel<UserData>(true, []);
  userManagementService: UsermanagementService;

  ifChecked: boolean;
  disableUserId: any;
  ifNotChecked: boolean;
  enableUserId: any;

  constructor(
    private userManagementServiceProvider: UsermanagementService, private http: HttpClient) {
    this.userManagementService = userManagementServiceProvider;
  }

  ngOnInit() {
    this.userManagementService.p = 1;
    this.userManagementService.a = 1;
    this.userManagementService.r = 1;
    this.userManagementService.noData = false;
    this.getUsers();
    if (this.userManagementService.pendingUsers.length == 0) {
      this.userManagementService.noData = true;
    }
  }

  getUsers() {
    this.userManagementService.getUserDetails().subscribe(data => {
      this.displayedColumnsForUsers = data['tableColumn'];
      this.userManagementService.users = data['tableData'];
      this.userManagementService.pendingUsers = data['tableData'].filter(user => user.status == 'PENDING');
      this.userManagementService.approveUsers = data['tableData'].filter(user => user.status == 'APPROVED');
      this.userManagementService.rejectedUsers = data['tableData'].filter(user => user.status == 'REJECTED');     
    })
  }
  reasonText(model) {
    this.userManagementService.reasonTexts = model;
  }

  // open modal
  approveUser(approve) {
    this.userManagementService.selectedUserList = [];
    this.userManagementService.selectedUserModelList = [];
    for (var i = 0; i < this.userManagementService.pendingUsers.length; i++) {
      if (this.userManagementService.pendingUsers[i].checked) {
        this.userManagementService.selectedUserList.push(this.userManagementService.pendingUsers[i].id);
        this.userManagementService.selectedUserModelList.push(this.userManagementService.pendingUsers[i]);
      }
    }
    if (this.userManagementService.selectedUserList.length == 0) {
      this.userManagementService.errorMessage = "Please select atleast one user ID";
      $("#errorModal").modal('show');
    }
    else {
      this.userManagementService.aprroveVariable = approve;
      if (approve) {
        this.userManagementService.infoMsg = "Please confirm if you want to approve the selected user(s)";
        $("#infoModal").modal("show");
      }
      else {
        $("#rejectionInfoMessage").modal("show");
        this.userManagementService.infoMsg = "Please confirm if you want to reject the selected user(s)"
      }
    }
  }

  approveSelected(approve) {

    $("#infoModal").modal("hide");
    $("#rejectionInfoMessage").modal("hide");

    if (approve) {
      this.http.get(Constants.HOME_URL + "approveUser?ids=" + this.userManagementService.selectedUserList,
        { responseType: 'text' }).subscribe(response => {
          this.userManagementService.successApprovedOrreject = response;
          $('#successModal').modal('show');
          this.getUsers();
        },
          error => {
            this.userManagementService.errorMessage = "Some error found."
            $("#errorModal").modal('show');
          });
    }
    else {
      this.userManagementService.rejectionModel = [];
      this.userManagementService.selectedUserModelList.forEach(element => {
        this.userManagementService.rejectionModel.push({ id: element.id, rejectionMessage: element.rejectReason })
      });
      this.http.post(Constants.HOME_URL + "rejectUser?ids=" + this.userManagementService.selectedUserList,
        this.userManagementService.rejectionModel, { responseType: 'text' }).subscribe(response => {
          this.userManagementService.successApprovedOrreject = response;
          $('#successModal').modal('show');
          this.getUsers();
        },
          error => {
            this.userManagementService.errorMessage = error.error.text
            $("#errorModal").modal('show');
          });
    }
  }

  /**
 * Enable the selected user
 * @param id 
 */
  warnEnableUser(id) {
    this.enableUserId = id;
    $("#enableUserModal").modal('show');
  }
  enableUser() {
    this.http.get(Constants.HOME_URL + 'enableUser?userId=' + this.enableUserId, { responseType: 'text' }).subscribe((data) => {
      $("#enableUserModal").modal('hide');
      this.userManagementService.successApprovedOrreject = data;
      $("#successModal").modal('show');
    }, err => {
      $("#enableUserModal").modal('hide');
      this.userManagementService.successApprovedOrreject = 'some server error occured';
      $("#successModal").modal('show');

    });
  }
  disableUser(id) {
    this.disableUserId = id;
    $("#disableUserModal").modal('show');
  }
  /**
   * Disable the selected user
   * @param id 
   */
  disableUserDetails() {
    this.http.get(Constants.HOME_URL + 'disableUser?userId=' + this.disableUserId, { responseType: 'text' }).subscribe((data) => {
      $("#disableUserModal").modal('hide');
      this.userManagementService.successApprovedOrreject = data;
      $("#successModal").modal('show');
    }, err => {
      // console.log(err);
      $("#disableUserModal").modal('hide');
      this.userManagementService.successApprovedOrreject = 'some server error occured';
      $("#successModal").modal('show');
    });
  }
  selectUserForActivateDeactivate() { }
  refreshData() {
    this.userManagementService.selectedUserList = [];
    this.userManagementService.selectedUserModelList = [];
    this.userManagementService.getUserDetails().subscribe(data => {
      this.userManagementService.getData(data);
    });
  }

  //  selected tab
  tabChanged(e) {
    this.tabIndex = e.index;
    this.userManagementService.selectedUserModelList = [];
    if (e.index == 0) {
      if (this.userManagementService.pendingUsers.length == 0) {
        this.userManagementService.noData = true;
      } else {
        this.userManagementService.noData = false;
      }
      this.userManagementService.p = 1;
      this.userManagementService.showpPendingUsersPagination = true;
      this.userManagementService.showpAcceptedUsersPagination = false;
      this.userManagementService.showpRejectedUsersPagination = false;
      this.userManagementService.hideBtnsIfRejectedusers = true;
    } else if (e.index == 1) {
      if (this.userManagementService.approveUsers.length == 0) {
        this.userManagementService.noData = true;
      } else {
        this.userManagementService.noData = false;
      }
      this.userManagementService.a = 1;
      this.userManagementService.showpAcceptedUsersPagination = true;
      this.userManagementService.showpPendingUsersPagination = false;
      this.userManagementService.showpRejectedUsersPagination = false;
      this.userManagementService.hideBtnsIfRejectedusers = false;
    } else if (e.index == 2) {
      if (this.userManagementService.rejectedUsers.length == 0) {
        this.userManagementService.noData = true;
      } else {
        this.userManagementService.noData = false;
      }
      this.userManagementService.r = 1;
      this.userManagementService.showpPendingUsersPagination = false;
      this.userManagementService.showpAcceptedUsersPagination = false;
      this.userManagementService.showpRejectedUsersPagination = true;
      this.userManagementService.hideBtnsIfRejectedusers = false;
    } else {
      this.userManagementService.noData = false;
      this.userManagementService.hideBtnsIfRejectedusers = true;
    }
  }

  // download id proof file
  downloadImageFile(filePath) {
    window.location.href = Constants.HOME_URL + 'downloadFromFilePath?filePath=' + filePath;
  }
  selectUserForRejectiOrApproval(e, selectUser) {
    if (e == true) {
    }
  }
  convertText(model) {
    this.userManagementService.reasonTextLengthInNumber = parseInt(model);
  }
  showLists() {
    // $(".left-list").attr("style", "display: block !important");
    // $('.mob-left-list').attr("style", "display: none !important");
  }

}

import { Component, OnInit } from '@angular/core';
import { Constants } from 'src/app/constants';
import { HttpResponse, HttpEventType } from '@angular/common/http';
import saveAs from 'save-as';
declare var $: any;
import { RequestModel } from '../models/request.model';
import { UsermanagementService } from '../services/usermanagement.service';

@Component({
  selector: 'rmncha-bulk-user-registration',
  templateUrl: './bulk-user-registration.component.html',
  styleUrls: ['./bulk-user-registration.component.scss']
})
export class BulkUserRegistrationComponent implements OnInit {

  errorMessage: string;
  temlateDownloadDisclamer: string = Constants.DOWNLOAD_TEMPLATE;
  templateUploadDisclamer: string = Constants.UPLOAD_TEMPLATE;
  progress: { percentage: number } = { percentage: 0 };
  currentFileUpload: boolean = false;
  fileName: string = '';
  file: any;
  finalUpload: any;
  validated: boolean = false;
  validateResponse: ResponseModel;
  uploadedResponse: ResponseModel;
  sucessMessage: string = '';
  infoMessage: string = ''
  message: any;
  requestModel: RequestModel = new RequestModel();
  userServices: UsermanagementService;
  constructor(private userManagementServiceProvider: UsermanagementService) {
    this.userServices = userManagementServiceProvider;
  }

  ngOnInit() {

  }

  /* file selection */
  onFileChange($event) {
    if ($event.srcElement.files.length == 0) {
      this.fileName = undefined;
      this.file = undefined;
    } else {
      if ($event.srcElement.files[0]) {
        if (
          (($event.srcElement.files[0].name.split('.')[($event.srcElement.files[0].name.split('.') as string[]).length - 1] as String).toLocaleLowerCase() === 'xlsx')
          || (($event.srcElement.files[0].name.split('.')[($event.srcElement.files[0].name.split('.') as string[]).length - 1] as String).toLocaleLowerCase() === 'xls')
        ) {
          this.fileName = $event.srcElement.files[0].name;
          this.file = $event.srcElement.files[0];
          this.currentFileUpload = true;
          this.userServices.uploadBulkFile(this.file).subscribe(event => {
            if (event.type === HttpEventType.UploadProgress) {
              this.progress.percentage = Math.round(
                (100 * event.loaded) / event.total
              );
            } else if (event instanceof HttpResponse) {
              this.currentFileUpload = false;
              this.validateResponse = null;
              this.validated = false;
              this.finalUpload = event.body;
              if (this.finalUpload.length > 5) {
                this.currentFileUpload = false;
                this.validateResponse = null;
                this.validated = false;
                this.progress.percentage = 0;
                this.file = undefined;
                this.fileName = undefined;
                this.errorMessage = this.finalUpload;
                $('#errorModalLists').modal('show');
              }else if(this.finalUpload.length != 0){
                this.currentFileUpload = false;
                this.validateResponse = null;
                this.validated = false;
                this.progress.percentage = 0;
                this.file = undefined;
                this.fileName = undefined;
                $event.srcElement.value = null;
                this.errorMessage = this.finalUpload;
                $('#errorModal').modal('show');
              } else {
                this.sucessMessage = "Template uploaded successfully.";
                $('#successModal').modal('show');
              }
              this.progress.percentage = 0;
            }
          }, error => {
            this.currentFileUpload = false;
            this.validateResponse = null;
            this.validated = false;
            this.progress.percentage = 0;
            this.file = undefined;
            this.fileName = undefined;
            $event.srcElement.value = null;
            this.errorMessage = Constants.SERVER_ERROR_MESSAGE;
            $('#errorModal').modal('show');
          });
        } else {
          $event.srcElement.value = null;
          this.errorMessage = 'Upload a xlsx file';
          $('#errorModal').modal('show');
        }
      }
    }
  }
  uploadClicked() {
    $('#fileUpload').click();
  }

  resetField() {
    this.currentFileUpload = false;
    this.validateResponse = null;
    this.validated = false;
    this.progress.percentage = 0;
    this.file = undefined;
    this.fileName = undefined;
  }
  

  // download id proof file
  downloadBulkUserFile() {
    // window.location.href = Constants.COLLECTION_SERVICE_URL + 'downloadTemplate';
    this.userServices.downloadTemplateFile().subscribe(
      data => {
        saveAs(data, "Bulk User Template.xlsx");
      },
      error => {
        this.errorMessage = Constants.SERVER_ERROR_MESSAGE;
        $('#errorModal').modal('show');
      }
    );
  }

  uploadedFile() {
    saveAs(this.file, this.fileName);
  }

  downloadOutputFile(fileData: ResponseModel) {
    this.userServices.downloadFile(fileData.filePath).subscribe(
      data => {
        saveAs(data, fileData.fileName);
      },
      error => {
        this.errorMessage = Constants.SERVER_ERROR_MESSAGE;
        $('#errorModal').modal('show');
      }
    );
  }

  /* hide show left panel accroding to window size */
  showLists() {
    $(".left-list").attr("style", "display: block !important");
    $('.mob-left-list').attr("style", "display: none !important");
  }
  ngAfterViewInit() {
    $("input, textarea, .select-dropdown").focus(function () {
      $(this).closest(".input-holder").parent().find("> label").css({ "color": "#4285F4" })

    })
    $("input, textarea, .select-dropdown").blur(function () {
      $(this).closest(".input-holder").parent().find("> label").css({ "color": "#333" })
    })
    $('body,html').click(function (e) {
      if ((window.innerWidth) <= 767) {
        if (e.target.className == "mob-left-list") {
          return;
        } else {
          $(".left-list").attr("style", "display: none !important");
          $('.mob-left-list').attr("style", "display: block !important");
        }
      }
    });
  }

}

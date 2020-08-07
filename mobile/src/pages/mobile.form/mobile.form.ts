import { timer } from 'rxjs/observable/timer';
import { HttpClient } from '@angular/common/http';
import {
  Component,
  ViewChild,
  HostListener
} from '@angular/core';
import {
  IonicPage,
  NavController,
  NavParams,
  ViewController,
  Content,
  AlertController,
  Platform,
  IonicApp,
  Navbar,
  ActionSheetController
} from 'ionic-angular';

import {
  QuestionServiceProvider
} from '../../providers/question-service/question-service';
import {
  MessageServiceProvider
} from '../../providers/message-service/message-service';
import {
  FormServiceProvider
} from '../../providers/form-service/form-service';
import {
  DataSharingServiceProvider
} from '../../providers/data-sharing-service/data-sharing-service';
import {
  ConstantProvider
} from '../../providers/constant/constant';
import {
  DatePipe
} from '@angular/common';
import {
  DatePicker
} from '@ionic-native/date-picker';
import {
  EngineUtilsProvider
} from '../../providers/engine-utils/engine-utils';
import {
  AmazingTimePickerService
} from 'amazing-time-picker';
import {
  UUID
} from 'angular2-uuid';
import {
  WebFormComponent
} from '../../components/web/web.form';
import {
  IMyDpOptions,
  IMyDateModel
} from 'mydatepicker';
import {
  Geolocation
} from '@ionic-native/geolocation';
import {
  Camera,
  CameraOptions
} from '@ionic-native/camera';
import {
  ApplicationDetailsProvider
} from '../../providers/application/appdetails.provider';
import {
  CommonsEngineProvider
} from '../../providers/commons-engine/commons-engine';
import {
  UserServiceProvider
} from '../../providers/user-service/user-service';
import {
  Storage
} from '@ionic/storage';
import {
  ConstraintTokenizer
} from '../../providers/engine-utils/constraintsTokenizer';
import {
  ImagePicker
} from '@ionic-native/image-picker';
import {
  Base64
} from '@ionic-native/base64';
import {
  FileChooser
} from '@ionic-native/file-chooser';
import {
  FilePath
} from '@ionic-native/file-path';
import {
  File, IWriteOptions
} from '@ionic-native/file';
import { WebFormService } from '../../providers/web.form.service';
// import { SUPPORTED_LANGS,getSelectedLanguage } from './../../config/translate';
// import { TranslateService } from '@ngx-translate/core';

@IonicPage()
@Component({
  selector: 'mobile-form',
  templateUrl: 'mobile.form.html'
})
export class MobileFormComponent {

  isWeb: boolean = false;
  section: String;
  dataSharingService: DataSharingServiceProvider;
  repeatSubSection: Map<Number, IQuestionModel> = new Map()
  tempFormSubSections2;
  sectionNames = []
  sectionHeading: any;
  selectedSection: Array<Map<String, Array<IQuestionModel>>>;
  subSections: Array<Map<String, Array<IQuestionModel>>>
  sectionMap: Map<String, Array<Map<String, Array<IQuestionModel>>>> = new Map();
  data: Map<String, Array<Map<String, Array<IQuestionModel>>>> = new Map();
  dbFormModel: IDbFormModel;
  sectionHeader: any;
  minDate: any;
  maxDate: any;
  questionMap: {} = {}
  optionMap: {} = []
  formId: string
  formTitle: String
  formTitleActive: boolean = false;
  errorStatus: boolean = false;
  mandatoryQuestion: {} = {};
  disableStatus: boolean = false;
  disablePrimaryStatus: boolean = false;
  saveType: String
  uniqueId: String = "";
  fullDate: any;
  createdDate: String = "";
  updatedDate: String = "";
  updatedTime: String = "";
  checkFieldContainsAnyvalueStatus: boolean = false;
  segment: boolean = false
  countBeginRepeat = 0;
  beginrepeatArraySize: number = 0;
  isNewFormEntry: boolean;
  beginRepeatKey: Number;
  base64Image: any;
  options = {
    enableHighAccuracy: true,
    timeout: 10000
  };


  questionDependencyArray: {} = {}
  questionFeaturesArray: {} = {}
  constraintsArray: {} = {}
  beginRepeatArray: {} = {}
  scoreKeyMapper: {} = [];

  //for score keeper
  questionInSectionMap: {} = [];
  questionInSubSectionMap: {} = [];

  sectionScoreKeyMapper: {} = [];
  subSectionScoreKeyMapper: {} = [];

  sectionMapScoreKeeper: {} = [];
  subSectionMapScoreKeeper: {} = [];

  checklist_score_keeper_colName: any
  indexMap: {} = {};
  public photo: any;
  existingForm: any;
  areaList=[]

  shelterList:any
  filteredShelterList=[]
  villageList:any
  filteredVillageList=[]

  fromDate=""
  toDate=""
  /**
   * If it is new form entry, the vaue will be true otherwise the value will be false
   * @author Ratikanta
   * @type {boolean}
   * @memberof FormListPage
   */

  @ViewChild(Navbar) navBar: Navbar;
  @ViewChild(Content) content: Content;
  @ViewChild(WebFormComponent) customComponent: WebFormComponent;
  @HostListener('window:popstate', ['$event'])
  onbeforeunload(event) {
    if (window.location.href.substr(window.location.href.length - 4) == 'form') {

    }
    if (window.location.href.substr(window.location.href.length - 5) == 'login') {
      history.pushState(null, null, "" + window.location.href);
    }
  }
  viewCanLeave = false;
  isExitConfirmDialogOpen = false;
  constructor(private constraintTokenizer: ConstraintTokenizer, public commonsEngineProvider: CommonsEngineProvider, private applicationDetailsProvider: ApplicationDetailsProvider, public questionService: QuestionServiceProvider,
    public messageService: MessageServiceProvider, private navCtrl: NavController, public datepipe: DatePipe, public datePicker: DatePicker,
    public viewCtrl: ViewController, public formService: FormServiceProvider, public navParams: NavParams, private dataSharingProvider: DataSharingServiceProvider,
    private atp: AmazingTimePickerService, private alertCtrl: AlertController, private platform: Platform,private http:HttpClient,
    private app: IonicApp, private engineUtilsProvider: EngineUtilsProvider, private imagePicker: ImagePicker, public actionSheetCtrl: ActionSheetController,
    private geolocation: Geolocation, private camera: Camera, private storage: Storage, private userService: UserServiceProvider,
    public filePath: FilePath, public fileChooser: FileChooser, public base64: Base64, private file: File, public webFormService: WebFormService) {
      // translate.setDefaultLang('en');
      // translate.addLangs(SUPPORTED_LANGS);
      // translate.use(getSelectedLanguage(translate));
    this.dataSharingService = dataSharingProvider;
  }

  /**
   * This method is used to fetch the geo location.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  getGeoLocation(question) {
    this.messageService.showLoader(ConstantProvider.message.pleaseWait);
    this.geolocation.getCurrentPosition(this.options).then(resp => {
      this.messageService.stopLoader();
      this.questionMap[question.columnName].value = "Lat :" + resp.coords.latitude + " Long :" + resp.coords.longitude;
    }).catch(error => {
      this.messageService.stopLoader();
      this.messageService.showErrorToast("Unable to fetch location, please try again.");
      console.log("Error getting location", error);
    });
  }

  /**
   * This method used to open the gallery for camera.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  openCamera(question) {
    document.getElementById(question.columnName + "file-input").click();
  }

  /**
   * This method is used for selection of image from the gallery
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param $event
   * @param question
   */
  openCameraGallery(question) {
    // if (question.value != null) {
    let actionSheet = this.actionSheetCtrl.create({
      title: 'Select Image Source',
      buttons: [{
        text: 'Load from Library',
        handler: () => {
          this.openGallery(question);
        }
      },
      {
        text: 'Use Camera',
        handler: () => {
          this.takePhoto(question);
        }
      },
      {
        text: 'Cancel',
        role: 'cancel'
      }
      ]
    });
    actionSheet.present();
    // }
  }

  /**
   * This method is used for selection of image from the gallery
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  openGallery(question) {

    this.imagePicker.hasReadPermission().then(
      (result) => {
        if (result == false) {
          // no callbacks required as this opens a popup which returns async
          this.imagePicker.requestReadPermission().then(d => {
            if (d == true) {
              this.fileReader(question)
            }
          })
        } else if (result == true) {
          this.fileReader(question)
        }
      }, (err) => {
        console.log(err);
      });
  }

  fileReader(question) {
    this.messageService.showLoader(ConstantProvider.message.pleaseWait);
    let numOfImage = 1
    let options = {
      maximumImagesCount: numOfImage,
      quality: 30, // picture quality
      destinationType: this.camera.DestinationType.FILE_URI,
      encodingType: this.camera.EncodingType.JPEG,
      mediaType: this.camera.MediaType.PICTURE
    };

    const iWriteOptions: IWriteOptions = {
      replace: true
    }
    this.imagePicker.hasReadPermission().then(
      (result) => {
        if (result == false) {
          // no callbacks required as this opens a popup which returns async
          this.imagePicker.requestReadPermission();
        } else if (result == true) {
          this.imagePicker.getPictures(options).then(async imageURL => {

            let fileName = imageURL[0].substr(imageURL[0].lastIndexOf('/') + 1)
            let base64String = await this.file.readAsDataURL(imageURL[0].substr(0, imageURL[0].lastIndexOf('/') + 1), fileName)
            let imageBlob = await this.commonsEngineProvider.dataURItoBlob(base64String, "image/jpeg")
            this.commonsEngineProvider.createFoldersInMobileDevice(this.formId, this.uniqueId, this.file, this.messageService).then((async d => {
              let writeToFilePath = this.file.externalRootDirectory + ConstantProvider.appFolderName + "/" + this.formId + "/" + this.uniqueId;
              let writtenFile = await this.file.writeFile(writeToFilePath, fileName, imageBlob, iWriteOptions)
              this.questionMap[question.columnName].value = []
              this.questionMap[question.columnName].value.push(writtenFile.nativeURL)

              this.messageService.stopLoader();
            })).catch(err => {
              this.messageService.stopLoader()
              this.messageService.showErrorToast(err)
            })
          }, (err) => {
            this.messageService.stopLoader();
            console.log(err);
          });
        }
      }).catch((err) => {
        this.messageService.stopLoader();
        console.log(err);
      });
  }

  /**
   * This method is used for take photo using phone camera
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  takePhoto(question) {
    this.messageService.showLoader(ConstantProvider.message.pleaseWait);
    const options: CameraOptions = {
      quality: 30, // picture quality
      destinationType: this.camera.DestinationType.DATA_URL,
      encodingType: this.camera.EncodingType.JPEG,
      sourceType: this.camera.PictureSourceType.CAMERA,
      mediaType: this.camera.MediaType.PICTURE,
      saveToPhotoAlbum: false
    }

    const iWriteOptions: IWriteOptions = {
      replace: true
    }

    this.camera.getPicture(options).then(async base64 => {
      let image = "data:image/jpeg;base64," + base64
      let imageBlob = await this.commonsEngineProvider.dataURItoBlob(image, this.commonsEngineProvider.getContentType(image))

      this.commonsEngineProvider.createFoldersInMobileDevice(this.formId, this.uniqueId, this.file, this.messageService).then((async d => {
        let currentTime = +new Date();
        let random = Math.random()
        let trends = random + ""
        let filename = trends.replace('.', '') + '_' + question.columnName + '_' + currentTime + ".jpg";

        let writeToFilePath = this.file.externalRootDirectory + ConstantProvider.appFolderName + "/" + this.formId + "/" + this.uniqueId;
        let writtenFile = await this.file.writeFile(writeToFilePath, filename, imageBlob, iWriteOptions)

        this.questionMap[question.columnName].value = []
        this.questionMap[question.columnName].value.push(writtenFile.nativeURL)
        this.messageService.stopLoader()
      })).catch(err => {
        this.messageService.stopLoader()
        this.messageService.showErrorToast(err)
      })

    }, (err) => {
      console.log(err);
      this.messageService.stopLoader()
      this.messageService.showErrorToast("Please clear your cache.<br>" + err)
    });
  }

  /**
   * This method is used to delete the photo
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   * @param index
   */
  deletePhoto(question, index) {
    if (!this.disableStatus) {
      let confirm = this.alertCtrl.create({
        title: 'Warning',
        message: '<strong>Are sure you want to delete this photo?</strong>',
        buttons: [{
          text: 'No',
          handler: () => {
            console.log('Disagree clicked');
          }
        }, {
          text: 'Yes',
          handler: () => {
            console.log('Agree clicked');
            this.questionMap[question.columnName].value.splice(index, 1);
          }
        }]
      });
      confirm.present();
    }
  }

  /**
   * This method is used for selection of image from the gallery
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param $event
   * @param question
   */
  onCameraFileChange($event, question) {
    let files = $event.target.files;
    let file = files[0];

    if (
      (file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String)
        .toLocaleLowerCase() === "png" || (file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String)
          .toLocaleLowerCase() === "jpg" || (file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String)
            .toLocaleLowerCase() === "jpeg"
    ) {
      let reader = new FileReader()
      reader.onload = this._handleReaderLoaded.bind(this);
      this.base64Image = question;
      reader.readAsBinaryString(file);
    } else {
      this.messageService.showErrorToast("Please select a image")
    }
  }

  /**
   * This method is used to convert the image file into base64 and set it ti the src variable ,also attach the lat long accurarcy in the meta_info
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param readerEvt
   */
  _handleReaderLoaded(readerEvt) {
    let binaryString = readerEvt.target.result;
    this.geolocation
      .getCurrentPosition(this.options)
      .then(resp => {
        this.questionMap[this.base64Image.columnName].value = {
          src: "data:image/jpeg;base64," + btoa(binaryString),
          meta_info: "Latitude :" + resp.coords.latitude + "; Longitude :" + resp.coords.longitude + "; Accuracy :" + resp.coords.accuracy
        }
      })
      .catch(error => {
        this.questionMap[this.base64Image.columnName].value = {
          src: "data:image/jpeg;base64," + btoa(binaryString)
        }
      });

  }

  /**
   * This method call up the initial load. Get all the form data from database
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  async ngOnInit() {
    await this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then((userAndForm) => {
      if (userAndForm) {
        let areaList = userAndForm['getAllAreaList']
        this.shelterList=areaList['shelter home']
        this.villageList=areaList['village']
      }
    });

    this.isWeb = this.applicationDetailsProvider.getPlatform().isWebPWA
    this.formId = this.navParams.get('formId')
    this.formTitle = this.navParams.get('formTitle')
    this.existingForm = this.navParams.get("existingForm")
    this.isNewFormEntry = this.navParams.get('isNew') ? true : false;
    if (!this.isNewFormEntry) {
      this.viewCanLeave = true;
    }
    this.segment = this.navParams.get('segment') == 'save' ? true : false
    let monthRes = Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[1]) - 1
    let yearRes = Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[0])
    let dayRes = Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[2])
    let fullDateRes = yearRes + "-" + monthRes + "-" + dayRes
    this.minDate = this.datepipe.transform(new Date(fullDateRes), "yyyy-MM-dd")
    this.maxDate = this.datepipe.transform(new Date(), 'yyyy-MM-dd')
    this.fullDate = this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-");
    if (!this.isWeb) {
      this.messageService.showLoader(ConstantProvider.message.pleaseWait)
      if (this.navParams) {
        if (!(this.navParams.get("submission") == undefined)) {
          this.saveType = "old";


          let tempData = await this.storage.get(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username);
          let tempSubmissions = tempData[this.formId as any]
          let tempSubmission = tempSubmissions[(this.navParams.get("submission") as IDbFormModel).uniqueId as any] as any
          this.data = tempSubmission.formData

          let tempval = await this.storage.get(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username);
          let forSub = tempval[this.formId as any]
          let submission = forSub[(this.navParams.data.submission.uniqueId)] as any
          this.data = submission.formData

          this.disableStatus = submission.formStatus == "save" || submission.formStatus == "rejected" ? false : true;
          this.uniqueId = submission.uniqueId;
          this.createdDate = submission.createdDate;
          this.updatedDate = submission.updatedDate;
          this.updatedTime = submission.updatedTime;

          this.disablePrimaryStatus = true;
          //s means saved
          await this.questionService.getQuestionBank(this.formId, null, ConstantProvider.lastUpdatedDate).then(async schema => {
            if (schema) {
              this.data = await this.commonsEngineProvider.loadDataIntoSchemaDef(schema, this.data)
              await this.loadQuestionBankIntoUI(this.data, "s");
            } else {
              this.viewCanLeave = true;
              this.navCtrl.setRoot("LoginPage");
            }
          });

        } else {
          this.saveType = "new";
          this.uniqueId = UUID.UUID();
          if (this.formId) {
            await this.questionService.getQuestionBank(this.formId, null, ConstantProvider.lastUpdatedDate).then(async data => {
              if (data) {
                let formData = data;
                // n means new
                await this.loadQuestionBankIntoUI(formData, "n");
              } else {
                this.viewCanLeave = true;
                this.navCtrl.setRoot("LoginPage");
              }
            });
          } else {
            this.viewCanLeave = true;
            this.navCtrl.setRoot("LoginPage");
          }
        }
      } else {
        this.viewCanLeave = true;
        this.navCtrl.setRoot("LoginPage");
      }
    }
    // area
  }

  /**
   * This function is use to set the options in the date picker like dateFormat,disableSince,editableDateField,showTodayBtn,showClearDateBtn
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  public myDatePickerOptions: IMyDpOptions = {
    // other options...
    dateFormat: 'dd-mm-yyyy',
    disableSince: {
      year: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[0]),
      month: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[1]),
      day: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')[2]) + 1
    },
    editableDateField: false,
    showTodayBtn: false,
    showClearDateBtn: false
  };



  async loadQuestionBankIntoUI(data, status) {
    this.data = data;
    // added new method to load options into the schema defination if form is saved form
    if (status == "s") {
      this.optionMap = await this.commonsEngineProvider.loadOptionsIntoData(this.formId);
    }

    for (let index = 0; index < Object.keys(data).length; index++) {
      this.sectionMap.set(Object.keys(data)[index], data[Object.keys(data)[index]]);
      for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
        let subSections = data[Object.keys(data)[index]][0];
        let counter = 1;
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q];
            question.questionOrderDisplay = false;

            if (question.attachmentsInBase64 == null) question.attachmentsInBase64 = [];
            switch (question.controlType) {
              case "table":
              case "tableWithRowWiseArithmetic":
                question.displayComponent = true;
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                this.questionMap[question.columnName] = question;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;
                for (let row = 0; row < question.tableModel.length; row++) {
                  for (let column = 0; column < Object.keys(question.tableModel[row]).length; column++) {
                    let value = question.tableModel[row][Object.keys(question.tableModel[row])[column]];
                    if (typeof value == "object") {
                      let cell = value;
                      //convert the number type data to string (while rejection data is commming)
                      if (cell.value != null) {
                        cell.value = String(cell.value)
                      }
                      cell.dependecy = cell.relevance != null ? true : false;
                      cell.displayComponent = cell.relevance == null ? true : false;
                      this.questionMap[cell.columnName] = cell;
                      //   cell = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(cell);
                      cell.relevance != null ? this.drawDependencyGraph(cell.relevance, cell) : null;
                      this.mandatoryQuestion[cell.columnName] = cell.finalizeMandatory;
                      if (this.disableStatus) {
                        cell.showErrMessage = false;
                      }
                    }
                  }
                }
                break;
              case 'beginrepeat':
                question.displayComponent = true
                question.beginRepeatMinusDisable = false
                question.beginrepeatDisableStatus = false
                this.repeatSubSection.set(question.key, question)
                question.beginRepeatMinusDisable = false
                if (question.beginRepeat.length == 1) {
                  question.beginRepeatMinusDisable = true
                }
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                this.questionMap[question.columnName] = question;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;
                for (let index = 0; index < question.beginRepeat.length; index++) {
                  let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[index];
                  for (let beginRepeatQuestion of beginRepeatQuestions) {
                    if (status == "s") {
                      switch (beginRepeatQuestion.controlType) {
                        case "dropdown":
                        case "autoCompleteTextView":
                        case "autoCompleteMulti":
                          let optionTemp = this.optionMap[beginRepeatQuestion.columnName.split("-")[3]]
                          beginRepeatQuestion.options = JSON.parse(JSON.stringify(optionTemp));
                          break
                      }
                    }

                    //convert the number type data to string (while rejection data is commming)
                    if (beginRepeatQuestion.controlType == 'textbox' && beginRepeatQuestion.type == 'tel' && beginRepeatQuestion.value != null) {
                      beginRepeatQuestion.value = String(beginRepeatQuestion.value)
                    }



                    beginRepeatQuestion.dependecy = beginRepeatQuestion.relevance != null ? true : false;
                    beginRepeatQuestion.displayComponent = beginRepeatQuestion.relevance == null ? true : false;
                    this.questionMap[beginRepeatQuestion.columnName] = beginRepeatQuestion;
                    beginRepeatQuestion.relevance != null ? this.drawDependencyGraph(beginRepeatQuestion.relevance, beginRepeatQuestion) : null;
                    this.mandatoryQuestion[beginRepeatQuestion.columnName] = beginRepeatQuestion.finalizeMandatory;
                    if (this.disableStatus) {
                      beginRepeatQuestion.showErrMessage = false;
                    }
                  }
                }
                break;
              case 'camera':
                // if(question.attachmentsInBase64){

                //   question.attachmentsInBase64 = []
                //   for(let fpath of question.attachmentsInBase64){
                //         question.value[index]=fpath
                //         question.attachmentsInBase64=null
                //     }
                //   }
                question.questionOrderDisplay = true;
                this.indexMap[question.questionOrder] = counter;
                counter++;

                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                this.questionMap[question.columnName] = question;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;

                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                if (this.disableStatus) {
                  question.showErrMessage = false;
                }
                break;

              // add this 2 case ie autoCompleteTextView and autoCompleteMulti for displaying the component in ui ie one is for single select autocomplete textview
              // and multi select autocomplete textview.
              case "dropdown":
              case "autoCompleteTextView":
              case "autoCompleteMulti":
                {

                  if (status == "s") {
                    question.options = this.optionMap[question.columnName]
                  }
                  if(question.controlType=='autoCompleteTextView'){
                    if(question.value!=null){
                      if(question.columnName=='undp_f1_q20_w4'){
                        let keyVal=question.value
                        let tempVal=this.villageList.filter(f=>Number(f.areaId)==Number(keyVal))
                        let tempOption={
                          "key" : Number(tempVal[0].areaId),
                          "value" : tempVal[0].areaName
                        }
                         question.value=tempOption

                      }else if(question.columnName=='undp_f1_q33'){
                        let keyVal=question.value
                        let tempVal=this.shelterList.filter(f=>Number(f.areaId)==keyVal)
                        let tempOption={
                          "key" : Number(tempVal[0].areaId),
                          "value" : tempVal[0].areaName
                        }
                         question.value=tempOption
                      }
                      else{
                        let keyVal=question.value
                        let tempVal=question.options.filter(d=>d.key==keyVal)
                         question.value=tempVal[0]

                      }

                    }

                  }
                  if (!question.label.includes('Score')) {
                    question.questionOrderDisplay = true;
                    this.indexMap[question.questionOrder] = counter;
                    counter++;
                  }
                  //convert the number type data to string (while rejection data is commming)
                  if (question.controlType == 'textbox' && question.type == 'tel' && question.value != null) {
                    question.value = String(question.value)
                  }
                  question.optionsOther = question.options;
                  // if(question.controlType=='autoCompleteTextView'){
                  //   let keyVal=question.value
                  //   question.value=question.optionsOther.filter(d=>d.key==keyVal)
                  // }
                  question.dependecy = question.relevance != null ? true : false;
                  question.displayComponent = question.relevance == null ? true : false;
                  //   question = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(question)
                  this.questionMap[question.columnName] = question;
                  question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;

                  this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                  if (this.disableStatus) {
                    question.showErrMessage = false;
                  }
                }
                break
              case "textbox":
              case "heading":
              case "Time Widget":
              case "cell":
              case "textarea":
              case "file":
              case 'geolocation':

                if (!question.label.includes('Score')) {
                  question.questionOrderDisplay = true;
                  this.indexMap[question.questionOrder] = counter;
                  counter++;
                }
                //convert the number type data to string (while rejection data is commming)
                if (question.controlType == 'textbox' && question.type == 'tel' && question.value != null) {
                  question.value = String(question.value)
                }
                question.optionsOther = question.options;

                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                //   question = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(question)
                this.questionMap[question.columnName] = question;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;

                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                if (this.disableStatus) {
                  question.showErrMessage = false;
                }
                break;
              case "Date Widget":
              case "Month Widget":
                question.questionOrderDisplay = true;
                this.indexMap[question.questionOrder] = counter;
                counter++;
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                //   question = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(question);
                this.questionMap[question.columnName] = question;
                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;
                if (this.disableStatus) {
                  question.showErrMessage = false;
                }else{
                  if(question.columnName=='undp_f1_date_1'){
                    this.toDate=this.maxDate
                    this.fromDate=this.datepipe.transform(new Date("2020-03-01"), "yyyy-MM-dd")
                    question.value=this.datepipe.transform(new Date(), "yyyy-MM-dd")
                  }
                  if(question.columnName=='undp_f1_q25_5'){
                    question.value=this.datepipe.transform(new Date(), "yyyy-MM-dd")
                  }
                  if(question.columnName=='undp_f1_qrtn_compltn_dt'){
                    let dateVal = new Date()
                    dateVal.setDate(dateVal.getDate() + 14);
                    question.value=this.convertDateToMMDDYYYY(dateVal)
                  }
                }
                break;
              case "checkbox":
                question.questionOrderDisplay = true;
                this.indexMap[question.questionOrder] = counter;
                counter++;
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                //   question = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(question);
                this.questionMap[question.columnName] = question;
                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;
                if (this.disableStatus) {
                  question.showErrMessage = false;
                }
                break;
            }
            //set the default
          }
        }
      }
    }

    // check relevance fro each question
    this.checkRelevanceForEachQuestion()
    for (let questionKey of this.dataSharingService.getKeys(this.beginRepeatArray)) {
      let question = this.questionMap[questionKey];
      let bgQuestion = this.beginRepeatArray[questionKey];
      if (question.value == null || question.value == 0) {
        bgQuestion.beginrepeatDisableStatus = true;
      }
    }

    for (let q of Object.keys(this.questionMap)) {
      let ques = this.questionMap[q]
      this.setupDefaultSettingsAndConstraintsAndFeatureGraph(ques)
      // ques.relevance != null ? this.drawDependencyGraph(ques.relevance, ques) : null;
    }

    for (let questionKey of this.dataSharingService.getKeys(this.questionMap)) {
      let question = this.questionMap[questionKey]
      if (question.features != null) {
        let feature: string = question.features.split("@AND")
        switch (question.controlType) {
          case 'dropdown':
          case "autoCompleteMulti":
          case "autoCompleteTextView":
            for (let i = 0; i < feature.length; i++) {
              if (feature[i].includes('area_group')) {
                let groupQuestions = this.commonsEngineProvider.getDependentAreaGroupName(feature[i])
                let childLevelQuestion = this.questionMap[groupQuestions];
                let optionCount = 0;
                childLevelQuestion.optionsOther = []
                for (let option of childLevelQuestion.options) {
                  if (question.type == "checkbox") {
                    if (question.value && question.value.indexOf(option["parentId"]) != -1) {
                      //this is used to make the village field disabled initially by making option["visible"] = false; and optionCount++
                      if (option["parentId2"] == -2) {
                        option["visible"] = false;
                        optionCount++
                      } else {
                        option["visible"] = true;
                        childLevelQuestion.optionsOther.push(option)
                      }
                    } else {
                      //this is used to used to adding the other to the filtered village by making the option["visible"] = true;
                      if (option["parentId2"] == -2) {
                        option["visible"] = true;
                      } else {
                        option["visible"] = false;
                      }
                      optionCount++
                    }
                  } else {
                    if (option['parentId'] == question.value) {
                      //this is used to make the village field disabled initially by making option["visible"] = false; and optionCount++
                      if (option["parentId2"] == -2) {
                        option["visible"] = false;
                        optionCount++
                      } else {
                        option["visible"] = true;
                        childLevelQuestion.optionsOther.push(option)
                      }
                    } else {
                      //this is used to used to adding the other to the filtered village by making the option["visible"] = true;
                      if (option["parentId2"] == -2) {
                        option["visible"] = true;
                      } else {
                        option["visible"] = false;
                      }
                      optionCount++
                    }
                  }
                }
                if (optionCount == childLevelQuestion.options.length) {
                  // childLevelQuestion.constraints = "disabled"
                } else {
                  childLevelQuestion.constraints = ""
                }
              }
            }
            break;
        }
      }


    }
    // console.log(this.questionMap);
    // console.log("features array: ", this.questionFeaturesArray);
    // console.log("dependencies array: ", this.questionDependencyArray);
    // console.log("beginRepeatArray", this.beginRepeatArray);
    // console.log("repeat Subsection", this.repeatSubSection);
    // console.log('constraints array', this.constraintsArray)
    // console.log('scoreKeyMapper array', this.scoreKeyMapper)
    this.sectionNames = Array.from(this.sectionMap.keys());
    this.section = this.sectionNames[0];
    this.sectionSelected(this.section);
    this.messageService.stopLoader()
  }
  //Biswa
  tempCaryId: any;

  /**
   * This method called when user need to switch to sub-section dynamically
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @author Biswa Ranjan (biswaranjan@sdrc.co.in)
   * @param sectionHeading
   */
  sectionSelected(key?: any) {
    this.sectionHeader = this.section
    this.content.scrollToTop(300);
    this.selectedSection = this.sectionMap.get(this.section)
  }

  /**
   * This method is called, to show a alert message before deletion of the begin repeat section..
   *
   * @author Jagat (jagat@sdrc.co.in)
   * @param key
   * @param bgquestion
   */
  deleteWorkerConfirmation(key: Number, bgquestion: IQuestionModel) {
    let confirm = this.alertCtrl.create({
      enableBackdropDismiss: false,
      cssClass: 'custom-font',
      title: 'Warning',
      message: "Are you sure you want to delete?",
      buttons: [{
        text: 'No',
        handler: () => { }
      },
      {
        text: 'Yes',
        handler: () => {
          this.deleteLastWorker(key, bgquestion)
        }
      }
      ]
    });
    confirm.present();
  }

  /**
   * This method is for table data calculation.
   *
   * @author Azhar (azaruddin@sdrc.co.in)
   * @param cell
   * @param columnIndex
   * @param rowIndex
   * @param tableModel
   */
  async deleteLastWorker(key: Number, bgquestion: IQuestionModel) {
    let beginRepeatParent: IQuestionModel = this.repeatSubSection.get(key);
    let clonedQuestion: IQuestionModel[] = beginRepeatParent.beginRepeat[beginRepeatParent.beginRepeat.length - 1];
    for (let index = 0; index < clonedQuestion.length; index++) {
      clonedQuestion[index].relevance != null ? this.removeFromDependencyGraph(clonedQuestion[index].relevance, clonedQuestion[index]) : null
    }

    if (bgquestion.beginRepeat.length > 1) {
      await this.commonsEngineProvider.removeFilesAndCameraAttachementsIfAny(clonedQuestion);
      bgquestion.beginRepeat.pop();
      if (bgquestion.beginRepeat.length == 1) {
        bgquestion.beginRepeatMinusDisable = true;
      } else {
        bgquestion.beginRepeatMinusDisable = false;
      }
    } else {
      for (let i = 0; i < bgquestion.beginRepeat.length; i++) {
        for (let j = 0; j < bgquestion.beginRepeat[i].length; j++) {
          bgquestion.beginRepeat[i][j].value = null;
        }
      }
    }
  }

  /**
   * This method will call, when user clicks on save or finalize button.
   * This methods checks saveMandatory field to save the record and checks all mandatory fields while finalizing the form.
   * If any field is blank, while checking the mandatory field, switch the control to that particular section and show the blank question name.
   *
   *
   * @author Jagat (jagat@sdrc.co.in)
   * @param type
   */
  async onSave(type: String) {
    // let uniqueName;
    let formId;
    //   let headerData: Map < string, string | number | any[] > = new Map();
    let headerData: {} = {}
    let image: string = ConstantProvider.defaultImage;
    this.messageService.showLoader(ConstantProvider.message.pleaseWait);
    let visitedDate;
    for (let index = 0; index < Object.keys(this.data).length; index++) {
      this.sectionMap.set(Object.keys(this.data)[index], this.data[Object.keys(this.data)[index]])
      for (let j = 0; j < this.data[Object.keys(this.data)[index]].length; j++) {
        let subSections = this.data[Object.keys(this.data)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
            formId = question.formId
            switch (question.controlType) {
              case "Date Widget":
                if (question.defaultSettings == "prefetchDate:current_date") {
                  visitedDate = this.datepipe.transform(question.value, "dd-MM-yyyy")
                }
                break;
              case 'camera': {
                image = question.value ? question.value.src : null
              }
                break
            }
            if (question.value)
              headerData = this.commonsEngineProvider.prepareHeaderData(question, headerData)
          }
        }
      }
    }
    if (type == 'save') {
      this.errorStatus = false;
      this.commonsEngineProvider.checkMandatory(this.sectionMap, this.data, 'save', this)

      if (!this.errorStatus) {
        this.data = await this.commonsEngineProvider.getKeyValue(this.data, this.userService.user.username, this.formId, this.uniqueId, null)

        this.dbFormModel = {
          createdDate: this.createdDate,
          updatedDate: this.updatedDate,
          formStatus: type == 'save' ? 'save' : 'finalized',
          extraKeys: null,
          formData: this.data,
          formSubmissionId: formId,
          uniqueId: this.uniqueId,
          formDataHead: headerData,
          image: image,
          attachmentCount: 0,
          visitedDate: visitedDate,
          formId: this.formId
        }
        await this.formService.saveData(this.formId, this.dbFormModel, this.saveType).then(data => {
          if (data == 'data') {
            this.viewCanLeave = true;
            this.navCtrl.pop()
            if (type == 'save') {
              if (this.existingForm == undefined)
                this.messageService.stopLoader()
              this.messageService.showSuccessToast(ConstantProvider.message.saveSuccess)
            } else {
              // this.messageService.showSuccessToast(ConstantProvider.message.finalizedSuccess)
              this.messageService.showSuccessToast(ConstantProvider.message.submittedSuccess)
            }
          } else {
            this.viewCanLeave = true;
            this.navCtrl.pop()
          }
        });
      } else {
        this.messageService.stopLoader();
      }
    }
    if (type == 'finalized') {
      this.errorStatus = false;
      this.commonsEngineProvider.checkMandatory(this.sectionMap, this.data, 'finalized', this)
      if (!this.errorStatus) {
        this.checkFinalizedConstraints().then(data => {
          if (!data) {

            if (!this.errorStatus) {

              this.messageService.stopLoader();
              let confirm = this.alertCtrl.create({
                enableBackdropDismiss: false,
                cssClass: 'custom-font',
                title: 'Warning',
                message: "एक बार आप फॉर्म जमा करते हैं, तो इसे आगे संपादित नहीं किया जा सकता है।<br><br><strong> क्या आप वाकई यह फ़ॉर्म सबमिट करना चाहते हैं?</strong>",
                buttons: [{
                  text: 'नहीं',
                  handler: () => {
                    // this.navCtrl.pop()
                  }
                },
                {
                  text: 'हाँ',
                  handler: async () => {
                    this.data = await this.commonsEngineProvider.getKeyValue(this.data, this.userService.user.username, this.formId, this.uniqueId, null)

                    this.dbFormModel = {
                      createdDate: this.createdDate,
                      updatedDate: this.updatedDate,
                      formStatus: type == 'save' ? 'save' : 'finalized',
                      extraKeys: null,
                      formData: this.data,
                      formSubmissionId: formId,
                      uniqueId: this.uniqueId,
                      formDataHead: headerData,
                      image: image,
                      attachmentCount: 0,
                      visitedDate: visitedDate,
                      formId: this.formId
                    }
                    await this.formService.saveData(this.formId, this.dbFormModel, this.saveType).then(data => {
                      this.viewCanLeave = true;
                      if (data == 'data') {
                        this.navCtrl.pop()
                        // this.messageService.showSuccessToast(ConstantProvider.message.finalizedSuccess)
                        this.messageService.showSuccessToast(ConstantProvider.message.submittedSuccess)
                      } else {
                        this.navCtrl.pop()
                      }
                    });

                  }
                }
                ]
              });
              confirm.present();
            } else {
              this.messageService.stopLoader();
            }
          } else {
            this.messageService.stopLoader()
          }
        });
      } else {
        this.messageService.stopLoader()
      }
    }
  }




  /**
   * This method checks ie the field should not take the value after current year
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param e
   * @param type
   * @param question
   */
  checkNumber(e, type, question) {
    if (type == 'tel') {
      try {
        let s: string = question.value
        for (let i = 0; i < s.length; i++) {
          if (!(s.charCodeAt(i) > 47 && s.charCodeAt(i) < 58)) {
            question.value = ""
            return false
          }
        }
        return true
      } catch (e) {
        return false
      }
    } else {
      return e.keyCode == 101 || e.keyCode == 69 ? false : true
    }
  }

  /**
   * This method is for table data calculation.
   *
   * @author Azhar (azaruddin@sdrc.co.in)
   * @param cell
   * @param columnIndex
   * @param rowIndex
   * @param tableModel
   */
  calculateTableArithmetic(cellQ: any, columnIndex: number, rowIndex: number, tableModel: IQuestionModel[]) {
    //-------------calculation using features array-------------------------
    let cellEventSource = tableModel[rowIndex][Object.keys(tableModel[rowIndex])[columnIndex]];
    let fresult: number = null;
    let cells = this.questionFeaturesArray[cellEventSource.columnName];
    if (cells) {
      for (let cell of cells) {
        if (typeof cell == "object" && cell.features != null && cell.features.includes("exp:") && cell.features.includes("{" + cellEventSource.columnName + "}")) {
          for (let feature of cell.features.split("@AND")) {
            switch (feature.split(":")[0]) {
              case "exp":
                for (let cols of feature.split(":")[1].split("&")) {
                  let arithmeticExpression = feature.split(":")[1];
                  let result = this.engineUtilsProvider.resolveExpression(arithmeticExpression, this.questionMap, "default");
                  if (result != null && result != "NaN" && result != NaN && !isNaN(result) && result != "null" && cell.type == "tel") {
                    fresult = parseInt(result as string);
                    cell.value = fresult;
                  }
                  if (fresult == null) {
                    cell.value = null;
                  }
                }
                break;
            }
          }
        }
      }
    }
  }


  /**
   * This method check the number pattern and decimal pattern
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param event
   * @param question
   */
  numberInput(event, question) {
    if (question.type == 'tel') {
      let pass = /[4][8-9]{1}/.test(event.charCode) || /[5][0-7]{1}/.test(event.charCode) || event.keyCode === 8;
      if (!pass) {
        return false;
      }
    } else if (question.type == 'singledecimal') {
      let pass = event.charCode == 46 || (event.charCode >= 48 && event.charCode <= 57) || event.keyCode === 8;
      if (!pass) {
        return false;
      }
    } else {
      return true;
    }
  }
  /**
 * This method check single decimal pattern
 *
 * @author Sourav Nath
 * @param event
 * @param question
 */

  numberInputSingledecimal(event: any, question) {
    const MY_REGEXP = /^\s*(\-|\+)?(\d+|(\d*(\.\d*)))([eE][+-]?\d+)?\s*$/;
    let newValue = event.target.value;
    if (question.type == 'singledecimal') {
      if (!MY_REGEXP.test(newValue)) {
        event.target.value = newValue.slice(0, -1);
      }
    }
  }


  /**
   * This method is use to compute 2 field and the result should be shown in another filed
   *
   * @author Azhar (azaruddin@sdrc.co.in)
   * @param event
   * @param focusedQuestion
   */
  compute(focusedQuestion: IQuestionModel) {
    if (focusedQuestion.type == "tel") {
      let dependencies = this.questionFeaturesArray[focusedQuestion.columnName];
      if (dependencies) {
        for (let question of dependencies) {
          if (question.controlType == "textbox" && question.features != null && question.features.includes("exp:")) {
            for (let feature of question.features.split("@AND")) {
              switch (feature.split(":")[0]) {
                case "exp":
                  let expression = feature.split(":")[1];
                  let result = this.engineUtilsProvider.resolveExpression(expression, this.questionMap, "default");
                  if (result != null && result != "NaN" && result != NaN && !isNaN(result) && result != "null") question.value = String(result);
                  else question.value = null;
                  break;
              }
            }
          }
          if (question.features != null) {
            this.compute(question);
          }
        }
        if (this.beginRepeatArray[focusedQuestion.columnName]) this.validateBeginRepeat(focusedQuestion);
      }
    }
  }

  /**
   * This method is used to validate begin repeat section
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param bgParentQuestion
   */
  validateBeginRepeat(focusedQuestion) {
    if (this.beginRepeatArray[focusedQuestion.columnName]) {
      let bgQuestion = this.beginRepeatArray[focusedQuestion.columnName];
      let dependentQuestion = this.questionMap[focusedQuestion.columnName];
      if (dependentQuestion.value != null && dependentQuestion.value > 0) {
        bgQuestion.beginrepeatDisableStatus = false;
        this.setRenderDefault(true, bgQuestion);
      }
      if (dependentQuestion.value == null || dependentQuestion.value == 0) {
        while (bgQuestion.beginRepeat.length > 1) {
          bgQuestion.beginRepeat.pop();
        }
        for (let i = 0; i < bgQuestion.beginRepeat.length; i++) {
          for (let j = 0; j < bgQuestion.beginRepeat[i].length; j++) {
            bgQuestion.beginRepeat[i][j].value = null;
          }
        }
        bgQuestion.beginrepeatDisableStatus = true;
      } else {
        let diff = bgQuestion.beginRepeat.length - dependentQuestion.value;
        while (diff >= 1) {
          bgQuestion.beginRepeat.pop();
          diff--;
        }
      }
      if (bgQuestion.beginRepeat.length == 1) {
        bgQuestion.beginRepeatMinusDisable = true;
      } else {
        bgQuestion.beginRepeatMinusDisable = false;
      }
    }
  }

  /**
   * This method is called to set the default value of the field by checking the status ie dependentCondition == render_default
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param status
   * @param bgquestion
   */
  setRenderDefault(status: boolean, bgquestion: IQuestionModel) {
    let beginrepeat = bgquestion.beginRepeat
    for (let i = 0; i < beginrepeat.length; i++) {
      for (let j = 0; j < beginrepeat[i].length; j++) {
        if (beginrepeat[i][j].controlType == 'dropdown') {
          if (beginrepeat[i][j].features && beginrepeat[i][j].features.includes('render_default')) {
            if (status) {
              beginrepeat[i][j].value = Number(beginrepeat[i][j].defaultValue)
            } else {
              beginrepeat[i][j].value = null
            }
          }
        }
      }
    }
  }

  /**
   * This method is used to check the relevance, based on the relevance the status for the  displayComponent, disable status, dependency , value will be
   * change accordingly to show/hide the field with clearing of value of that field.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   * @param type
   */
  checkRelevance(question: IQuestionModel, type?: string) {
    this.removeColor(question.columnName)
    if (type == "ui" && question.constraints != null && question.constraints.split(":")[0] == 'clear') {
      setTimeout(() => {
        this.questionMap[question.constraints.split(":")[1]].value = null
      }, 100)
    }
    if (this.questionDependencyArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label] != null)
      for (let q of this.questionDependencyArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label]) {
        let arithmeticExpression: String = this.engineUtilsProvider.expressionToArithmeticExpressionTransfomerForRelevance(q.relevance, this.questionMap);
        let rpn: String[] = this.engineUtilsProvider.transformInfixToReversePolishNotationForRelevance(arithmeticExpression.split(" "));
        let isRelevant = this.engineUtilsProvider.arithmeticExpressionResolverForRelevance(rpn);
        q.tempFinalizedMandatory = false;
        q.tempSaveMandatory = false;

        if (isRelevant) {
          q.displayComponent = isRelevant;
          if (q.defaultSettings && q.defaultSettings.includes("disabled")) {
            q.disabled = true
          } else {
            q.disabled = false
          }
          q.dependecy = true;
          if (q.defaultSettings && (q.defaultSettings.includes("prefetchNumber"))) {
            q.value = Number(q.defaultSettings.split(",")[0].split(":")[1])
          }
          if (q.defaultSettings && q.defaultSettings.includes("prefetchDropdown")) {
            for (let settings of q.defaultSettings.split(",")) {
              switch (settings.split(":")[0]) {
                case 'prefetchDropdown':
                  q.value = Number(settings.split(":")[1]);
                  break
              }
            }
          }
          // if(q.label == "State"){
          //   q.controlType = "textbox"
          //   q.value = "Odisha"
          //   q.disabled = true
          // }
          if (q.finalizeMandatory && isRelevant) q.tempFinalizedMandatory = true;
          if (q.saveMandatory && isRelevant) q.tempSaveMandatory = true;
        } else {
          q.displayComponent = false;
          q.disabled = true
          q.value = null;
          if (q.controlType == "file" || q.controlType == "mfile") q.attachmentsInBase64 = [];
          q.dependecy = false;
          q.duplicateFilesDetected = false;
          q.errorMsg = null;
          q.wrongFileExtensions = false;
          q.fileSizeExceeds = false;
          if (q.finalizeMandatory && !isRelevant) q.tempFinalizedMandatory = false;
          if (q.saveMandatory && !isRelevant) q.tempSaveMandatory = false;
        }
      }
  }

  /**
   * This method will clear the value of the field that contains some feature like area_group, filter_single, filter_multiple.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  clearFeatureFilters(question: IQuestionModel) {
    if (this.questionFeaturesArray[question.columnName + ':' + question.key + ':' + question.controlType + ':' + question.label] != null)
      for (let q of this.questionFeaturesArray[question.columnName + ':' + question.key + ':' + question.controlType + ':' + question.label]) {
        for (let feature of q.features.split("@AND")) {
          switch (feature) {
            case 'area_group':
            case "filter_single":
            case "filter_multiple":
              q.value = null;
              break;
          }
        }
      }
  }

  /**
   * This method is used to sync the data between 2 component, both having relevance with each other
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   * @param parentQuestion
   * @param event
   */
  syncGroup(question: IQuestionModel, parentQuestion: IQuestionModel, event) {
    if (question.features == null) return;
    for (let feature of question.features.split("@AND")) {
      feature = feature.trim()
      switch (feature.split(":")[0].trim()) {
        case "date_sync": {
          let groupQuestions = "";
          for (let f of feature.split(":")[1].split("&")) {
            groupQuestions = groupQuestions + f + ",";
          }
          groupQuestions = groupQuestions.substring(0, groupQuestions.length - 1);
          switch (question.controlType) {
            case "Date Widget": {
              if (question.value != null) {
                for (let qcolname of groupQuestions.split(",")) {
                  let groupQuestion: IQuestionModel;
                  if (parentQuestion == null) groupQuestion = this.questionMap[qcolname] as IQuestionModel;
                  else if (parentQuestion.controlType == "beginrepeat") {
                    let rowIndexOfQuestion = question.columnName.split("-")[1];
                    let questions: IQuestionModel[] = parentQuestion.beginRepeat[rowIndexOfQuestion];
                    for (let ques of questions) {
                      if (ques.columnName == qcolname) {
                        groupQuestion = this.questionMap[ques.columnName] as IQuestionModel;
                        break;
                      }
                    }
                  }
                  switch (groupQuestion.controlType) {
                    case "textbox": {
                      let dt1 = new Date();
                      let dt2 = new Date(question.value);
                      var diff = (dt1.getTime() - dt2.getTime()) / 1000;
                      diff /= 60 * 60 * 24;
                      let yearDiff = Math.abs(Math.round(diff / 365.25));
                      groupQuestion.value = String(yearDiff);
                    }
                      break;
                    case "dropdown": {
                      let dt1 = new Date();
                      let dt2 = new Date(question.value);
                      let diff = (dt1.getTime() - dt2.getTime()) / 1000;
                      diff /= 60 * 60 * 24;
                      let yearDiff = Math.abs(Math.round(diff / 365.25));
                      let enteredValue = yearDiff;
                      for (let option of groupQuestion.options) {
                        let start: number;
                        let end: number;
                        if ((option["value"] as String).includes("-")) {
                          start = parseInt(option["value"].split("-")[0]);
                          end = parseInt(option["value"].split("-")[1]);
                          if (enteredValue >= start && enteredValue <= end) {
                            groupQuestion.value = option["key"];
                            break;
                          }
                        } else {
                          start = parseInt(option["value"].split(" ")[0]);
                          if (enteredValue >= start) {
                            groupQuestion.value = option["key"];
                            break;
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
              break;
            case "textbox": {
              for (let qcolname of groupQuestions.split(",")) {
                let groupQuestion: IQuestionModel = this.questionMap[qcolname] as IQuestionModel;
                if (question.value == null || question.value == "") {
                  groupQuestion.value = null;
                }
                switch (groupQuestion.controlType) {
                  case "dropdown": {
                    let enteredValue = question.value;
                    for (let option of groupQuestion.options) {
                      let start: number;
                      let end: number;
                      if ((option["value"] as String).includes("-")) {
                        start = parseInt(option["value"].split("-")[0]);
                        end = parseInt(option["value"].split("-")[1]);
                        if (parseInt(enteredValue) >= start && parseInt(enteredValue) <= end) {
                          groupQuestion.value = option["key"];
                          break;
                        }
                      } else {
                        start = parseInt(option["value"].split(" ")[0]);
                        if (parseInt(enteredValue) >= start) {
                          groupQuestion.value = option["key"];
                          break;
                        }
                      }
                    }
                    if (question.value == null || question.value == "") {
                      groupQuestion.value = null;
                    }
                  }
                    break;
                  case "Date Widget": {
                    if (event == "" || (event.keyCode != undefined && event.keyCode === 8)) {
                      groupQuestion.value = null;
                    }
                  }
                    break;
                }
              }
            }
              break;
          }
          break;
        }
        case "area_group":
        case "filter_single": {
          if (question.features != null && (question.features.includes("area_group") || question.features.includes("filter_single"))) {
            let optionCount = 0
            let groupQuestions = feature.split(":")[1];
            let childLevelQuestion = this.questionMap[groupQuestions];
            childLevelQuestion.optionsOther = []

            for (let option of childLevelQuestion.options) {
              if (question.controlType == "autoCompleteMulti") {
                if (question.value && this.webFormService.checkValueInObjectArray(option["parentId"], question.value, 'key')) {
                  //this is used to make the village field disabled initially by making option["visible"] = false; and optionCount++
                  if (option["parentId2"] == -2) {
                    option["visible"] = false;
                    optionCount++

                  } else {
                    option["visible"] = true;
                    childLevelQuestion.optionsOther.push(option)
                  }
                } else {
                  //this is used to used to adding the other to the filtered village by making the option["visible"] = true;
                  if (option["parentId2"] == -2) {
                    option["visible"] = true;
                    childLevelQuestion.optionsOther.push(option)
                  } else {
                    option["visible"] = false;
                  }
                  optionCount++
                }
              } else if (question.type == "checkbox") {
               if (question.value && question.value.indexOf(option["parentId"]) != -1) {
                  //this is used to make the village field disabled initially by making option["visible"] = false; and optionCount++
                  if (option["parentId2"] == -2) {
                    option["visible"] = false;
                    optionCount++
                  } else {
                    option["visible"] = true;
                    childLevelQuestion.optionsOther.push(option)
                  }
                } else {
                  //this is used to used to adding the other to the filtered village by making the option["visible"] = true;
                  if (option["parentId2"] == -2) {
                    option["visible"] = true;
                    childLevelQuestion.optionsOther.push(option)
                  } else {
                    option["visible"] = false;

                  }
                  optionCount++
                }
              } else {
                if (option["parentId"] == question.value) {
                  //this is used to make the village field disabled initially by making option["visible"] = false; and optionCount++
                  if (option["parentId2"] == -2) {
                    option["visible"] = false;
                    optionCount++
                  } else {
                    option["visible"] = true;
                    childLevelQuestion.optionsOther.push(option)
                  }
                } else {
                  //this is used to used to adding the other to the filtered village by making the option["visible"] = true;
                  if (option["parentId2"] == -2) {
                    option["visible"] = true;
                    childLevelQuestion.optionsOther.push(option)
                  } else {
                    option["visible"] = false;
                  }
                  optionCount++
                }
              }
            }
            if (optionCount == childLevelQuestion.options.length) {
              childLevelQuestion.constraints = "disabled";
              if (feature.includes("area_group")) {
                if (childLevelQuestion.saveMandatory) {
                  childLevelQuestion.tempSaveMandatory = false;
                }
                if (childLevelQuestion.finalizeMandatory) {
                  childLevelQuestion.tempFinalizeMandatory = false;
                }
                this.syncGroup(childLevelQuestion, null, event)
              }
            } else {
              childLevelQuestion.constraints = "";
              if (feature.includes("area_group")) {
                if (childLevelQuestion.saveMandatory) {
                  childLevelQuestion.tempSaveMandatory = true;
                }
                if (childLevelQuestion.finalizeMandatory) {
                  childLevelQuestion.tempFinalizeMandatory = true;
                }
                this.syncGroup(childLevelQuestion, null, event)
              }
            }
            childLevelQuestion.value = null;
            if (childLevelQuestion.controlType == "autoCompleteMulti") {
              this.syncGroup(childLevelQuestion, null, event)
              this.checkRelevance(childLevelQuestion)
            }
          }
        }
          break;
        case "filter_multiple": {
          if (question.features != null && question.features.includes("filter_multiple")) {
            let groupQuestions = feature.split(":")[1];
            let childLevelQuestion = this.questionMap[groupQuestions];
            for (let option of childLevelQuestion.options) {
              option["visible"] = false;
              for (let parentId of option["parentIds"]) {
                if (parentId == question.value) {
                  option["visible"] = true;
                  break;
                }
              }
            }
            childLevelQuestion.value = null;
          }
        }
          break;

        case "filterByExp": {
          if (question.features != null && feature.includes("filterByExp") && !feature.includes("filterByExp:({when")) {
            feature = feature.trim()
            let optionCount = 0
            let groupQuestions = feature.split(":")[1];
            let childLevelQuestion = this.questionMap[groupQuestions];
            for (let option of childLevelQuestion.options) {
              option["visible"] = false;
              let result = this.engineUtilsProvider.resolveExpression(option["filterByExp"], this.questionMap, "default");
              if (result > 0) {
                option["visible"] = true;
              } else {
                option["visible"] = false;
                optionCount++
              }
            }
            if (optionCount == childLevelQuestion.options.length) {
              childLevelQuestion.disabled = true
            } else {
              childLevelQuestion.disabled = false
            }
            childLevelQuestion.value = null;
          } else if (question.features != null && feature.includes("filterByExp") && feature.includes("filterByExp:({when")) {
            feature = feature.trim()
            let expressionArr = feature.split(":");
            expressionArr.shift()
            let colName = expressionArr[expressionArr.length - 1].split(".")[0];
            let modExp = ""
            for (let index = 0; index < expressionArr.length - 1; index++) {
              modExp = modExp + expressionArr[index] + ":"
            }
            modExp = modExp.substring(0, modExp.length - 1)

            let index = 0;
            for (let option of this.questionMap[colName].options) {
              if (option['key'] == 31247) {
                option['key']
              }
              option['visible'] = false
              let arithmeticExpression: String = this.engineUtilsProvider.expressionToArithmeticExpressionTransfomerForRelevance(modExp, this.questionMap, index);
              index++
              let rpn: String[] = this.engineUtilsProvider.transformInfixToReversePolishNotationForRelevance(arithmeticExpression.split(" "));
              let isRelevantToDisplay = this.engineUtilsProvider.arithmeticExpressionResolverForRelevance(rpn);


              if (isRelevantToDisplay) {
                option['visible'] = true
              }
            }
          }
        }
          break;
        case "dropdown_auto_select": { }
      }
    }
  }

  /**
   * This methos is used to restict the copypaste value in the respective field.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  onPaste(question: any) {
    if (question.type != 'text') {
      setTimeout(() => {
        question.value = null;
      }, 0);
    }
  }

  /**
   * This method is called for each and every field tp check the null or blank value, it returns false if tyhe value is null or black
   * in any of the field or else return true.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param data
   */
  checkFieldContainsAnyvalue(data: any) {
    for (let index = 0; index < Object.keys(this.data).length; index++) {
      for (let j = 0; j < this.data[Object.keys(this.data)[index]].length; j++) {
        let subSections = this.data[Object.keys(this.data)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]

            switch (question.controlType) {
              case "textbox":
                if (question.value != null && (question.value as string).trim() != "")
                  return true
                break;
              case "dropdown":
              case "segment":
                if (question.value != null && question.value != "")
                  return true
                break;
              case "Time Widget":
                if (question.value != null && question.value != "")
                  return true
                break;
              case "Date Widget":
              case "Month Widget":
                if (question.value != null && question.value != "")
                  return true
                break;
              case "checkbox":
                if (question.value != null && question.value != "")
                  return true
                break;
              case 'tableWithRowWiseArithmetic': {
                let tableData = question.tableModel
                for (let i = 0; i < tableData.length; i++) {
                  for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                    let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                    if (typeof cell == 'object') {
                      if (cell.value != null && cell.value.trim() != "")
                        return true
                      break;
                    }
                  }
                }
              }
                break;
              case "beginrepeat":
                let beginrepeat = question.beginRepeat
                let beginrepeatArray: any[] = []
                let beginrepeatMap: {} = {}
                for (let i = 0; i < beginrepeat.length; i++) {
                  beginrepeatMap = {}
                  for (let j = 0; j < beginrepeat[i].length; j++) {
                    let colName = (beginrepeat[i][j].columnName as String).split('-')[3]
                    beginrepeatMap[colName] = beginrepeat[i][j].value
                    // console.log('begin-repeat', beginrepeat[i][j])
                    switch (beginrepeat[i][j].controlType) {
                      case "textbox":
                        if (beginrepeat[i][j].value != null && (beginrepeat[i][j].value as string).trim() != "")
                          return true
                        break;
                      case "dropdown":
                      case "segment":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].valu != "")
                          return true
                        break;
                      case "Time Widget":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "")
                          return true
                        break;
                      case "Date Widget":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "")
                          return true
                        break;
                      case "Month Widget":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "")
                          return true
                        break;
                      case "checkbox":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "")
                          return true
                        break;
                    }
                  }
                  beginrepeatArray.push(beginrepeatMap)
                }
                break;
            }
          }
        }
      }
    }
    return false;
  }

  /**
   * This method will add the columns to the "questionDependencyArray" having any relevances for further use.
   *
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param expression
   * @param question
   */
  drawDependencyGraph(expression: String, question: any) {
    for (let str of expression.split("}")) {
      let expressions: String[] = str.split(":");
      for (let i = 0; i < expressions.length; i++) {
        let exp: String = expressions[i];
        switch (exp) {
          case "optionEquals":
          case "optionEqualsMultiple":
          case "otherOptionEquals": {
            let dColName: any = expressions[i - 1];
            if (question.dependecy && this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] == undefined) {
              this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = [this.questionMap[question.columnName]];
            } else if (question.dependecy == true) {
              let a = this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label];
              let keyFound = false
              for (let dps of a) {
                if (dps.columnName == question.columnName)
                  keyFound = true
              }
              if (!keyFound) {
                a.push(this.questionMap[question.columnName]);
                this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = a;

              }

            }
            i = i + 2;
          }
            break;
          case "textEquals":
          case "equals":
          case "greaterThan":
          case "greaterThanEquals":
          case "lessThan":
          case "lessThanEquals": {
            let dColName: any = expressions[i - 1];
            if (question.dependecy && this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] == undefined) {
              this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = [this.questionMap[question.columnName]];
            } else if (question.dependecy == true) {
              let a = this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label];
              let keyFound = false
              for (let dps of a) {
                if (dps.columnName == question.columnName)
                  keyFound = true
              }
              if (!keyFound) {
                a.push(this.questionMap[question.columnName]);
                this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = a;

              }
            }
            i = i + 1;
          }
            break;
        }
      }
    }
  }

  /**
   * This method is use to set the default values having any constraints or features
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  setupDefaultSettingsAndConstraintsAndFeatureGraph(question: IQuestionModel): IQuestionModel {
    if (question.defaultSettings != null) {
      for (let settings of question.defaultSettings.split(',')) {

        switch (settings.split(":")[0]) {
          case 'current_date':
            question.value = this.datepipe.transform(new Date(), "yyyy-MM-dd")
            break;
          case "prefetchNumber":
            question.value = parseInt(settings.split(":")[1])
            break;
          case 'prefetchText':
            question.value = new String(settings.split(":")[1]);
            break;
          case 'prefetchDropdownWithValue':
            question.value = new Number(settings.split(":")[1]);
            break;
          case 'disabled':
            question.disabled = true
            break;
          case "prefetchDropdown":
            question.value = Number(settings.split(":")[1]);
            break;
          case "prefetchDate":
            if (settings.split(":")[1] == 'current_date') {
              // if (question.value == null) {
              //   question.value = this.datepipe.transform(new Date(), "yyyy-MM-dd")
              // }
            }
            break;
        }
      }
    }
    if (question.constraints != null) {
      for (let settings of question.constraints.split("@AND")) {
        switch (settings.split(":")[0].trim()) {
          case "maxLength":
            question.maxLength = parseInt(settings.split(":")[1]);
            break;
          case "minLength":
            question.minLength = parseInt(settings.split(":")[1]);
            break;
          case "maxValue":
            question.maxValue = parseInt(settings.split(":")[1]);
            break;
          case "minValue":
            question.minValue = parseInt(settings.split(":")[1]);
            break;
          case 'lessThan':
          case 'lessThanEquals':
          case 'greaterThan':
          case 'greaterThanEquals':
          case 'exp':
            this.commonsEngineProvider.generateConstraintGraph(question.constraints, question, this.constraintsArray, this.questionMap)
            break;
          case "limit_bg_repeat":
            question.limit_bg_repeat = settings;
            let dcolName = settings.split(":")[1];
            question.bgDependentColumn = dcolName;
            this.beginRepeatArray[dcolName] = question;
            break;
        }
      }
    }
    if (question.features != null) {
      for (let features of question.features.split("@AND")) {
        switch (features.split(":")[0]) {
          case "exp":
            let exp = features.split(":")[1] as String;
            let str = exp.split("");
            for (let i = 0; i < str.length; i++) {
              if (str[i] == "$") {
                let qName = "";
                for (let j = i + 2; j < str.length; j++) {
                  if (str[j] == "}") {
                    i = j;
                    break;
                  }
                  qName = qName + str[j];
                }
                if (this.questionFeaturesArray[qName] == undefined) this.questionFeaturesArray[qName] = [question];
                else {
                  let a = this.questionFeaturesArray[qName];
                  a.push(question);
                  this.questionFeaturesArray[qName] = a;
                }
              }
            }
            break;
        }
      }
    }


    return question
  }

  /**
   * This method is used to open the amazingTimePicker(a date widget) to select the date also checks the constraints like (lessThan/greaterThan).
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  open(question: any) {
    if (!this.disableStatus) {
      const amazingTimePicker = this.atp.open();
      amazingTimePicker.afterClose().subscribe(time => {
        question.value = time;
        if (question.constraints != null && question.constraints != "" && question.controlType == "Time Widget") {
          for (let settings of question.constraints.split("@AND")) {
            switch (settings.split(":")[0]) {
              case "lessThan":
              case "greaterThan": {
                if (question.value != null && this.questionMap[question.constraints.split(":")[1]].value != null) {
                  this.questionMap[question.constraints.split(":")[1]].value = null
                }
              }
            }
          }
        }
      });
    }
  }

  /**
   * This method is used to check the constraints(greatherThan/lessThan) on thr month widget.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  onMonthChanged(question: IQuestionModel) {
    if (question.constraints != null) {
      if (question.constraints.split(":")[0] == "greaterThan" && (this.questionMap[question.constraints.split(":")[1].split("-")[0]].value != null) && question.value != null) {

        let yearone = parseInt(this.questionMap[question.constraints.split(":")[1].split("-")[0]].value.split("-")[0])
        let monthone = parseInt(this.questionMap[question.constraints.split(":")[1].split("-")[0]].value.split("-")[1])
        let yeartwo = parseInt(question.value.split("-")[0])
        let monthtwo = parseInt(question.value.split("-")[1])
        if (yeartwo < yearone) {
          this.messageService.showErrorToast(question.cmsg);
          setTimeout(() => {
            question.value = null;
          }, 100)
        } else if (yeartwo == yearone && monthtwo < monthone) {
          this.messageService.showErrorToast(question.cmsg);
          setTimeout(() => {
            question.value = null;
          }, 100)
        } else {
          let numberOfMonths = (yeartwo - yearone) * 12 + (monthtwo - monthone);
          if (numberOfMonths < 2) {
            this.questionMap[question.constraints.split(":")[1].split("-")[1]].value = 3
          } else if (numberOfMonths < 3) {
            this.questionMap[question.constraints.split(":")[1].split("-")[1]].value = 2
          } else if (numberOfMonths == 3) {
            this.questionMap[question.constraints.split(":")[1].split("-")[1]].value = 1
          } else {
            this.questionMap[question.constraints.split(":")[1].split("-")[1]].value = 0
          }
          let sectionScoreKeeper = this.sectionMapScoreKeeper[this.questionMap['f3qescore'].sectionName]
          let sectionSum = 0;
          if (sectionScoreKeeper != undefined) {
            for (let scoreHolder of this.sectionScoreKeyMapper[sectionScoreKeeper.columnName]) {
              sectionSum = Number(sectionSum) + Number(scoreHolder.value);
            }
            sectionScoreKeeper.value = Number(sectionSum);
          }

        }
      } else if (question.constraints.split(":")[0] == "lessThan") {
        this.questionMap[question.constraints.split(":")[1].split("-")[0]].value = null
        this.questionMap[question.constraints.split(":")[1].split("-")[1]].value = null
        this.questionMap['f3qescore'].value = null
      }
    }
  }

  /**
   * This method is used to set the date value with check the relevance ie(date2 or end date should be always greather than or equals to date 1 or start date
   * and same as date 1 or start date should be less than or equals to the date 2 or end date), when the condition not satisfy the date value got clear the
   * from the field.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question1
   * @param question2
   * @param event
   */
showSecondDate:boolean=false;
maxValue2ndDate:string=null
  onDateChanged(question1: any, question2: any, event: IMyDateModel) {

    if (question1.value != null && question1.constraints != null) {
      switch (question1.constraints.split(":")[0]) {
        case 'lessThan':
          if (this.questionMap[question1.constraints.split(":")[1]].value != null) {
            if (new Date(this.questionMap[question1.constraints.split(":")[1]].value) < new Date(question1.value)) {
              setTimeout(() => {
                question1.value = null;
              }, 100)
            }
          }
          break;
        case 'greaterThan':
          if (this.questionMap[question1.constraints.split(":")[1]].value != null) {
            if (new Date(question1.value) < new Date(this.questionMap[question1.constraints.split(":")[1]].value)) {
              setTimeout(() => {
                question1.value = null;
              }, 100)
            }
          }
          break;
      }
    }
  }

  /**
   * This method is called to check all the constraint ie (min value, max value, greaterThan, lessThan).
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question1
   * @param question2
   */
  checkMinMax(question1, question2) {
    if(question1.columnName=='undp_f1_bank_3' && question1.value!=null && question1.value!=""){
      if(question1.value.length<11){
        question1.value = null;
        question1.showErrMessage = true
        return (question1.value = null);
      }
    }
    if (question1.value != null && question1.constraints != null && question1.controlType == "textbox") {
      if (question1.maxValue != null && Number(question1.value) > question1.maxValue) {
        question1.value = null;
        question1.showErrMessage = true
        this.syncGroup(question1, question2, null);
        return (question1.value = null);
      } else if (question1.maxValue != null && Number(question1.value) < question1.minValue) {
        question1.value = null;
        question1.showErrMessage = true
        this.syncGroup(question1, question2, null);
        return (question1.value = null);
      } else if (Number(question1.value) < question1.minValue) {
        question1.value = null;
        question1.showErrMessage = true
        return (question1.value = null);
      }else{
        this.removeColor(question1.columnName)
        question1.showErrMessage = false
      }
    } else if (question1.value != null && question1.constraints != null && question1.controlType == "Time Widget") {
      for (let settings of question1.constraints.split("@AND")) {
        switch (settings.split(":")[0].trim()) {
          case "greaterThan": {
            if (this.questionMap[settings.split(":")[1]].value != null && question1.value) {
              let timeOfConstraint = this.questionMap[settings.split(":")[1]].value;
              let timeOfActiveQ = question1.value;
              let hourOfConstraint = parseInt(timeOfConstraint.split(":")[0]);
              let minuteOfConstraint = parseInt(timeOfConstraint.split(":")[1]);
              let hourOfActiveQ = parseInt(timeOfActiveQ.split(":")[0]);
              let minuteOfActiveQ = parseInt(timeOfActiveQ.split(":")[1]);
              // passing year, month, day, hourOfA and minuteOfA to Date()
              let dateOfConstraint: Date = new Date(2010, 6, 15, hourOfConstraint, minuteOfConstraint);
              let dateOfActiveQ: Date = new Date(2010, 6, 15, hourOfActiveQ, minuteOfActiveQ);
              if (dateOfConstraint > dateOfActiveQ) {
                setTimeout(() => {
                  question1.value = null;
                }, 100)
              }
            }
          }
          case "lessThan": {
            if (this.questionMap[settings.split(":")[1]].value != null && question1.value) {
              let timeOfConstraint = this.questionMap[settings.split(":")[1]].value;
              let timeOfActiveQ = question1.value;
              let hourOfConstraint = parseInt(timeOfConstraint.split(":")[0]);
              let minuteOfConstraint = parseInt(timeOfConstraint.split(":")[1]);
              let hourOfActiveQ = parseInt(timeOfActiveQ.split(":")[0]);
              let minuteOfActiveQ = parseInt(timeOfActiveQ.split(":")[1]);
              // passing year, month, day, hourOfA and minuteOfA to Date()
              let dateOfConstraint: Date = new Date(2010, 6, 15, hourOfConstraint, minuteOfConstraint);
              let dateOfActiveQ: Date = new Date(2010, 6, 15, hourOfActiveQ, minuteOfActiveQ);
              if (dateOfActiveQ > dateOfConstraint) {
                setTimeout(() => {
                  question1.value = null;
                }, 100)
              }
            }
          }
        }
      }
    }
    if (question1.value != null && question1.constraints != null && question1.controlType == "textbox" && question1.type == "tel") {
      for (let settings of question1.constraints.split("@AND")) {
        switch (settings.split(":")[0].trim()) {
          case "minLength": {
            if (question1.value.length < settings.split(":")[1]) {
              setTimeout(() => {
                question1.value = null;
                question1.showErrMessage = true
              }, 100)
            }
          }
        }
      }
    }else{
      this.removeColor(question1.columnName)
      question1.showErrMessage = false
    }
  }

  /**
   * This method will help to select the (file or image) for uploading. it will also checking what max number offile a user can select ,
   * what should be the max file can be upload each, which file extension should be allow.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param event
   * @param question
   */
  onFileChange(event, question: IQuestionModel) {
  }
  /**
   * This method help to delete the list of file that have been selected.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param fIndex
   * @param question
   */
  deleteFile(fIndex, question) {
    if (!this.disableStatus) {
      question.attachmentsInBase64.splice(fIndex, 1);
      // console.log(question.attachmentsInBase64);
      question.errorMsg = null;
    }
  }

  /**
   * This method is use to check the constraint of textbox or table only.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param fq
   */
  checkConstraints(fq: IQuestionModel) {
    let ccd = this.constraintsArray[fq.columnName as any]
    if (ccd) {
      for (let qq of ccd) {
        if ((qq.controlType == 'textbox' || qq.controlType == 'cell') && (qq.constraints != null) && (qq.constraints.includes('exp:'))) {
          for (let c of qq.constraints.split('@AND')) {
            switch (c.split(":")[0].replace(/\s/g, '')) {
              case 'exp': {
                let exp = c.split(":")[1]
                let rr = this.constraintTokenizer.resolveExpression(exp, this.questionMap, "constraint")
                if (rr != null && rr != NaN && rr != "null" && rr != "NaN") {
                  // if in some devices data is not clear make sure to increase delay time
                  if (parseInt(rr) == 0) {
                    setTimeout(() => {
                      fq.showErrMessage = true
                      fq.value = null
                      this.checkRelevance(fq);
                      this.clearFeatureFilters(fq);
                      this.compute(fq);
                      this.validateBeginRepeat(fq);

                    }, 500)
                  } else {

                    //   this.removeColor(fq.columnName)
                    fq.showErrMessage = false
                  }
                }
              }
                break;
            }
          }
        }

      }
    } else if (fq.finalizeMandatory == true && (fq.controlType == 'textbox' || fq.controlType == 'cell') && fq.constraints == null && fq.cmsg != null && (fq.value == null || fq.value == "")) {
      fq.value = null
    } else {
      fq.showErrMessage = false
    }

  }
  /**
   * This method is use to remove the error color from the field.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param key
   */
  removeColor(key: any) {
    if (this.tempCaryId != null) {
      let temp = document.getElementById(this.tempCaryId + "")
      if (temp != null && temp != undefined)
        document.getElementById(this.tempCaryId + "").style.removeProperty("border-bottom");
      this.tempCaryId = null;
    }

    if (key != null && key != '' && key != undefined) {
      let temp = document.getElementById(key + "")
      if (temp != null && temp != undefined)
        document.getElementById(key + "").style.removeProperty("border-bottom");
      this.tempCaryId = key;
    }
  };

  /**
   * This method is used to set the error color to the field.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param sectionHeading
   * @param key
   * @param type
   */
  errorColor(sectionHeading: any, key: any, type?: any) {
    this.sectionHeading = sectionHeading;
    this.selectedSection = this.sectionMap.get(sectionHeading);
    this.section = sectionHeading

    if (this.tempCaryId != null) this.removeColor(this.tempCaryId);
    if (key != null && key != "" && key != undefined) {
      setTimeout(() => {
        let eleId = document.getElementById(key + "");
        if (eleId != null) {

          let box = eleId.parentNode.parentElement.getBoundingClientRect();
          let body = document.body;
          let docEl = document.documentElement;
          let scrollTop = window.pageYOffset || docEl.scrollTop || body.scrollTop;
          let clientTop = docEl.clientTop || body.clientTop || 0;
          let top = box.top + scrollTop - clientTop;
          let cDim = this.content.getContentDimensions();

          let scrollOffset = Math.round(top) + cDim.scrollTop - cDim.contentTop;
          this.content.scrollTo(0, scrollOffset - 10, 1000);
          eleId.style.setProperty("border-bottom", "#FF0000 double 1px", "important");
          let ddHeight = scrollOffset + 50
          let cols = document.getElementsByClassName('popover-content');
          for (let i = 0; i < cols.length; i++) {
            cols[i].setAttribute('top', 'ddHeight');
          }
          this.tempCaryId = key;

        } else {
          if (type != "table")
            document.getElementsByClassName("scroll-content")[3].scrollTop;
        }
      }, 50);
    } else {
      document.getElementsByClassName("scroll-content")[3].scrollTop = 0;
    }
  }

  /**
   * This method is used to check the relevances for each question
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  checkRelevanceForEachQuestion() {
    for (let questionKey of this.dataSharingService.getKeys(this.questionMap)) {
      let question = this.questionMap[questionKey];

      switch (question.controlType) {
        case "dropdown":
        case "segment":
          if (question.groupType == "area_group" || question.groupType == "filter_single" || question.groupType == "filter_multiple") {
            if (question.groupQuestions != null) {
              let childLevelQuestion = this.questionMap[question.groupQuestions];
              let optionCount = 0;
              for (let option of childLevelQuestion.options) {
                if (option["parentId"] == question.value) {
                  option["visible"] = true;
                } else {
                  option["visible"] = false;
                  optionCount++;
                }
              }
              if (optionCount == childLevelQuestion.options.length) {
                childLevelQuestion.constraints = "disabled";
              } else {
                childLevelQuestion.constraints = "";
              }
            }
          }
          this.checkRelevance(question)
          break;
        case "autoCompleteMulti":
          if (question.groupType == "area_group" || question.groupType == "filter_single" || question.groupType == "filter_multiple") {
            if (question.groupQuestions != null) {
              let childLevelQuestion = this.questionMap[question.groupQuestions];
              let optionCount = 0;
              for (let option of childLevelQuestion.options) {
                if (question.value && this.webFormService.checkValueInObjectArray(option["parentId"], question.value, 'key')) {
                  option["visible"] = true;
                } else {
                  option["visible"] = false;
                  optionCount++;
                }
              }
              if (optionCount == childLevelQuestion.options.length) {
                childLevelQuestion.constraints = "disabled";
              } else {
                childLevelQuestion.constraints = "";
              }
            }
          }
          this.checkRelevance(question)
          break;
        case "table":
        case "tableWithRowWiseArithmetic":
          for (let row = 0; row < question.tableModel.length; row++) {
            for (let column = 0; column < Object.keys(question.tableModel[row]).length; column++) {
              let value = question.tableModel[row][Object.keys(question.tableModel[row])[column]];
              if (typeof value == "object") {
                let cell = value;
                this.checkRelevance(cell)
              }
            }
          }
          break;
        case 'beginrepeat':
          for (let index = 0; index < question.beginRepeat.length; index++) {
            let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[index];
            for (let beginRepeatQuestion of beginRepeatQuestions) {
              this.checkRelevance(beginRepeatQuestion)
            }
          }
          break;
        default:
          this.checkRelevance(question)
          break;
      }
    }
  }

  /**
   * This method is used to remove the dependency from the "questionDependencyArray".
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param expression
   * @param question
   */
  removeFromDependencyGraph(expression: String, question: IQuestionModel) {
    if (this.questionMap[question.parentColumnName] && this.questionMap[question.parentColumnName].controlType == 'beginrepeat') {
      delete this.questionDependencyArray[question.columnName]
      return
    }
    for (let str of expression.split("}")) {
      let expressions: String[] = str.split(":");
      for (let i = 0; i < expressions.length; i++) {
        let exp: String = expressions[i];
        switch (exp) {
          case "optionEquals":
          case "optionEqualsMultiple":
          case "otherOptionEquals": {
            let dColName: any = expressions[i - 1];
            if (question.dependecy && this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] == undefined) {
              this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = [this.questionMap[question.columnName]];
            } else if (question.dependecy == true) {
              let a = this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label];
              let keyFoundIndex = -1
              for (let i = 0; a.length; i++) {
                if (a[i].columnName == question.columnName) {
                  // remove the object from dependent key. if array size is 1, remove the key itself
                  if (a.length == 1) {
                    delete this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label]
                  } else {
                    keyFoundIndex = i
                    break
                  }
                }

              }
              if (keyFoundIndex > -1) {
                a.splice(keyFoundIndex, 1)
              }

            }
            i = i + 2;
          }
            break;
          case "textEquals":
          case "equals":
          case "greaterThan":
          case "greaterThanEquals":
          case "lessThan":
          case "lessThanEquals": {
            let dColName: any = expressions[i - 1];
            if (question.dependecy && this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] == undefined) {
              this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = [this.questionMap[question.columnName]];
            } else if (question.dependecy == true) {
              let a = this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label];
              let keyFound = false
              for (let dps of a) {
                if (dps.columnName == question.columnName)
                  keyFound = true
              }
              if (!keyFound) {
                a.push(this.questionMap[question.columnName]);
                this.questionDependencyArray[this.questionMap[dColName].columnName + ":" + this.questionMap[dColName].key + ":" + this.questionMap[dColName].controlType + ":" + this.questionMap[dColName].label] = a;

              }
            }
            i = i + 1;
          }
            break;
        }
      }
    }
  }


  /**
   * This method is for table data calculation.
   *
   * @author Azhar (azaruddin@sdrc.co.in)
   * @param cell
   * @param columnIndex
   * @param rowIndex
   * @param tableModel
   */
  addAnotherWorker(key: Number) {
    let beginRepeatParent: IQuestionModel = this.repeatSubSection.get(key);
    let beginRepeatQuestionList: IQuestionModel[] = beginRepeatParent.beginRepeat[beginRepeatParent.beginRepeat.length - 1];
    let size = beginRepeatParent.beginRepeat.length;
    let clonedQuestion: IQuestionModel[];
    clonedQuestion = JSON.parse(JSON.stringify(beginRepeatQuestionList));

    for (let index = 0; index < clonedQuestion.length; index++) {
      //if dependent question is inside begin repeat section,
      // we have rename dependent column name as we have renamed depending question column name
      let colName = (clonedQuestion[index].columnName as String).split("-")[3];
      let colIndex = (clonedQuestion[index].columnName as String).split("-")[2];


      clonedQuestion[index].value = null;
      clonedQuestion[index].othersValue = false;
      clonedQuestion[index].isOthersSelected = false;
      clonedQuestion[index].dependecy = clonedQuestion[index].relevance != null ? true : false;
      clonedQuestion[index].columnName = beginRepeatParent.columnName + "-" + size + "-" + colIndex + "-" + colName;
      clonedQuestion[index] = this.commonsEngineProvider.renameRelevanceAndFeaturesAndConstraintsAndScoreExpression(clonedQuestion[index], this.questionMap, beginRepeatParent, size);

      //setting up default setting and added to dependency array and feature array
      clonedQuestion[index].displayComponent = clonedQuestion[index].relevance == null ? true : false;
      this.questionMap[clonedQuestion[index].columnName] = clonedQuestion[index];
    }

    for (let index = 0; index < clonedQuestion.length; index++) {
      clonedQuestion[index].relevance != null ? this.drawDependencyGraph(clonedQuestion[index].relevance, clonedQuestion[index]) : null;
      clonedQuestion[index] = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(clonedQuestion[index]);
    }

    this.checkRelevanceForEachQuestion()
    if (beginRepeatParent.limit_bg_repeat) {
      if (this.questionMap[beginRepeatParent.bgDependentColumn as any].value != null) {
        if (beginRepeatParent.beginRepeat.length < this.questionMap[beginRepeatParent.bgDependentColumn as any].value) {
          beginRepeatParent.beginRepeat.push(clonedQuestion);
        } else {
          this.messageService.showErrorToast("Exceed Size");
        }
      } else {
        this.sectionSelected(Object.keys(this.data)[0]);
        this.messageService.showErrorToast("Please enter " + this.questionMap[beginRepeatParent.bgDependentColumn as any].label);
      }
    } else {
      beginRepeatParent.beginRepeat.push(clonedQuestion);
    }
    if (beginRepeatParent.beginRepeat.length > 1) {
      beginRepeatParent.beginRepeatMinusDisable = false;
    }

    for (let questionKey of this.dataSharingService.getKeys(this.questionMap)) {
      let question = this.questionMap[questionKey]
      if (question.features != null) {
        let feature: string = question.features.split("@AND")
        switch (question.controlType) {
          case 'dropdown':
          case "autoCompleteMulti":
          case "autoCompleteTextView":
            for (let i = 0; i < feature.length; i++) {
              if (feature[i + 1] != undefined && feature[i + 1].split(":")[0].trim() == 'area_group') {

                if (feature[i + 1].split(":")[1] != undefined) {
                  let groupQuestions = feature[i + 1].split(":")[1];
                  let childLevelQuestion = this.questionMap[groupQuestions];
                  let optionCount = 0;
                  for (let option of childLevelQuestion.options) {
                    if (option['parentId'] == question.value) {
                      //this is used to make the village field disabled initially by making option["visible"] = false; and optionCount++
                      if (option["parentId2"] == -2) {
                        option["visible"] = false;
                        optionCount++
                      } else {
                        option["visible"] = true;
                      }
                    } else {
                      //this is used to used to adding the other to the filtered village by making the option["visible"] = true;
                      if (option["parentId2"] == -2) {
                        option["visible"] = true;
                      } else {
                        option["visible"] = false;
                      }
                      optionCount++
                    }
                  }
                  if (optionCount == childLevelQuestion.options.length) {
                    childLevelQuestion.constraints = "disabled"
                  } else {
                    childLevelQuestion.constraints = ""
                  }
                }
              }
            }
            break;
        }
      }
    }
  }

  checkFinalizedConstraints(): Promise<Boolean> {

    return new Promise<Boolean>((resolve, reject) => {
      let wasConstraintFailed = false;
      for (let index = 0; index < Object.keys(this.data).length; index++) {
        this.sectionMap.set(Object.keys(this.data)[index], this.data[Object.keys(this.data)[index]]);
        for (let j = 0; j < this.data[Object.keys(this.data)[index]].length; j++) {
          let subSections = this.data[Object.keys(this.data)[index]][0];
          for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
            for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
              let fq: IQuestionModel = subSections[Object.keys(subSections)[qs]][q];
              if (fq.controlType == 'tableWithRowWiseArithmetic' || fq.controlType == 'table') {
                {
                  let tableData = fq.tableModel;
                  for (let i = 0; i < tableData.length; i++) {
                    for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                      let cell = tableData[i][Object.keys(tableData[i])[j]];
                      if (typeof cell == "object") {
                        let ccd = this.constraintsArray[cell.columnName as any]
                        if (ccd) {
                          for (let qq of ccd) {
                            if (qq.displayComponent == true && qq.finalizeMandatory == true && (qq.controlType == 'textbox' || qq.controlType == 'cell') && (qq.constraints != null) && (qq.constraints.includes('exp:'))) {
                              // console.log("question" + qq.columnName)
                              for (let c of qq.constraints.split('@AND')) {
                                switch (c.split(":")[0].replace(/\s/g, '')) {
                                  case 'exp': {
                                    let exp = c.split(":")[1]

                                    let rr = this.constraintTokenizer.resolveExpression(exp, this.questionMap, "constraint")
                                    if (rr != null && rr != NaN && rr != "null" && rr != "NaN") {
                                      // if in some devices data is not clear make sure to increase delay time
                                      if (parseInt(rr) == 0) {
                                        wasConstraintFailed = true;
                                        resolve(wasConstraintFailed)
                                        setTimeout(() => {
                                          cell.value = null
                                          this.section = Object.keys(this.data)[index]
                                          this.errorColor(Object.keys(this.data)[index], cell.columnName);
                                        }, 500)
                                      } else {
                                        setTimeout(() => {
                                          cell.showErrMessage = false
                                          this.errorStatus = false;
                                          return wasConstraintFailed = false;
                                        }, 500)
                                      }
                                      // console.log(qq, exp, rr)
                                    }
                                  }
                                    break;
                                }
                              }
                            }
                          }
                          // }
                        } else if (cell.displayComponent == true && cell.finalizeMandatory == true && (cell.controlType == 'textbox' || cell.controlType == 'cell') && cell.constraints == null && cell.cmsg != null && (cell.value == null || cell.value == "")) {
                          cell.value = null
                          this.section = Object.keys(this.data)[index]
                          this.errorColor(Object.keys(this.data)[index], cell.columnName);
                          wasConstraintFailed = true;
                          resolve(wasConstraintFailed)
                        } else {
                          cell.showErrMessage = false
                        }
                      }
                    }
                  }
                }
              } else if (fq.controlType == 'beginrepeat') {
                for (let bgindex = 0; bgindex < fq.beginRepeat.length; bgindex++) {
                  let beginRepeatQuestions: IQuestionModel[] = fq.beginRepeat[bgindex];
                  for (let beginRepeatQuestion of beginRepeatQuestions) {
                    let ccd = this.constraintsArray[beginRepeatQuestion.columnName as any]
                    if (ccd) {
                      for (let qq of ccd) {
                        if (qq.displayComponent == true && qq.finalizeMandatory == true && (qq.controlType == 'textbox' || qq.controlType == 'cell') && (qq.constraints != null) && (qq.constraints.includes('exp:'))) {
                          for (let c of qq.constraints.split('@AND')) {
                            switch (c.split(":")[0].replace(/\s/g, '')) {
                              case 'exp': {
                                let exp = c.split(":")[1]
                                let rr = this.constraintTokenizer.resolveExpression(exp, this.questionMap, "constraint")
                                if (rr != null && rr != NaN && rr != "null" && rr != "NaN") {
                                  // if in some devices data is not clear make sure to increase delay time
                                  if (parseInt(rr) == 0) {
                                    wasConstraintFailed = true;
                                    resolve(wasConstraintFailed)
                                    setTimeout(() => {
                                      beginRepeatQuestion.value = null
                                      this.section = Object.keys(this.data)[index]
                                      this.errorColor(Object.keys(this.data)[index], beginRepeatQuestion.columnName);
                                    }, 500)
                                  } else {
                                    setTimeout(() => {
                                      beginRepeatQuestion.showErrMessage = false
                                      return wasConstraintFailed = false;
                                    }, 500)
                                  }
                                }
                              }
                                break;
                            }
                          }
                        }
                      }
                    }
                  }
                }
              } else {
                let ccd = this.constraintsArray[fq.columnName as any]
                if (ccd) {
                  for (let qq of ccd) {
                    if (qq.displayComponent == true && qq.finalizeMandatory == true && (qq.controlType == 'textbox' || qq.controlType == 'cell') && (qq.constraints != null) && (qq.constraints.includes('exp:'))) {
                      for (let c of qq.constraints.split('@AND')) {
                        switch (c.split(":")[0].replace(/\s/g, '')) {
                          case 'exp': {
                            let exp = c.split(":")[1]
                            let rr = this.constraintTokenizer.resolveExpression(exp, this.questionMap, "constraint")
                            if (rr != null && rr != NaN && rr != "null" && rr != "NaN") {
                              // if in some devices data is not clear make sure to increase delay time
                              if (parseInt(rr) == 0) {
                                wasConstraintFailed = true;
                                resolve(wasConstraintFailed)
                                setTimeout(() => {
                                  fq.value = null
                                  this.section = Object.keys(this.data)[index]
                                  this.errorColor(Object.keys(this.data)[index], fq.columnName);
                                }, 500)
                              } else {
                                setTimeout(() => {
                                  fq.showErrMessage = false
                                  return wasConstraintFailed = false;
                                }, 500)
                              }
                            }
                          }
                            break;
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        // if(index == Object.keys(this.data).length -1){
        //   resolve(false)
        // }
      }
      resolve(wasConstraintFailed)
      // ;
    });

  }


  /**
   * to extract keys of an opbject
   * @param obj:Object
   */
  getKeys(obj) {
    if (obj)
      return Object.keys(obj)
    else
      return [];
  }

  /**
   * This method returns the selectedOption by checking all the option with selected option key
   * @Author Laxman(laxman@sdrc.co.in)
   * @param selectedOptionKey
   * @param allOptions
   */
  getOptionByKey(selectedOptionKey, allOptions) {
    if (selectedOptionKey && allOptions) {
      for (let i = 0; i < allOptions.length; i++) {
        const opt = allOptions[i];
        if (opt.key == selectedOptionKey) {

          // if(question.features && question.features.includes('displayNoteFromExtraKey')){
          let extraKeyMapKeys = this.getKeys(opt.extraKeyMap);
          for (const key of extraKeyMapKeys) {
            this.questionMap[opt.extraKeyMap[key].ColumnName].value = opt.extraKeyMap[key].Value;
          }
          // }
          return opt;

        }
      };
    } else {
      return null;
    }
  }

  ionViewCanLeave() {
    if(!this.disableStatus){
      if (this.viewCanLeave) {
        return true;
      } else {
        if (!this.isExitConfirmDialogOpen) {
          let activePortal = this.app._loadingPortal.getActive() ||
               this.app._modalPortal.getActive() ||
               this.app._toastPortal.getActive() ||
               this.app._overlayPortal.getActive();

          if (activePortal) {
              activePortal.dismiss();
          } else {
            this.initializeNavBackButton();
          }
        }
        return false;
      }
    }

  }

  /**
   * This method is called, when clicks on the hardware back button app device.
   * This method checks the save mandaotry field and gave a alert fro save confirmation the record.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  initializeNavBackButton() {
    this.isExitConfirmDialogOpen = true;
    let confirm = this.alertCtrl.create({
      enableBackdropDismiss: false,
      cssClass: 'custom-font',
      title: 'Warning',
      message: "क्या आप यह रिकॉर्ड जमा करना चाहते हैं?",
      buttons: [
        {
          text: 'नहीं',
          handler: () => {
            this.viewCanLeave = true;
            this.navCtrl.pop()
          }
        },
        {
          text: 'हाँ',
          handler: () => {
            this.isExitConfirmDialogOpen = false;
            if (this.isWeb) {
              // this.customComponent.onSave('save')
              this.customComponent.onSave('finalized')
            } else {
              // this.onSave('save')
              this.onSave('finalized');
            }
          }
        }
      ]
    });
    confirm.present();
  }

  /**
   * This method is used to check the relevant questions are hidden in this section or not
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param questions
   */
  checkQuestionSizeBasedOnSubsectionRelevance(questions: IQuestionModel[]) {
    for (let q of questions) {
      if (q.displayComponent == true) {
        if (q.defaultSettings && !q.defaultSettings.includes('hidden')) {
          return true
        } else if (!q.defaultSettings) {
          return true
        }
      }
    }
    return false
  }

  /**
   * This method is use to restrict the input alpha field to alpha only.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   * @param event
   */
  _alphabetsKeyPress(question: IQuestionModel, event: any) {
    if (question.type == 'alpha') {
      const pattern = /^[a-zA-Z .,]*$/;
      var a = event.charCode;
      if (a == 0) {
        return;
      }
      let inputChar = String.fromCharCode(event.charCode);
      if (event.target["value"].length >= 200) {
        event.preventDefault();
      }
      if (!pattern.test(inputChar)) {
        event.preventDefault();
      }
    }
  }

  /**
   * This method is use to choose multiple file/image from the mobile storage.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  fileChoose(question: IQuestionModel) {
    this.fileChooser.open().then(uri => {
      this.filePath.resolveNativePath(uri)
        .then(async nativePath => {
          question.wrongFileExtensions = false;
          let fileSizeLimit: number = 2048
          if (question.attachmentsInBase64 == null) question.attachmentsInBase64 = [];
          if (nativePath.length > 1 && question.attachmentsInBase64.length < 5) {
            let filePath: string = nativePath;
            let extension = filePath.split(".")[filePath.split(".").length - 1];
            let fileName = filePath.split("/")[filePath.split("/").length - 1]
            let tempBaseFileName = null
            if (question.value == null) question.value = [];
            if (extension.toLowerCase() == "png" || extension.toLowerCase() == "jpeg" || extension.toLowerCase() == "jpg" || extension.toLowerCase() == "pdf" || extension.toLowerCase() == "doc" || extension.toLowerCase() == "docx" || extension.toLowerCase() == "xls" || extension.toLowerCase() == "xlsx") {
              if (filePath) {

                let orginalFilePath = filePath.toString().split('/');
                let urlForImage: string = '';
                let data: any;
                if (orginalFilePath[3] == "data") {
                  console.log("In Attachments : data", nativePath)
                  urlForImage = this.file.cacheDirectory
                  tempBaseFileName = fileName
                  data = await this.file.readAsArrayBuffer(urlForImage, decodeURI(fileName))
                } else if (orginalFilePath[6] == "Android") {
                  console.log("In Attachments : Android", nativePath)
                  // This is SD card / external storage partition Andriod folder -> data -> fani -> cache folder
                  urlForImage = this.file.externalCacheDirectory
                  tempBaseFileName = fileName
                  data = await this.file.readAsArrayBuffer(urlForImage, decodeURI(fileName))
                } else {
                  console.log("In Attachments : Root", nativePath)
                  urlForImage = this.file.externalRootDirectory
                  let pathFileName: string = '';
                  for (let index = 6; index < orginalFilePath.length; index++) {
                    if (pathFileName == '') {
                      pathFileName = orginalFilePath[index]
                    } else {
                      pathFileName = pathFileName + "/" + orginalFilePath[index]
                    }
                  }
                  tempBaseFileName = pathFileName
                  data = await this.file.readAsArrayBuffer(urlForImage, decodeURI(pathFileName))
                }
                if (Math.round(data.byteLength / 1024) >= fileSizeLimit) {
                  question.errorMsg = "Can't upload!! size limit exceeds (" + fileSizeLimit + " kb) for " + fileName + " !!";
                  question.fileSizeExceeds = true;
                } else {
                  let f = {
                    fileType: extension,
                    fileName: fileName,
                    fp: filePath,
                    basePath: urlForImage,
                    pathFileName: tempBaseFileName
                  };
                  question.errorMsg = null
                  question.attachmentsInBase64.push(f as any);
                  question.value.push(filePath);
                }
              }
            } else {
              question.errorMsg = "Wrong extention type"
              question.wrongFileExtensions = true;
            }
          } else {
            question.duplicateFilesDetected = true;
            question.errorMsg = "Can't upload more than 5 files"
          }
        })
        .catch(err => console.log(err));
    })
      .catch(e => alert('uri' + JSON.stringify(e)));
  }
  public displayError(question, index, msg, controlType?) {
    this.errorStatus = true;
    switch (controlType) {
      case "table":
      case 'tableWithRowWiseArithmetic':
      case "Date Widget":
        this.errorColor(Object.keys(this.data)[index], question.columnName, controlType);
        break;
      default:
        this.errorColor(Object.keys(this.data)[index], question.columnName);
        break;
    }
    this.messageService.showErrorToast(msg);
  }
  errorColorNoScroll(key?: any) {
    if (this.tempCaryId != null)
      this.removeColor(this.tempCaryId);

    if (key != null && key != '' && key != undefined) {
      setTimeout(() => {

        let eleId = document.getElementById(key + "");

        if (eleId != null) {

          eleId.style.setProperty("border", "1px solid red", "important");
        }
      }, 50)

    }

  }
  openErrorModal(content) {
    document.onkeydown = function (e) {
      return false;
    }
    let confirm = this.alertCtrl.create({
      enableBackdropDismiss: false,
      title: '&emsp;&emsp;<strong>&#9888;</strong> Warning',
      message: "<strong>" + content + "</strong>",
      cssClass: 'custom-font',
      buttons: [{
        text: 'OK',
        cssClass: 'cancel-button',
        handler: () => {
          // this.tempColName = "a"
          document.onkeydown = function (e) {
            return true;
          }
        }
      }
      ]
    });
    confirm.present();
  }

  setAutocmplete(question,section){

    switch(question.columnName){
      case 'undp_f1_tahasil':{
        this.filterBlocks(question.value,section);
        this.filterVillageDropdown(question.value,section)

      }
      break;
      case 'undp_f1_q18':{
        this.resetAreaType(section)
        this.filterTehsils(question.value,section)
        this.filterULBs(question.value,section);
        this.filterShelterDropdown(question.value,section)
      }
      break;
      case 'undp_f1_q9':{
        this.filterSourceDistrict(question.value,section);
      }
      break;

    }
  }
  filterTehsils(districtId,section){
    section[26].optionsOther=[]
    section[26].value=null
    section[27].value=null
    section[27].displayComponent=false
    section[26].optionsOther =section[26].options.filter(f=>Number(f.extraKeyMap.district_id)==Number(districtId.key) || Number(f.parentId2)==Number(-2))
    this.filterVillageDropdown(null,section)
  }
  filterSourceDistrict(stateId,section){
    section[21].optionsOther=[]
    section[21].value=null
    section[21].optionsOther =section[21].options.filter(f=>Number(f.extraKeyMap.state_id)==Number(stateId.key) || Number(f.parentId2)==Number(-2))
  }
  filterBlocks(blockId,section){
      section[28].optionsOther=[]
      section[28].value=null
      section[29].value=null
      section[29].displayComponent=false
      section[28].optionsOther =section[28].options.filter(f=>Number(f.extraKeyMap.tahasil_id)==Number(blockId.key) || Number(f.parentId2)==Number(-2))
      // this.filterVillageDropdown(null,section)
  }
  filterULBs(ulbId,section){
    section[30].optionsOther=[]
    section[30].value=null
    section[31].value=null
    section[31].displayComponent=false
    section[30].optionsOther =section[30].options.filter(f=>Number(f.extraKeyMap.district_id)==Number(ulbId.key) || Number(f.parentId2)==Number(-2))
  }
  // filter block ulb ends
  filterShelterDropdown(districtId,section){
    let tempList1=[]
      this.filteredShelterList=null
      section[49].options=[]
      section[49].value=null
      section[50].value=null
      section[50].displayComponent=false
      section[49].optionsOther=[]
      this.filteredShelterList =this.shelterList.filter(f=>Number(f.parentAreaId)==Number(districtId.key) || Number(f.parentAreaId)==Number(-2))
      // console.log(this.filteredShelterList)
    for (let index = 0; index < this.filteredShelterList.length; index++) {
      let tempOption={
        "key" : Number(this.filteredShelterList[index].areaId),
        "value" : this.filteredShelterList[index].areaName
        // "order" : null,
        // "parentId" : Number(this.filteredShelterList[index].parentAreaId)
       }
       tempList1.push(tempOption)
    }
    section[49].options=tempList1
    section[49].optionsOther=section[49].options
  }

  // shelter ends
  // village starts

  filterVillageDropdown(tehsilid,section){
    if(tehsilid==null){
      this.filteredVillageList=null
      section[33].options=[]
      section[33].value=null
      section[34].value=null
      section[34].displayComponent=false
      section[33].optionsOther=[]
    }else{
      let tempList=[]
      this.filteredVillageList=null
        section[33].options=[]
        section[33].value=null
        section[34].value=null
        section[34].displayComponent=false
        section[33].optionsOther=[]
        this.filteredVillageList =this.villageList.filter(f=>Number(f.parentAreaId)==Number(tehsilid.key) || Number(f.parentAreaId)==Number(-2))
        // console.log(this.filteredVillageList)
      for (let index = 0; index < this.filteredVillageList.length; index++) {
        let tempOption={
          "key" : Number(this.filteredVillageList[index].areaId),
          "value" : this.filteredVillageList[index].areaName
          // "order" : null,
          // "parentId" : Number(this.filteredShelterList[index].parentAreaId)
         }
        tempList.push(tempOption)
      }
      section[33].options=tempList
      section[33].optionsOther=section[33].options
    }
  }
  resetAreaType(section){
    section[25].value=null
  }
  resetOthersBasedonAreaType(question,section){
    if(question.columnName=='undp_f1_q18_1'){
      section[27].value=null
      section[27].displayComponent=false
      section[29].value=null
      section[29].displayComponent=false
      section[31].value=null
      section[31].displayComponent=false
    }
  }

// village ends
  onDateSelect(question,section){
    if(question.columnName=='undp_f1_date_1'){

      switch(section[0].value){
        case 2:{
          section[38].value=question.value
          let dateVal = new Date(question.value)
          dateVal.setDate(dateVal.getDate() + 14);
          section[39].value=this.convertDateToMMDDYYYY(dateVal)
        }
        break;
        case 3:{
          let dateVal = new Date(question.value)
          dateVal.setDate(dateVal.getDate() + 14);
          section[39].value=this.convertDateToMMDDYYYY(dateVal)
        }
        break;
    }
  }
}
  convertDateToMMDDYYYY(dateVal){
    let date=new Date(dateVal);
    let year = date.getFullYear();
    let month = (1 + date.getMonth()).toString();
    month = month.length > 1 ? month : '0' + month;
    let day = date.getDate().toString();
    day = day.length > 1 ? day : '0' + day;
    return year + '-' + month + '-' + day
  }
  loadershow(){


  }
  setDates(question,section){
    if(question.columnName=='undp_f1_q0'){
      section[13].value=null
      section[38].value=null
      section[39].value=null

      switch(question.value){
        case 1:{
          section[13].value=this.datepipe.transform(new Date(), "yyyy-MM-dd")
        }
        break;
        case 2:{
          section[13].value=this.datepipe.transform(new Date(), "yyyy-MM-dd")
          section[38].value=this.datepipe.transform(new Date(), "yyyy-MM-dd")
          let dateVal = new Date()
          dateVal.setDate(dateVal.getDate() + 14);
          section[39].value=this.convertDateToMMDDYYYY(dateVal)

        }
        break;
        case 3:{
          section[13].value=this.datepipe.transform(new Date(), "yyyy-MM-dd")
          let dateVal = new Date()
          dateVal.setDate(dateVal.getDate() + 14);
          section[39].value=this.convertDateToMMDDYYYY(dateVal)

        }
        break;
      }

    }

  }
  showHideMsg=true
  showHideMsg1=true
  checAlphaNumerickregex(question) {
    if(question.columnName=='undp_f1_bank_3'){
      if (question.value !=null && !question.value.match(/^[a-zA-Z0-9]+$/) && question.value != "") {
        question.value= "";
        if(this.showHideMsg==true){
          this.showHideMsg = false
          this.messageService.showErrorToast("कृपया केवल अक्षर और संख्या दर्ज करें")
        }
        timer(3000).subscribe(() => this.showHideMsg = true);
      }
    }
    if (question.value !=null && question.columnName=='undp_f1_q1' || question.columnName=='undp_f1_q4_1'){
      if (!question.value.match(/^[a-zA-Z ]+$/) && question.value != "") {
        question.value= "";
        if(this.showHideMsg1==true){
          this.showHideMsg1 = false
          this.messageService.showErrorToast("कृपया केवल अक्षर दर्ज करें")
        }
        timer(3000).subscribe(() => this.showHideMsg1 = true);
       }
    }

  }
  trimSpaces(question){
    if(question.type!='tel'){
      question.value=question.value==null?null:question.value.trim();
    }

  }
}

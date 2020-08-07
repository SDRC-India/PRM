import {
  Component,
  ViewChild,
  ElementRef
}
  from "@angular/core";
import {
  ConstantProvider
}
  from "../../providers/constant/constant";
import {
  MessageServiceProvider
}
  from "../../providers/message-service/message-service";
import {
  QuestionServiceProvider
}
  from "../../providers/question-service/question-service";
import {
  NavController,
  ViewController,
  NavParams,
  ModalController,
  AlertController,
  ActionSheetController,
}
  from "ionic-angular";
import {
  FormServiceProvider
}
  from "../../providers/form-service/form-service";
import {
  DataSharingServiceProvider
}
  from "../../providers/data-sharing-service/data-sharing-service";
import {
  DatePipe
}
  from "@angular/common";
import {
  EngineUtilsProvider
}
  from "../../providers/engine-utils/engine-utils";
import {
  UUID
}
  from "angular2-uuid";
import {
  IMyDpOptions,
  IMyDateModel
}
  from "mydatepicker";
import {
  AmazingTimePickerService
}
  from "amazing-time-picker";
import {
  Geolocation
}
  from "@ionic-native/geolocation";
import {
  ApplicationDetailsProvider
}
  from "../../providers/application/appdetails.provider";
import {
  CommonsEngineProvider
} from "../../providers/commons-engine/commons-engine";
import {
  UserServiceProvider
} from "../../providers/user-service/user-service";
import {
  Storage
} from '@ionic/storage';
import {
  ConstraintTokenizer
} from "../../providers/engine-utils/constraintsTokenizer";
import * as moment from 'moment';
import { WebFormService } from "../../providers/web.form.service";
// import { SUPPORTED_LANGS,getSelectedLanguage } from './../../config/translate';
// import { TranslateService } from '@ngx-translate/core';
/**../../providers/web.form.service
 * Generated class for the AnganwadiQuestionSectionComponent component.
 *
 * See https://angular.io/api/core/Component for more info on Angular
 * Components.
 */
@Component({
  selector: "web-form",
  templateUrl: "web.form.html"
})
export class WebFormComponent {

  @ViewChild('scrollId') scrollId: ElementRef;

  isWeb: boolean = false;
  section: String;
  repeatSubSection: Map<Number, IQuestionModel> = new Map();
  sectionNames = [];
  selectedSection: Array<Map<String, Array<IQuestionModel>>>;
  sectionMap: Map<String, Array<Map<String, Array<IQuestionModel>>>> = new Map();
  data: Map<String, Array<Map<String, Array<IQuestionModel>>>> = new Map();
  dbFormModel: IDbFormModel;
  sectionHeading: any;
  questionMap: {} = {};
  optionMap: {} = []
  formId: string;
  formTitle: String;
  formModel: {} = {};
  dataModel: {} = {};
  minDate: any;
  maxDate: any;
  errorStatus: boolean = false;
  mandatoryQuestion: {} = {};
  disableStatus: boolean = false;
  disablePrimaryStatus: boolean = false;
  saveType: any;
  questionDependencyArray: {} = {};
  questionFeaturesArray: {} = {};
  constraintsArray: {} = {};
  beginRepeatArray: {} = {};
  uniqueId: String;
  checkFieldContainsAnyvalueStatus: boolean = false;
  fullDate: any;
  createdDate: String = "";
  updatedDate: String = "";
  updatedTime: String = "";
  base64Image: any;
  currentLocation: any;
  options = {
    enableHighAccuracy: true,
    timeout: 10000
  };
  scoreKeyMapper: {} = [];
  date = moment("27-02-2018", 'DD-MM-YYYY').toDate();

  //for score keeper
  questionInSectionMap: {} = [];
  questionInSubSectionMap: {} = [];

  sectionScoreKeyMapper: {} = [];
  subSectionScoreKeyMapper: {} = [];

  sectionMapScoreKeeper: {} = [];
  subSectionMapScoreKeeper: {} = [];

  checklist_score_keeper_colName: any
  indexMap: {} = {};

  public input: string = '';
  public countries: string[] = [];
  todayDate: Number;
  existingForm: any;
  cameraQuestion: IQuestionModel;

  constructor(private constraintTokenizer: ConstraintTokenizer, public commonsEngineProvider: CommonsEngineProvider,
    private applicationDetailsProvider: ApplicationDetailsProvider, public questionService: QuestionServiceProvider,
    public messageService: MessageServiceProvider, private navCtrl: NavController, public viewCtrl: ViewController, public datepipe: DatePipe,
    public formService: FormServiceProvider, public navParams: NavParams, public dataSharingService: DataSharingServiceProvider,
    public modalCtrl: ModalController, private engineUtilsProvider: EngineUtilsProvider,
    private atp: AmazingTimePickerService, private geolocation: Geolocation, private storage: Storage, private userService: UserServiceProvider,
    private alertCtrl: AlertController, public actionSheetCtrl: ActionSheetController, public webFormService: WebFormService) {

    // console.log("Moment date " + this.date)
  }

  /**
   * This method call up the initial load. Get all the form data from database
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  async ngOnInit() {
    this.isWeb = this.applicationDetailsProvider.getPlatform().isWebPWA;
    this.formId = this.navParams.get("formId");
    this.existingForm = this.navParams.get("existingForm")
    let monthRes = Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[1]) - 1
    let yearRes = Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[0])
    let dayRes = Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[2])
    let fullDateRes = yearRes + "-" + monthRes + "-" + dayRes
    this.minDate = this.datepipe.transform(new Date(fullDateRes), "yyyy-MM-dd")
    this.maxDate = this.datepipe.transform(new Date(), "yyyy-MM-dd");
    this.fullDate = this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-");
    if (this.isWeb) {
      if (this.navParams.get("formId"))
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
          await this.questionService.getQuestionBank(this.formId, null, ConstantProvider.lastUpdatedDate).then(async schema => {

            if (schema) {
              this.data = await this.commonsEngineProvider.loadDataIntoSchemaDef(schema, this.data)
              await this.loadQuestionBankIntoUI(this.data, "s");
            } else {
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
                await this.loadQuestionBankIntoUI(formData, "n");
              } else {
                this.navCtrl.setRoot("LoginPage");
              }
            });
          } else {
            this.navCtrl.setRoot("LoginPage");
          }
        }
      } else {
        this.navCtrl.setRoot("LoginPage");
      }
    }
  }

  /**
   * This method called when user need to switch to sub-section dynamically
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @author Biswa Ranjan (biswaranjan@sdrc.co.in)
   * @param sectionHeading
   */
  sectionSelected(sectionHeading: any) {
    // document.getElementsByClassName("scroll-content")[3].scrollTo(0, 0);

    this.sectionHeading = sectionHeading;
    this.selectedSection = this.sectionMap.get(sectionHeading);
    this.scrollId.nativeElement.scrollTop = 0
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
      // console.log("Error getting location", error);
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
  onCameraFileChange($event, question) {
    this.cameraQuestion = question;
    let files = $event.target.files;
    let file = files[0];
    if ((file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String).toLocaleLowerCase() === "png" || (file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String).toLocaleLowerCase() === "jpg" || (file.name.split(".")[(file.name.split(".") as string[]).length - 1] as String).toLocaleLowerCase() === "jpeg") {
      let reader = new FileReader();
      reader.onload = this._handleReaderLoaded.bind(this);
      this.base64Image = question;
      reader.readAsBinaryString(file);
    } else {
      this.messageService.showErrorToast("Please select an image");
    }
  }

  /**
   * This method is used to convert the image file into base64 and set it ti the src variable ,also attach the lat long accurarcy in the meta_info
   * Also check that the image size should not be greater than 5mb.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param readerEvt
   */
  _handleReaderLoaded(readerEvt) {
    let binaryString = readerEvt.target.result;
    let fileSizeLimit: number = 5120
    if (Math.round(binaryString.length / 1024) >= fileSizeLimit) {
      this.errorColor(null, this.cameraQuestion.columnName);
      this.cameraQuestion.errorMsg = "Can't upload!! size limit exceeds (" + fileSizeLimit + " kb) !! ";
      this.cameraQuestion.fileSizeExceeds = true;
      this.geolocation.getCurrentPosition(this.options).then(resp => {
        this.questionMap[this.cameraQuestion.columnName].value = null
      }).catch(error => {
        this.questionMap[this.cameraQuestion.columnName].value = null
      });
    } else {
      this.cameraQuestion.errorMsg = null;
      this.cameraQuestion.fileSizeExceeds = false;
      this.geolocation.getCurrentPosition(this.options).then(resp => {
        this.questionMap[this.cameraQuestion.columnName].value = {
          src: "data:image/jpeg;base64," + btoa(binaryString),
          meta_info: "Lat :" + resp.coords.latitude + "; Long :" + resp.coords.longitude + "; Accuracy :" + resp.coords.accuracy
        };
      }).catch(error => {
        this.questionMap[this.cameraQuestion.columnName].value = {
          src: "data:image/jpeg;base64," + btoa(binaryString)
        };
      });
    }
  }

  /**
   * This function is use to set the options in the date picker like dateFormat,disableSince,editableDateField,showTodayBtn,showClearDateBtn
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  public myDatePickerOptions: IMyDpOptions = {
    // other options...
    dateFormat: "dd-mm-yyyy",
    // disableUntil: {
    //   year: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[0]),
    //   month: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[1]) - 1,
    //   day: 1
    // },
    disableSince: {
      year: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[0]),
      month: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[1]),
      day: Number(this.datepipe.transform(new Date(), "yyyy-MM-dd").split("-")[2]) + 1
    },
    editableDateField: false,
    showTodayBtn: false,
    showClearDateBtn: false
  };

  /**
   * This method is use to load ans set all the data from json to ui
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param data
   */
  async loadQuestionBankIntoUI(data, status) {
    this.data = data;

    if (status == "s") {
      this.optionMap = await this.commonsEngineProvider.loadOptionsIntoData(this.formId);
    }

    for (let index = 0; index < Object.keys(data).length; index++) {
      this.sectionMap.set(Object.keys(data)[index], data[Object.keys(data)[index]]);
      for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
        let subSections = data[Object.keys(data)[index]][0];
        //for score keeper
        let counter = 1;
        let sectionName = Object.keys(data)[index];

        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {

          //for score keeper
          let subSectionName = Object.keys(subSections)[qs];
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q];
            question.questionOrderDisplay = false;

            if (question.attachmentsInBase64 == null) question.attachmentsInBase64 = [];
            switch (question.controlType) {
              case "sub-score-keeper":
              case "score-keeper":
                question.sectionName = Object.keys(data)[index];
                question.subSectionName = Object.keys(subSections)[qs]
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                this.questionMap[question.columnName] = question;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;
                break;
              case "score-holder":
                question.sectionName = Object.keys(data)[index];
                question.subSectionName = Object.keys(subSections)[qs]
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                this.questionMap[question.columnName] = question;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;

                //for score keeper
                if (this.questionInSectionMap[sectionName] == undefined) {
                  this.questionInSectionMap[sectionName] = []
                  this.questionInSectionMap[sectionName].push(question)
                } else {
                  let a = this.questionInSectionMap[sectionName]
                  a.push(question)
                  this.questionInSectionMap[sectionName] = a
                }
                if (this.questionInSubSectionMap[sectionName + "_" + subSectionName] == undefined) {
                  this.questionInSubSectionMap[sectionName + "_" + subSectionName] = []
                  this.questionInSubSectionMap[sectionName + "_" + subSectionName].push(question)
                } else {
                  let a = this.questionInSubSectionMap[sectionName + "_" + subSectionName]
                  a.push(question)
                  this.questionInSubSectionMap[sectionName + "_" + subSectionName] = a
                }
                break;
              case "checklist-score-keeper":
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                this.questionMap[question.columnName] = question;
                break;
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
                    //convert the number type data to string (while rejection data is comming)
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
                    if (beginRepeatQuestion.controlType == 'Date Widget') {
                      if (beginRepeatQuestion.value != null) {
                        if (beginRepeatQuestion.value.date == null) {
                          let fullDate = beginRepeatQuestion.value.split('-')
                          beginRepeatQuestion.value = {
                            date: {
                              year: Number(fullDate[0]),
                              month: Number(fullDate[1]),
                              day: Number(fullDate[2])
                            }
                          }
                        } else {

                        }
                      }
                    }
                  }
                }
                break;

              case 'camera':
                question.questionOrderDisplay = true;
                this.indexMap[question.questionOrder] = counter;
                counter++;
                if (question.value != null && question.value.length == 1 && question.attachmentsInBase64) {
                  question.value.src = question.attachmentsInBase64[0]
                }
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                this.questionMap[question.columnName] = question;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;

                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                if (this.disableStatus) {
                  question.showErrMessage = false;
                }
                break;

              case "dropdown":
              case "autoCompleteTextView":
              case "autoCompleteMulti":
                {
                  if (status == "s") {
                    question.options = this.optionMap[question.columnName]
                  }
                  if (!question.label.includes('Score')) {
                    question.questionOrderDisplay = true;
                    this.indexMap[question.questionOrder] = counter;
                    counter++;
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
                }
                break
              case "heading":
              case "textbox":
              case "Time Widget":
              case "cell":
              case "textarea":
              case "uuid":
              // add this case "file" for displaying the component in ui
              case "file":
              case "mfile":
              case 'geolocation':
              case 'segment':
                if (!question.label.includes('Score')) {
                  question.questionOrderDisplay = true;
                  this.indexMap[question.questionOrder] = counter;
                  counter++;
                }
                //convert the number type data to string (while rejection data is commming)
                if (question.controlType == 'textbox' && question.type == 'tel' && question.value != null) {
                  question.value = String(question.value)
                }
                // question.optionsOther = question.options;

                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                this.questionMap[question.columnName] = question;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;

                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                if (this.disableStatus) {
                  question.showErrMessage = false;
                }


                break;
              case "Date Widget":
                question.questionOrderDisplay = true;
                this.indexMap[question.questionOrder] = counter;
                counter++;
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                this.questionMap[question.columnName] = question;
                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;
                if (question.value != null) {
                  if (question.value.date == null) {
                    let fullDate = question.value.split('-')
                    question.value = {
                      date: {
                        year: Number(fullDate[0]),
                        month: Number(fullDate[1]),
                        day: Number(fullDate[2])
                      },
                      formatted: question.value
                    }
                  }
                }
                // if defaultValue is "current date" then set the default date in the ui
                if (question.defaultValue == 'current date') {
                  if (question.value == null) {
                    let fullDate = this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')
                    question.value = {
                      date: {
                        year: Number(fullDate[0]),
                        month: Number(fullDate[1]),
                        day: Number(fullDate[2])
                      },
                      formatted: this.datepipe.transform(new Date(), "yyyy-MM-dd")
                    }
                  } else if (question.value.date == null) {
                    let fullDate = question.value.split('-')
                    question.value = {
                      date: {
                        year: Number(fullDate[0]),
                        month: Number(fullDate[1]),
                        day: Number(fullDate[2])
                      },
                      formatted: question.value
                    }
                  }
                }
                if (this.disableStatus) {
                  question.showErrMessage = false;
                }

                break;
              case "Month Widget":
                question.questionOrderDisplay = true;
                this.indexMap[question.questionOrder] = counter;
                counter++;
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
                this.questionMap[question.columnName] = question;
                this.mandatoryQuestion[question.columnName] = question.finalizeMandatory;
                question.relevance != null ? this.drawDependencyGraph(question.relevance, question) : null;
                if (this.disableStatus) {
                  question.showErrMessage = false;
                }
                break;
              case "checkbox":
                question.questionOrderDisplay = true;
                this.indexMap[question.questionOrder] = counter;
                counter++;
                question.dependecy = question.relevance != null ? true : false;
                question.displayComponent = question.relevance == null ? true : false;
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


    //for score keeper
    for (let index = 0; index < Object.keys(data).length; index++) {
      this.sectionMap.set(Object.keys(data)[index], data[Object.keys(data)[index]]);
      for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
        let subSections = data[Object.keys(data)[index]][0];

        //for score keeper
        let sectionName = Object.keys(data)[index];
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {

          //for score keeper
          let subSectionName = Object.keys(subSections)[qs];
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q];
            if (question.attachedFiles == null) question.attachedFiles = [];
            switch (question.controlType) {
              case "sub-score-keeper":
                this.subSectionScoreKeyMapper[question.columnName] = this.questionInSubSectionMap[sectionName + "_" + subSectionName];
                this.subSectionMapScoreKeeper[sectionName + "_" + subSectionName] = question;
                break;
              case "score-keeper":
                this.sectionScoreKeyMapper[question.columnName] = this.questionInSectionMap[sectionName];
                this.sectionMapScoreKeeper[sectionName] = question;
                break;
              case "checklist-score-keeper":
                this.checklist_score_keeper_colName = question.columnName
                break;
            }
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

    // enable or disable the dropdown/autoCompleteMulti/autoCompleteTextView by checking the feature , if it is a "area_group"
    for (let questionKey of this.dataSharingService.getKeys(this.questionMap)) {
      let question = this.questionMap[questionKey]
      if (question.features != null) {
        let feature: string = question.features.split("@AND")
        switch (question.controlType) {
          case "autoCompleteMulti":
            for (let i = 0; i < feature.length; i++) {
              if (feature[i].includes('area_group')) {
                let groupQuestions = this.commonsEngineProvider.getDependentAreaGroupName(feature[i])
                let childLevelQuestion = this.questionMap[groupQuestions];
                let optionCount = 0;
                childLevelQuestion.optionsOther = []
                for (let option of childLevelQuestion.options) {
                  if (question.value && this.webFormService.checkValueInObjectArray(option["parentId"], question.value, 'key')) {
                    if (option["parentId2"] == -2) {
                      option["visible"] = false;
                      optionCount++
                    } else {
                      option["visible"] = true;
                      childLevelQuestion.optionsOther.push(option)
                    }
                  } else {
                    if (option["parentId2"] == -2) {
                      option["visible"] = true;
                      childLevelQuestion.optionsOther.push(option)
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

            this.checkRelevance(question)
            break;
          case 'dropdown':
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
                  childLevelQuestion.constraints = "disabled"
                } else {
                  childLevelQuestion.constraints = ""
                }
              }
            }
            break;
        }
      }

    }
    // console.log("indexmap", this.indexMap);
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



  /**
   * This method will call, when user clicks to add one more begin reapet section in the ui.
   *
   * @author Jagat (jagat@sdrc.co.in)
   * @param key
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

      // clone the value, dependency, columnn name (after renaming)
      clonedQuestion[index].value = null;
      clonedQuestion[index].othersValue = false;
      clonedQuestion[index].isOthersSelected = false;
      clonedQuestion[index].dependecy = clonedQuestion[index].relevance != null ? true : false;
      clonedQuestion[index].columnName = beginRepeatParent.columnName + "-" + size + "-" + colIndex + "-" + colName;
      //clone all the relevance, feature, constraint and score expression
      clonedQuestion[index] = this.commonsEngineProvider.renameRelevanceAndFeaturesAndConstraintsAndScoreExpression(clonedQuestion[index], this.questionMap, beginRepeatParent, size);

      //setting up default setting and added to dependency array and feature array
      clonedQuestion[index].displayComponent = clonedQuestion[index].relevance == null ? true : false;
      this.questionMap[clonedQuestion[index].columnName] = clonedQuestion[index];
      //  clonedQuestion[index].displayComponent = true
    }

    for (let index = 0; index < clonedQuestion.length; index++) {
      clonedQuestion[index].relevance != null ? this.drawDependencyGraph(clonedQuestion[index].relevance, clonedQuestion[index]) : null;
      clonedQuestion[index] = this.setupDefaultSettingsAndConstraintsAndFeatureGraph(clonedQuestion[index]);
    }

    this.checkRelevanceForEachQuestion()
    // checking the limit size of the begin repeat before adding.
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
   * This method is called, when clicks on the delete icon to delete the beginRepeat section and clear the value of fields inside beginRepeat.
   *
   * @author Jagat (jagat@sdrc.co.in)
   * @param key
   */
  async deleteLastWorker(key: Number, bgquestion: IQuestionModel) {

    let beginRepeatParent: IQuestionModel = this.repeatSubSection.get(key);
    let clonedQuestion: IQuestionModel[] = beginRepeatParent.beginRepeat[beginRepeatParent.beginRepeat.length - 1];
    for (let index = 0; index < clonedQuestion.length; index++) {
      clonedQuestion[index].relevance != null ? this.removeFromDependencyGraph(clonedQuestion[index].relevance, clonedQuestion[index]) : null
    }

    // used to disable or enable the minus/delete button based on the criteria.
    if (bgquestion.beginRepeat.length > 1) {
      await this.commonsEngineProvider.removeFilesAndCameraAttachementsIfAny(clonedQuestion);
      bgquestion.beginRepeat.pop();
      if (bgquestion.beginRepeat.length == 1) {
        bgquestion.beginRepeatMinusDisable = true;
      } else {
        bgquestion.beginRepeatMinusDisable = false;
      }
    } else {
      // clear the value of the field after deletion of the begin repeat section.
      for (let i = 0; i < bgquestion.beginRepeat.length; i++) {
        for (let j = 0; j < bgquestion.beginRepeat[i].length; j++) {
          bgquestion.beginRepeat[i][j].value = null;
        }
      }
    }
  }
  //Biswa
  tempCaryId: any;

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
              // to set the visitedDate in "dbFormModel", extract the value from question.value and set it to "visitedDate" variable for future use.
              case "Date Widget":
                if (question.defaultSettings == "prefetchDate:current_date") {
                  if (this.isWeb && question.value != null && question.value.date != null) {
                    let dateValue = question.value.date.day + "-" + question.value.date.month + "-" + question.value.date.year
                    visitedDate = dateValue
                  } else {
                    visitedDate = question.value
                  }
                }
                break;
              case 'camera': {
                if (question.value && question.reviewHeader)
                  image = question.value ? question.value.src : null

              }
                break
            }
            if (question.value && question.reviewHeader)
              headerData = this.commonsEngineProvider.prepareHeaderData(question, headerData)
          }
        }
      }
    }
    if (type == 'save') {
      this.errorStatus = false;
      this.commonsEngineProvider.checkMandatory(this.sectionMap, this.data, 'save', this)

      if (!this.errorStatus) {
        // this.data = this.commonsEngineProvider.sanitizeOptions(this.data)
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
            this.navCtrl.pop()
            if (type == 'save') {
              if (this.existingForm == undefined)
                this.messageService.stopLoader()
              this.messageService.showSuccessToast(ConstantProvider.message.saveSuccess)
            } else {
              this.messageService.showSuccessToast(ConstantProvider.message.finalizedSuccess)
            }
          } else {
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
                title: 'Warning',
                cssClass: 'custom-font',
                message: "Once you finalize the form, it can't be further edited.<br><br><strong>Are you sure you want to finalize this form?</strong>",
                buttons: [{
                  text: 'No',
                  handler: () => {
                    // this.navCtrl.pop()
                  }
                },
                {
                  text: 'Yes',
                  handler: async () => {
                    // this.data = this.commonsEngineProvider.sanitizeOptions(this.data)
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
                        this.navCtrl.pop()
                        this.messageService.showSuccessToast(ConstantProvider.message.finalizedSuccess)
                      } else {
                        this.navCtrl.pop()
                      }
                    });
                    // })
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
   * This method checks the number
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param e
   * @param type
   * @param question
   */
  checkNumber(e, type, question) {
    if (question.type == "tel") {
      let newValue = e.target.value;
      let pass = /[4][8-9]{1}/.test(e.charCode) || /[5][0-7]{1}/.test(e.charCode) || e.keyCode === 8 || e.keyCode === 32;
      if (!pass) {
        let regExp = new RegExp('^[0-9?]+$');
        if (!regExp.test(newValue)) {
          e.target.value = newValue.slice(0, -1);
        }
      }
    }
  }

  /**
   * This method is for table data calculation.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param cellQ
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
                  // console.log("table value :" + result)
                  if (result != null && result != "NaN" && result != NaN && !isNaN(result) && result != "null" && cell.type == "tel") {
                    fresult = parseInt(result as string);
                    cell.value = fresult;
                  } else {
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
   * This method is use to compute 2 field and the result should be shown in another filed
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
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
  validateBeginRepeat(bgParentQuestion) {
    if (this.beginRepeatArray[bgParentQuestion.columnName]) {
      let bgQuestion = this.beginRepeatArray[bgParentQuestion.columnName];
      let dependentQuestion = this.questionMap[bgParentQuestion.columnName];
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
      // used to disable or enable the minus/delete button based on the criteria.
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
    let beginrepeat = bgquestion.beginRepeat;
    for (let i = 0; i < beginrepeat.length; i++) {
      for (let j = 0; j < beginrepeat[i].length; j++) {
        if (beginrepeat[i][j].controlType == "dropdown") {
          if (beginrepeat[i][j].features && beginrepeat[i][j].features.includes("render_default")) {
            if (status) {
              beginrepeat[i][j].value = Number(beginrepeat[i][j].defaultValue);
            } else {
              beginrepeat[i][j].value = null;
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

    if (type == "ui" && question.constraints != null && question.constraints.split(":")[0] == 'clear') {
      setTimeout(() => {
        this.questionMap[question.constraints.split(":")[1]].value = null
      }, 100)
    }

    if (this.questionDependencyArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label] != null)
      for (let q of this.questionDependencyArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label]) {

        if (q.relevance) {
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
  }

  /**
   * This method will clear the value of the field that contains some feature like area_group, filter_single, filter_multiple.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question
   */
  clearFeatureFilters(question: IQuestionModel) {
    if (this.questionFeaturesArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label] != null)
      for (let q of this.questionFeaturesArray[question.columnName + ":" + question.key + ":" + question.controlType + ":" + question.label]) {
        for (let feature of q.features.split("@AND")) {
          switch (feature) {
            case "area_group":
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
                  // in optionCount we are counting how many options are
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
              else if (question.type == "checkbox") {
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
                // This for dropdown or autocompleteTextView Single Selection
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
              if (feature.includes("area_group") || question.features.includes("filter_single")) {
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

              if (feature.includes("area_group") || question.features.includes("filter_single")) {
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
            childLevelQuestion.value = null;
            for (let option of childLevelQuestion.options) {
              option["visible"] = false;
              for (let parentId of option["parentIds"]) {
                if (parentId == question.value) {
                  option["visible"] = true;
                  break;
                }
              }
            }

          }
        }
          break;

        case "filterByExp": {
          if (question.features != null && feature.includes("filterByExp") && !feature.includes("filterByExp:(")) {
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
          } else if (question.features != null && feature.includes("filterByExp") && feature.includes("filterByExp:(")) {
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
        let subSections = this.data[Object.keys(this.data)[index]][0];
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q];
            switch (question.controlType) {
              case "textbox":
                if (question.value != null && (question.value as string).trim() != "") return true;
                break;
              case "segment":
              case "dropdown":
                if (question.value != null && question.value != "") return true;
                break;
              case "Time Widget":
                if (question.value != null && question.value != "") return true;
                break;
              case "Date Widget":
                if (question.value != null && question.value != "") return true;
                break;
              case "Month Widget":
                if (question.value != null && question.value != "") return true;
                break;
              case "checkbox":
                if (question.value != null && question.value != "") return true;
                break;
              case "table":
              case "tableWithRowWiseArithmetic": {
                let tableData = question.tableModel;
                for (let i = 0; i < tableData.length; i++) {
                  for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                    let cell = tableData[i][Object.keys(tableData[i])[j]];
                    if (typeof cell == "object") {
                      if (cell.value != null && cell.value.trim() != "") return true;
                      break;
                    }
                  }
                }
              }
                break;
              case "beginrepeat":
                let beginrepeat = question.beginRepeat;
                let beginrepeatArray: any[] = [];
                let beginrepeatMap: {} = {};
                for (let i = 0; i < beginrepeat.length; i++) {
                  beginrepeatMap = {};
                  for (let j = 0; j < beginrepeat[i].length; j++) {
                    let colName = (beginrepeat[i][j].columnName as String).split("-")[3];
                    beginrepeatMap[colName] = beginrepeat[i][j].value;

                    switch (beginrepeat[i][j].controlType) {
                      case "textbox":
                        if (beginrepeat[i][j].value != null && (beginrepeat[i][j].value as string).trim() != "") return true;
                        break;
                      case "dropdown":
                      case "segment":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].valu != "") return true;
                        break;
                      case "Time Widget":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "") return true;
                        break;
                      case "Date Widget":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "") return true;
                        break;
                      case "Month Widget":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "") return true;
                        break;
                      case "checkbox":
                        if (beginrepeat[i][j].value != null && beginrepeat[i][j].value != "") return true;
                        break;
                    }
                  }
                  beginrepeatArray.push(beginrepeatMap);
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
  drawDependencyGraph(expression: String, question: IQuestionModel) {
    for (let str of expression.split("}")) {
      let expressions: String[] = str.split(":");
      for (let i = 0; i < expressions.length; i++) {
        let exp: String = expressions[i];
        switch (exp) {
          case "optionEquals":
          case "optionEqualsMultiple":
          // add otherOptionEquals in the "questionDependencyArray" for the relevance check, so that when user select other option from village, the
          // "if Other, please specify" quesion will be appear to the user.
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
      for (let settings of question.defaultSettings.split(",")) {
        switch (settings.split(":")[0]) {
          // this case is used to set the current date to the date field.
          case "current_date":
            question.value = this.datepipe.transform(new Date(), "yyyy-MM-dd");
            break;
          // this case is used to prefetch any number to set in the textbox (type tel)
          case "prefetchNumber":
            question.value = parseInt(settings.split(":")[1])
            break;
          // this case is used to prefetch any text to set in the textbox (type text)
          case "prefetchText":
            question.value = new String(settings.split(":")[1]);
            break;
          case "prefetchDropdownWithValue":
            question.value = new Number(settings.split(":")[1]);
            break;
          // this case is used to disabled any field.
          case "disabled":
            question.disabled = true;
            break;
          // this case is used to prefield the value in the dropdown field (for state)
          case "prefetchDropdown":
            question.value = Number(settings.split(":")[1]);
            break;
          // this case is used to set the prefetch date to the date field.
          case "prefetchDate":
            if (settings.split(":")[1] == "current_date") {
              if (question.value != null) {
                if (question.value.date == null) {
                  let fullDate = question.value.split('-')
                  question.value = {
                    date: {
                      year: Number(fullDate[0]),
                      month: Number(fullDate[1]),
                      day: Number(fullDate[2])
                    }
                  }
                }
              } else if (question.value == null) {
                let fullDate = this.datepipe.transform(new Date(), "yyyy-MM-dd").split('-')
                question.value = {
                  date: {
                    year: Number(fullDate[0]),
                    month: Number(fullDate[1]),
                    day: Number(fullDate[2])
                  }
                }
              } else if (question.value.date == null) {
                let fullDate = question.value.split('-')
                question.value = {
                  date: {
                    year: Number(fullDate[0]),
                    month: Number(fullDate[1]),
                    day: Number(fullDate[2])
                  }
                }
              }
            }
            break;
        }
      }
    }
    // this is used to set the maxLength, minLength, maxValue, minValue, lessThan, etc to the respective variables for use of constraints.
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
    //this is used to add the columns which contains features to the "questionFeaturesArray" variable
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
    return question;
  }
  ///resolver methods to handle Relevance using Djisktra Shunting Algorithm

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
  onDateChanged(question1: any, question2: any, event: IMyDateModel) {
    question1.value = event.formatted;
    this.syncGroup(question1, question2, null);
    if (question1.value != null && question1.constraints != null) {
      switch (question1.constraints.split(":")[0]) {
        case 'lessThan':
          if (this.questionMap[question1.constraints.split(":")[1]].value != null) {
            let startDD = Number(this.questionMap[question1.constraints.split(":")[1]].value.formatted.split("-")[0])
            let startMM = Number(this.questionMap[question1.constraints.split(":")[1]].value.formatted.split("-")[1])
            let startYY = Number(this.questionMap[question1.constraints.split(":")[1]].value.formatted.split("-")[2])
            let endtDD = Number(question1.value.split("-")[0])
            let endMM = Number(question1.value.split("-")[1])
            let endYY = Number(question1.value.split("-")[2])
            let startDate: Date = new Date(startYY, startMM, startDD);
            let enddate: Date = new Date(endYY, endMM, endtDD);
            if (startDate < enddate) {
              setTimeout(() => {
                question1.value = null;
              }, 100)
            }
          }
          break;
        case 'greaterThan':
          if (this.questionMap[question1.constraints.split(":")[1]].value != null) {
            let startDD = Number(this.questionMap[question1.constraints.split(":")[1]].value.formatted.split("-")[0])
            let startMM = Number(this.questionMap[question1.constraints.split(":")[1]].value.formatted.split("-")[1])
            let startYY = Number(this.questionMap[question1.constraints.split(":")[1]].value.formatted.split("-")[2])
            let endtDD = Number(question1.value.split("-")[0])
            let endMM = Number(question1.value.split("-")[1])
            let endYY = Number(question1.value.split("-")[2])
            let startDate: Date = new Date(startYY, startMM, startDD);
            let enddate: Date = new Date(endYY, endMM, endtDD);
            if (enddate < startDate) {
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
   * This method is called to check all the constraint ie (min value, max value, greaterThan, lessThan).
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param question1
   * @param question2
   */
  checkMinMax(question1, question2) {
    if (question1.value != null && question1.constraints != null && question1.controlType == "textbox") {
      if (question1.maxValue != null && Number(question1.value) > question1.maxValue) {
        question1.value = null;
        this.syncGroup(question1, question2, null);
        return (question1.value = null);
      } else if (question1.maxValue != null && Number(question1.value) < question1.minValue) {
        question1.value = null;
        this.syncGroup(question1, question2, null);
        return (question1.value = null);
      } else if (Number(question1.value) < question1.minValue) {
        question1.value = null;
        return (question1.value = null);
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
              }, 100)
            }
          }
        }
      }
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
    if (event.target.files) {
      let files = event.target.files;
      // console.log(files);
      question.duplicateFilesDetected = false;
      question.wrongFileExtensions = false;
      let testDuplicate: boolean = false;
      let fileSizeLimit: number = 2048

      // checking file upload upto 5 file max
      if (files.length && question.attachmentsInBase64.length < 5) {
        for (let a = 0; a < files.length; a++) {
          let file = files[a];
          let extension = file.name.split(".")[file.name.split(".").length - 1];
          // checking the extention
          if (extension.toLowerCase() == "png" || extension.toLowerCase() == "jpeg" || extension.toLowerCase() == "jpg" || extension.toLowerCase() == "pdf" || extension.toLowerCase() == "doc" || extension.toLowerCase() == "docx" || extension.toLowerCase() == "xls" || extension.toLowerCase() == "xlsx") {
            let reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = () => {
              let f = {
                base64: (reader.result as String).split(",")[1],
                fileName: file.name,
                fileSize: file.size,
                columnName: question.columnName,
                attachmentId: null,
                fileType: extension.toLowerCase()
              };
              if (testDuplicate) {
                question.errorMsg = file.name + " :File has been already attached!!";
                // checking the file size of each should not be max than 2mb.
              } else if (Math.round(files[a].size / 1024) >= fileSizeLimit) {
                question.errorMsg = "Can't upload!! size limit exceeds (" + fileSizeLimit + " kb) for " + file.name + " !!";
                question.fileSizeExceeds = true;
              } else {
                question.errorMsg = null;
                question.attachmentsInBase64.push(f as any);
                question.fileSizeExceeds = false;
              }
            };
          } else {
            question.wrongFileExtensions = true;
          }
        }
      } else {
        question.duplicateFilesDetected = true;
        question.errorMsg = "Can't upload more than 5 files"
      }
    } else {
      question.attachmentsInBase64 = [];
    }
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
                    // if (fq.controlType == 'cell')
                    // this.messageService.showErrorToast(fq.cmsg)
                    setTimeout(() => {
                      // this.questionMap[qq.columnName].value = null;
                      //   this.errorColor1(fq.columnName)
                      fq.value = null
                      this.checkRelevance(fq);
                      this.clearFeatureFilters(fq);
                      this.compute(fq);
                      this.validateBeginRepeat(fq.columnName);
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
      let temp = document.getElementById(this.tempCaryId + "");
      if (temp != null && temp != undefined) document.getElementById(this.tempCaryId + "").style.removeProperty("border");
      if (temp != null && temp != undefined) document.getElementById(this.tempCaryId + "").style.removeProperty("outline");
      this.tempCaryId = null;
    }
    if (key != null && key != "" && key != undefined) {
      let temp = document.getElementById(key + "");
      if (temp != null && temp != undefined) document.getElementById(key + "").style.removeProperty("border");
      if (temp != null && temp != undefined) document.getElementById(key + "").style.removeProperty("outline");
      this.tempCaryId = key;
    }
  }

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
    if (sectionHeading != null) {
      this.selectedSection = this.sectionMap.get(sectionHeading);
    }
    if (this.tempCaryId != null) this.removeColor(this.tempCaryId);
    if (key != null && key != "" && key != undefined) {
      setTimeout(() => {
        let eleId = document.getElementById(key + "");
        if (eleId != null) {
          let toscrl = eleId.parentNode.parentElement.offsetTop;
          if (type == "Date Widget") {
            eleId.style.setProperty("outline", "#FF0000 double 1px", "important");
          } else {
            eleId.style.setProperty("border", "#FF0000 solid 1px", "important");
          }
          if (document
            .getElementsByClassName("scroll-content")[2]) {
            document
              .getElementsByClassName("scroll-content")[2]
              .scrollTo(0, toscrl);
          }
          this.tempCaryId = key;
        } else {
          if (type != "table")
            document.getElementsByClassName("scroll-content")[2].scrollTop;
        }
      }, 50);
    } else {
      document.getElementsByClassName("scroll-content")[2].scrollTop = 0;
    }
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
          // add otherOptionEquals in the "questionDependencyArray" for the relevance check, so that it will help to remove the relevance question
          // "if Other, please specify" from the ui.
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
   * This method is used to check the finalized constraint.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
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

}

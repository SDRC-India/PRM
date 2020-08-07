import {
  Component, HostListener
} from '@angular/core';
import {
  IonicPage,
  NavController,
  NavParams,
  Events,
  AlertController,
  Nav,
  MenuController
} from 'ionic-angular';
import {
  QuestionServiceProvider
} from '../../providers/question-service/question-service';
import {
  MessageServiceProvider
} from '../../providers/message-service/message-service';
import {
  ConstantProvider
} from '../../providers/constant/constant';
import { DatePipe } from '@angular/common';
import { UserServiceProvider } from '../../providers/user-service/user-service';
import {
  Storage
} from '@ionic/storage'
// import { SUPPORTED_LANGS,getSelectedLanguage } from './../../config/translate';
// import { TranslateService } from '@ngx-translate/core';
/**
 * Generated class for the HomePage page.
 *
 * See https://ionicframework.com/docs/components/#navigation for more info on
 * Ionic pages and navigation.
 */

@IonicPage()
@Component({
  selector: 'page-home',
  templateUrl: 'home.html',
})
export class HomePage {
  @HostListener('window:popstate', ['$event'])
  onbeforeunload(event) {
    if (window.location.href.substr(window.location.href.length - 5) == 'login') {
      history.pushState(null, null, "" + window.location.href);
    }
  }

  formList: any
  homePageModelArr: IHomePageModel[] = [];
  public unregisterBackButtonAction: any;
  searchTerm:string;
  currentMonth: Number;
  previousMonth: Number;
  deadlineDateModel: any;
  dateLineDate: string;
  syncModal: boolean = true;
  viewCanLeave = false;
  isLogoutConfirmDialogOpened = false;

  constructor(public storage: Storage, public userService: UserServiceProvider,public navCtrl: NavController, public navParams: NavParams, public questionService: QuestionServiceProvider,
    public messageProvider: MessageServiceProvider, public events: Events,
    private alertCtrl: AlertController, private nav: Nav,private datePipe: DatePipe,
    public menuCtrl: MenuController) {
      // translate.setDefaultLang('en');
      // translate.addLangs(SUPPORTED_LANGS);
      // translate.use(getSelectedLanguage(translate));
    }

  /**
   * This method call up the initial load. subscribe the syncStatus to refresh the page
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  ngOnInit() {
    this.events.subscribe('syncStatus', data=>{
      if(data){
        this.syncModal = false
        this.loadForms()
      }
    })
    this.events.subscribe('takeRefresh', data=>{
      if(data){
        this.loadForms()
      }
    })
  }

  ionViewDidEnter(){
    this.viewCanLeave = false;
    this.userService.userTryingToLogout = false;
    if(this.syncModal)
    this.messageProvider.showLoader(ConstantProvider.message.pleaseWait);
    this.loadForms()
  }

  /**
   * This method will fetch all the user specific froms
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  async fetchData() {
    await this.questionService.getAllFormsId().then(async (formIds) => {
      this.formList = await formIds;
    });
  }

  /**
   * This method is called after fetchData to show the count of different submission of each form.
   *
   * @author Azhar (azaruddin@sdrc.co.in)
   */
  async loadForms(){
    let dbData = await this.questionService.getAllFilledFormsAgainstLoggedInUser()

    // let isSanitized = await this.questionService.isOldFormsSanitized()

    // if(!isSanitized && dbData){
    //   // start setting all options field to blank array for all forms then push into db
    //   let forms = dbData
    //   for (let form = 0; form < Object.keys(forms).length; form++) {
    //     let submittedForms = forms[Object.keys(forms)[form]]
    //     for (let submittedForm = 0; submittedForm < Object.keys(submittedForms).length; submittedForm++) {

    //       let facilityId = Object.keys(submittedForms)[submittedForm]

    //       let data = submittedForms[facilityId];

    //       let sectionMap = data.formData;
    //         for (let index = 0; index < Object.keys(sectionMap).length; index++) {
    //           for (let j = 0; j < sectionMap[Object.keys(sectionMap)[index]].length; j++) {
    //             let subSections = sectionMap[Object.keys(sectionMap)[index]][0]
    //             for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
    //               for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
    //                 let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
    //                 switch (question.controlType) {
    //                   case "dropdown":
    //                   case "autoCompleteTextView":
    //                   case "autoCompleteMulti":{
    //                           question.options = []
    //                   }
    //                   break;
    //                   case 'beginrepeat':
    //                   for (let index = 0; index < question.beginRepeat.length; index++) {
    //                         let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[index];
    //                         for (let beginRepeatQuestion of beginRepeatQuestions) {

    //                         switch (beginRepeatQuestion.controlType) {
    //                                 case "dropdown":
    //                                 case "autoCompleteTextView":
    //                                 case "autoCompleteMulti":
    //                                     beginRepeatQuestion.options = []
    //                                   break
    //                               }
    //                             }
    //                           }
    //                 }
    //               }
    //             }
    //         }
    //       }
    //     }
    //   }
    //   await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username, forms);
    //   await this.questionService.oldFormsSanitized()
    //   dbData = forms;
    // }

    await this.fetchData()
    for (let i = 0; i < this.formList.length; i++) {

      let save: number = 0;
      let finalize: number = 0;
      let sent: number = 0;
      let reject: number = 0;
      let pendingForSync: number = 0;
      let form: IHomePageModel = {
        formKeyName: this.formList[i],
        formName: this.formList[i].split("_")[1],
        formId: this.formList[i].split("_")[0],
        saveCount: save,
        rejectCount: reject,
        finalizeCount: finalize,
        sentCount: sent,
        pendingForSyncCount: pendingForSync
      }
      if(dbData != null && dbData[form.formKeyName] != undefined){
        for (let i = 0; i < Object.keys(dbData[form.formKeyName]).length; i++) {
          if(dbData[form.formKeyName][Object.keys(dbData[form.formKeyName])[i]].formStatus == "save"){
            save++
          }else if(dbData[form.formKeyName][Object.keys(dbData[form.formKeyName])[i]].formStatus == "finalized"){
            finalize++
          }else if(dbData[form.formKeyName][Object.keys(dbData[form.formKeyName])[i]].formStatus == "sent"){
            sent++
          }else{
            reject++
          }
        }
      }

       form['saveCount'] = save
       form['rejectCount'] = reject
       form['finalizeCount'] = finalize
       form['sentCount'] = sent
       form['pendingForSyncCount'] = pendingForSync
       this.homePageModelArr[i] = form
     }
     if(this.syncModal)
     this.messageProvider.stopLoader();
  }

  /**
   * This method is called when user click on specific from. This method take the specific from data to next page.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param formId
   */

  form(formId: any,segment: any) {
    this.viewCanLeave = true;
    this.questionService.getQuestionBank(formId, null, ConstantProvider.lastUpdatedDate).then(data => {
        if (data) {
          this.navCtrl.push('FormListPage', {
            formId: formId,
            segment: segment
          })
        }
      })
      .catch((error) => {
        if (error.status == 500) {
          this.messageProvider.showErrorToast(ConstantProvider.message.networkError)
        }
      })
  }


    /**
   * This method will called, when user clicks on the add new button to fill a data in the new form
   *
   * @author Harsh Pratyush (Harsh@sdrc.co.in)
   */
  openNewBlankForm(formId: any,segment: any,xyz) {
    this.viewCanLeave = true;
     this.navCtrl.push("MobileFormComponent", {
      formId: formId,
      formTitle: formId.split("_")[1].replace('_',' '),
      isNew: true,
      segment: segment
    });
  }


  /**
   * This method will show a confirmation popup to exit the app, when user click on the hardware back button
   * in the home page
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 1.0.0
   */
  private customHandleBackButton(): void {
    this.isLogoutConfirmDialogOpened = true
    let confirm = this.alertCtrl.create({
      enableBackdropDismiss: false,
      cssClass: 'custom-font',
      title: 'Warning',
      message: "क्या आप लॉग आउट करना चाहते हैं ?",
      buttons: [{
          text: 'नहीं',
          handler: () => {
            this.isLogoutConfirmDialogOpened = false
          }
        },
        {
          text: 'हाँ',
          handler: () => {
            this.viewCanLeave = true;
            this.messageProvider.showSuccessToast("सफलतापूर्वक लॉग आउट किया गया।")
            this.nav.setRoot('LoginPage')
          }
        }
      ]
    });
    confirm.present();
  }

  /**
   * Fired when you leave a page, before it stops being the active one
   * Unregister the hardware backbutton
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 1.0.0
   */
  ionViewWillLeave() {
    // Unregister the custom back button action for this page
    this.unregisterBackButtonAction && this.unregisterBackButtonAction();
  }

  ionViewDidLeave() {
    this.viewCanLeave = false;
  }

  deductDate(date: Date, notifyDay: number): string{

    let newDate = new Date(date)
    newDate.setDate(newDate.getDate() - (notifyDay+1))
    let revisedDate = new Date(newDate)
    return this.datePipe.transform(revisedDate, "dd-MM-yyyy")

  }

  addMonth(dates: Date): string{
    let newDates = new Date(dates)
    return this.datePipe.transform(newDates, "dd-MM-yyyy")
  }

  ionViewCanLeave() {
    if (this.viewCanLeave || this.userService.userTryingToLogout) {
      return true;
    } else {
      if (this.menuCtrl.isOpen()) {
        this.menuCtrl.close();
        if (!this.isLogoutConfirmDialogOpened) {
          this.customHandleBackButton();
        }
      } else {
        if (!this.isLogoutConfirmDialogOpened) {
          this.customHandleBackButton();
        }
      }
      return false;
    }
  }

}

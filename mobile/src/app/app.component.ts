import {
  Component,
  ViewChild
} from '@angular/core';
import {
  Platform,
  AlertController,
  Nav, Events
} from 'ionic-angular';
import {
  StatusBar
} from '@ionic-native/status-bar';
import {
  SplashScreen
} from '@ionic-native/splash-screen';
import {
  ApplicationPlatformImpl
} from '../class/ApplicationPlatformImpl';

import {
  MessageServiceProvider
} from '../providers/message-service/message-service';
import {
  ConstantProvider
} from '../providers/constant/constant';
import {
  SyncServiceProvider
} from '../providers/sync-service/sync-service';
import {
  Storage
} from '@ionic/storage'
import { UserServiceProvider } from '../providers/user-service/user-service';
import { LoginServiceProvider } from '../providers/login-service/login-service';
import { Network } from '@ionic-native/network';
import { ApplicationDetailsProvider } from '../providers/application/appdetails.provider';
import { QuestionServiceProvider } from '../providers/question-service/question-service';
import { HttpErrorResponse } from '@angular/common/http';
import { File } from '@ionic-native/file';
import { timer } from 'rxjs/observable/timer';

@Component({
  templateUrl: 'app.html'
})
export class MyApp {
  @ViewChild(Nav) nav: Nav;
  showSplash = true;
  rootPage: any;
  userdata: any;
  activeComponent: string;
  splitEnabled: boolean = false;

  constructor(public platform: Platform, public statusBar: StatusBar, public splashScreen: SplashScreen,
    private applicationDetailsProvider: ApplicationDetailsProvider, public messageProvider: MessageServiceProvider, public constantProvider: ConstantProvider,
    private alertCtrl: AlertController, public syncSerivice: SyncServiceProvider, public storage: Storage, private userService: UserServiceProvider,
    public loginService: LoginServiceProvider, public events: Events, public network: Network,
    private questionService: QuestionServiceProvider, private messageService: MessageServiceProvider,private file:File) {
    this.initializeApp();
  }

  initializeApp() {
    this.platform.ready().then(() => {
      // Okay, so the platform is ready and our plugins are available.
      // Here you can do any higher level native things you might need.
      this.rootPage = 'LoginPage'
      // this.statusBar.overlaysWebView(false);

      timer(5000).subscribe(() => this.showSplash = false);
      // set status bar to white
      this.statusBar.styleDefault();

      this.splashScreen.hide();
      // this.setupBackButtonBehavior ();
      //Setting platforms
      let applicationPlatform: ApplicationPlatform = new ApplicationPlatformImpl()

      if (this.platform.is('mobileweb')) {
        applicationPlatform.isMobilePWA = true
      } else if (this.platform.is('core')) {
        applicationPlatform.isWebPWA = true
      } else if (this.platform.is('android') && this.platform.is('cordova')) {
        // this.createProjectFolder()
         applicationPlatform.isAndroid = true
      }
      this.applicationDetailsProvider.setPlatform(applicationPlatform)

    });
  }

  ngOnInit() {
    this.events.subscribe('user', data => {
      if (data.username != undefined) {
        this.userdata = data.username;
      } else {
        this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then(data => {
          if (data) {
            this.userdata = data.user.username
          }
        })
        this.rootPage = 'LoginPage'
      }
    })
  }
  /**
   * This method will update all master forms and the user specific froms in local storage from server.
   *
   * @author Sourav Nath (souravnath@sdrc.co.in)
   */
  async updateForms() {
let areaList=null
    this.messageService.showLoader(ConstantProvider.message.formUpdating);
    this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then(async (item) => {
      if (item) {
        await this.getAreaForUser(item.tokens.accessToken, item.lastUpdatedDate).then(areas=>{
          areaList=areas
        })
        this.getAllFormsForUser(item.tokens.accessToken, item.lastUpdatedDate).then(async forms => {
          if (Object.keys(forms.allQuestions).length > 0) {
            let newObjForm = {};
            for (let key in item.getAllForm) {
              let newKey = key.split("_")[0];
              await this.getNewFormIndex(newKey, forms.allQuestions).then((index) => {
                if (index == -1) {
                  newObjForm[key] = item.getAllForm[key];
                } else {
                  newObjForm[index] = forms.allQuestions[index];
                }
              }).catch((error) => {
                console.log(error)
                //reject(error)
              });
            }
            for (let key in forms.allQuestions) {
              let newKey = key.split("_")[0];
              await this.getNewFormIndex(newKey, item.getAllForm).then((index) => {
                if (index == -1) {
                  newObjForm[key] = item.getAllForm[key];
                }
              }).catch((error) => {
                console.log(error)
                //reject(error)
              });
            }
            item.getAllForm = newObjForm;
            let area=(areaList.village !=undefined)?areaList:item.getAllAreaList;
            let userAndForm: IUserAndForm = {
              user: item.user,
              getAllForm: newObjForm,
              getAllAreaList:area,
              tokens: item.tokens,
              lastUpdatedDate: forms.lastUpdatedDate
            }
            this.messageService.stopLoader();


              await this.checkFormExists(item, forms).then(val => {
                if (val) {
                  let confirm = this.alertCtrl.create({
                    enableBackdropDismiss: false,
                    title: 'Confirmation',
                    message: "यह क्रिया मोबाइल ऐप से उन सभी डेटा को हटा देगी जो सर्वर पर सबमिट नहीं किए गए हैं। क्या आप वाकई अपडेट के साथ आगे बढ़ना चाहते हैं ?",
                    buttons: [{
                      text: 'रद्द करें',
                      handler: () => { }
                    },
                    {
                      text: 'अपडेट करें',
                      handler: () => {
                        this.saveData(userAndForm,item,forms)
                      }
                    }
                    ]
                  });
                  confirm.present();

                } else {
                  this.messageProvider.showSuccessToast(ConstantProvider.message.formUpdationSuccess)
                }

              }).catch((error) => {
                //reject(error)
              })

          } else {
            this.messageService.stopLoader();
            this.messageProvider.showSuccessToast(ConstantProvider.message.formUpdationNotFound)
          }
        }).catch((error) => {
          //reject(error)
        })
      }
    })
  }
  async saveData(userAndForm,item,forms){
    await this.userService.saveNewForms(userAndForm).then(async a => {

      let user = item.user["username"]
      this.storage.get("form-" + user).then(async (data) => {
        if (data) {
          for (let key in data) {
            let newKey = key.split("_")[0];
            await this.getNewFormIndex(newKey, forms.allQuestions).then((index) => {
              if (index !== -1) {
                delete data[key];
              }
            }).catch((error) => {
              console.log(error)
              //reject(error)
            });
          }
          this.userService.updateSaveAndFinalizeForm("form-" + user, data)
          this.messageProvider.showSuccessToast(ConstantProvider.message.formUpdationSuccess)
        }
      })
    }).catch((error) => {
      //reject(error)
    })
  }
  checkFormExists(item, newforms): Promise<any> {
    return new Promise<any>(async (resolve, reject) => {
      let user = item.user["username"];
      await this.storage.get("form-" + user).then(async (data) => {
        if (data) {
          for (let key in data) {
            let newKey = key.split("_")[0];
            await this.checkFormStatus(data[key]).then(async (status) => {
              if (status) {
                await this.getNewFormIndex(newKey, newforms.allQuestions).then((index) => {
                  if (index !== -1) {
                    resolve(true)
                  }
                }).catch((error) => {
                  resolve(true)
                });
              }
          }).catch((error) => {
            resolve(true)
          });
          }
        }
      })
      resolve(false)
    });
  }
  checkFormStatus(checkList): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      for (let key in checkList) {
        if (checkList[key].formStatus == "save" || checkList[key].formStatus == "finalized") {
          resolve(true)
          break
        }
      }
      resolve(false)
    });
  }
  getNewFormIndex(newKey, checkList): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      for (let key in checkList) {
        let oldKey = key.split("_")[0];
        if (oldKey == newKey) {
          resolve(key)
          break
        }
      }
      resolve(-1)
    });
  }
  async getAllFormsForUser(accessToken, lastUpdatedDate): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      this.questionService.getUpdateQuestionBank(null, accessToken, lastUpdatedDate).then(data => {
        resolve(data)
      })
        .catch((error) => {
          reject(error)
        })
    });
  }
  // areajson
  async getAreaForUser(accessToken, lastUpdatedDate): Promise<any> {
    return new Promise<any>((resolve, reject) => {
      this.questionService.getUpdatedArea(accessToken, lastUpdatedDate).then(data => {
        resolve(data)
      })
        .catch((error) => {
          reject(error)
        })
    });
  }
  async sync() {
    this.messageProvider.showLoader(ConstantProvider.message.syncingPleaseWait);
    try {
      let scount = await this.syncSerivice.synchronizeDataWithServer().then((sCount)=>{
        return sCount;
      })
      // let rcount =  await this.syncSerivice.getRejectedForms().then((rCount)=>{
      //   return rCount
      // })
      let rcount =0
      if((scount > 0 && rcount > 0) || (scount > 0 || rcount > 0)){
       this.messageService.stopLoader();
      //  this.messageService.showSuccessAlert("Info", "(" +scount +") Forms succesfully sent.<br>" + "(" +rcount + ") Forms rejected." )
      this.messageService.showSuccessAlert("Info", "सबमिट किए गए"+"(" +scount +")पंजीकरण फॉर्म को सर्वर पर सफलतापूर्वक सबमिट किया गया। <br>")
       this.events.publish('syncStatus', true)
      }else{
       this.messageService.stopLoader();
       this.messageService.showSuccessAlert("Info", "सर्वर को भेजे जाने के लिए कोई पंजीकरण फॉर्म उपलब्ध नहीं है।" )
      }
    } catch (error) {
      HttpErrorResponse
      console.log(error)
      // this.messageService.showErrorToast(JSON.stringify(error))
      if(error.status==0){
        this.messageService.stopLoader();
        this.messageService.showErrorToast(ConstantProvider.message.checkInternetConnection)

      }else if (error.status == 101) {
        this.messageService.stopLoader();
        this.messageService.showErrorToast("सिंक विफल। कृपया सिंक के दौरान हाई स्पीड इंटरनेट कनेक्टिविटी का उपयोग करें।")
        throw (error);
      }
      else if (error.status == 412) {
        this.messageService.stopLoader();
        this.messageService.showErrorToast(error.error)
        throw (error);
      } else if (error.status == 417) {
        this.messageService.stopLoader();
        this.messageService.showErrorToast(error.error)
        throw (error);
      } else if (error.status == 401) {
        this.messageService.stopLoader();
        this.messageService.showErrorToast(error.error)
        throw (error);
      }else if (!navigator.onLine) {
        this.messageService.stopLoader();
        this.messageService.showErrorToast(ConstantProvider.message.checkInternetConnection)
      } else {
        this.messageService.stopLoader();
        console.log(error)
        this.messageService.showErrorToast(error.message)
      }
      this.events.publish('syncStatus', true)
    }
  }

  logout() {
    let confirm = this.alertCtrl.create({
      enableBackdropDismiss: false,
      title: '&emsp;&emsp;<strong>&#9888;</strong> Warning',
      message: "<strong>क्या आप लॉग आउट करना चाहते हैं ?</strong>",
      cssClass: 'custom-font',
      buttons: [{
        text: 'नहीं',
        cssClass: 'cancel-button',
        handler: () => { }
      },
      {
        text: 'हाँ',
        cssClass: 'exit-button',
        handler: () => {
          this.userService.userTryingToLogout = true;
          let isLogoutClicked = `true`;
          // localStorage.setItem('isLogoutClicked', isLogoutClicked);
          this.messageProvider.showSuccessToast("सफलतापूर्वक लॉग आउट किया गया।")
          // if(!(this.platform.is('android') && this.platform.is('cordova'))){
          //   history.pushState(null,null,"/#/login");
          // }
          this.nav.setRoot('LoginPage')
        }
      }
      ]
    });
    confirm.present();
  }
  createProjectFolder() {

    //checking folder existance
    this.file.checkDir(this.file.externalRootDirectory, ConstantProvider.appFolderName)
      .catch(err => {
        if (err.code === 1) {
          // folder not present, creating new folder
          this.file.createDir(this.file.externalRootDirectory, ConstantProvider.appFolderName, false)
            .then(data => {
            })
            .catch(err => {
              this.messageService.showErrorToast(err.message)
            })
        }
      })
  }

  menuOpened() {
    this.userService.userTryingToLogout = false;
  }
}

import { timer } from 'rxjs/observable/timer';
import { HttpClient } from '@angular/common/http';
import {
  Component,
  HostListener,
  ViewChild
} from '@angular/core';
import {
  IonicPage,
  NavController,
  NavParams,
  Platform,
  Events,
  AlertController,
  MenuController
} from 'ionic-angular';
import {
  MessageServiceProvider
} from '../../providers/message-service/message-service';
import {
  AppVersion
} from '@ionic-native/app-version';
import {
  UserServiceProvider
} from '../../providers/user-service/user-service';
import {
  LoginServiceProvider
} from '../../providers/login-service/login-service';

import {
  QuestionServiceProvider
} from '../../providers/question-service/question-service';
import {
  UtilServiceProvider
} from '../../providers/util-service/util-service';
import {
  Storage
} from '@ionic/storage'
import {
  ConstantProvider
} from '../../providers/constant/constant';
import {
  ApplicationDetailsProvider
} from '../../providers/application/appdetails.provider';

/**
 * This is used for Login page
 *
 * @author Jagat Bandhu (jagat@sdrc.co.in)
 * @since 0.0.1
 */
@IonicPage()
@Component({
  selector: 'page-login',
  templateUrl: 'login.html',
})
export class LoginPage {
  @HostListener('window:popstate', ['$event'])
  onbeforeunload(event) {
    if (window.location.href.substr(window.location.href.length - 5) == 'login') {
      history.pushState(null, null, "" + window.location.href);
    }
  }

  // @ViewChild('searchInput') sInput;

  loginData: ILoginData;
  appVersionNumber: string;
  type: string = 'password';
  showPass: boolean = false;
  connectSubscription: any;
  isWeb: boolean = false;
  rememberMe = false;

  constructor(public navCtrl: NavController, public navParams: NavParams, private loginService: LoginServiceProvider,private http:HttpClient,
    private messageService: MessageServiceProvider, private userService: UserServiceProvider, public applicationDetailsProvider: ApplicationDetailsProvider, public messageProvider: MessageServiceProvider,
    private appVersion: AppVersion, private platform: Platform, private events: Events, public questionService: QuestionServiceProvider,
    public uitlService: UtilServiceProvider, private alertCtrl: AlertController, private storage: Storage, private menuCtrl: MenuController) {
    // this.platform.ready().then((readySource) => {
      // if (this.platform.is('android') && this.platform.is('cordova')) {
      //   this.appVersion.getVersionNumber()
      //     .then(data => {
      //       this.appVersionNumber = data
      //     })
      // }
      // this.appVersionNumber = this.applicationDetailsProvider.getAppVersionName()
    // });
  }

  ionViewDidLoad(){
    // console.log("ionViewDidLoad");
    // setTimeout(() => {
    //   this.sInput.setFocus();
    // }, 300);
  }

  /**
   * This method will initilize the username and password variables.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   */
  ngOnInit() {

    this.isWeb = this.applicationDetailsProvider.getPlatform().isWebPWA
    this.loginData = {
      username: '',
      password: ''
    }

  }

  rememberMeWork() {
    // this.messageProvider.showLoader(ConstantProvider.message.pleaseWait)
    this.loginService.getRememberMe().then((rememberMe) => {
      if (rememberMe) {
        this.userService.getUserAndForm().then((userAndForm) => {
          if (userAndForm != undefined && userAndForm != null) {
            this.loginData.username = userAndForm['user'].username;
            this.loginData.password = userAndForm['user'].password;
          }
          this.rememberMe = true;
          // this.messageProvider.stopLoader();
        }).catch(()=>{
          // this.messageProvider.stopLoader();
        });
      } else {
        // this.messageProvider.stopLoader();
      }
    });
  }

  rememberMeChanged() {
    if (this.rememberMe) {
      this.loginService.addRememberMe();
    } else {
      this.loginService.removeRememberMe();
    }
  }



  /**
   * This method is called when user clicks on login button, this method checks for valid username and password.
   * If user has given valid credentials then it checks for user is exsit or not. If user exsit then redirect to
   * login page or else call saveUser().
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  async login() {
    this.messageProvider.showLoader(ConstantProvider.message.pleaseWait)
    if (this.loginData.username == "") {
      this.messageProvider.stopLoader()
      this.messageService.showErrorToast(ConstantProvider.message.validUserName)
    } else if (this.loginData.password == "") {
      this.messageProvider.stopLoader()
      this.messageService.showErrorToast(ConstantProvider.message.validPassword)
    } else {
      let userAndForm = await this.userService.getUserAndForm()
      // console.log("loggininn", userAndForm)
      if (userAndForm == undefined || userAndForm == null) {
        this.getBlankFormsAndSaveUserDetailsAndTokens(this.loginData);
      } else {
        if (userAndForm && userAndForm['user'].username == this.loginData.username.trim()
        && userAndForm['user'].password == this.loginData.password.trim()) {

          await this.userService.initializeUserInService();
          this.messageProvider.stopLoader()
          this.navCtrl.setRoot('HomePage');
        } else {
          this.getBlankFormsAndSaveUserDetailsAndTokens(this.loginData);
        }
      }
    }
  }


  async getBlankFormsAndSaveUserDetailsAndTokens(loginInputs: ILoginData) {
    await this.loginService.authenticate(this.loginData)
      .then((data) => {
        let tokens = data
        let user = loginInputs
        this.userService.accessToken = tokens.accessToken
        this.userService.refreshToken = tokens.refreshToken
        this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then(data => {
          if (data) {
            this.messageProvider.stopLoader()
            this.clearDbAlert(ConstantProvider.message.dataClearMsg, "Warning").then((data) => {
              if (!data) {
                return new Promise < any > ((resolve, reject) => {

                        // this.getAllFormsForUser(tokens.accessToken, ConstantProvider.lastUpdatedDate).then(forms => {
                          this.getAllFormsForUserFromJson().then(forms => {
                          if (forms) {
                            this.getAllAreaFromJson().then(areas => {
                              if (areas) {
                                let userAndForm: IUserAndForm = {
                                  user: user,
                                  getAllForm: forms.allQuestions,
                                  getAllAreaList:areas,
                                  tokens: tokens,
                                  lastUpdatedDate: forms.lastUpdatedDate
                                }
                                this.userService.saveUserFormAndTokensAndPublishUser(userAndForm).then(a => {
                                  this.messageService.stopLoader();
                                  this.navCtrl.setRoot('HomePage');
                                }).catch((error) => {
                                  reject(error)
                                })
                              }
                            })

                          }
                        }).catch((error) => {
                          reject(error)
                        })

                      // area

                })
              } else {
                this.messageService.showErrorToast(ConstantProvider.message.errorWhileClearingFile)
              }
            });
          } else {
            return new Promise < any > ((resolve, reject) => {
              // this.questionService.getDeadlineDate(tokens.accessToken).then(data => {
              //   if (data) {
              //     this.storage.set(ConstantProvider.dbKeyNames.deadLineDate, data).then(() => {
                  this.messageProvider.stopLoader()

                    // this.getAllFormsForUser(tokens.accessToken, ConstantProvider.lastUpdatedDate).then(forms => {
                      this.getAllFormsForUserFromJson().then(forms => {
                        if (forms) {
                          this.getAllAreaFromJson().then(areas => {
                            if (areas) {
                              let userAndForm: IUserAndForm = {
                                user: user,
                                getAllForm: forms.allQuestions,
                                getAllAreaList:areas,
                                tokens: tokens,
                                lastUpdatedDate: forms.lastUpdatedDate
                              }
                              this.userService.saveUserFormAndTokensAndPublishUser(userAndForm).then(a => {
                                this.messageService.stopLoader();
                                this.navCtrl.setRoot('HomePage');
                              }).catch((error) => {
                                reject(error)
                              })
                            }
                          }).catch((error) => {
                            reject(error)
                          })

                        }
                    }).catch((error) => {
                      // console.log("inner catch", error)
                      reject(error)
                    })
                  // });
              //   }
              // })
            })
          }
        });
      })
      .catch((error) => {
        this.messageService.stopLoader();
        // console.log("outer catch", error)
        if (error.status == 400) {
          this.messageService.showErrorToast(ConstantProvider.message.invalidUserNameOrPassword)
        } else if (!navigator.onLine) {
          this.messageService.showErrorToast(ConstantProvider.message.checkInternetConnection)
        } else if (error.status == 500) {
          this.messageProvider.showErrorToast(error.message)
          // console.log(error)
        } else if (error.status == 0) {
          this.messageService.showErrorToast(ConstantProvider.message.serverError)
        } else if (error.status == 401) {
          this.messageService.showErrorToast(ConstantProvider.message.invalidUserNameOrPassword)
        } else {
          this.messageService.showErrorToast("सर्वर त्रुटि (अज्ञात)!")
          // console.log(error)401
        }
      })

  }


  /**
   * This method will show a alert message for forgot password
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   */
  forgotPassword() {
    // this.messageService.showOkAlert(ConstantProvider.messages.info,ConstantProvider.messages.forgotPasswordMessage);
  }


  /**
   * This method will show/hide the password to the user
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   */
  showPassword() {
    this.showPass = !this.showPass;

    if (this.showPass) {
      this.type = 'text';
    } else {
      this.type = 'password';
    }
  }

  /**
   * This method will call, when user clicks the enter key from keyboard after giving the username and password.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param event
   */
  _runScript(event: any) {
    if (event.keyCode == 13) {
      this.login();
    }
  }

  /**
   * This method will called, after succesfull authentication, to get the all user specific forms.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   */
  async getAllFormsForUser(accessToken,lastUpdatedDate): Promise < any > {
    // await this.loginService.getUserDetails()
    // passing null to fetch all forms from server

    return new Promise < any > ((resolve, reject) => {
      this.questionService.getQuestionBank(null, accessToken,lastUpdatedDate).then(data => {
          resolve(data)
        })
        .catch((error) => {
          // console.log(error)
          //  throw Error(error)
          reject(error)
        })
    });
  }

  async getAllFormsForUserFromJson(): Promise < any > {
    // await this.loginService.getUserDetails()
    // passing null to fetch all forms from server
    return this.http.get('assets/questions/questions.json').toPromise();
  }

  async getAllAreaFromJson(): Promise < any > {
    // await this.loginService.getUserDetails()
    // passing null to fetch all forms from server
    return this.http.get('assets/questions/area.json').toPromise();
  }

  ionViewDidEnter() {
    this.menuCtrl.swipeEnable(false);
    this.events.publish("navController:current", this.navCtrl);
    this.rememberMeWork();
  }

  /**
   * This method will called to show the custom alert to user for clearing the data of exsiting user.
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param message
   * @param title
   */
  private clearDbAlert(message: string, title: string): Promise < any > {
    let username;
    return new Promise < any > ((resolve, reject) => {
      let alert = this.alertCtrl.create({
        title: title,
        cssClass: '',
        message: message,
        buttons: [{
          text: 'Cancel'
        }, {
          text: 'OK',
          handler: () => {
            this.messageService.showLoader(ConstantProvider.message.clearingDataPleaseWait);
            this.storage.remove(ConstantProvider.dbKeyNames.deadLineDate).then(async data => {
              if (!data) {
                await this.storage.get(ConstantProvider.dbKeyNames.userAndForm).then(async userAndForm => {
                    username = userAndForm.user.username
                    await this.storage.remove(ConstantProvider.dbKeyNames.form + "-" + username).then(data => {
                      if (!data) {
                        this.storage.remove(ConstantProvider.dbKeyNames.userAndForm).then(async data => {
                          if (!data) {
                            await this.messageService.stopLoader()
                            if ('serviceWorker' in navigator) {
                              let temp = async function (this_temp) {
                                // console.log("Login data" + this_temp.loginData)
                                let registrations = await navigator.serviceWorker.getRegistrations()
                                for (let registration of registrations) {
                                  registration.unregister()
                                  // console.log("Service worker unregistered.")
                                }
                              }
                              temp(this)
                            }
                            resolve(data)
                          } else {
                            this.messageService.stopLoader()
                            reject(data)
                          }
                        });
                      } else {
                        this.messageService.stopLoader()
                        reject(data)
                      }
                    });
                });
              } else {
                this.messageService.stopLoader()
                reject(data)
              }
            });
          }
        }]
      });
      alert.present();
    });
  }
  keyUpCheckerUname(){
    this.loginData.username= this.loginData.username.trim();
    }
    keyUpCheckerPwd(){
      this.loginData.password= this.loginData.password.trim();
    }
    showHideMsg1=true;
    time: number = 0;
    checkregex() {
      if (this.loginData.username !=null && !this.loginData.username.match(/^[a-zA-Z0-9]+$/) && this.loginData.username != "") {
        this.loginData.username= "";
        if(this.showHideMsg1==true){
          this.showHideMsg1 = false
          this.messageService.showErrorToast("कृपया केवल अक्षर और संख्या दर्ज करें")
        }
        timer(3000).subscribe(() => this.showHideMsg1 = true);
        // setTimeout(function() {
        //   console.log(this.time)
        //   this.showHideMsg1=true
        //   this.time++
        // }, 3000);

      }
  }
}

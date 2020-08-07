import { Injectable } from '@angular/core';
import { LoadingController, ToastController, AlertController, Loading } from 'ionic-angular';

/*
  Generated class for the MessageServiceProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class MessageServiceProvider {
  loading: Loading;
  constructor(private toastCtrl: ToastController, private loadingCtrl: LoadingController,
    public alertCtrl :  AlertController) {
  }

  /**
   * This method will be used to show success toast to user
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param message The message we want to show the user
   * @since 0.0.1
   */
  async showSuccessToast(message: string){
    let toast = await this.toastCtrl.create({
      cssClass: "su",
      message: message,
      duration: 3000
    });
    toast.present();
  }

  /**
   * This method will be used to show error toast to user
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param message The message we want to show the user
   * @since 0.0.1
   */
  async showErrorToast(message: string){
    let toast = await this.toastCtrl.create({
      cssClass: "er",
      message: message,
      showCloseButton: true,
      duration: 3000
    });
    toast.present();
  }

  /**
   * This method will display loader above the page which is being rendered
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param message The message which we want to show the user
   * @since 0.0.1
   */
  showLoader(message: string) {
    this.loading = this.loadingCtrl.create({
      spinner: 'crescent',
      content: message,
      cssClass: "custom-font",
    });

    this.loading.present();
  }

  /**
   * This method will stop the showing  loader above the page
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   */
  stopLoader(){
    this.loading.dismiss();
  }

  showSuccessAlert(title: string, message: string): Promise<boolean>{
    let promise: Promise<boolean> = new Promise((resolve, reject)=>{
      let confirm = this.alertCtrl.create({
        enableBackdropDismiss: false,
        cssClass: 'custom-font',
        title: title,
        message: message,
        buttons: [
          {
            text: 'Ok',
            handler: () => {
              resolve()
            }
          }]
      });
      confirm.present();
    })
    return promise;
  }

  showAlert(title: string, message: string): Promise<boolean>{
    let promise: Promise<boolean> = new Promise((resolve, reject)=>{
      let confirm = this.alertCtrl.create({
        enableBackdropDismiss: false,
        cssClass: 'custom-font',
        title: title,
        message: message,
        buttons: [
          {
            text: 'No',
            handler: () => {
              resolve(false)
            }
          },
          {
            text: 'Yes',
            handler: () => {
              resolve(true)
            }
          }
        ]
      });
      confirm.present();
    })
    return promise;
  }

}

import { Injectable } from '@angular/core';
import { AlertController } from 'ionic-angular';
import { ConstantProvider } from '../constant/constant';
import { MessageServiceProvider } from '../message-service/message-service';
import { Storage } from '@ionic/storage'
import { UserServiceProvider } from '../user-service/user-service';
import { DatePipe } from '@angular/common';

/*
  Generated class for the FormServiceProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class FormServiceProvider {
  formDataTransfer: Map < String, Array < Map < String, Array < IQuestionModel >>> > = new Map();
  uniquueIdForNewRecord: String;
  constructor(private alertCtrl: AlertController, public constantService: ConstantProvider, public messageService: MessageServiceProvider,
    public storage: Storage, public userService: UserServiceProvider, private datePipe: DatePipe) {}

  /**
   * This method is going to show the alert after submitting the form
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1 *
   * @private
   * @memberof SyncServiceProvider
   */
  public showAlert(message: string, title: string) {
    let alert = this.alertCtrl.create({
      title: title,
      cssClass: '',
      message: message,
      buttons: [{
        text: 'OK',
        handler: () => {}
      }]
    });
    alert.present();
  }

  /**
   *
   * This method  will  save the from data in local database
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @since 0.0.1
   */
  async saveData(formId: any, dataModel: any, type: any) {
    let checkCount: boolean = false;
    let formModel: {} = {}
    let mainFormsDataforSave: {} = {};
    let promise = new Promise((resolve, reject) => {
      // console.log('formservice',this.userService.user)
      this.storage.get( ConstantProvider.dbKeyNames.form +"-" +this.userService.user.username)
        .then((val) => {
          loop1: if (val != null) {
            mainFormsDataforSave = val
            if ((mainFormsDataforSave[formId])) {
              if (type == 'new' && Object.keys(mainFormsDataforSave[formId]).length > 0) {
                for (let i = 0; i < Object.keys(mainFormsDataforSave[formId]).length; i++) {
                  if (Object.keys(mainFormsDataforSave[formId])[i] == dataModel.facilityName) {
                    checkCount = true
                    switch (formId) {
                      case 1:
                        this.messageService.showErrorToast("Submission for the anganwadi already exist")
                        break;
                      case 2:
                        this.messageService.showErrorToast("Submission for the DISE CODE already exist")
                        break;
                      case 3:
                        this.messageService.showErrorToast("Submission for the house hold survey already exist")
                        break;
                    }
                    resolve(null)
                    break loop1;
                  }
                }
                if (!checkCount) {
                  formModel = (mainFormsDataforSave[formId])
                  dataModel.createdDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy')+" "+this.datePipe.transform(new Date(), 'HH:mm:ss')
                  dataModel.updatedDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy')+" "+this.datePipe.transform(new Date(), 'HH:mm:ss')
                  // dataModel.uniqueId = (Number(Object.keys(mainFormsDataforSave[formId])[Object.keys(mainFormsDataforSave[formId]).length-1])+1)
                  formModel[dataModel.uniqueId] = dataModel
                  mainFormsDataforSave[formId] = formModel
                }
              } else if (type == 'old') {
                formModel = (mainFormsDataforSave[formId])
                dataModel.updatedDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy')+" "+this.datePipe.transform(new Date(), 'HH:mm:ss')
                formModel[dataModel.uniqueId] = dataModel
                mainFormsDataforSave[formId] = formModel
              } else {
                dataModel.createdDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy')+" "+this.datePipe.transform(new Date(), 'HH:mm:ss')
                dataModel.updatedDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy')+" "+this.datePipe.transform(new Date(), 'HH:mm:ss')
                formModel[dataModel.uniqueId] = dataModel
                mainFormsDataforSave[formId] = formModel
              }
            } else {
              dataModel.createdDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy')+" "+this.datePipe.transform(new Date(), 'HH:mm:ss')
              dataModel.updatedDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy')+" "+this.datePipe.transform(new Date(), 'HH:mm:ss')
              formModel[dataModel.uniqueId] = dataModel
              mainFormsDataforSave[formId] = formModel
            }
          } else {
            dataModel.createdDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy')+" "+this.datePipe.transform(new Date(), 'HH:mm:ss')
            dataModel.updatedDate = this.datePipe.transform(new Date(), 'dd-MM-yyyy')+" "+this.datePipe.transform(new Date(), 'HH:mm:ss')
            formModel[dataModel.uniqueId] = dataModel
            mainFormsDataforSave[formId] = formModel
          }
        })
        .then(() => {
          this.storage.set( ConstantProvider.dbKeyNames.form +"-" +this.userService.user.username, mainFormsDataforSave)
            .then(data => {
              resolve('data')
            })
            .catch(err => {
              formModel[0].value = null
            })
        })
    })
    return promise;
  }
}

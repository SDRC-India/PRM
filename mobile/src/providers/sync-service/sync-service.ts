import {
  HttpClient,
  HttpHeaders
} from '@angular/common/http';
import {

  Injectable
} from '@angular/core';
import {
  Storage
} from '@ionic/storage'
import {
  ConstantProvider
} from '../constant/constant';
import {
  MessageServiceProvider
} from '../message-service/message-service';
import {
  UserServiceProvider
} from '../user-service/user-service';
import {
  Events
} from 'ionic-angular';
import {
  DatePipe
} from '@angular/common';
import {
  File, IWriteOptions
} from '@ionic-native/file';
import {
  ApplicationDetailsProvider
} from '../application/appdetails.provider';
import { CommonsEngineProvider } from '../commons-engine/commons-engine';

/*
  Generated class for the SyncServiceProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/
@Injectable()
export class SyncServiceProvider {

  isWeb: any;
  username: string;
  constructor(public http: HttpClient, public storage: Storage, public constantService: ConstantProvider, public events: Events,
    public messageService: MessageServiceProvider, public userService: UserServiceProvider,
    private datePipe: DatePipe, private file: File, private commonEngine: CommonsEngineProvider, private applicationDetailsProvider: ApplicationDetailsProvider) { }


  b64toBlob(b64Data, contentType, sliceSize?) {
    contentType = contentType || '';
    sliceSize = sliceSize || 512;

    var byteCharacters = atob(b64Data);
    var byteArrays = [];

    for (var offset = 0; offset < byteCharacters.length; offset += sliceSize) {
      var slice = byteCharacters.slice(offset, offset + sliceSize);

      var byteNumbers = new Array(slice.length);
      for (var i = 0; i < slice.length; i++) {
        byteNumbers[i] = slice.charCodeAt(i);
      }

      var byteArray = new Uint8Array(byteNumbers);

      byteArrays.push(byteArray);
    }

    var blob = new Blob(byteArrays, {
      type: contentType
    });
    return blob;
  }

  async getUsrName() {
    let userAndForm = await this.userService.getUserAndForm()
    this.username = userAndForm['user'].username
  }

  dataURItoBlob(dataURI, type) {
    // convert base64 to raw binary data held in a string
    var byteString = atob(dataURI.split(',')[1]);

    // separate out the mime component
    // var mimeString = dataURI.split(',')[0].split(':')[1].split(';')[0]

    // write the bytes of the string to an ArrayBuffer
    var ab = new ArrayBuffer(byteString.length);
    var ia = new Uint8Array(ab);
    for (var i = 0; i < byteString.length; i++) {
      ia[i] = byteString.charCodeAt(i);
    }

    // write the ArrayBuffer to a blob, and you're done
    var bb = new Blob([ab], {
      type: type
    });
    return bb;
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
          return opt;

        }
      };
    } else {
      return null;
    }
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


  async synchronizeDataWithServer() {
    let sentCount = 0;
    this.isWeb = this.applicationDetailsProvider.getPlatform().isWebPWA
    await this.getUsrName();
    let schema = await this.commonEngine.getSchemaDefinationInKeyValueFormat();
    let accessToken = this.userService.accessToken;
    let d = await this.storage.get(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username)

    if (d) {
      let forms = d
      for (let form = 0; form < Object.keys(forms).length; form++) {

        let submittedForms = forms[Object.keys(forms)[form]]

        for (let submittedForm = 0; submittedForm < Object.keys(submittedForms).length; submittedForm++) {

          let UUID = Object.keys(submittedForms)[submittedForm]

          let data = submittedForms[UUID];

          if (data.formStatus != "finalized") {
            continue
          }

          let keyValueData = data.formData;

          let formSchema = schema[data.formId]

          let dataWithSchemaDef = await this.commonEngine.loadDataIntoSchemaDef(formSchema, keyValueData)

          let preparedKeyValueData = await this.prepareKeyValueDataMap(dataWithSchemaDef)

          let attachmentCount = await this.getAttachmentCount(dataWithSchemaDef)

          const httpOptions = {
            headers: new HttpHeaders({
              'Authorization': 'Bearer ' + accessToken
            })
          };
          let submissionDataTemp = {};
          preparedKeyValueData.forEach((value, key) => {
            submissionDataTemp[(key as any)] = value
          })
          let syncDataModel = {
            formId: Number(data.formId.split("_")[0]),
            createdDate: data.createdDate,
            updatedDate: data.updatedDate,
            uniqueId: data.uniqueId,
            submissionData: submissionDataTemp,
            attachmentCount: attachmentCount
          }

          let submissionId = (await this.http.post(ConstantProvider.baseUrl + 'api/saveData', syncDataModel, httpOptions).timeout(30000).toPromise())

          if (attachmentCount > 0) {
            // start sending camera images and file attachements
            let filesSent = 0;
            for (let index = 0; index < Object.keys(dataWithSchemaDef).length; index++) {
              for (let j = 0; j < dataWithSchemaDef[Object.keys(dataWithSchemaDef)[index]].length; j++) {
                let subSections = dataWithSchemaDef[Object.keys(dataWithSchemaDef)[index]][0]
                for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
                  for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
                    let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]

                    switch (question.controlType) {

                      case "camera":
                        if (keyValueData[question.columnName]) {
                          if (question.value) {
                            if (this.applicationDetailsProvider.getPlatform().isAndroid) {
                              await this.sendImageOrFileOfAndriodToServer(keyValueData[question.columnName], question.controlType, question.columnName,
                                submissionId, data.formId.split("_")[0], accessToken)
                              filesSent += 1
                            } else {
                              await this.sendImageOrFileForWebPWAToServer(keyValueData[question.columnName], question.controlType, question.columnName,
                                submissionId, data.formId.split("_")[0], accessToken)
                              filesSent += 1
                            }
                          }
                        }

                        break;
                      case "file":
                      case "mfile":
                        if (keyValueData[question.columnName]) {
                          if (question.attachmentsInBase64.length > 0) {
                            if (this.applicationDetailsProvider.getPlatform().isAndroid) {
                              await this.sendImageOrFileOfAndriodToServer(keyValueData[question.columnName], question.controlType, question.columnName,
                                submissionId, data.formId.split("_")[0], accessToken)
                              filesSent += 1
                            }
                            else {
                              // FOR WEBPWA THIS CODE IS EXECUTED.
                              // NOTE : FOR MOBILE PWA THIS CODE CAN THRROW ERROR.
                              await this.sendImageOrFileForWebPWAToServer(keyValueData[question.columnName], question.controlType, question.columnName,
                                submissionId, data.formId.split("_")[0], accessToken)
                              filesSent += 1
                            }
                          }
                        }
                        break;
                      case "beginrepeat":
                        let beginrepeat = question.beginRepeat
                        for (let i = 0; i < beginrepeat.length; i++) {
                          for (let j = 0; j < beginrepeat[i].length; j++) {
                            switch (beginrepeat[i][j].controlType) {
                              case "camera":
                                if (beginrepeat[i][j].value) {
                                  if (this.applicationDetailsProvider.getPlatform().isAndroid) {
                                    await this.sendImageOrFileOfAndriodToServer(keyValueData[beginrepeat[i][j].columnName], beginrepeat[i][j].controlType, beginrepeat[i][j].columnName,
                                      submissionId, data.formId.split("_")[0], accessToken)
                                    filesSent += 1
                                  } else {
                                    await this.sendImageOrFileForWebPWAToServer(keyValueData[beginrepeat[i][j].columnName], beginrepeat[i][j].controlType, beginrepeat[i][j].columnName,
                                      submissionId, data.formId.split("_")[0], accessToken)
                                    filesSent += 1
                                  }
                                }
                                break;
                              case "file":
                              case "mfile":
                                if (beginrepeat[i][j].attachmentsInBase64.length > 0) {
                                  if (this.applicationDetailsProvider.getPlatform().isAndroid) {
                                    await this.sendImageOrFileOfAndriodToServer(keyValueData[beginrepeat[i][j].columnName], beginrepeat[i][j].controlType, beginrepeat[i][j].columnName,
                                      submissionId, data.formId.split("_")[0], accessToken)
                                    filesSent += 1
                                  }
                                  else {
                                    await this.sendImageOrFileForWebPWAToServer(keyValueData[beginrepeat[i][j].columnName], beginrepeat[i][j].controlType, beginrepeat[i][j].columnName,
                                      submissionId, data.formId.split("_")[0], accessToken)
                                    filesSent += 1
                                  }
                                }
                                break;
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

          //update status of model to sent
          if (submissionId) {
            data.formStatus = "sent";
            await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username, d)
            sentCount += 1
          }


        }
      }
    }
    return sentCount
  }

  async sendImageOrFileForWebPWAToServer(dbKey, controlType, questionColumnName, submissionId, formId, accessToken) {


    let originalName;
    let extentionType;

    await this.storage.get(dbKey).then((async fileOrImage => {

      switch (controlType) {
        case "camera":
          if (fileOrImage['src']) {
            const formdata: FormData = new FormData();
            let cameraData = fileOrImage.src;
            let myBaseString = cameraData;
            let block = myBaseString.split(";");
            let dataType = block[0].split(":")[1];
            let currentTime = +new Date();
            let random = Math.random()
            let trends = random + ""
            let filename = trends.replace('.', '') + '_' + questionColumnName + '_' + currentTime + ".jpg";
            originalName = filename;
            extentionType = dataType
            let blobdata = this.dataURItoBlob(cameraData, dataType)

            let imageParameter = {
              columnName: questionColumnName,
              submissionId: submissionId,
              formId: Number(formId),
              originalName: originalName,
              fileExtension: filename.split(".")[filename.split(".").length - 1],
            }
            formdata.append('fileModel', JSON.stringify(imageParameter))


            extentionType = "image/" + extentionType
            let file: Blob = new Blob([blobdata], {
              type: extentionType
            });
            formdata.append('file', file, originalName);

            await this.http.post(ConstantProvider.baseUrl + 'api/uploadFile', formdata, {
              reportProgress: true,
              responseType: 'json',
              headers: new HttpHeaders({
                'Authorization': 'Bearer ' + accessToken
              })
            }).retry(3).toPromise().catch(error => {
              throw error
            });
          }

          break;
        case "file":
        case "mfile":
          {
            for (let index = 0; index < fileOrImage.length; index++) {
              const formdata: FormData = new FormData();
              let fileData = fileOrImage[index].base64;
              let dataType = "application/" + fileOrImage[index].fileType;
              let blobdata = this.dataURItoBlob(dataType + "," + fileData, dataType)

              originalName = fileOrImage[index].fileName
              extentionType = fileOrImage[index].fileType

              let fileParameters = {
                columnName: questionColumnName,
                submissionId: submissionId,
                formId: Number(formId),
                originalName: originalName,
                fileExtension: extentionType,
              }
              formdata.append('fileModel', JSON.stringify(fileParameters))

              if (extentionType == 'jpg' || extentionType == 'png' || extentionType == 'jpeg') {
                extentionType = "image/" + extentionType
              } else {
                extentionType = "application/" + extentionType
              }
              let file: Blob = new Blob([blobdata], {
                type: dataType
              });
              formdata.append('file', file, originalName);

              await this.http.post(ConstantProvider.baseUrl + 'api/uploadFile', formdata, {
                reportProgress: true,
                responseType: 'json',
                headers: new HttpHeaders({
                  'Authorization': 'Bearer ' + accessToken
                })
              }).retry(3).toPromise().catch(error => {
                throw error
              });
            }
          }
      }
    }))



  }


  async sendImageOrFileOfAndriodToServer(dbKey, controlType, questionColumnName, submissionId, formId, accessToken) {

    await this.storage.get(dbKey).then((async fileOrImage => {

      switch (controlType) {
        case "camera":
          if (fileOrImage) {
            // If the submission is new i.e not reverse synced, in db we have NativeFilePath of Android device.
            // We get the base64 encoded string reading the image from file Path
            for (let index = 0; index < fileOrImage.length; index++) {
              const formdata: FormData = new FormData();
              let filePath = fileOrImage[index]
              let cameraData;
              let fileName = filePath.substr(filePath.lastIndexOf('/') + 1)
              let baseImagePath = filePath.substr(0, filePath.lastIndexOf('/') + 1);
              cameraData = await this.file.readAsDataURL(baseImagePath, fileName).then(data => {
                return data
              })
              let contentType = await this.commonEngine.getContentType(cameraData);
              let blobdata = await this.dataURItoBlob(cameraData, contentType)

              let imageParameter = {
                columnName: questionColumnName,
                submissionId: submissionId,
                formId: Number(formId),
                originalName: fileName,
                fileExtension: fileName.split(".")[fileName.split(".").length - 1],
              }
              formdata.append('fileModel', JSON.stringify(imageParameter))

              let file: Blob = new Blob([blobdata], {
                type: contentType
              });
              formdata.append('file', file, fileName);

              await this.http.post(ConstantProvider.baseUrl + 'api/uploadFile', formdata, {
                reportProgress: true,
                responseType: 'json',
                headers: new HttpHeaders({
                  'Authorization': 'Bearer ' + accessToken
                })
              }).retry(3).toPromise().catch(error => {
                console.log(error);
                throw error
              });
            }
          }
          break;
        case "file":
        case "mfile":
          {

            for (let index = 0; index < fileOrImage.length; index++) {
              const formdata: FormData = new FormData();
              let originalName;

              let fileData = await this.file.readAsDataURL(fileOrImage[index].basePath, fileOrImage[index].pathFileName).then(data => {
                return data
              })
              let contentType = await this.commonEngine.getContentType(fileData);
              
              let blobdata = await this.dataURItoBlob(fileData, contentType)

              originalName = fileOrImage[index].fileName

              let fileParameters = {
                columnName: questionColumnName,
                submissionId: submissionId,
                formId: Number(formId),
                originalName: originalName,
                fileExtension: (originalName.split(".")[originalName.split(".").length - 1]).toLowerCase()
              }
              formdata.append('fileModel', JSON.stringify(fileParameters))

              let file: Blob = new Blob([blobdata], {
                type: contentType
              });
              formdata.append('file', file, originalName);

              await this.http.post(ConstantProvider.baseUrl + 'api/uploadFile', formdata, {
                reportProgress: true,
                responseType: 'json',
                headers: new HttpHeaders({
                  'Authorization': 'Bearer ' + accessToken
                })
              }).retry(3).toPromise().catch(error => {
                console.log(error);

                throw error
              });
            }
          }
      }
    }))




  }

  prepareKeyValueDataMap(sectionMap) {

    let serverData = new Map()
    for (let index = 0; index < Object.keys(sectionMap).length; index++) {
      for (let j = 0; j < sectionMap[Object.keys(sectionMap)[index]].length; j++) {
        let subSections = sectionMap[Object.keys(sectionMap)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
            switch (question.controlType) {
              case "geolocation":
                serverData.set(question.columnName, question.value);
                break;
              case "textbox":
                if (question.type == 'tel' && question.value != null) {
                  if (question.constraints && question.constraints == 'maxScore' && question.displayComponent == false) {
                    question.value = null
                    serverData.set(question.columnName, question.value)
                  } else {
                    serverData.set(question.columnName, Number(question.value))
                  }
                } else {
                  serverData.set(question.columnName, question.value)
                }
                break;
              case "textarea":
                serverData.set(question.columnName, question.value)
                break;
              case "dropdown":
                serverData.set(question.columnName, question.value)
                break;
              case "checkbox":
                serverData.set(question.columnName, question.value)
                break;
              case "autoCompleteTextView":
                serverData.set(question.columnName, question.value)
                break;
              case "autoCompleteMulti":
                serverData.set(question.columnName, question.value)
                break;
              case "Time Widget":
                serverData.set(question.columnName, question.value)
                break;
              case "score-holder":
                if (question.type == 'tel' && question.value != null) {
                  serverData.set(question.columnName, Number(question.value))
                } else {
                  serverData.set(question.columnName, question.value)
                }
                break;
              case "score-keeper":
                if (question.type == 'tel' && question.value != null) {
                  serverData.set(question.columnName, Number(question.value))
                } else {
                  serverData.set(question.columnName, question.value)
                }
                break;
              case "sub-score-keeper":
                if (question.type == 'tel' && question.value != null) {
                  serverData.set(question.columnName, Number(question.value))
                } else {
                  serverData.set(question.columnName, question.value)
                }
                break;
              case "checklist-score-keeper":
                if (question.type == 'tel' && question.value != null) {
                  serverData.set(question.columnName, Number(question.value))
                } else {
                  serverData.set(question.columnName, question.value)
                }
                break;
              case "Month Widget":
                serverData.set(question.columnName, question.value)
                break;
              case "Date Widget":
                if (this.isWeb && question.value != null) {
                  if (this.isWeb && question.value.date != null) {
                    let dateValue = question.value.date.day + "-" + question.value.date.month + "-" + question.value.date.year
                    serverData.set(question.columnName, dateValue)
                  } else {
                    serverData.set(question.columnName, question.value)
                  }
                } else if (this.isWeb && question.value == null) {
                  serverData.set(question.columnName, question.value)
                } else if (!this.isWeb && question.value != null) {
                  if (question.value.split("-")[0].length > 0) {
                    serverData.set(question.columnName, this.datePipe.transform(question.value, "dd-MM-yyyy"))
                  } else {
                    serverData.set(question.columnName, question.value)
                  }
                } else {
                  serverData.set(question.columnName, question.value)
                }
                break;
              case 'tableWithRowWiseArithmetic': {
                let tableData = question.tableModel
                let tableArray: any[] = []
                for (let i = 0; i < tableData.length; i++) {
                  let tableRow: {} = {}
                  for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                    let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                    if (typeof cell != 'string' && cell.value != null) {
                      tableRow[cell.columnName] = Number(cell.value)
                    } else if (typeof cell != 'string') {
                      tableRow[cell.columnName] = cell.value
                    }
                    if (typeof cell != 'string' && cell.controlType == 'textarea') {
                      tableRow[cell.columnName] = cell.value
                    }
                  }
                  tableArray.push(tableRow)
                }
                serverData.set(question.columnName, tableArray)
              }
                break;
              case 'tableWithRowAndColumnWiseArithmetic': {
                let tableData = question.tableModel
                let tableArray: any[] = []
                for (let i = 0; i < tableData.length; i++) {
                  let tableRow: {} = {}
                  for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                    let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                    if (typeof cell != 'string' && cell.value != null) {
                      tableRow[cell.columnName] = Number(cell.value)
                    } else if (typeof cell != 'string') {
                      tableRow[cell.columnName] = cell.value
                    }
                  }
                  tableArray.push(tableRow)
                }
                serverData.set(question.columnName, tableArray)
              }
                break;
              case "table":
                let tableData = question.tableModel
                let tableArray: any[] = []
                for (let i = 0; i < tableData.length; i++) {
                  let tableRow: {} = {}
                  for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                    let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                    if (typeof cell != 'string' && cell.value != null) {
                      tableRow[cell.columnName] = Number(cell.value)
                    } else if (typeof cell != 'string') {
                      tableRow[cell.columnName] = cell.value
                    }
                  }
                  tableArray.push(tableRow)
                }
                serverData.set(question.columnName, tableArray)
                break;
              case "beginrepeat":
                let beginrepeat = question.beginRepeat
                let beginrepeatArray: any[] = []
                let beginrepeatMap: {} = {}
                for (let i = 0; i < beginrepeat.length; i++) {
                  beginrepeatMap = {}
                  for (let j = 0; j < beginrepeat[i].length; j++) {
                    let colName = (beginrepeat[i][j].columnName as String).split('-')[3]
                    if (beginrepeat[i][j].controlType == 'Date Widget') {
                      if (this.isWeb && beginrepeat[i][j].value != null) {
                        if (this.isWeb && beginrepeat[i][j].value.date != null) {
                          let dateValue = beginrepeat[i][j].value.date.day + "-" + beginrepeat[i][j].value.date.month + "-" + beginrepeat[i][j].value.date.year
                          beginrepeatMap[colName] = dateValue
                        } else {
                          beginrepeatMap[colName] = beginrepeat[i][j].value
                        }
                      } else if (this.isWeb && beginrepeat[i][j].value == null) {
                        beginrepeatMap[colName] = beginrepeat[i][j].value
                      } else if (!this.isWeb && beginrepeat[i][j].value != null) {
                        if (beginrepeat[i][j].value.split("-")[0].length > 0) {
                          beginrepeatMap[colName] = this.datePipe.transform(beginrepeat[i][j].value, "dd-MM-yyyy")
                        } else {
                          beginrepeatMap[colName] = beginrepeat[i][j].value
                        }
                      } else {
                        beginrepeatMap[colName] = beginrepeat[i][j].value
                      }
                    } else if (beginrepeat[i][j].controlType == 'textbox' && beginrepeat[i][j].type == 'tel') {
                      beginrepeatMap[colName] = Number(beginrepeat[i][j].value)
                    } else if (beginrepeat[i][j].controlType == 'textbox' && beginrepeat[i][j].type != 'tel') {
                      beginrepeatMap[colName] = beginrepeat[i][j].value
                    } else if (beginrepeat[i][j].controlType != 'heading' && beginrepeat[i][j].controlType != 'camera' && beginrepeat[i][j].controlType != 'file') {
                      beginrepeatMap[colName] = beginrepeat[i][j].value
                    }
                  }
                  beginrepeatArray.push(beginrepeatMap)
                }
                serverData.set(question.columnName, beginrepeatArray)
                break;
            }
          }
        }
      }
    }
    return serverData
  }

  getAttachmentCount(sectionMap) {
    let attachements = 0;
    for (let index = 0; index < Object.keys(sectionMap).length; index++) {
      for (let j = 0; j < sectionMap[Object.keys(sectionMap)[index]].length; j++) {
        let subSections = sectionMap[Object.keys(sectionMap)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
            switch (question.controlType) {
              case "camera":
                if (question.value) {
                  if (typeof question.value == 'object' && question.value['src'] != undefined) {
                    attachements += 1
                  } else {
                    for (let a of question.value) {
                      attachements += 1
                    }
                  }
                }
                break;
              case "file":
              case "mfile":
                attachements += question.attachmentsInBase64.length
                break;
              case "beginrepeat":
                let beginrepeat = question.beginRepeat

                for (let i = 0; i < beginrepeat.length; i++) {
                  for (let j = 0; j < beginrepeat[i].length; j++) {
                    switch (beginrepeat[i][j].controlType) {
                      case "camera":
                        if (beginrepeat[i][j].value) {
                          attachements += 1
                        }
                        break;
                      case "file":
                      case "mfile":
                        attachements += beginrepeat[i][j].attachmentsInBase64.length
                        break;
                    }
                    break;
                  }
                }
            }
          }
        }
      }
    }
    return attachements
  }


  async getRejectedForms() {
    let accessToken
    let rejectionCount: number = 0;
    let formIdName: string;
    let loginResponse = await this.storage.get("userAndForm")


    accessToken = loginResponse['tokens'].accessToken
    const httpOptions = {
      headers: new HttpHeaders({
        'Authorization': 'Bearer ' + accessToken,
        'Content-type': 'application/x-www-form-urlencoded; charset=utf-8'
      })
    };

    let baseURL = ConstantProvider.baseUrl + 'api/getRejectedData';
    let rejectedData = await this.http.get(baseURL, httpOptions).toPromise();
    console.log("rejected data", rejectedData)
    let localDbData = null
    await this.storage.get(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username)
      .then((val) => {
        localDbData = val;
      });
    for (let i = 0; i < Object.keys(rejectedData).length; i++) {
      formIdName = Object.keys(rejectedData)[i]

      for (let j = 0; j < rejectedData[Object.keys(rejectedData)[i]].length; j++) {

        rejectionCount++
        let formModel: {} = {}
        let mainFormsDataforSave: {} = {}
        let dbFormModel: IDbFormModel;
        //If unique ID exists in local db for the submission that came from server
        // we change the status to rejected only.

        if (localDbData != null && localDbData[formIdName] != null && localDbData[formIdName][rejectedData[Object.keys(rejectedData)[i]][j].uniqueId]) {
          let submissions = localDbData[formIdName]
          dbFormModel = submissions[rejectedData[Object.keys(rejectedData)[i]][j].uniqueId]
          dbFormModel.formStatus = 'rejected'

        } else {
          //If unique ID doesn't exists in local db for the submission that came from server
          // we prepare key value pair as the formData key consists of data with form schema defination.
          //getKeyValue - creates key value pairs and pushes any camera or attachments if any to localdb

          //push camera and attachements into db in case of WEBPWA
          if (this.applicationDetailsProvider.getPlatform().isWebPWA || this.applicationDetailsProvider.getPlatform().isMobilePWA) {
            rejectedData[Object.keys(rejectedData)[i]][j].formData = await this.pushCameraImagesAndAttachementsIntoDb(rejectedData[Object.keys(rejectedData)[i]][j].formData);
          } else if (this.applicationDetailsProvider.getPlatform().isAndroid) {
            rejectedData[Object.keys(rejectedData)[i]][j].formData = await this.pushCameraImagesAndAttachementsIntoApplicationFolder(rejectedData[Object.keys(rejectedData)[i]][j].formData, formIdName, rejectedData[Object.keys(rejectedData)[i]][j].uniqueId);
          }

          let optimizeData = await this.commonEngine.getKeyValue(
            rejectedData[Object.keys(rejectedData)[i]][j].formData,
            this.userService.user.username, formIdName,
            rejectedData[Object.keys(rejectedData)[i]][j].uniqueId, "rejected");

          dbFormModel = {
            createdDate: rejectedData[Object.keys(rejectedData)[i]][j].createdDate,
            updatedDate: rejectedData[Object.keys(rejectedData)[i]][j].updatedDate,
            formStatus: 'rejected',
            extraKeys: rejectedData[Object.keys(rejectedData)[i]][j].extraKeys,
            formData: optimizeData,
            formSubmissionId: rejectedData[Object.keys(rejectedData)[i]][j].formId,
            uniqueId: rejectedData[Object.keys(rejectedData)[i]][j].uniqueId,
            checked: true,
            image: rejectedData[Object.keys(rejectedData)[i]][j].image,
            attachmentCount: 0,
            formDataHead: rejectedData[Object.keys(rejectedData)[i]][j].formDataHead,
            formId: formIdName
          }
        }
        if (localDbData != null) {
          mainFormsDataforSave = localDbData
          if (mainFormsDataforSave[formIdName] != undefined) {
            if (dbFormModel.uniqueId != Object.keys(mainFormsDataforSave[formIdName])[i]) {
              formModel = mainFormsDataforSave[formIdName]
              formModel[dbFormModel.uniqueId as any] = dbFormModel
              mainFormsDataforSave[formIdName] = formModel
            } else if (mainFormsDataforSave[formIdName][Object.keys(mainFormsDataforSave[formIdName])[i]].formStatus == 'sent') {
              formModel = mainFormsDataforSave[formIdName]
              formModel[dbFormModel.uniqueId as any] = dbFormModel
              mainFormsDataforSave[formIdName] = formModel
            }
          } else {
            formModel[dbFormModel.uniqueId as any] = dbFormModel
            mainFormsDataforSave[formIdName] = formModel
          }
        } else {
          formModel[dbFormModel.uniqueId as any] = dbFormModel
          mainFormsDataforSave[formIdName] = formModel
        }
        localDbData = mainFormsDataforSave
      }
    }
    await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + this.userService.user.username, localDbData)
    return rejectionCount
  }

  async pushCameraImagesAndAttachementsIntoDb(data): Promise<any> {

    for (let index = 0; index < Object.keys(data).length; index++) {
      for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
        let subSections = data[Object.keys(data)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]

            switch (question.controlType) {

              case "camera":
              case "file":
              case "mfile":
                {
                  if (question.attachmentsInBase64) {
                    let index = 0;
                    for (let path of question.attachmentsInBase64) {
                      let file = await this.http.get(question.controlType == 'camera' ? path : path.base64, { responseType: 'blob' }).toPromise()
                      let base64String = await this.convertBlobToBase64(file);
                      if (question.controlType == "camera") {
                        question.attachmentsInBase64[index] = base64String
                      } else {
                        question.attachmentsInBase64[index].base64 = base64String
                      }
                      index += 1;
                    }
                  }
                }
                break;
              case "beginrepeat":
                {
                  for (let bgindex = 0; bgindex < question.beginRepeat.length; bgindex++) {
                    let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[bgindex];
                    for (let beginRepeatQuestion of beginRepeatQuestions) {
                      if (beginRepeatQuestion.controlType == "file" || beginRepeatQuestion.controlType == "mfile" || beginRepeatQuestion.controlType == "camera") {
                        beginRepeatQuestion.value = []
                        let index = 0
                        for (let path of beginRepeatQuestion.attachmentsInBase64) {
                          let file = await this.http.get(beginRepeatQuestion.controlType == 'camera' ? path : path.base64, { responseType: 'blob' }).toPromise()
                          var fileReader = new FileReader();
                          fileReader.readAsDataURL(file)
                          fileReader.onloadend = () => {
                            let base64String = fileReader.result
                            if (beginRepeatQuestion.controlType == 'camera') {
                              beginRepeatQuestion.attachmentsInBase64[index] = base64String
                            } else {
                              beginRepeatQuestion.attachmentsInBase64[index].base64 = base64String
                            }
                          }
                          index++
                        }
                      }
                    }
                  }
                }

                break;
            }

          }
        }
      }
    }
    return data
  }
  convertBlobToBase64 = blob => new Promise((resolve, reject) => {
    const reader = new FileReader;
    reader.onerror = reject;
    reader.onload = () => {
      resolve(reader.result);
    };
    reader.readAsDataURL(blob);
  });


  async pushCameraImagesAndAttachementsIntoApplicationFolder(data, formId, uniqueId): Promise<any> {

    const iWriteOptions: IWriteOptions = {
      replace: true
    }

    for (let index = 0; index < Object.keys(data).length; index++) {
      for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
        let subSections = data[Object.keys(data)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]

            switch (question.controlType) {

              case "camera":
              case "file":
              case "mfile":
                {
                  if (question.attachmentsInBase64) {
                    let index = 0;
                    question.value = []
                    for (let path of question.attachmentsInBase64) {
                      let file = await this.http.get(question.controlType == 'camera' ? path : path.base64, { responseType: 'blob' }).toPromise()
                      let base64String = await this.convertBlobToBase64(file);
                      let imageBlob = await this.commonEngine.dataURItoBlob(base64String, this.commonEngine.getContentType(base64String))
                      await this.commonEngine.createFoldersInMobileDevice(formId, uniqueId, this.file, this.messageService).then((async d => {
                        let currentTime = +new Date();
                        let random = Math.random()
                        let trends = random + ""

                        if (question.controlType == "camera") {
                          let filename = trends.replace('.', '') + '_' + question.columnName + '_' + currentTime + ".jpg";
                          let writeToFilePath = this.file.externalRootDirectory + ConstantProvider.appFolderName + "/" + formId + "/" + uniqueId;
                          let writtenFile = await this.file.writeFile(writeToFilePath, filename, imageBlob, iWriteOptions)
                          question.attachmentsInBase64[index] = writtenFile.nativeURL
                          question.value[index] = writtenFile.nativeURL
                        } else {

                          let filename = trends.replace('.', '') + '_' + question.columnName + '_' + question.attachmentsInBase64[index].fileName;
                          let writeToFilePath = this.file.externalRootDirectory + ConstantProvider.appFolderName + "/" + formId + "/" + uniqueId;
                          let writtenFile = await this.file.writeFile(writeToFilePath, filename, imageBlob, iWriteOptions)
                          let f = {
                            fileType: question.attachmentsInBase64[index].fileType,
                            fileName: question.attachmentsInBase64[index].fileName,
                            fp: writtenFile.nativeURL,
                            basePath: writeToFilePath,
                            pathFileName: filename
                          };
                          question.attachmentsInBase64[index] = f
                        }
                      }))
                      index += 1;
                    }
                  }
                }
                break;
              case "beginrepeat":
                {
                  for (let bgindex = 0; bgindex < question.beginRepeat.length; bgindex++) {
                    let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[bgindex];
                    for (let beginRepeatQuestion of beginRepeatQuestions) {
                      if (beginRepeatQuestion.controlType == "file" || beginRepeatQuestion.controlType == "mfile" || beginRepeatQuestion.controlType == "camera") {
                        beginRepeatQuestion.value = []

                        let index = 0;

                        for (let path of beginRepeatQuestion.attachmentsInBase64) {
                          let file = await this.http.get(beginRepeatQuestion.controlType == 'camera' ? path : path.base64, { responseType: 'blob' }).toPromise()
                          let base64String = await this.convertBlobToBase64(file);
                          let imageBlob = await this.commonEngine.dataURItoBlob(base64String, this.commonEngine.getContentType(base64String))
                          await this.commonEngine.createFoldersInMobileDevice(formId, uniqueId, this.file, this.messageService).then((async d => {
                            let currentTime = +new Date();
                            let random = Math.random()
                            let trends = random + ""
                            let filename = trends.replace('.', '') + '_' + beginRepeatQuestion.columnName + '_' + currentTime + ".jpg";
                            let writeToFilePath = this.file.externalRootDirectory + ConstantProvider.appFolderName + "/" + formId + "/" + uniqueId;
                            let writtenFile = await this.file.writeFile(writeToFilePath, filename, imageBlob, iWriteOptions)
                            if (beginRepeatQuestion.controlType == "camera") {
                              beginRepeatQuestion.attachmentsInBase64[index] = writtenFile.nativeURL
                              beginRepeatQuestion.value[index] = writtenFile.nativeURL
                            } else {
                              let fileBasePath = writtenFile.nativeURL[0].substr(0, writtenFile.nativeURL[0].lastIndexOf('/') + 1);
                              let f = {
                                fileType: beginRepeatQuestion.attachmentsInBase64[index].fileType,
                                fileName: beginRepeatQuestion.attachmentsInBase64[index].fileName,
                                fp: writtenFile.nativeURL,
                                basePath: fileBasePath,
                                pathFileName: beginRepeatQuestion.attachmentsInBase64[index].fileName
                              };
                              beginRepeatQuestion.attachmentsInBase64[index] = f
                            }
                          }))
                          index += 1;
                        }


                      }
                    }
                  }
                }
                break;
            }
          }
        }
      }
    }
    return data
  }
}



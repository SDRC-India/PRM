import {
  HttpClient
} from '@angular/common/http';
import {
  Injectable
} from '@angular/core';
import {
  EngineUtilsProvider
} from '../engine-utils/engine-utils';
import { ConstantProvider } from '../constant/constant';
import { QuestionServiceProvider } from '../question-service/question-service';
import {
  Storage
} from '@ionic/storage';
import { UserServiceProvider } from '../user-service/user-service';
import { ApplicationDetailsProvider } from '../application/appdetails.provider';
/*
  Generated class for the CommonsEngineProvider provider.

  See https://angular.io/guide/dependency-injection for more info on providers
  and Angular DI.
*/

@Injectable()
export class CommonsEngineProvider {

  constructor(public http: HttpClient, private engineUtilsProvider: EngineUtilsProvider,
    public questionService: QuestionServiceProvider, private storage: Storage,
    private userService: UserServiceProvider, private applicationDetailsProvider: ApplicationDetailsProvider) {

  }

  calculateScore(question: IQuestionModel, questionMap) {
    if (question.scoreExp) {
      return this.engineUtilsProvider.resolveExpression(question.scoreExp, questionMap, "score")
    }
    return null;
  }

  renameRelevanceAndFeaturesAndConstraintsAndScoreExpression(question: IQuestionModel, questionMap, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    question = this.renameRelevance(question, questionMap, repeatQuestion, size)
    question = this.renameFeatures(question, repeatQuestion, size)
    question = this.renameConstraints(question, repeatQuestion, size)
    question = this.renameScoreExpression(question, repeatQuestion, size)
    return question
  }

  renameRelevance(question: IQuestionModel, questionMap, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    if (question.relevance != null) {
      let relevanceString = "";
      for (let rel of question.relevance.split(":")) {
        // if (questionMap[rel] != undefined && questionMap[rel].parentColumnName && questionMap[rel].controlType !='cell') {
        if (rel.includes("-")) {
          let depColNames = "";
          let depColName = rel.split("-")[3];
          let depColIndex = rel.split("-")[2];
          depColNames = depColNames + repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
          relevanceString = relevanceString + depColNames + ":";
        } else {
          relevanceString = relevanceString + rel + ":";
        }
      }
      relevanceString = relevanceString.substr(0, relevanceString.length - 1);
      question.relevance = relevanceString;
    }
    return question;
  }


  renameFeatures(question: IQuestionModel, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    if (question.features != null) {
      for (let feature of question.features.split("@AND")) {
        switch (feature.split(":")[0]) {
          case "exp":
            {
              this.renamefeatureExpression(question, repeatQuestion, size)
            }
            break;
          case "date_sync":
            {
              let rColNames;
              for (let colName of feature.split(":")[1].split("&")) {
                if (colName.includes("-")) {
                  let depColName = colName.split("-")[3];
                  let depColIndex = colName.split("-")[2];
                  rColNames = repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
                  question.features = question.features.replace(colName, rColNames);
                }
              }
            }
            break;
          case "area_group":
          case "filter_single":
          case "filter_multiple":
            {
              let rColNames;
              let areaColName = feature.split(":")[1];
              if (areaColName.includes("-")) {
                let depColName = areaColName.split("-")[3];
                let depColIndex = areaColName.split("-")[2];
                rColNames = repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
                question.features = question.features.replace(areaColName, rColNames);
              }
            }
            break;
        }
      }
    }
    return question;
  }

  renameConstraints(question: IQuestionModel, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    if (question.constraints != null) {
      let constraints = question.constraints.replace(" ", "");
      let str: String[] = constraints.split("");
      let alteredConstraint = ""
      for (let i = 0; i < str.length; i++) {

        let ch: string = str[i] as string;
        if (ch == '$') {
          let qName = "";
          for (let j = i + 2; j < str.length; j++) {
            if (str[j] == "}") {
              i = j;
              break;
            }
            qName = qName + (str[j]);
            if (qName.includes("-")) {
              let depColName = qName.split("-")[3];
              let depColIndex = qName.split("-")[2];
              qName = repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
            }
            alteredConstraint = alteredConstraint + "${" + qName + "}"
          }
        } else {
          alteredConstraint = alteredConstraint + ch
        }
      }
      question.constraints = alteredConstraint
    }
    return question
  }

  renameScoreExpression(question: IQuestionModel, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    if (question.scoreExp != null) {
      let expression = question.scoreExp.replace(" ", "");
      let str: String[] = expression.split("");
      let alteredExpression = ""
      for (let i = 0; i < str.length; i++) {

        let ch: string = str[i] as string;
        if (ch == '$') {
          let qName = "";
          for (let j = i + 2; j < str.length; j++) {
            if (str[j] == "}") {
              i = j;
              break;
            }
            qName = qName + (str[j]);
          }
          if (qName.includes("-")) {
            let depColName = qName.split("-")[3];
            let depColIndex = qName.split("-")[2];
            qName = repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
          }
          alteredExpression = alteredExpression + "${" + qName + "}"
        } else {
          alteredExpression = alteredExpression + ch
        }
      }
      question.scoreExp = alteredExpression
    }

    return question
  }

  renamefeatureExpression(question: IQuestionModel, repeatQuestion: IQuestionModel, size: Number): IQuestionModel {
    if (question.features != null) {
      let expression = question.features.replace(" ", "");
      let str: String[] = expression.split("");
      let alteredExpression = ""
      for (let i = 0; i < str.length; i++) {

        let ch: string = str[i] as string;
        if (ch == '$') {
          let qName = "";
          for (let j = i + 2; j < str.length; j++) {
            if (str[j] == "}") {
              i = j;
              break;
            }
            qName = qName + (str[j]);
          }
          if (qName.includes("-")) {
            let depColName = qName.split("-")[3];
            let depColIndex = qName.split("-")[2];
            qName = repeatQuestion.columnName + "-" + size + "-" + depColIndex + "-" + depColName;
          }
          alteredExpression = alteredExpression + "${" + qName + "}"
        } else {
          alteredExpression = alteredExpression + ch
        }
      }
      question.features = alteredExpression
    }

    return question
  }

  generateConstraintGraph(constraintExpression, question: IQuestionModel, constraintsArray, questionMap) {

    let str: String[] = constraintExpression.split("");
    for (let i = 0; i < str.length; i++) {

      let ch: string = str[i] as string;
      if (ch == '$') {
        let qName = "";
        for (let j = i + 2; j < str.length; j++) {
          if (str[j] == "}") {
            i = j;
            break;
          }
          qName = qName + str[j];
        }
        if (constraintsArray[question.columnName] == undefined) {
          // constraintsArray[question.columnName] = [questionMap[qName]] ;
          constraintsArray[question.columnName] = [question];
        }
        else {
          // let exist = false
          // let c = constraintsArray[question.columnName];
          // for(let e of c){
          //   // if(e == undefined){
          //   //     console.log(question,c,qName)
          //   // }
          //   // if(e.columnName == qName){
          //   //   exist = true
          //   //   break;
          //   // }
          // }
          // if(!exist){
          //   c.push(questionMap[qName]);
          //   constraintsArray[question.columnName] = c;
          // }

        }
      }
    }

  }

  sanitizeOptions(data: any): any {
    for (let index = 0; index < Object.keys(data).length; index++) {
      for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
        let subSections = data[Object.keys(data)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
            switch (question.controlType) {
              case "dropdown":
              case "autoCompleteTextView":
              case "autoCompleteMulti": {
                question.options = []

              }
                break;
              case 'beginrepeat':
                for (let index = 0; index < question.beginRepeat.length; index++) {
                  let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[index];
                  for (let beginRepeatQuestion of beginRepeatQuestions) {
                    switch (beginRepeatQuestion.controlType) {
                      case "dropdown":
                      case "autoCompleteTextView":
                      case "autoCompleteMulti":
                        beginRepeatQuestion.options = []
                        break
                    }
                  }
                }
            }
          }
        }
      }
    }
    return data
  }


  /**
   * This method is use to load ans set all the data from json to ui
   *
   * @author Jagat Bandhu (jagat@sdrc.co.in)
   * @param data
   */

  async loadOptionsIntoData(formId) {

    let oMap = await this.questionService.getQuestionBank(formId, null, ConstantProvider.lastUpdatedDate).then(data => {
      let optionMap: {} = []
      for (let index = 0; index < Object.keys(data).length; index++) {

        for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
          let subSections = data[Object.keys(data)[index]][0];
          for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
            for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
              let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q];
              switch (question.controlType) {
                case "dropdown":
                case "autoCompleteTextView":
                case "autoCompleteMulti":
                  optionMap[question.columnName] = question.options
                  break
                case 'beginrepeat':
                  let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[0];
                  for (let beginRepeatQuestion of beginRepeatQuestions) {
                    switch (beginRepeatQuestion.controlType) {
                      case "dropdown":
                      case "autoCompleteTextView":
                      case "autoCompleteMulti":
                        optionMap[beginRepeatQuestion.columnName.split("-")[3]] = beginRepeatQuestion.options
                        break
                    }
                  }
              }
            }
          }
        }
      }
      return optionMap;
    });
    return oMap;
  }


  getDependentAreaGroupName(features: string): any {
    if (features) {
      let featuresArray = features.split("@AND");
      for (let feature of featuresArray) {
        switch (feature.trim().split(":")[0]) {
          case 'area_group':
          case 'filter_single':
          case 'filter_multiple':
            return feature.split(":")[1].trim()


        }
      }
    } else {
      return ""
    }
  }


  checkMandatory(sectionMap, data, type, service): any {
    loop1: for (let index = 0; index < Object.keys(data).length; index++) {
      sectionMap.set(Object.keys(data)[index], data[Object.keys(data)[index]])
      for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
        let subSections = data[Object.keys(data)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]
            let mandatory = false
            if ((type == "save" && question.saveMandatory && question.tempSaveMandatory !== false)) {
              mandatory = true
            }
            else if ((type == "finalized" && question.finalizeMandatory && question.tempFinalizeMandatory !==false)) {
              mandatory = true
            }

              switch (question.controlType) {
                case "geolocation":
                  if (mandatory == true && question.displayComponent == true && (question.value == null || question.value == "")) {
                    service.displayError(question, index, "कृपया " + question.label+" दर्ज करें")
                    break loop1;
                  }
                  break;
                case "camera":
                case "file":
                  if (this.applicationDetailsProvider.getPlatform().isAndroid) {
                    if (mandatory == true && question.displayComponent == true && !(question.value)) {
                      service.displayError(question, index, "कृपया choose " + question.label)
                      break loop1;
                    }
                  } else {
                    if (question.controlType == "camera" && this.applicationDetailsProvider.getPlatform().isWebPWA) {
                      if (mandatory == true && question.displayComponent == true && question.value == null) {
                        service.displayError(question, index, "कृपया choose " + question.label)
                        break loop1;
                      }
                    } else {
                      if (mandatory == true && question.displayComponent == true && question.attachmentsInBase64.length == 0) {
                        service.displayError(question, index, "कृपया choose " + question.label)
                        break loop1;
                      }
                    }

                  }

                  break;

                case 'dropdown':
                  if (mandatory == true && question.displayComponent == true && (question.value == null || question.value.length == 0)) {
                    service.displayError(question, index, "कृपया " + question.label+" चुने")
                    break loop1
                  } else if (mandatory == true && (question.value == null || question.value.length == 0) && (question.displayComponent == true || question.displayComponent == null)) {
                    service.displayError(question, index, "कृपया " + question.label+" चुने")
                    break loop1
                  }

                  break;
                case 'autoCompleteMulti':
                case 'autoCompleteTextView':
                  if (mandatory == true && question.displayComponent == true && (question.value == null || question.value == '')) {
                    service.displayError(question, index, "कृपया " + question.label+" चुने")
                    break loop1
                  }
                  break;
                case 'textarea':
                case 'textbox':
                  if (mandatory == true && question.displayComponent == true && (question.value == null || question.value.trim() == "")) {
                    service.displayError(question, index, "कृपया " + question.label+" दर्ज करें")
                    break loop1
                  }
                  break;
                case "Time Widget":
                case 'Month Widget':
                case "Date Widget":
                  if (mandatory == true && question.displayComponent == true && (question.value == null || question.value == "")) {
                    service.displayError(question, index, "कृपया " + question.label+" चुने")
                    break loop1;
                  }
                  break;

                case "table":
                case 'tableWithRowWiseArithmetic':
                  let tableData = question.tableModel
                  for (let i = 0; i < tableData.length; i++) {
                    for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                      let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                      let mandatory = false
                      if (typeof cell == 'object') {

                        if ((type == "save" && cell.saveMandatory)) {
                          mandatory = true
                        }
                        else if ((type == "finalized" && cell.finalizeMandatory)) {
                          mandatory = true
                        }
                        if (mandatory == true && cell.displayComponent == true && (cell.value == null || cell.value == '')) {
                          service.displayError(cell, index, "कृपया " + question.label + " " + (cell.label).replace('@@split@@', '')+" दर्ज करें", "table")
                          break loop1;
                        }
                      }
                    }
                  }
                  break;

                case 'beginrepeat':
                  for (let bgindex = 0; bgindex < question.beginRepeat.length; bgindex++) {
                    let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[bgindex];
                    for (let beginRepeatQuestion of beginRepeatQuestions) {
                      mandatory = false
                      if ((type == "save" && beginRepeatQuestion.saveMandatory)) {
                        mandatory = true
                      }
                      else if ((type == "finalized" && beginRepeatQuestion.finalizeMandatory)) {
                        mandatory = true
                      }
                      switch (beginRepeatQuestion.controlType) {
                        case "geolocation":
                          if (mandatory == true && beginRepeatQuestion.displayComponent == true && (beginRepeatQuestion.value == null || beginRepeatQuestion.value == "")) {
                            service.displayError(beginRepeatQuestion, index, "कृपया " + beginRepeatQuestion.label +" चुने")
                            break loop1;
                          }
                          break;
                        case "camera":
                          if (mandatory == true && beginRepeatQuestion.displayComponent == true && beginRepeatQuestion.value) {
                            service.displayError(beginRepeatQuestion, index, "कृपया " + beginRepeatQuestion.label+" दर्ज करें")
                            break loop1;
                          }
                          break;
                        case "file":
                          if (mandatory == true && beginRepeatQuestion.displayComponent == true && beginRepeatQuestion.attachmentsInBase64.length == 0) {
                            service.displayError(beginRepeatQuestion, index, "कृपया choose " + beginRepeatQuestion.label)
                            break loop1;
                          }
                          break;
                        case 'dropdown':
                          for (let i = 0; i < beginRepeatQuestion.options.length; i++) {
                            if (mandatory == true && beginRepeatQuestion.displayComponent == true && beginRepeatQuestion.value == null) {
                              service.displayError(beginRepeatQuestion, index, "कृपया " + beginRepeatQuestion.label+" चुने")
                              break loop1
                            } else if (mandatory == true && beginRepeatQuestion.value == null && (beginRepeatQuestion.displayComponent == true || beginRepeatQuestion.displayComponent == null)) {
                              service.displayError(beginRepeatQuestion, index, "कृपया " + beginRepeatQuestion.label+" चुने")
                              break loop1
                            }
                          }
                          break;
                        case 'autoCompleteMulti':
                          if (mandatory == true && beginRepeatQuestion.displayComponent == true && (beginRepeatQuestion.value == null || beginRepeatQuestion.value == '')) {
                            service.displayError(beginRepeatQuestion, index, "कृपया " + beginRepeatQuestion.label+" चुने")
                            break loop1
                          }
                          break;
                        case 'autoCompleteTextView':
                          if (mandatory == true && beginRepeatQuestion.displayComponent == true && (beginRepeatQuestion.value == null || beginRepeatQuestion.value == '')) {
                            service.displayError(beginRepeatQuestion, index, "कृपया " + beginRepeatQuestion.label+" चुने")
                            break loop1
                          }
                          break;
                        case 'textbox':
                          if (mandatory == true && beginRepeatQuestion.displayComponent == true && (beginRepeatQuestion.value == null || beginRepeatQuestion.value.trim() == '')) {
                            service.displayError(beginRepeatQuestion, index, "कृपया a valid " + beginRepeatQuestion.label+" दर्ज करें")
                            break loop1
                          }
                          break;
                        case 'Date Widget':
                        case 'Month Widget':
                        case 'Time Widget':
                          if (mandatory == true && beginRepeatQuestion.displayComponent == true && (beginRepeatQuestion.value == null || beginRepeatQuestion.value == '')) {
                            service.displayError(beginRepeatQuestion, index, "कृपया " + beginRepeatQuestion.label+" चुने")
                            break loop1
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

  async getKeyValue(data, username, formId, uniqueId, calledDuring): Promise<any> {
    let keyValueMap: {} = {}

    for (let index = 0; index < Object.keys(data).length; index++) {
      for (let j = 0; j < data[Object.keys(data)[index]].length; j++) {
        let subSections = data[Object.keys(data)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]

            switch (question.controlType) {

              default:
                {
                  keyValueMap[question.columnName as any] = question.value
                }
                break;
              case "autoCompleteTextView"  :{
                keyValueMap[question.columnName as any] = question.value==null?null:question.value.key
              }
              break;
              case "camera":
                // during save or finalize operation we keep path in case of mobile and base64 image data in case WebPWA
                // In case of rejected form the server sends base64.So we have to set path in db.
                // So we have to write
                if (calledDuring && calledDuring == 'rejected') {

                  if (question.attachmentsInBase64) {
                    if (this.applicationDetailsProvider.getPlatform().isWebPWA) {
                      for (let index = 0; index < question.attachmentsInBase64.length; index++) {
                        question.attachmentsInBase64[index] = question.attachmentsInBase64[index].replace("data:application/octet-stream", "data:image/jpeg")
                      }
                    }

                    await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + question.columnName + "_" + uniqueId, question.attachmentsInBase64);
                    keyValueMap[question.columnName as any] = ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + question.columnName + "_" + uniqueId
                  } else {
                    keyValueMap[question.columnName as any] = null
                  }

                } else {
                  if (question.value) {
                    await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + question.columnName + "_" + uniqueId, question.value);
                    keyValueMap[question.columnName as any] = ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + question.columnName + "_" + uniqueId
                  } else {
                    keyValueMap[question.columnName as any] = null
                  }
                }
                break;
              case "file":
              case "mfile":
                if (question.attachmentsInBase64 && question.attachmentsInBase64.length > 0) {

                  if (calledDuring && calledDuring == 'rejected') {
                    if (this.applicationDetailsProvider.getPlatform().isWebPWA) {
                      let index = 0;
                      for (let att of question.attachmentsInBase64) {
                        question.attachmentsInBase64[index].base64 = att.base64.replace("data:application/octet-stream;base64,", "");
                        index++
                      }
                    }
                  }
                  await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + question.columnName + "_" + uniqueId, question.attachmentsInBase64);
                  keyValueMap[question.columnName as any] = ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + question.columnName + "_" + uniqueId
                }
                else {
                  keyValueMap[question.columnName as any] = null

                }
                break;
              case "table":
              case 'tableWithRowWiseArithmetic':
                {
                  let tableData = question.tableModel
                  keyValueMap[question.columnName as any] = []
                  for (let i = 0; i < tableData.length; i++) {
                    let rowData: {} = {}
                    for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                      let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                      if (typeof cell == 'object') {
                        rowData[cell.columnName as any] = cell.value
                      }
                    }
                    keyValueMap[question.columnName as any].push(rowData)
                  }
                }
                break;
              case 'beginrepeat':
                keyValueMap[question.columnName as any] = []
                for (let bgindex = 0; bgindex < question.beginRepeat.length; bgindex++) {
                  let rowData: {} = {}
                  let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[bgindex];
                  for (let beginRepeatQuestion of beginRepeatQuestions) {

                    switch (beginRepeatQuestion.controlType) {

                      case "camera":
                        if (calledDuring && calledDuring == 'rejected') {
                          if (beginRepeatQuestion.attachmentsInBase64) {
                            if (this.applicationDetailsProvider.getPlatform().isWebPWA) {
                              for (let index = 0; index < beginRepeatQuestion.attachmentsInBase64.length; index++) {
                                beginRepeatQuestion.attachmentsInBase64[index] = beginRepeatQuestion.attachmentsInBase64[index].replace("data:application/octet-stream", "data:image/jpeg")
                              }
                            }
                            await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + beginRepeatQuestion.columnName + "_" + uniqueId, beginRepeatQuestion.attachmentsInBase64);
                            rowData[beginRepeatQuestion.columnName as any] = ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + beginRepeatQuestion.columnName + "_" + uniqueId
                          } else {
                            rowData[beginRepeatQuestion.columnName as any] = null
                          }
                        } else {
                          if (beginRepeatQuestion.value) {
                            await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + beginRepeatQuestion.columnName + "_" + uniqueId, beginRepeatQuestion.value);
                            rowData[beginRepeatQuestion.columnName as any] = ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + beginRepeatQuestion.columnName + "_" + uniqueId
                          } else {
                            rowData[beginRepeatQuestion.columnName as any] = null
                          }
                        }
                        break;
                      case "file":
                      case "mfile":
                        if (beginRepeatQuestion.attachmentsInBase64 && beginRepeatQuestion.attachmentsInBase64.length > 0) {
                          if (calledDuring && calledDuring == 'rejected') {
                            if (this.applicationDetailsProvider.getPlatform().isWebPWA) {
                              let index = 0;
                              for (let att of beginRepeatQuestion.attachmentsInBase64) {
                                beginRepeatQuestion.attachmentsInBase64[index].base64 = att.base64.replace("data:application/octet-stream;base64,", "");
                                index++
                              }
                            }
                          }
                          await this.storage.set(ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + beginRepeatQuestion.columnName + "_" + uniqueId, beginRepeatQuestion.attachmentsInBase64);
                          rowData[beginRepeatQuestion.columnName as any] = ConstantProvider.dbKeyNames.form + "-" + username + "-" + formId + "-" + beginRepeatQuestion.columnName + "_" + uniqueId
                        }
                        else {
                          rowData[beginRepeatQuestion.columnName as any] = null
                        }
                        break;
                      default:
                        rowData[beginRepeatQuestion.columnName as any] = beginRepeatQuestion.value
                        break;
                    }
                  }
                  keyValueMap[question.columnName as any].push(rowData)
                }
                break;
            }
          }
        }
      }
    }
    return keyValueMap
  }

  async loadDataIntoSchemaDef(schemadef, keyValueMap): Promise<any> {

    for (let index = 0; index < Object.keys(schemadef).length; index++) {
      for (let j = 0; j < schemadef[Object.keys(schemadef)[index]].length; j++) {
        let subSections = schemadef[Object.keys(schemadef)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]

            switch (question.controlType) {

              default:
                {
                  question.value = keyValueMap[question.columnName as any]
                }
                break;
              case "camera":
                if (keyValueMap[question.columnName as any]) {
                  question.value = await this.storage.get(keyValueMap[question.columnName as any]);
                  question.attachmentsInBase64 = question.value
                } else {
                  question.value = []
                }
                break;
              case "file":
              case "mfile":
                if (this.applicationDetailsProvider.getPlatform().isAndroid) {

                  if (keyValueMap[question.columnName as any]) {
                    question.value = []
                    question.attachmentsInBase64 = await this.storage.get(keyValueMap[question.columnName as any]);
                    if (question.attachmentsInBase64) {
                      for (let attachement of question.attachmentsInBase64) {
                        question.value.push(attachement.fp)
                      }
                    }
                  }
                  else
                    question.attachmentsInBase64 = []
                } else {
                  if (keyValueMap[question.columnName as any])
                    question.attachmentsInBase64 = await this.storage.get(keyValueMap[question.columnName as any]);
                  else
                    question.attachmentsInBase64 = []
                }

                break;
              case "table":
              case 'tableWithRowWiseArithmetic':
                {
                  let tableData = question.tableModel
                  for (let i = 0; i < tableData.length; i++) {
                    let rowData = keyValueMap[question.columnName as any][i]
                    for (let j = 0; j < Object.keys(tableData[i]).length; j++) {
                      let cell = (tableData[i])[Object.keys(tableData[i])[j]]
                      if (typeof cell == 'object') {
                        cell.value = rowData[cell.columnName as any]
                      }
                    }
                  }
                }
                break;

              case 'beginrepeat':
                let sizeInData = keyValueMap[question.columnName as any].length
                //add the beginrepeat accordian to schema defination
                for (let i = 0; i < sizeInData - 1; i++) {
                  question.beginRepeat.push(this.getNewBeginRepeatAccordian(question))
                }
                for (let bgindex = 0; bgindex < question.beginRepeat.length; bgindex++) {
                  let rowData = keyValueMap[question.columnName as any][bgindex]
                  let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[bgindex];
                  for (let beginRepeatQuestion of beginRepeatQuestions) {
                    if (beginRepeatQuestion.controlType == "file" || beginRepeatQuestion.controlType == "mfile") {
                      beginRepeatQuestion.attachmentsInBase64 = keyValueMap[beginRepeatQuestion.columnName as any] ? await this.storage.get(keyValueMap[beginRepeatQuestion.columnName as any]) : [];
                    }
                    else if (beginRepeatQuestion.controlType == "camera") {
                      beginRepeatQuestion.value = await this.storage.get(keyValueMap[beginRepeatQuestion.columnName as any]);
                    } else {
                      beginRepeatQuestion.value = rowData[beginRepeatQuestion.columnName as any]
                    }
                  }
                }
                break;
            }
          }
        }
      }
    }
    return schemadef
  }

  getNewBeginRepeatAccordian(beginRepeatParent: IQuestionModel): any {

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
      clonedQuestion[index] = this.renameRelevanceAndFeaturesAndConstraintsAndScoreExpression(clonedQuestion[index], null, beginRepeatParent, size);

      //setting up default setting and added to dependency array and feature array
      clonedQuestion[index].displayComponent = clonedQuestion[index].relevance == null ? true : false;
    }
    return clonedQuestion
  }


  prepareHeaderData(question, headerData) {
    //checking the review header and adding the value to headerData variable to show the list of data in the form list.
    if (question.reviewHeader) {
      switch (question.controlType) {
        case 'segment':
        case 'dropdown':

          if (question.type == "option") {
            headerData[question.reviewHeader] = question.options && question.options.length ?
              question.options.filter(d => d.key === question.value)[0].value : question.value;
          } else {
            // for checkbox
            let names = "";
            let vals = question.options.filter(d => question.value.find(element => {
              return (d.key === element)
            }))
            vals.forEach(e => {
              names = names + e.value + ","
            })
            if (names.includes(",")) {
              names = names.substring(0, names.lastIndexOf(","))
            }
            headerData[question.reviewHeader] = names;
          }
          break;

        case "Time Widget":
        case "Month Widget":
        case 'textbox':
          headerData[question.reviewHeader] = question.value;
          break;
        case "Date Widget":
          headerData[question.reviewHeader] = question.value['formatted'] ? question.value.formatted : question.value ? question.value : null;
          break
        case 'camera':
          headerData[question.reviewHeader] = question.value.src;

          break;
      }
    }
    return headerData
  }


  async getSchemaDefinationInKeyValueFormat() {
    let userAndForm = await this.userService.getUserAndForm()
    return userAndForm['getAllForm']
  }


  async removeFilesAndCameraAttachementsIfAny(clonedQuestion) {

    for (let index = 0; index < clonedQuestion.length; index++) {
      switch (clonedQuestion[index].controlType) {
        case "camera":
        case "file":
        case "mfile":
          await this.storage.remove(clonedQuestion[index].value)
          break;
      }
    }

  }

  async removeFilesAndCameraAttachementsFromUniqueId(schemadef, keyValueMap): Promise<any> {

    for (let index = 0; index < Object.keys(schemadef).length; index++) {
      for (let j = 0; j < schemadef[Object.keys(schemadef)[index]].length; j++) {
        let subSections = schemadef[Object.keys(schemadef)[index]][0]
        for (let qs = 0; qs < Object.keys(subSections).length; qs++) {
          for (let q = 0; q < subSections[Object.keys(subSections)[qs]].length; q++) {
            let question: IQuestionModel = subSections[Object.keys(subSections)[qs]][q]

            switch (question.controlType) {
              case "camera":
                await this.storage.remove(keyValueMap[question.columnName as any]);
                break;
              case "file":
              case "mfile":
                await this.storage.remove(keyValueMap[question.columnName as any]);
                break;
              case 'beginrepeat':

                for (let bgindex = 0; bgindex < question.beginRepeat.length; bgindex++) {
                  let beginRepeatQuestions: IQuestionModel[] = question.beginRepeat[bgindex];
                  for (let beginRepeatQuestion of beginRepeatQuestions) {
                    if (beginRepeatQuestion.controlType == "file" || beginRepeatQuestion.controlType == "mfile" || beginRepeatQuestion.controlType == "camera") {
                      await this.storage.remove(keyValueMap[beginRepeatQuestion.columnName as any]);
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

  public getContentType(base64Data: any) {
    let block = base64Data.split(";");
    let contentType = block[0].split(":")[1];
    return contentType;
  }


  async dataURItoBlob(dataURI, type) {
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


  createFoldersInMobileDevice(formId, uniqueId, file, message): Promise<any> {

    return new Promise((resolve, reject) => {
      //checking folder existance
      file.checkDir(file.externalRootDirectory, ConstantProvider.appFolderName).then(() => {
        file.createDir(file.externalRootDirectory + ConstantProvider.appFolderName, formId, false).then(() => {
          file.createDir(file.externalRootDirectory + ConstantProvider.appFolderName + "/" + formId, uniqueId as string, false).then(() => {
            resolve(true)
          }).catch(err => {
            resolve(true)
          });
        }).catch(err => {
          file.createDir(file.externalRootDirectory + ConstantProvider.appFolderName + "/" + formId, uniqueId as string, false).then(() => {
            resolve(true)
          }).catch(err => {
            resolve(true)
          });
        });
      }).catch(err => {
        if (err.code === 1) {
          message.stopLoader()
          message.showErrorToast("The application folder has been deleted from memory. Please re-install the application to continue data entry.")
        }
      })
    })

  }


  checkManadatoryStatus(rowData){
    let allMandatory = true;
    loop1: for (let j = 0; j < Object.keys(rowData).length; j++) {
      let key = Object.keys(rowData)[j]
      let cell = rowData[key]
      if (typeof cell == 'object') {
          if(cell.finalizeMandatory == false){
            allMandatory = false;
            break loop1;
          }
      }
    }
    return allMandatory;
  }

  isString(ob){
    return typeof ob === 'string'
  }
}

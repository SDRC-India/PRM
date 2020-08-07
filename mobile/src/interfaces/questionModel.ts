interface IQuestionModel {
          key : number,
          label: String,
          type: String,
          columnName: any,
          dependecy: boolean,

          frequency: String,
          roleId: Number,
          options: any[],
          typeId: Number,
          controlType: String,
          value: any,
          isOthersSelected: boolean,
          othersValue: any,
          parentId: Number,
          parentColumnName:string,
          prefetched: boolean,
          formId: Number,
          tableModel: any[],
          beginRepeat: any[],
          beginRepeatSize : number,
          beginrepeatDisableStatus:boolean,
          limit_bg_repeat:String,
          beginRepeatMinusDisable:boolean,
          bgDependentColumn:String,

        //   typeDetailIdOfDependencyType:number,
          maxLength: number,
          minLength: number,
          minValue: number,
          maxValue:number,
        //   typeDetailIdsOfDependencyTypes : String,
          defaultValue: Object,

          saveMandatory: boolean,
          finalizeMandatory: boolean,
          relevance: String,
          constraints: String,
          defaultSettings: String,
          features: String,

          disabled: boolean,
          displayComponent: boolean,
          displayScore: boolean ,
          scoreExp:string,
          scoreValue:any,

          duplicateFilesDetected : boolean,
          wrongFileExtensions : boolean,
          fileSizeExceeds : boolean,
          attachedFiles: any[],
          errorMsg:String,
          fileExtensions:String,

          reviewHeader?:string,
          cmsg?:string,
          showErrMessage?:boolean
          sectionName: string,
          subSectionName: string,
          questionOrder: number,
          questionOrderDisplay: boolean;
          attachmentsInBase64?:any;
          optionsOther?: any[],
          tempSaveMandatory?:boolean
          tempFinalizeMandatory?:boolean
        }

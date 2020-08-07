import { trigger, state, style, transition, animate, group } from '@angular/animations';
import { environment } from '@src/environments/environment';

export class Constants {
  // public static get HOME_URL(): string { return "https://devserver.sdrc.co.in/scpstamilnadu/"; };
  // public static get HOME_URL(): string { return "http://localhost:8080/scpstamilnadu/"; };

  public static get HOME_URL(): string { return "/covid19rmrs/"; };
  public static get API_URL(): string { return "/covid19rmrs/api/"; };
  public static get CMS_URL(): string { return "/cms/"; }
  public static get DOWNLOAD_TEMPLATE(): string {return 'Please download the template from here to update the data, only '
  + 'the template downloaded from here will accepted.'; }
  public static get UPLOAD_TEMPLATE(): string {return 'Please upload the template downloaded '
   + 'from this page. Any other template or a blank template will get rejected'; }
  public static get SERVER_ERROR_MESSAGE(): string { return 'Server error'; }
  public static BASE_URL = environment.apiBaseUrl;


  public static defaultImage: string;

  public static get INDEX_COMPUTE_FOOT_NOTE(): string { return 'The basis of calculation of indices have changed from TBD' }

  static message = {
    invalidPassword: "The password field should accept minimum 8 to maximum 13 characters and at least one uppercase, lowercase, numeric, special character",
    enterField: "Please Enter",
    /* not using */
    checkInternetConnection: "Please check your internet connection.",
    serverError: "Error connecting to server ! Please try after some time.",
    networkError: 'Server error.',
    pleaseWait: 'Please wait..',
    validUserName: 'Please enter username.',
    validPassword: 'Please enter Password.',
    dataClearMsg: 'Last user saved data will be erased. Are you sure you want to login?',
    invalidUser: 'No data entry facility available for state and national level user.',
    invalidUserNameOrPassword: 'Invalid usename or password.',
    syncingPleaseWait: 'Syncing please wait...',
    syncSuccessfull: 'Sync Successful.',
    getForm: 'Fetching forms from server, please wait...',
    warning: 'Warning',
    deleteFrom: 'Do you want to delete the selected record?',
    saveSuccess: 'Saved Successfully.',
    finalizedSuccess: 'Submitted Successfully.',
    fillAtleastOnField: 'Please fill data of atleast one field',
    autoSave: 'Auto save Successfully',
    anganwadiCenter: 'Please select the anganwadi center number.',
    schoolname: 'Please enter the school name.',
    respondentName: 'Please enter the respondent name.',
    womanName: 'Please enter the woman name.',
    errorWhileClearingFile: 'Error while deleting data of previous user.',
    clearingDataPleaseWait: 'Clearing data, please wait...',
    frameworknotapplicable: ' is not applicable for your Department.',
    uploadframework: 'Framework format is not correct'
  }

  public static fadeAnimation = [
    // the fade-in/fade-out animation.
    trigger('simpleFadeAnimation', [

      // the "in" style determines the "resting" state of the element when it is visible.
      state('in', style({ opacity: 1 })),

      // fade in when created. this could also be written as transition('void => *')
      transition(':enter', [
        style({ opacity: 0 }),
        animate(600)
      ]),

      // fade out when destroyed. this could also be written as transition('void => *')
      transition(':leave',
        animate(600, style({ opacity: 0 })))
    ])
  ]

  public static SlideDownAnimation = [
    trigger('slideInOut', [
      state('in', style({ height: '*', opacity: 0 })),
      transition(':leave', [
        style({ height: '*', opacity: 1 }),

        group([
          animate(300, style({ height: 0 })),
          animate('200ms ease-in-out', style({ 'opacity': '0' }))
        ])

      ]),
      transition(':enter', [
        style({ height: '0', opacity: 0 }),

        group([
          animate(300, style({ height: '*' })),
          animate('500ms ease-in-out', style({ 'opacity': '1' }))
        ])

      ])
    ])
  ];


  public static SlideUpAnimation = [
    trigger('slideUpIn', [
      state('in', style({ height: '*', opacity: 0 })),
      transition(':leave', [
        style({ top: '0' }),

        group([
          animate(1000, style({ top: '100%' })),
        ])

      ]),
      transition(':enter', [
        style({ top: '100%', opacity: 1 }),

        group([
          animate(1000, style({ top: '0' })),
          animate('500ms ease-in-out', style({ 'opacity': '1' }))
        ])

      ])
    ])
  ];

  public static SlideRightAnimation = [
    trigger('slideRightLeft', [
      state('in', style({ width: '*', opacity: 0 })),
      transition(':leave', [
        style({ width: '*', opacity: 1 }),

        group([
          animate(600, style({ width: 0 })),
          animate('400ms ease-in-out', style({ 'opacity': '0' }))
        ])

      ]),
      transition(':enter', [
        style({ width: '0', opacity: 1 }),

        group([
          animate(600, style({ width: '*' })),
          animate('1000ms ease-in-out', style({ 'opacity': '1' }))
        ])

      ])
    ])
  ];



  public static regularExp = {
    passwordRegx: /^(?!.*[\s])(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,13}$/g,
    // passwordRegx:/(?!.*[\s])(?=.*[a-zA-z])(?=.*\d)(?=.*[#$^+=!*()@%&]).{8,13}$/g,
    usernameRegx: /^\s*$/g,
    noWhiteSpaceAtBegining: /^[^.\s]/,
    username: /^[a-z0-9_-]{3,15}$/g,
    mobile: /^\d{10}$/g,
    pincode: /([0-9]{6}|[0-9]{3}\s[0-9]{3})/,
    email: /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  }

  public static statusColor ={
    confirmed:{color1:'#ffeda0'},
    active:{color1:'#feb24c'},
    recovered:{color1:'#feb24c'},
    deceased:{color1:'#FD8D3C'},


  }

}

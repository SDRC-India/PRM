
export class ConstantProvider {
    // static baseUrl : string = 'https://testserver.sdrc.co.in:8443/covid19rmrs/';
    static baseUrl : string = 'https://rahatmitraup.in/covid19rmrs/';
    // static baseUrl : string = 'http://prod4.sdrc.co.in/covid19rmrs/';
    // static baseUrl : string = 'https://uat.sdrc.co.in/covid19rmrs/'
    // static baseUrl : string ='https://prod2.sdrc.co.in/fani/';
    static defaultImage: string ='assets/imgs/uploadImage.png';
    static message: IMessages = {
    // checkInternetConnection: "Please check your internet connection.",
    // serverError:"Error connecting to server ! Please try after some time.",
    // networkError: 'Server error.',
    // pleaseWait: 'Please wait..',
    // validUserName: 'Please enter username.',
    // validPassword:'Please enter Password.',
    // dataClearMsg:'Last user saved data will be erased. Are you sure you want to login?',
    checkInternetConnection: "अपने इंटरनेट कनेक्शन की जाँच करें और पुन: प्रयास करें।",
    serverError:"सर्वर से कनेक्ट करने में त्रुटि। कृपया कुछ देर बाद प्रयास करें।",
    networkError: 'सर्वर त्रुटि।',
    pleaseWait: 'कृपया प्रतीक्षा कीजिये..',
    validUserName: 'कृपया उपयोगकर्ता नाम दर्ज करें।',
    validPassword:'कृप्या पास्वर्ड दर्ज करें।',
    dataClearMsg:'पिछले उपयोगकर्ता का सहेजा गया डेटा मिटा दिया जाएगा। क्या आप वाकई लॉग इन करना चाहते हैं?',

    invalidUser:'No data entry facility available for state and national level user.',
    invalidUserNameOrPassword:'अमान्य उपयोगकर्ता या पासवर्ड।',
    syncingPleaseWait: 'सर्वर पर डेटा जमा किया जा रहा है कृपया प्रतीक्षा करें ...',
    syncSuccessfull: 'सर्वर पे डेटा सबमिशन सफल।',
    getForm: 'फॉर्म सर्वर से लिया जा रहा है, कृपया प्रतीक्षा करें ...',
    warning: 'Warning',
    deleteFrom: 'क्या आप चयनित रिकॉर्ड को हटाना चाहते हैं?',
    saveSuccess: 'Saved Successfully.',
    finalizedSuccess: 'Finalized Successfully.',
    submittedSuccess: 'फ़ॉर्म सफलतापूर्वक सबमिट किया गया।',
    fillAtleastOnField: 'Please fill data of atleast one field',
    autoSave: 'Auto save Successfully',
    anganwadiCenter: 'Please select the anganwadi center number.',
    schoolname: 'Please enter the school name.',
    respondentName: 'Please enter the respondent name.',
    womanName: 'Please enter the woman name.',
    errorWhileClearingFile: 'पिछले उपयोगकर्ता का डेटा हटाते समय त्रुटि।',
    clearingDataPleaseWait: 'डेटा हटाया जा रहा है, कृपया प्रतीक्षा करें ...',
    formUpdationSuccess: 'पंजीकरण फॉर्म का अपडेट सफल',
    formUpdationNotFound: 'यह पंजीकरण फॉर्म का नवीनतम संस्करण है, अपडेट की आवश्यकता नहीं है।',
    formUpdating: 'सर्वर से पंजीकरण फॉर्म अपडेट की जांच की जा रही है, कृपया प्रतीक्षा करें ...'
  }
  static dbKeyNames: IDBKeyNames = {
    // user: "user",
    form: "form",
    // getAllForm: "getAllForm",
    // getBlankForm: "getBlankForm",
    submissionData:"submissionData",
    dataToSend:'dataToSend',
    // loginResponse: 'loginResponse'
    userAndForm : 'userAndForm',
    deadLineDate: 'deadLineDate',
    oldFormSanitized: "oldFormSanitized",
    REMEMBER_ME: "REMEMBER_ME"

  }

  anganwadiSection: AnganwadiSection = {
    section1: "Details of the Anganwadi workers",
    section2: "Reach of the center",
    section3: "Enrollment particulars",
    section4: "Facilities/ services available at the centre",
    section5: "Registers maintained at the centre  ",
    section6: "Services",
    section7: "Availability of services",
    section8: "Preschool Details",
  }
  static lastUpdatedDate : string  = "10-03-2020 19:53:52";
  static appFolderName: string ="upprma";
}

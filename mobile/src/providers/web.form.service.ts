import { Injectable } from "@angular/core";

@Injectable()
export class WebFormService {


    constructor() {

    }

    checkValueInObjectArray(val, objArr, objArrKey) {
        for (let i = 0; i < objArr.length; i++) {
            const obj = objArr[i];
            if(obj[objArrKey] == val) {
                return true;
            }
        }
        return false;
    }

}
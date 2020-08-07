interface IDbFormModel {
    createdDate: String,
    updatedDate: String,
    formStatus: string,
    extraKeys: any,
    formData: any,
    formSubmissionId: Number,
    uniqueId: String,
    formDataHead?:{},
    image:string,
    checked?:boolean,
    attachmentCount:number,
    visitedDate?: String,
    pendingForSyncRecord?: boolean,
    formId: string
}

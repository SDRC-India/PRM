interface FormIndicatorModel{
    key: number;
    label: string;
    aggRole: string;
    type: string;
    columnName: string;
    dependecy: boolean;
    dependentColumn: string;
    dependentCondition: any[];
    frequency: string;
    roleId: number;
    options: any[];
    typeId: number;
    controlType: string;
    value: string | number;
    isOthersSelected ?: boolean,
    othersValue ?: null,
    prefetched: boolean,
    formId: number
}

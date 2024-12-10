export interface CommonDescription {
  useControl: string;
  propertyCustomerJsonKey: any;
  status: string;
  isVisible: string;
  propertyDisplayName: string;
  propertyName: string;
  propertyDesc: string;
  showTrueOrFalse: boolean;
  propertyValue: string | number;
  sortNum: string;
  color: string | null;
  textSize: string | null;
  bold: boolean;
  readOnly: boolean;
  hasChild: boolean;
  childDisplayProperty: any[];
  highBrightnessDisplay: boolean;
  inputMethod?: string;
  tooltip?: boolean;
}

export enum BusinessType {
  TARGET = 'TARGET', // 目标箱
  SOURCE = 'SOURCE', // 来源箱
}

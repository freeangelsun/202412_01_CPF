export type ParameterType='STRING'|'INTEGER'|'LONG'|'DECIMAL'|'BOOLEAN'|'DATE'|'DATETIME'|'ENUM'|'JSON_OBJECT'|'SECRET_REFERENCE'|'PATH_ALIAS'|'SERVICE_REFERENCE'|'FILE_REFERENCE'|'CODE_REFERENCE'
export type ParameterDefinition={
  name:string;type:ParameterType;label:string;description?:string;required:boolean;sensitive:boolean;identifying?:boolean;
  runtimeOverrideAllowed:boolean;defaultValue?:unknown;allowedValues?:string[];pattern?:string;minValue?:number|null;maxValue?:number|null;
  minLength?:number|null;maxLength?:number|null;referenceType?:string;alias?:string;placeholder?:string;displayOrder?:number;
  visibleWhen?:{parameterName:string;operator:'ALWAYS'|'EQUALS'|'NOT_EQUALS'|'IN'|'PRESENT';values:string[]}
}
export type ParameterFieldError={field:string;code:string;message:string}
export const parameterTypes:ParameterType[]=['STRING','INTEGER','LONG','DECIMAL','BOOLEAN','DATE','DATETIME','ENUM','JSON_OBJECT','SECRET_REFERENCE','PATH_ALIAS','SERVICE_REFERENCE','FILE_REFERENCE','CODE_REFERENCE']
export function isReferenceType(type:ParameterType){return ['SECRET_REFERENCE','PATH_ALIAS','SERVICE_REFERENCE','FILE_REFERENCE','CODE_REFERENCE'].includes(type)}
export function visible(def:ParameterDefinition,values:Record<string,unknown>){const c=def.visibleWhen;if(!c||c.operator==='ALWAYS')return true;const actual=String(values[c.parameterName]??'');if(c.operator==='PRESENT')return actual.length>0;if(c.operator==='NOT_EQUALS')return !c.values.includes(actual);return c.values.includes(actual)}
export function validateParameter(def:ParameterDefinition,raw:unknown):ParameterFieldError|undefined{
 const text=String(raw??def.defaultValue??'').trim();if(!text)return def.required?{field:def.name,code:'REQUIRED',message:`${def.label} 값이 필요합니다.`}:undefined
 if(def.minLength!=null&&text.length<def.minLength)return{field:def.name,code:'MIN_LENGTH',message:`최소 길이는 ${def.minLength}입니다.`}
 if(def.maxLength!=null&&text.length>def.maxLength)return{field:def.name,code:'MAX_LENGTH',message:`최대 길이는 ${def.maxLength}입니다.`}
 if(def.pattern){try{if(!new RegExp(def.pattern).test(text))return{field:def.name,code:'PATTERN',message:'형식이 올바르지 않습니다.'}}catch{return{field:def.name,code:'SCHEMA_PATTERN',message:'등록된 Pattern이 올바르지 않습니다.'}}}
 if(def.allowedValues?.length&&!def.allowedValues.includes(text))return{field:def.name,code:'NOT_ALLOWED',message:'허용 값이 아닙니다.'}
 if(['INTEGER','LONG','DECIMAL'].includes(def.type)){const n=Number(text);if(!Number.isFinite(n))return{field:def.name,code:'TYPE_MISMATCH',message:'숫자 형식이 아닙니다.'};if(def.minValue!=null&&n<def.minValue)return{field:def.name,code:'MIN_VALUE',message:`최소값은 ${def.minValue}입니다.`};if(def.maxValue!=null&&n>def.maxValue)return{field:def.name,code:'MAX_VALUE',message:`최대값은 ${def.maxValue}입니다.`}}
 if(def.type==='BOOLEAN'&&!['true','false','Y','N'].includes(text))return{field:def.name,code:'TYPE_MISMATCH',message:'Boolean 값이 아닙니다.'}
 if(isReferenceType(def.type)&&!/^[A-Za-z0-9._:/-]{1,300}$/.test(text))return{field:def.name,code:'INVALID_REFERENCE',message:'승인된 Reference 형식이 아닙니다.'}
 if(def.type==='JSON_OBJECT'&&!(text.startsWith('{')&&text.endsWith('}')))return{field:def.name,code:'TYPE_MISMATCH',message:'JSON Object 형식이 아닙니다.'}
 return undefined
}

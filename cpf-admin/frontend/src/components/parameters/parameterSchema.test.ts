import {describe,expect,it} from 'vitest'
import {validateParameter,visible,type ParameterDefinition} from './parameterSchema'
const numberDef:ParameterDefinition={name:'timeout',type:'INTEGER',label:'Timeout',required:true,sensitive:false,runtimeOverrideAllowed:true,minValue:1,maxValue:300}
describe('parameterSchema',()=>{it('validates range',()=>{expect(validateParameter(numberDef,60)).toBeUndefined();expect(validateParameter(numberDef,0)?.code).toBe('MIN_VALUE')});it('applies visibility',()=>{const d:{visibleWhen:NonNullable<ParameterDefinition['visibleWhen']>} & ParameterDefinition={...numberDef,visibleWhen:{parameterName:'protocol',operator:'EQUALS',values:['HTTP']}};expect(visible(d,{protocol:'HTTP'})).toBe(true);expect(visible(d,{protocol:'TCP'})).toBe(false)})})

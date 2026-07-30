export interface MenuRow {menuCode:string;parentMenuCode?:string|null;menuName:string;routePath?:string|null;sortOrder?:number;useYn?:string;versionNo?:number;[key:string]:unknown}
export interface MenuNode extends MenuRow {children:MenuNode[];depth:number;orphan:boolean;cycle:boolean}

export function buildMenuTree(rows:MenuRow[]):MenuNode[]{
  const byCode=new Map<string,MenuNode>();
  for(const row of rows){if(!row.menuCode||byCode.has(row.menuCode))continue;byCode.set(row.menuCode,{...row,children:[],depth:0,orphan:false,cycle:false});}
  const roots:MenuNode[]=[];
  const createsCycle=(node:MenuNode,parent:MenuNode)=>{let current:MenuNode|undefined=parent;const seen=new Set<string>([node.menuCode]);while(current){if(seen.has(current.menuCode))return true;seen.add(current.menuCode);current=current.parentMenuCode?byCode.get(current.parentMenuCode):undefined;}return false;};
  for(const node of byCode.values()){
    const parentCode=String(node.parentMenuCode||"");
    if(!parentCode){roots.push(node);continue;}
    const parent=byCode.get(parentCode);
    if(!parent){node.orphan=true;roots.push(node);continue;}
    if(createsCycle(node,parent)){node.cycle=true;roots.push(node);continue;}
    parent.children.push(node);
  }
  const sort=(nodes:MenuNode[],depth=0)=>nodes.sort((a,b)=>Number(a.sortOrder||0)-Number(b.sortOrder||0)||a.menuCode.localeCompare(b.menuCode)).map(n=>{n.depth=depth;n.children=sort(n.children,depth+1);return n;});
  return sort(roots);
}

export function flattenMenuTree(nodes:MenuNode[]):MenuNode[]{return nodes.flatMap(node=>[node,...flattenMenuTree(node.children)]);}
export function descendantCodes(rows:MenuRow[],menuCode:string):string[]{const result:string[]=[];const visit=(code:string)=>{for(const row of rows.filter(r=>r.parentMenuCode===code)){result.push(row.menuCode);visit(row.menuCode);}};visit(menuCode);return result;}
export function validateParentMove(rows:MenuRow[],menuCode:string,parentMenuCode?:string|null):string|null{
  if(!parentMenuCode)return null;if(menuCode===parentMenuCode)return "자기 자신을 상위 메뉴로 지정할 수 없습니다.";
  if(descendantCodes(rows,menuCode).includes(parentMenuCode))return "하위 메뉴로 이동하면 순환 구조가 발생합니다.";
  if(!rows.some(r=>r.menuCode===parentMenuCode))return "상위 메뉴가 존재하지 않습니다.";return null;
}

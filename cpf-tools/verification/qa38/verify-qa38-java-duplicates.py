from pathlib import Path
import re,sys,collections
R=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[3]
errors=[]

def sanitize(text):
 out=list(text);i=0;state='code';quote=''
 while i<len(text):
  c=text[i];n=text[i+1] if i+1<len(text) else ''
  if state=='code':
   if c=='/' and n=='/': out[i]=out[i+1]=' ';state='line';i+=2;continue
   if c=='/' and n=='*': out[i]=out[i+1]=' ';state='block';i+=2;continue
   if c in ('"',"'"): out[i]=' ';state='string';quote=c;i+=1;continue
  elif state=='line':
   if c=='\n': state='code'
   else: out[i]=' '
  elif state=='block':
   if c=='*' and n=='/': out[i]=out[i+1]=' ';state='code';i+=2;continue
   if c!='\n':out[i]=' '
  elif state=='string':
   if c=='\\': out[i]=' ';
   if c=='\\' and i+1<len(text): out[i+1]=' ';i+=2;continue
   if c==quote: out[i]=' ';state='code';i+=1;continue
   if c!='\n':out[i]=' '
  i+=1
 return ''.join(out)

def class_ranges(s):
 ranges=[]
 for m in re.finditer(r'\b(class|record|interface|enum)\s+(\w+)[^{;]*\{',s):
  op=s.find('{',m.start());depth=0;close=None
  for i in range(op,len(s)):
   if s[i]=='{':depth+=1
   elif s[i]=='}':
    depth-=1
    if depth==0:close=i;break
  if close is not None:ranges.append((op,close,m.group(2)))
 return ranges

def owner(ranges,pos):
 c=[r for r in ranges if r[0]<pos<r[1]]
 return min(c,key=lambda r:r[1]-r[0]) if c else None

for p in R.rglob('src/main/**/*.java'):
 text=p.read_text(encoding='utf-8');s=sanitize(text);ranges=class_ranges(s)
 fields=collections.defaultdict(list);methods=collections.defaultdict(list)
 for m in re.finditer(r'\b(?:public|protected|private)\s+(?:static\s+)?(?:final\s+)?(?:volatile\s+)?[\w.$<>?,\[\] ]+\s+(\w+)\s*(?:=[^;{}]*)?;',s):
  o=owner(ranges,m.start());
  if o: fields[(o[0],o[2])].append(m.group(1))
 for m in re.finditer(r'\b(?:public|protected|private)\s+(?:static\s+)?(?:final\s+)?(?:synchronized\s+)?[\w.$<>?,\[\] ]+\s+(\w+)\s*\(([^)]*)\)',s):
  o=owner(ranges,m.start());
  if not o:continue
  params=m.group(2).strip();types=[]
  if params:
   # generics with commas are rare here; normalize by dropping parameter names
   for part in params.split(','):
    toks=re.sub(r'\bfinal\s+','',part.strip()).split()
    types.append(' '.join(toks[:-1]) if len(toks)>1 else part.strip())
  methods[(o[0],o[2])].append((m.group(1),tuple(types)))
 for (start,cl),items in fields.items():
  for name,c in collections.Counter(items).items():
   if c>1:errors.append(f'duplicate field {cl}.{name} x{c}: {p.relative_to(R)}')
 for (start,cl),items in methods.items():
  for sig,c in collections.Counter(items).items():
   if c>1:errors.append(f'duplicate method {cl}.{sig} x{c}: {p.relative_to(R)}')
print('JAVA_FILES',sum(1 for _ in R.rglob('src/main/**/*.java')))
if errors:
 print('\n'.join('ERROR '+e for e in errors));sys.exit(1)
print('JAVA CLASS-SCOPE DUPLICATE MEMBER VALIDATION PASS')

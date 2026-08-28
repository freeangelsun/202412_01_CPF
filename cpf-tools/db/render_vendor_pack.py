#!/usr/bin/env python3
"""Render CPF current DB snapshots from one canonical model.

Historical migration directories are intentionally never written by this tool.
The renderer owns only cpf-tools/db/generated/current/<vendor>.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
from collections import defaultdict, deque
from pathlib import Path

OFFICIAL = ("mariadb", "postgresql", "oracle")


def read_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def write_text(path: Path, text: str) -> str:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text.rstrip() + "\n", encoding="utf-8", newline="\n")
    return hashlib.sha256(path.read_bytes()).hexdigest()


def parse_type(raw: str):
    s = raw.strip().upper()
    m = re.fullmatch(r"([A-Z]+)(?:\(([^)]+)\))?", s)
    if not m:
        raise ValueError(f"unsupported canonical type: {raw}")
    return m.group(1), tuple(int(x.strip()) for x in m.group(2).split(',')) if m.group(2) else ()


def render_type(vendor: str, raw: str) -> str:
    base, args = parse_type(raw)
    if base in {"INT", "INTEGER"}:
        return "INT" if vendor == "mariadb" else ("INTEGER" if vendor == "postgresql" else "NUMBER(10)")
    if base == "BIGINT":
        return "BIGINT" if vendor != "oracle" else "NUMBER(19)"
    if base == "SMALLINT":
        return "SMALLINT" if vendor != "oracle" else "NUMBER(5)"
    if base == "DECIMAL":
        p, s = args
        return f"DECIMAL({p},{s})" if vendor != "oracle" else f"NUMBER({p},{s})"
    if base in {"VARCHAR", "CHAR"}:
        n = args[0]
        if vendor == "oracle":
            return f"{'VARCHAR2' if base == 'VARCHAR' else 'CHAR'}({n} CHAR)"
        return f"{base}({n})"
    if base in {"TEXT", "MEDIUMTEXT", "LONGTEXT", "CLOB"}:
        if vendor == "mariadb":
            return {"TEXT":"TEXT","MEDIUMTEXT":"MEDIUMTEXT","LONGTEXT":"LONGTEXT","CLOB":"LONGTEXT"}[base]
        return "TEXT" if vendor == "postgresql" else "CLOB"
    if base in {"LONGBLOB", "BLOB"}:
        return "LONGBLOB" if vendor == "mariadb" else ("BYTEA" if vendor == "postgresql" else "BLOB")
    if base == "VARBINARY":
        n = args[0]
        return f"VARBINARY({n})" if vendor == "mariadb" else ("BYTEA" if vendor == "postgresql" else f"RAW({n})")
    if base == "DATE":
        return "DATE"
    if base == "TIME":
        return "TIME" if vendor != "oracle" else "VARCHAR2(15 CHAR)"
    if base in {"DATETIME", "TIMESTAMP"}:
        precision = args[0] if args else None
        if vendor == "mariadb":
            return f"{'DATETIME' if base == 'DATETIME' else 'TIMESTAMP'}{f'({precision})' if precision is not None else ''}"
        if vendor == "postgresql":
            return f"TIMESTAMP{f'({precision})' if precision is not None else ''} WITHOUT TIME ZONE"
        return f"TIMESTAMP{f'({precision})' if precision is not None else ''}"
    raise ValueError(f"unsupported canonical type: {raw}")


def render_default(vendor: str, raw):
    if raw is None:
        return None
    s = str(raw).strip()
    if s.upper() == "NULL":
        return "NULL"
    m = re.fullmatch(r"CURRENT_TIMESTAMP(?:\((\d+)\))?", s, re.I)
    if m:
        p = m.group(1)
        return f"CURRENT_TIMESTAMP{f'({p})' if p else ''}"
    return s


def table_order(tables):
    names = {t["targetTableName"] for t in tables}
    indeg = {n: 0 for n in names}
    edges = defaultdict(list)
    for t in tables:
        child = t["targetTableName"]
        for fk in t.get("foreignKeys") or []:
            parent = fk["refTable"]
            if parent in names and parent != child:
                edges[parent].append(child)
                indeg[child] += 1
    q = deque(sorted(n for n, d in indeg.items() if d == 0))
    out = []
    while q:
        n = q.popleft(); out.append(n)
        for c in sorted(edges[n]):
            indeg[c] -= 1
            if indeg[c] == 0: q.append(c)
    if len(out) != len(names):
        cyc = sorted(n for n,d in indeg.items() if d)
        raise ValueError(f"foreign-key cycle in canonical model: {cyc[:20]}")
    rank = {n:i for i,n in enumerate(out)}
    return sorted(tables, key=lambda t: rank[t["targetTableName"]])


def q_ident(vendor: str, name: str) -> str:
    # Canonical names are governed and currently portable; avoid quoted identifiers.
    if not re.fullmatch(r"[A-Za-z][A-Za-z0-9_]*", name):
        raise ValueError(f"unsafe identifier: {name}")
    return name


def render_index_columns(vendor: str, index) -> str:
    """Render portable index columns plus an explicit vendor-only override.

    Prefix lengths such as ``column(255)`` are MariaDB syntax, not a portable
    expression.  Keep canonical ``columns`` portable and require any vendor
    specialization to be declared by the index owner.
    """
    columns = (index.get("vendorColumns") or {}).get(vendor, index["columns"])
    pattern = r"[A-Za-z][A-Za-z0-9_]*(?:\(\d+\))?" if vendor == "mariadb" else r"[A-Za-z][A-Za-z0-9_]*"
    for column in columns:
        if not re.fullmatch(pattern, column):
            raise ValueError(f"unsafe {vendor} index column: {column}")
    return ", ".join(columns)


def render_column(vendor, col):
    name = q_ident(vendor, col["name"])
    typ = render_type(vendor, col["type"])
    bits = [name, typ]
    if col.get("autoIncrement"):
        if vendor == "mariadb": bits.append("AUTO_INCREMENT")
        elif vendor == "postgresql": bits.append("GENERATED BY DEFAULT AS IDENTITY")
        else: bits.append("GENERATED BY DEFAULT AS IDENTITY")
    default = render_default(vendor, col.get("default"))
    if default is not None:
        bits.extend(["DEFAULT", default])
    bits.append("NULL" if col.get("nullable") else "NOT NULL")
    if col.get("onUpdate") and vendor == "mariadb":
        bits.extend(["ON UPDATE", render_default(vendor, col["onUpdate"])])
    return "    " + " ".join(bits)


def render_table(vendor, table):
    name = q_ident(vendor, table["targetTableName"])
    clauses = [render_column(vendor, c) for c in table["columns"]]
    if table.get("primaryKey"):
        clauses.append("    CONSTRAINT PK_%s PRIMARY KEY (%s)" % (name, ", ".join(table["primaryKey"])))
    for uk in table.get("uniqueKeys") or []:
        clauses.append("    CONSTRAINT %s UNIQUE (%s)" % (q_ident(vendor, uk["name"]), ", ".join(uk["columns"])))
    for ck in table.get("checks") or []:
        expression = (ck.get("vendorExpressions") or {}).get(vendor, ck["expression"])
        clauses.append("    CONSTRAINT %s CHECK (%s)" % (q_ident(vendor, ck["name"]), expression))
    for fk in table.get("foreignKeys") or []:
        line = "    CONSTRAINT %s FOREIGN KEY (%s) REFERENCES %s (%s)" % (
            q_ident(vendor, fk["name"]), ", ".join(fk["columns"]), q_ident(vendor, fk["refTable"]), ", ".join(fk["refColumns"])
        )
        on_delete = str(fk.get("onDelete") or "").strip().upper()
        # Oracle implements RESTRICT/NO ACTION as the implicit default and
        # rejects both spellings in an explicit ON DELETE clause.
        if on_delete and not (vendor == "oracle" and on_delete in {"RESTRICT", "NO ACTION"}):
            line += " ON DELETE " + on_delete
        clauses.append(line)
    ddl = f"CREATE TABLE {name} (\n" + ",\n".join(clauses) + "\n)"
    if vendor == "mariadb": ddl += " ENGINE=InnoDB"
    ddl += ";\n"
    # comments are emitted separately and escaped.
    comment = (table.get("comment") or "").replace("'", "''")
    if comment:
        if vendor == "mariadb": ddl += f"ALTER TABLE {name} COMMENT = '{comment}';\n"
        else: ddl += f"COMMENT ON TABLE {name} IS '{comment}';\n"
    for c in table["columns"]:
        cc=(c.get("comment") or "").replace("'", "''")
        if cc and vendor != "mariadb":
            ddl += f"COMMENT ON COLUMN {name}.{c['name']} IS '{cc}';\n"
    for idx in table.get("indexes") or []:
        unique = "UNIQUE " if idx.get("unique") else ""
        ddl += f"CREATE {unique}INDEX {idx['name']} ON {name} ({render_index_columns(vendor, idx)});\n"
    return ddl


def render_touch_support(vendor, tables):
    touch = []
    for t in tables:
        cols=[c for c in t["columns"] if c.get("onUpdate")]
        if cols:
            touch.append((t, cols))
    if vendor == "mariadb" or not touch:
        return ""
    out=[]
    if vendor == "postgresql":
        out.append("CREATE OR REPLACE FUNCTION CPF_TOUCH_UPDATED_AT() RETURNS TRIGGER AS $$\nBEGIN\n  NEW.updated_at = CURRENT_TIMESTAMP;\n  RETURN NEW;\nEND;\n$$ LANGUAGE plpgsql;\n")
        for t,cols in touch:
            # Current canonical onUpdate is uniformly updated_at; gate verifies this.
            out.append(f"CREATE TRIGGER TRG_{t['targetTableName']}_TOUCH BEFORE UPDATE ON {t['targetTableName']} FOR EACH ROW EXECUTE FUNCTION CPF_TOUCH_UPDATED_AT();\n")
    else:
        for t,cols in touch:
            col=cols[0]["name"]
            trg=("TRG_"+t["targetTableName"]+"_TOUCH")[:128]
            out.append(f"CREATE OR REPLACE TRIGGER {trg}\nBEFORE UPDATE ON {t['targetTableName']}\nFOR EACH ROW\nBEGIN\n  :NEW.{col} := CURRENT_TIMESTAMP;\nEND;\n/\n")
    return "\n".join(out)


def render_schema(vendor, schema, role):
    tables = [t for t in schema["tables"] if t.get('targetDatabaseRole') == role]
    ordered = table_order(tables)
    header = [
        "-- GENERATED FILE. DO NOT EDIT.",
        "-- Source: cpf-tools/db/canonical/platform-schema.json",
        f"-- Canonical schemaVersion: {schema['schemaVersion']}",
        f"-- Vendor: {vendor}",
        "-- Historical migrations are immutable and are not generated by this renderer.",
        ""
    ]
    body = []
    for t in ordered:
        body.append(render_table(vendor,t))
    body.append(render_touch_support(vendor, ordered))
    return "\n".join(header+body)


def render_non_table(vendor, contract, table_map):
    lines=["-- GENERATED FILE. DO NOT EDIT.","-- Source: cpf-tools/db/canonical/platform-non-table-objects.json",""]
    cfg=contract["vendorDefinition"][vendor]
    for obj in contract.get("objects") or []:
        name=obj["name"]
        # Current Batch tables are currentized to BAT_SB_*. Sequence names follow the same owner prefix.
        if obj.get("idTable") in table_map:
            tgt=table_map[obj["idTable"]]
            if name.startswith("BATCH_"): name="BAT_SB_"+name[len("BATCH_"):]
        if vendor == "mariadb":
            lines.append(f"CREATE SEQUENCE {name} START WITH {cfg['startWith']} MINVALUE {cfg['minValue']} MAXVALUE {cfg['maxValue']} INCREMENT BY {cfg['incrementBy']} {cfg.get('cache','NOCACHE')} {cfg.get('cycle','NOCYCLE')};")
        elif vendor == "postgresql":
            lines.append(f"CREATE SEQUENCE {name} START WITH {cfg['startWith']} MINVALUE {cfg['minValue']} MAXVALUE {cfg['maxValue']} INCREMENT BY {cfg['incrementBy']} {cfg.get('cycle','NO CYCLE')};")
        else:
            lines.append(f"CREATE SEQUENCE {name} START WITH {cfg['startWith']} MINVALUE {cfg['minValue']} MAXVALUE {cfg['maxValue']} INCREMENT BY {cfg['incrementBy']} {cfg.get('cache','NOCACHE')} {cfg.get('order','NOORDER')} {cfg.get('cycle','NOCYCLE')};")
    return "\n".join(lines)+"\n"


def render_rollback(vendor, schema, role):
    tables=[t for t in schema["tables"] if t.get('targetDatabaseRole')==role]
    ordered=table_order(tables)
    lines=["-- GENERATED FILE. DO NOT EDIT.", f"-- Role: {role}", f"-- Vendor: {vendor}", ""]
    for t in reversed(ordered):
        name=t['targetTableName']
        if vendor=='oracle':
            lines.append(f"BEGIN EXECUTE IMMEDIATE 'DROP TABLE {name} CASCADE CONSTRAINTS'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;\n/")
        else:
            lines.append(f"DROP TABLE IF EXISTS {name} CASCADE;")
    return "\n".join(lines)+"\n"


def render_verify(vendor, schema, role):
    tables=[t for t in schema["tables"] if t.get('targetDatabaseRole')==role]
    lines=["-- GENERATED FILE. DO NOT EDIT.", f"-- Role: {role}", f"-- Vendor: {vendor}", ""]
    for t in sorted(tables,key=lambda x:x['targetTableName']):
        name=t['targetTableName']
        if vendor=='oracle':
            lines.append(f"SELECT '{name}' AS table_name, COUNT(*) AS present FROM USER_TABLES WHERE TABLE_NAME = '{name.upper()}';")
        else:
            lines.append(f"SELECT '{name}' AS table_name, COUNT(*) AS present FROM information_schema.tables WHERE UPPER(table_name) = '{name.upper()}';")
    return "\n".join(lines)+"\n"

def remap_seed_text(text: str, name_map: dict[str,str]) -> str:
    # Longest-first, word boundary current-name to target-name mapping; also strips old cpfDB qualifiers.
    out=text
    out=re.sub(r"\b(?:cpfDB|cmnDB|admDB|batDB|bzaDB|refDB)\.", "", out, flags=re.I)
    for old,new in sorted(name_map.items(), key=lambda kv: len(kv[0]), reverse=True):
        out=re.sub(rf"(?<![A-Za-z0-9_]){re.escape(old)}(?![A-Za-z0-9_])", new, out, flags=re.I)
    return out


def convert_time_expr(vendor: str, text: str) -> str:
    if vendor == "mariadb": return text
    temporal_base = r"(?:NOW\(\d*\)|CURRENT_TIMESTAMP(?:\(\d+\))?|CURRENT_DATE|[A-Za-z_][A-Za-z0-9_.]*)"
    date_math = re.compile(
        rf"DATE_(?P<direction>ADD|SUB)\(\s*(?P<base>{temporal_base})\s*,\s*"
        r"INTERVAL\s+(?P<amount>\d+)\s+(?P<unit>MINUTE|HOUR|DAY)\s*\)",
        re.I,
    )

    def render_date_math(match: re.Match[str]) -> str:
        base = re.sub(
            r"NOW\(\d*\)",
            "CURRENT_TIMESTAMP" if vendor == "postgresql" else "SYSTIMESTAMP",
            match.group("base"),
            flags=re.I,
        )
        operator = "+" if match.group("direction").upper() == "ADD" else "-"
        amount = match.group("amount")
        unit = match.group("unit")
        if vendor == "postgresql":
            return f"({base} {operator} INTERVAL '{amount} {unit.lower()}')"
        return f"({base} {operator} INTERVAL '{amount}' {unit.upper()})"

    text=date_math.sub(render_date_math,text)
    text=re.sub(
        r"NOW\(\d*\)",
        "CURRENT_TIMESTAMP" if vendor == "postgresql" else "SYSTIMESTAMP",
        text,
        flags=re.I,
    )
    text=re.sub(
        r"CURRENT_TIMESTAMP\(\d+\)",
        "CURRENT_TIMESTAMP" if vendor == "postgresql" else "SYSTIMESTAMP",
        text,
        flags=re.I,
    )
    if vendor=='postgresql':
        text=re.sub(r"CAST\(CONCAT\(CURRENT_DATE,\s*' 02:00:00'\) AS DATETIME\)", "CURRENT_DATE + TIME '02:00:00'", text, flags=re.I)
        text=re.sub(r"\bLIMIT\s+1\b", "FETCH FIRST 1 ROW ONLY", text, flags=re.I)
    elif vendor=='oracle':
        text=re.sub(r"CAST\(CONCAT\(CURRENT_DATE,\s*' 02:00:00'\) AS DATETIME\)", "CAST(CURRENT_DATE AS TIMESTAMP) + INTERVAL '2' HOUR", text, flags=re.I)
        text=re.sub(r"\bLIMIT\s+1\b", "FETCH FIRST 1 ROW ONLY", text, flags=re.I)
        text=re.sub(r"\bDATE\(\s*('(?:''|[^'])*')\s*\)", r"TO_DATE(\1, 'YYYY-MM-DD HH24:MI:SS.FF')", text, flags=re.I)
    return text


def _split_top_level(text: str, delimiter: str=',') -> list[str]:
    """Split SQL expressions on a delimiter while respecting quotes/parentheses."""
    out=[]; buf=[]; depth=0; i=0; quote=None
    while i < len(text):
        ch=text[i]
        if quote:
            buf.append(ch)
            if ch==quote:
                if i+1 < len(text) and text[i+1]==quote:
                    buf.append(text[i+1]); i+=2; continue
                quote=None
            i+=1; continue
        if ch in ("'", '"'):
            quote=ch; buf.append(ch); i+=1; continue
        if ch=='(':
            depth+=1
        elif ch==')':
            depth=max(0,depth-1)
        if ch==delimiter and depth==0:
            out.append(''.join(buf).strip()); buf=[]
        else:
            buf.append(ch)
        i+=1
    if ''.join(buf).strip(): out.append(''.join(buf).strip())
    return out


def _split_value_rows(source: str) -> list[list[str]]:
    """Parse canonical VALUES tuple list into expression rows without evaluating SQL."""
    rows=[]; i=0; n=len(source)
    while i<n:
        while i<n and (source[i].isspace() or source[i]==','): i+=1
        if i>=n: break
        if source[i] != '(':
            raise ValueError(f'VALUES source must contain parenthesized rows near: {source[i:i+80]}')
        start=i+1; depth=1; quote=None; i+=1
        while i<n and depth:
            ch=source[i]
            if quote:
                if ch==quote:
                    if i+1<n and source[i+1]==quote: i+=2; continue
                    quote=None
                i+=1; continue
            if ch in ("'", '"'): quote=ch; i+=1; continue
            if ch=='(': depth+=1
            elif ch==')': depth-=1
            i+=1
        if depth: raise ValueError('unterminated VALUES tuple')
        rows.append(_split_top_level(source[start:i-1]))
    return rows


def _find_top_level_from(select_sql: str) -> int:
    quote=None; depth=0; i=0; upper=select_sql.upper()
    while i<len(select_sql):
        ch=select_sql[i]
        if quote:
            if ch==quote:
                if i+1<len(select_sql) and select_sql[i+1]==quote: i+=2; continue
                quote=None
            i+=1; continue
        if ch in ("'", '"'): quote=ch; i+=1; continue
        if ch=='(': depth+=1
        elif ch==')': depth=max(0,depth-1)
        elif depth==0 and upper.startswith('FROM', i):
            before=select_sql[i-1] if i else ' '; after=select_sql[i+4] if i+4<len(select_sql) else ' '
            if not (before.isalnum() or before=='_') and not (after.isalnum() or after=='_'): return i
        i+=1
    return -1


def _alias_select_projection(source: str, columns: list[str]) -> str:
    m=re.match(r'(?is)^\s*SELECT\s+',source)
    if not m: raise ValueError('Oracle canonical MERGE source must start with SELECT')
    body=source[m.end():]; pos=_find_top_level_from(body)
    if pos<0: projection=body.strip(); tail=''
    else: projection=body[:pos].strip(); tail=body[pos:].strip()
    exprs=_split_top_level(projection)
    if len(exprs)!=len(columns):
        raise ValueError(f'canonical SELECT projection/column mismatch {len(exprs)} != {len(columns)}: {source[:160]}')
    aliased=', '.join(f'{expr} AS {col}' for expr,col in zip(exprs,columns))
    return 'SELECT '+aliased+((' '+tail) if tail else ' FROM dual')


def _replace_sql_variables(text: str, variables: dict[str,str]) -> str:
    """Replace @var outside quoted SQL literals only; unknown variables remain detectable."""
    out=[]; i=0; quote=None
    while i<len(text):
        ch=text[i]
        if quote:
            out.append(ch)
            if ch==quote:
                if i+1<len(text) and text[i+1]==quote:
                    out.append(text[i+1]); i+=2; continue
                quote=None
            i+=1; continue
        if ch in ("'", '"'):
            quote=ch; out.append(ch); i+=1; continue
        if ch=='@':
            m=re.match(r'@([A-Za-z_][A-Za-z0-9_]*)', text[i:])
            if m:
                name=m.group(1)
                if name in variables:
                    expr=variables[name].strip()
                    out.append(expr if (expr.startswith('(') and expr.endswith(')')) else f'({expr})')
                    i+=len(m.group(0)); continue
        out.append(ch); i+=1
    return ''.join(out)


def _sql_variables(text: str) -> list[str]:
    # Detect variables outside quoted strings by replacing known-none and then scanning with the same state machine.
    found=[]; i=0; quote=None
    while i<len(text):
        ch=text[i]
        if quote:
            if ch==quote:
                if i+1<len(text) and text[i+1]==quote: i+=2; continue
                quote=None
            i+=1; continue
        if ch in ("'", '"'): quote=ch; i+=1; continue
        if ch=='@':
            m=re.match(r'@([A-Za-z_][A-Za-z0-9_]*)', text[i:])
            if m: found.append(m.group(1)); i+=len(m.group(0)); continue
        i+=1
    return found


def _oracle_merge_from_values(table: str, columns: list[str], source: str, conflict: list[str], updates: list[dict], name_map: dict[str,str]) -> str:
    if not conflict: raise ValueError(f'Oracle canonical upsert requires conflict columns: {table}')
    selects=[]
    rows=_split_value_rows(source)
    for row in rows:
        if len(row)!=len(columns): raise ValueError(f'{table}: VALUES width {len(row)} != columns {len(columns)}')
        selects.append('SELECT '+', '.join(f'{expr} AS {col}' for expr,col in zip(row,columns))+' FROM dual')
    if not selects: raise ValueError(f'{table}: canonical VALUES seed is empty')
    on=' AND '.join(f'tgt.{c}=src.{c}' for c in conflict)
    pairs=[]
    for u in updates:
        expr=remap_seed_text(u['expression'],name_map)
        expr=re.sub(r'VALUES\(([^)]+)\)',r'src.\1',expr,flags=re.I)
        pairs.append(f'tgt.{u["column"]}={expr}')
    insert_cols=', '.join(columns); insert_vals=', '.join('src.'+c for c in columns)
    statements=[]
    for using in selects:
        stmt=f'MERGE INTO {table} tgt\nUSING ({using}) src\nON ({on})'
        if pairs: stmt+='\nWHEN MATCHED THEN UPDATE SET '+', '.join(pairs)
        stmt+=f'\nWHEN NOT MATCHED THEN INSERT ({insert_cols}) VALUES ({insert_vals});'
        statements.append(stmt)
    return '\n'.join(statements)


def _oracle_merge_from_select(table: str, columns: list[str], source: str, conflict: list[str], updates: list[dict], name_map: dict[str,str]) -> str:
    if not conflict: return f'INSERT INTO {table} ({", ".join(columns)})\n{source.rstrip(";")};'
    using=_alias_select_projection(source,columns)
    on=' AND '.join(f'tgt.{c}=src.{c}' for c in conflict)
    pairs=[]
    for u in updates:
        expr=remap_seed_text(u['expression'],name_map)
        expr=re.sub(r'VALUES\(([^)]+)\)',r'src.\1',expr,flags=re.I)
        pairs.append(f'tgt.{u["column"]}={expr}')
    insert_cols=', '.join(columns); insert_vals=', '.join('src.'+c for c in columns)
    stmt=f'MERGE INTO {table} tgt\nUSING ({using}) src\nON ({on})'
    if pairs: stmt+='\nWHEN MATCHED THEN UPDATE SET '+', '.join(pairs)
    stmt+=f'\nWHEN NOT MATCHED THEN INSERT ({insert_cols}) VALUES ({insert_vals});'
    return stmt


def render_seed_statement(vendor, st, name_map, variables):
    kind=st.get("statementKind")
    if kind=='use': return ""
    if kind in {'delete','update'}:
        sql=_replace_sql_variables(st['sql'],variables)
        unknown=_sql_variables(sql)
        if unknown: raise ValueError(f'unresolved canonical seed variable(s) {unknown}: {st.get("sourceFile")}')
        return convert_time_expr(vendor,remap_seed_text(sql,name_map)).rstrip(';')+';'
    if kind=='set':
        var=st['variable']; expr=remap_seed_text(st['expression'],name_map)
        expr=_replace_sql_variables(expr,variables)
        unknown=_sql_variables(expr)
        if unknown: raise ValueError(f'unresolved canonical seed variable(s) {unknown} while defining {var}')
        expr=convert_time_expr(vendor,expr).strip()
        variables[var]=expr
        if vendor=='mariadb': return f"SET @{var} = {expr};"
        # PG/Oracle inline this canonical expression into later statements; no session variable is required.
        return f"-- CPF_SEED_INLINE_VARIABLE {var}"
    if kind!='insert':
        raise ValueError(f"unsupported seed statementKind: {kind}")
    table=remap_seed_text(st.get('tableName') or st['table'],name_map)
    columns=list(st['columns']); cols=', '.join(columns)
    source=remap_seed_text(st['source'],name_map)
    source=_replace_sql_variables(source,variables)
    unknown=_sql_variables(source)
    if unknown: raise ValueError(f'{table}: unresolved canonical seed variable(s) {unknown}')
    source=convert_time_expr(vendor,source)
    updates=st.get('updates') or []
    conflict=st.get('conflictColumns') or []
    source_columns={column.lower() for column in columns}
    conflict_columns={column.lower() for column in conflict}
    for update in updates:
        for referenced in re.findall(r'VALUES\(([A-Za-z0-9_]+)\)', update['expression'], flags=re.I):
            if referenced.lower() not in source_columns:
                raise ValueError(
                    f'{table} update references VALUES({referenced}) but the source column is absent'
                )
        if update['column'].lower() in conflict_columns:
            raise ValueError(f'{table} update column {update["column"]} is also a conflict column')
    if vendor=='oracle':
        if st.get('sourceKind')=='values':
            return _oracle_merge_from_values(table,columns,source,conflict,updates,name_map)
        if st.get('sourceKind')=='select':
            return _oracle_merge_from_select(table,columns,source,conflict,updates,name_map)
        raise ValueError(f'{table}: unsupported sourceKind {st.get("sourceKind")}')
    sql=f"INSERT INTO {table} ({cols})\n{('VALUES '+source) if st.get('sourceKind')=='values' else source}"
    if updates:
        if vendor=='mariadb':
            pairs=[]
            for u in updates:
                expr=remap_seed_text(u['expression'],name_map)
                pairs.append(f"{u['column']}={expr}")
            sql += "\nON DUPLICATE KEY UPDATE " + ', '.join(pairs)
        elif vendor=='postgresql' and conflict:
            pairs=[]
            for u in updates:
                expr=re.sub(r"VALUES\(([^)]+)\)",r"EXCLUDED.\1",u['expression'],flags=re.I)
                pairs.append(f"{u['column']}={expr}")
            sql += f"\nON CONFLICT ({', '.join(conflict)}) DO UPDATE SET " + ', '.join(pairs)
    return sql.rstrip(';')+';'

def render_seed(vendor, seed, name_map, role, target_role_map):
    lines=["-- GENERATED FILE. DO NOT EDIT.","-- Source: cpf-tools/db/canonical/seed-model.json",f"-- Vendor: {vendor}",f"-- Role: {role}",""]
    role_by_db={'cpfDB':'CPF_PLATFORM_DB','mbwDB':'CUSTOMER_BUSINESS_DB','referenceFixture':'REFERENCE_FIXTURE'}
    variables={}
    for st in seed['statements']:
        statement_role=role_by_db.get(st.get('logicalDatabase'))
        if st.get('statementKind')=='insert':
            raw=(st.get('tableName') or st.get('table') or '').split('.')[-1]
            mapped=name_map.get(raw, raw)
            statement_role=target_role_map.get(mapped, statement_role)
        if statement_role != role:
            continue
        rendered=render_seed_statement(vendor,st,name_map,variables)
        if rendered: lines.append(rendered+"\n")
    return "\n".join(lines)


def manifest_for(vendor, hashes, schema, seed, non_table):
    return {
        "schemaVersion": 1,
        "vendor": vendor,
        "generated": True,
        "owner": "cpf-tools/db",
        "canonical": {
            "platformSchemaVersion": schema["schemaVersion"],
            "seedModelVersion": seed["schemaVersion"],
            "nonTableObjectVersion": non_table["schemaVersion"]
        },
        "artifacts": hashes,
        "historicalMigrationImmutable": True,
        "overrideManifest": "cpf-tools/db/canonical/vendor-overrides.json"
    }


def main():
    ap=argparse.ArgumentParser()
    ap.add_argument('--root',default='.')
    ap.add_argument('--vendor',choices=OFFICIAL)
    ap.add_argument('--check',action='store_true',help='render to memory and compare with generated current pack')
    args=ap.parse_args()
    root=Path(args.root).resolve(); db=root/'cpf-tools/db'; canonical=db/'canonical'
    schema=read_json(canonical/'platform-schema.json'); seed=read_json(canonical/'seed-model.json'); nto=read_json(canonical/'platform-non-table-objects.json')
    vendors=(args.vendor,) if args.vendor else OFFICIAL
    if tuple(schema.get('canonicalPolicy',{}).get('officialVendors',[])) not in (OFFICIAL, tuple(reversed(OFFICIAL))):
        if set(schema.get('canonicalPolicy',{}).get('officialVendors',[])) != set(OFFICIAL):
            raise SystemExit('official vendor set mismatch')
    name_map={t.get('currentName',t['name']):t['targetTableName'] for t in schema['tables']}
    # Also map canonical pre-target names because some seed rows already use intermediate names.
    name_map.update({t['name']:t['targetTableName'] for t in schema['tables']})
    for vendor in vendors:
        out=db/'generated/current'/vendor
        target_role_map={t['targetTableName']:t['targetDatabaseRole'] for t in schema['tables']}
        texts={
            'cpf-platform-schema.sql':render_schema(vendor,schema,'CPF_PLATFORM_DB'),
            'cpf-platform-seed.sql':render_seed(vendor,seed,name_map,'CPF_PLATFORM_DB',target_role_map),
            'cpf-platform-verify.sql':render_verify(vendor,schema,'CPF_PLATFORM_DB'),
            'cpf-platform-rollback.sql':render_rollback(vendor,schema,'CPF_PLATFORM_DB'),
            'backoffice-schema.sql':render_schema(vendor,schema,'CUSTOMER_BUSINESS_DB'),
            'backoffice-seed.sql':render_seed(vendor,seed,name_map,'CUSTOMER_BUSINESS_DB',target_role_map),
            'backoffice-verify.sql':render_verify(vendor,schema,'CUSTOMER_BUSINESS_DB'),
            'backoffice-rollback.sql':render_rollback(vendor,schema,'CUSTOMER_BUSINESS_DB'),
            'reference-fixture-schema.sql':render_schema(vendor,schema,'REFERENCE_FIXTURE'),
            'reference-fixture-seed.sql':render_seed(vendor,seed,name_map,'REFERENCE_FIXTURE',target_role_map),
            'reference-fixture-verify.sql':render_verify(vendor,schema,'REFERENCE_FIXTURE'),
            'reference-fixture-rollback.sql':render_rollback(vendor,schema,'REFERENCE_FIXTURE'),
            'non-table-objects.sql':render_non_table(vendor,nto,name_map),
        }
        if args.check:
            mismatches=[]
            for n,content in texts.items():
                p=out/n
                if not p.is_file() or p.read_text(encoding='utf-8-sig') != content.rstrip()+"\n": mismatches.append(n)
            if mismatches: raise SystemExit(f'{vendor} generated drift: {mismatches}')
            continue
        hashes={n:write_text(out/n,content) for n,content in texts.items()}
        write_text(out/'manifest.json',json.dumps(manifest_for(vendor,hashes,schema,seed,nto),ensure_ascii=False,indent=2))
    print('CPF_DB_VENDOR_RENDER=PASS vendors='+','.join(vendors))

if __name__=='__main__': main()

package com.cpf.starter.data.transaction.jta;

import javax.sql.XADataSource;
import java.lang.reflect.Method;
import java.util.Map;

/** Oracle/PostgreSQL/MariaDB XADataSource를 driver compile dependency 없이 생성하는 DB3 factory입니다. */
public final class CpfXaDataSourceFactory {
    private static final Map<String,String> TYPES = Map.of(
            "oracle", "oracle.jdbc.xa.client.OracleXADataSource",
            "postgresql", "org.postgresql.xa.PGXADataSource",
            "mariadb", "org.mariadb.jdbc.MariaDbDataSource");
    public XADataSource create(String vendor, String url, String user, char[] password) {
        try {
            String className = TYPES.get(vendor == null ? "" : vendor.toLowerCase());
            if (className == null) throw new IllegalArgumentException("Unsupported XA vendor: " + vendor);
            Object candidate = Class.forName(className).getConstructor().newInstance();
            if (!(candidate instanceof XADataSource xa)) throw new IllegalStateException(className + " does not implement XADataSource");
            set(candidate, new String[]{"setURL","setUrl"}, url);
            set(candidate, new String[]{"setUser"}, user);
            set(candidate, new String[]{"setPassword"}, password == null ? null : new String(password));
            return xa;
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException("Unable to create XADataSource", ex); }
        finally { if (password != null) java.util.Arrays.fill(password, '\0'); }
    }
    private static void set(Object target, String[] names, String value) throws ReflectiveOperationException {
        for (String name : names) {
            try { Method m=target.getClass().getMethod(name,String.class); m.invoke(target,value); return; }
            catch (NoSuchMethodException ignored) { }
        }
        throw new NoSuchMethodException(String.join("/", names));
    }
}

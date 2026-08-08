package com.cpf.reference.online.integrated;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Oracle/PostgreSQL/MariaDB 실제 DB에서 Spring REQUIRED transaction + Non-XA remote UNKNOWN/Reconcile을 검증합니다.
 */
public final class OnlineAbcdSpringJdbcRuntimeHarness {
    /**
     * 환경변수로 지정한 실제 DB에서 success/rollback/UNKNOWN/reconcile을 실행합니다.
     * @param args 사용하지 않음
     */
    public static void main(String[] args) {
        String url=req("CPF_REF_DB_URL"), user=req("CPF_REF_DB_USER"), password=req("CPF_REF_DB_PASSWORD");
        String table=System.getenv().getOrDefault("CPF_REF_ONLINE_TABLE","cpf_ref_online_abcd");
        DataSource ds = new DriverDataSource(url,user,password);
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        var repo = new OnlineAbcdSpringJdbcRepository(jdbc, table);
        var remote = new OnlineAbcdReferenceFlow.ScenarioRemote();
        var c = new OnlineAbcdReferenceFlow.DomainC(remote);
        var b = new OnlineAbcdReferenceFlow.DomainB(c, repo);
        var controller = new OnlineAbcdReferenceFlow.Controller(new OnlineAbcdReferenceFlow.DomainA(b));
        var service = new OnlineAbcdSpringTransactionService(controller, new DataSourceTransactionManager(ds));
        var d = new OnlineAbcdReferenceFlow.DomainD(repo, remote);

        String key="CPF_REF_SPRING_"+System.currentTimeMillis();
        put(jdbc,table,key,"OLD");
        var ok=service.execute(new OnlineAbcdReferenceFlow.Request("TX-SPRING-OK",key,"NEW",1));
        check(ok.outcome()==OnlineAbcdReferenceFlow.Outcome.SUCCESS,"success flow");
        check(read(jdbc,table,key).startsWith("REMOTE:"),"commit visible");

        String rollbackKey=key+"_RB"; put(jdbc,table,rollbackKey,"OLD"); b.failAfterSave=true;
        var unknown=service.execute(new OnlineAbcdReferenceFlow.Request("TX-SPRING-RB",rollbackKey,"NEW",1));
        check(unknown.outcome()==OnlineAbcdReferenceFlow.Outcome.UNKNOWN,"remote-success/local-fail must be UNKNOWN");
        check("OLD".equals(read(jdbc,table,rollbackKey)),"Spring rollback-only must preserve OLD");
        check(remote.sideEffects.get()==2,"remote side effect occurs before local rollback");
        check(d.reconcile(unknown).outcome()==OnlineAbcdReferenceFlow.Outcome.RECONCILED,"reconcile");
        check(remote.sideEffects.get()==1,"unknown remote side effect compensated once");
        System.out.println("[CPF][REFERENCE][ONLINE-SPRING-JDBC][PASS] propagation=REQUIRED rollbackOnly=true unknownReconcile=true transactionIdStable=true");
    }

    private static String read(JdbcTemplate jdbc,String table,String key){
        return jdbc.query("SELECT value_text FROM "+table+" WHERE business_key=?", rs->rs.next()?rs.getString(1):null,key);
    }
    private static void put(JdbcTemplate jdbc,String table,String key,String value){
        int n=jdbc.update("UPDATE "+table+" SET value_text=? WHERE business_key=?",value,key);
        if(n==0) jdbc.update("INSERT INTO "+table+" (business_key,value_text) VALUES (?,?)",key,value);
    }
    private static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
    private static String req(String n){String v=System.getenv(n);if(v==null||v.isBlank())throw new IllegalStateException(n+" is required");return v;}
    private record DriverDataSource(String url,String user,String password) implements DataSource {
        public Connection getConnection() throws SQLException{return DriverManager.getConnection(url,user,password);}
        public Connection getConnection(String u,String p)throws SQLException{return DriverManager.getConnection(url,u,p);}
        public PrintWriter getLogWriter()throws SQLException{return DriverManager.getLogWriter();}
        public void setLogWriter(PrintWriter out)throws SQLException{DriverManager.setLogWriter(out);}
        public void setLoginTimeout(int seconds)throws SQLException{DriverManager.setLoginTimeout(seconds);}
        public int getLoginTimeout()throws SQLException{return DriverManager.getLoginTimeout();}
        public Logger getParentLogger(){return Logger.getGlobal();}
        public <T>T unwrap(Class<T> iface)throws SQLException{if(iface.isInstance(this))return iface.cast(this);throw new SQLException("unwrap");}
        public boolean isWrapperFor(Class<?> iface){return iface.isInstance(this);}
    }
    private OnlineAbcdSpringJdbcRuntimeHarness(){}
}

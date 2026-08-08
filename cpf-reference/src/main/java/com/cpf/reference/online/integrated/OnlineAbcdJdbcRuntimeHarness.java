package com.cpf.reference.online.integrated;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 * Oracle/PostgreSQL/MariaDB 실제 연결에서 Online A→B→C/D JDBC rollback을 실행하는 Runtime harness.
 * 검증 schema에는 business_key/value_text 컬럼을 가진 격리 table을 사전 준비한다.
 */
public final class OnlineAbcdJdbcRuntimeHarness {
    public static void main(String[] args) {
        String url=req("CPF_REF_DB_URL"), user=req("CPF_REF_DB_USER"), password=req("CPF_REF_DB_PASSWORD");
        String table=System.getenv().getOrDefault("CPF_REF_ONLINE_TABLE","cpf_ref_online_abcd");
        var repo=new OnlineAbcdReferenceFlow.JdbcRepository(new DriverDataSource(url,user,password),table);
        var remote=new OnlineAbcdReferenceFlow.ScenarioRemote();
        var c=new OnlineAbcdReferenceFlow.DomainC(remote); var b=new OnlineAbcdReferenceFlow.DomainB(c,repo);
        var controller=new OnlineAbcdReferenceFlow.Controller(new OnlineAbcdReferenceFlow.DomainA(b));
        var d=new OnlineAbcdReferenceFlow.DomainD(repo,remote);
        String key="CPF_REF_"+System.currentTimeMillis();
        put(repo,key,"OLD");
        var ok=controller.execute(new OnlineAbcdReferenceFlow.Request("TX-JDBC-OK",key,"NEW",1));
        check(ok.outcome()==OnlineAbcdReferenceFlow.Outcome.SUCCESS,"success flow");
        check(read(repo,key).startsWith("REMOTE:"),"commit visible");

        String rollbackKey=key+"_RB"; put(repo,rollbackKey,"OLD"); b.failAfterSave=true;
        var unknown=controller.execute(new OnlineAbcdReferenceFlow.Request("TX-JDBC-RB",rollbackKey,"NEW",1));
        check(unknown.outcome()==OnlineAbcdReferenceFlow.Outcome.UNKNOWN,"remote-success/local-fail must be UNKNOWN");
        check("OLD".equals(read(repo,rollbackKey)),"rollback must restore OLD");
        check(remote.sideEffects.get()==2,"two remote side effects before reconcile");
        check(d.reconcile(unknown).outcome()==OnlineAbcdReferenceFlow.Outcome.RECONCILED,"reconcile");
        check(remote.sideEffects.get()==1,"unknown side effect compensated once");
        System.out.println("[CPF][REFERENCE][ONLINE-JDBC][PASS] transactionIdStable=true rollback=true unknownReconcile=true");
    }
    private static String read(OnlineAbcdReferenceFlow.JdbcRepository repo,String key){repo.begin(key);try{String v=repo.find(key).orElse(null);repo.commit(key);return v;}catch(RuntimeException e){repo.rollback(key);throw e;}}
    private static void put(OnlineAbcdReferenceFlow.JdbcRepository repo,String key,String value){repo.begin(key);try{repo.save(key,value);repo.commit(key);}catch(RuntimeException e){repo.rollback(key);throw e;}}
    private static void check(boolean v,String m){if(!v)throw new AssertionError(m);} private static String req(String n){String v=System.getenv(n);if(v==null||v.isBlank())throw new IllegalStateException(n+" is required");return v;}
    private record DriverDataSource(String url,String user,String password) implements DataSource {
        public Connection getConnection() throws SQLException{return DriverManager.getConnection(url,user,password);} public Connection getConnection(String u,String p)throws SQLException{return DriverManager.getConnection(url,u,p);} public PrintWriter getLogWriter()throws SQLException{return DriverManager.getLogWriter();} public void setLogWriter(PrintWriter out)throws SQLException{DriverManager.setLogWriter(out);} public void setLoginTimeout(int seconds)throws SQLException{DriverManager.setLoginTimeout(seconds);} public int getLoginTimeout()throws SQLException{return DriverManager.getLoginTimeout();} public Logger getParentLogger(){return Logger.getGlobal();} public <T>T unwrap(Class<T> iface)throws SQLException{if(iface.isInstance(this))return iface.cast(this);throw new SQLException("unwrap");} public boolean isWrapperFor(Class<?> iface){return iface.isInstance(this);}
    }
    private OnlineAbcdJdbcRuntimeHarness(){}
}

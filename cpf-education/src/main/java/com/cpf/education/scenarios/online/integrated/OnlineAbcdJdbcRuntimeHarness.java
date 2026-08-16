package com.cpf.education.scenarios.online.integrated;
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
        String url=req("CPF_EDU_DB_URL"), user=req("CPF_EDU_DB_USER"), password=req("CPF_EDU_DB_PASSWORD");
        String table=System.getenv().getOrDefault("CPF_EDU_ONLINE_TABLE","cpf_ref_online_abcd");
        var repo=new OnlineAbcdEducationFlow.JdbcRepository(new DriverDataSource(url,user,password),table);
        var remote=new OnlineAbcdEducationFlow.ScenarioRemote();
        var c=new OnlineAbcdEducationFlow.DomainC(remote); var b=new OnlineAbcdEducationFlow.DomainB(c,repo);
        var controller=new OnlineAbcdEducationFlow.Controller(new OnlineAbcdEducationFlow.DomainA(b));
        var d=new OnlineAbcdEducationFlow.DomainD(repo,remote);
        String key="CPF_EDU_"+System.currentTimeMillis();
        put(repo,key,"OLD");
        var ok=controller.execute(new OnlineAbcdEducationFlow.Request("TX-JDBC-OK",key,"NEW",1));
        check(ok.outcome()==OnlineAbcdEducationFlow.Outcome.SUCCESS,"success flow");
        check(read(repo,key).startsWith("REMOTE:"),"commit visible");

        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        String rollbackKey=key+"_RB"; put(repo,rollbackKey,"OLD"); b.failAfterSave=true;
        var unknown=controller.execute(new OnlineAbcdEducationFlow.Request("TX-JDBC-RB",rollbackKey,"NEW",1));
        check(unknown.outcome()==OnlineAbcdEducationFlow.Outcome.UNKNOWN,"remote-success/local-fail must be UNKNOWN");
        check("OLD".equals(read(repo,rollbackKey)),"rollback must restore OLD");
        check(remote.sideEffects.get()==2,"two remote side effects before reconcile");
        check(d.reconcile(unknown).outcome()==OnlineAbcdEducationFlow.Outcome.RECONCILED,"reconcile");
        check(remote.sideEffects.get()==1,"unknown side effect compensated once");
        System.out.println("[CPF][EDU][ONLINE-JDBC][PASS] transactionIdStable=true rollback=true unknownReconcile=true");
    }
    private static String read(OnlineAbcdEducationFlow.JdbcRepository repo,String key){repo.begin(key);try{String v=repo.find(key).orElse(null);repo.commit(key);return v;}catch(RuntimeException e){repo.rollback(key);throw e;}}
    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    private static void put(OnlineAbcdEducationFlow.JdbcRepository repo,String key,String value){repo.begin(key);try{repo.save(key,value);repo.commit(key);}catch(RuntimeException e){repo.rollback(key);throw e;}}
    private static void check(boolean v,String m){if(!v)throw new AssertionError(m);} private static String req(String n){String v=System.getenv(n);if(v==null||v.isBlank())throw new IllegalStateException(n+" is required");return v;}
    private record DriverDataSource(String url,String user,String password) implements DataSource {
        public Connection getConnection() throws SQLException{return DriverManager.getConnection(url,user,password);} public Connection getConnection(String u,String p)throws SQLException{return DriverManager.getConnection(url,u,p);} public PrintWriter getLogWriter()throws SQLException{return DriverManager.getLogWriter();} public void setLogWriter(PrintWriter out)throws SQLException{DriverManager.setLogWriter(out);} public void setLoginTimeout(int seconds)throws SQLException{DriverManager.setLoginTimeout(seconds);} public int getLoginTimeout()throws SQLException{return DriverManager.getLoginTimeout();} public Logger getParentLogger(){return Logger.getGlobal();} public <T>T unwrap(Class<T> iface)throws SQLException{if(iface.isInstance(this))return iface.cast(this);throw new SQLException("unwrap");} public boolean isWrapperFor(Class<?> iface){return iface.isInstance(this);}
    }
    private OnlineAbcdJdbcRuntimeHarness(){}
}

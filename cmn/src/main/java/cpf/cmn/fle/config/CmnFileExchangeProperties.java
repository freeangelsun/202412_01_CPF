package cpf.cmn.fle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * CMN ?뚯씪/?먭꺽 ?곌퀎 怨듯넻 ?ㅼ젙?낅땲??
 */
@ConfigurationProperties(prefix = "cpf.cmn.file-exchange")
public class CmnFileExchangeProperties {
    /**
     * 濡쒖뺄 ?뚯씪 ?곌퀎 湲곗? ?붾젆?곕━?낅땲?? ?곷? 寃쎈줈?????붾젆?곕━ 諛뽰쑝濡??섍컝 ???놁뒿?덈떎.
     */
    private String baseDir = "${java.io.tmpdir}/fps-cmn-file-exchange";

    /**
     * SSH 낅졊 ?ㅽ뻾 ?덉슜 ?щ??낅땲?? 湲곕낯? 蹂댁븞??false?낅땲??
     */
    private boolean sshEnabled = false;

    /**
     * SSH/SCP/SFTP ?묒냽 ?덉슜 ?몄뒪??⑸줉?낅땲?? 鍮꾩뼱 ?덉쑝硫??먭꺽 ?ㅽ뻾???덉슜?섏? ?딆뒿?덈떎.
     */
    private List<String> allowedHosts = new ArrayList<>();

    /**
     * ?몃? 낅졊 ??꾩븘??珥덉엯?덈떎.
     */
    private int timeoutSeconds = 15;

    /**
     * ?먭꺽 ?뚯씪 ?꾩넚???ㅼ젣 ?ㅽ뻾?섏? ?딄퀬 낅졊 怨꾪쉷留?諛섑솚?좎? ?щ??낅땲??
     */
    private boolean dryRun = true;

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public boolean isSshEnabled() {
        return sshEnabled;
    }

    public void setSshEnabled(boolean sshEnabled) {
        this.sshEnabled = sshEnabled;
    }

    public List<String> getAllowedHosts() {
        return allowedHosts;
    }

    public void setAllowedHosts(List<String> allowedHosts) {
        this.allowedHosts = allowedHosts;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
}


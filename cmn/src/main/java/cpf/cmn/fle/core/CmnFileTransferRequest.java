package cpf.cmn.fle.core;

/**
 * ?뚯씪 ?꾩넚 ?붿껌?낅땲??
 *
 * @param protocol      LOCAL, FTP, SFTP, SCP
 * @param direction     ?낅줈???ㅼ슫濡쒕뱶
 * @param host          ?먭꺽 ?몄뒪?? * @param port          ?먭꺽 ?ы듃
 * @param username      ?먭꺽 ?ъ슜?? * @param identityFile  SSH ???뚯씪 寃쎈줈
 * @param localPath     濡쒖뺄 寃쎈줈
 * @param remotePath    ?먭꺽 寃쎈줈
 * @param requestUser   ?붿껌?? */
public record CmnFileTransferRequest(
        CmnFileProtocol protocol,
        CmnFileTransferDirection direction,
        String host,
        Integer port,
        String username,
        String identityFile,
        String localPath,
        String remotePath,
        String requestUser) {
}


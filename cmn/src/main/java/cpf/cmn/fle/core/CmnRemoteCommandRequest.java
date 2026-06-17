package cpf.cmn.fle.core;

/**
 * SSH ?먭꺽 낅졊 ?붿껌?낅땲??
 *
 * @param host         ?먭꺽 ?몄뒪?? * @param port         SSH ?ы듃
 * @param username     ?먭꺽 ?ъ슜?? * @param identityFile SSH ???뚯씪 寃쎈줈
 * @param command      ?ㅽ뻾 낅졊
 * @param requestUser  ?붿껌?? */
public record CmnRemoteCommandRequest(
        String host,
        Integer port,
        String username,
        String identityFile,
        String command,
        String requestUser) {
}


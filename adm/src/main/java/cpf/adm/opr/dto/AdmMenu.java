package cpf.adm.opr.dto;

/**
 * 愿由ъ옄 硫붾돱 ?묐떟?낅땲??
 *
 * @param menuId       硫붾돱 ID
 * @param parentMenuId ?곸쐞 硫붾돱 ID
 * @param menuName     硫붾돱紐? * @param path         ?붾㈃ 寃쎈줈
 * @param sortOrder    ?뺣젹 ?쒖꽌
 */
public record AdmMenu(
        String menuId,
        String parentMenuId,
        String menuName,
        String path,
        int sortOrder) {
}


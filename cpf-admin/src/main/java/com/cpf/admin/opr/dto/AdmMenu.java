package cpf.adm.opr.dto;

/**
 * ADM menu and permission response.
 */
public record AdmMenu(
        String menuId,
        String parentMenuId,
        String menuName,
        String path,
        int sortOrder,
        boolean readAllowed,
        boolean writeAllowed,
        boolean deleteAllowed) {

    public AdmMenu(String menuId, String parentMenuId, String menuName, String path, int sortOrder) {
        this(menuId, parentMenuId, menuName, path, sortOrder, true, true, true);
    }
}

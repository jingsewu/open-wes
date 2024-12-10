// user module
export const api_user_add = "post:/user/api/user/add";
export const api_user_update = "post:/user/api/user/update";
export const api_user_get = "get:/user/api/user/${id}";
export const api_user_delete = "delete:/user/api/user/${id}"
export const api_user_reset_password = "post:/user/api/user/resetPassword/${id}";

// role module
export const api_role_add = "post:/user/api/role/add"
export const api_role_update = "post:/user/api/role/update";
export const api_role_get_role_menu = "get:/user/api/role/getRoleMenu/${id}";
export const api_role_update_role_menu = "post:/user/api/role/updateRoleMenu/${id}";
export const api_role_delete = "delete:/user/api/role/${id}";

// menu module
export const api_menu_add = "post:/user/api/menu/add"
export const api_menu_update = "post:/user/api/menu/update";
export const api_menu_update_status = "get:/user/api/menu/updateStatus";
export const api_menu_delete = "delete:/user/api/menu/${id}";

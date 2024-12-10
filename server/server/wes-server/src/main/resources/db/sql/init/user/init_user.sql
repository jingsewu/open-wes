-- add Admin User
INSERT INTO `u_user` (`id`, `create_time`, `create_user`, `update_time`, `update_user`, `account`, `avatar`,
                             `email`, `last_gmt_login_time`, `last_login_ip`, `locked`, `name`, `password`, `phone`,
                             `status`, `tenant_name`, `type`)
VALUES (1, 0, 'admin', 0, 'admin', 'admin', '2e418339355940a9b83f72d597a6da2c.jpg', NULL, '0', '127.0.0.1', 0, 'admin',
        '$2a$10$.1vJlEYUvwjdqJ2pR2iHBeHis2QI22cLF7M0eln/vT2SbFzXqiSOC', '', 1, 'test', 'NORMAL');

-- add Admin Role
INSERT INTO `u_role` (`id`, `create_time`, `create_user`, `update_time`, `update_user`, `code`, `description`,
                             `name`, `status`, `warehouse_codes`)
VALUES (1, 0, 'admin', 0, 'admin', 'superAdmin', 'admin', '管理员', 1, '[]');

-- add User Role relation
INSERT INTO `u_user_role` (`id`, `role_id`, `user_id`)
VALUES (1, 1, 1);

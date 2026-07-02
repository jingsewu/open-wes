package org.openwes.wes.task.domain.entity;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.http.Response;
import org.openwes.user.application.UserRoleService;
import org.openwes.user.application.UserService;
import org.openwes.user.controller.common.BaseResource;
import org.openwes.user.controller.param.user.UserDTO;
import org.openwes.user.controller.param.user.UserUpdateStatusParam;
import org.openwes.user.domain.entity.User;
import org.openwes.user.domain.entity.UserRole;
import org.openwes.user.domain.model.UserHasRole;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(BaseResource.API + "user")
@AllArgsConstructor
@Slf4j
@Tag(name = "User Module Api")
public class UserController {

    private final UserService userService;

    /**
     * @param userRegisterDTO
     * @return
     */
    @PostMapping("register")
    @Operation(summary = "注册用户")
    public Object register(@Valid UserRegisterDTO userRegisterDTO) {

        user.validate();
        User user = userService.queryByAccount(userRegisterDTO.getAccount());
        if (user != null) {
            throw new RuntimeException("account is exist");
        }

        user = userService.queryByMobile(userRegisterDTO.getMobile());
        if (user != null) {
            throw new RuntimeException("mobile is exist");
        }

        userService.createUser(userRegisterDTO);
    }


    @Data
    public class UserRegisterDTO {
        @NotEmpty
        @Size(128)
        private String name;
        @NotEmpty
        @Size(64)
        private String account;

        @NotEmpty
        private String mobile;
        private String email;
        @NotEmpty
        private String pwd;

        @NotEmpty
        private String repeatPwd;

        public void validate() {

            if (!pwd.equals(repeatPwd)) {
                throw new RuntimeException("repeatPwd must equals pwd");
            }
        }
    }
}

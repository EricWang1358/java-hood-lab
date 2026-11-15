package com.javahood.admin.controller;

import com.javahood.admin.audit.Audited;
import com.javahood.admin.common.ApiResponse;
import com.javahood.admin.common.PageResult;
import com.javahood.admin.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 32) String username,
            @NotBlank @Size(min = 6, max = 64) String password,
            @NotBlank String displayName,
            @NotBlank @Email String email,
            Set<String> roleCodes
    ) {
    }

    public record UpdateStatusRequest(boolean enabled) {
    }

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<String> me(Authentication authentication) {
        return ApiResponse.ok(authentication.getName());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('user:read')")
    public ApiResponse<PageResult<UserService.UserVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(userService.queryUsers(keyword, enabled, page, size));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:write')")
    @Audited(action = "USER_CREATE")
    public ApiResponse<UserService.UserVO> create(@Valid @RequestBody CreateUserRequest req) {
        return ApiResponse.ok(userService.createUser(new UserService.CreateUserCmd(
                req.username(), req.password(), req.displayName(), req.email(), req.roleCodes()
        )));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('user:write')")
    @Audited(action = "USER_STATUS_UPDATE")
    public ApiResponse<UserService.UserVO> updateStatus(@PathVariable Long id,
                                                        @RequestBody UpdateStatusRequest req) {
        return ApiResponse.ok(userService.updateStatus(id, req.enabled()));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('user:export')")
    @Audited(action = "USER_CSV_EXPORT")
    public void exportCsv(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Boolean enabled,
                          HttpServletResponse response) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users.csv\"");
        userService.exportCsv(keyword, enabled, response.getWriter());
    }
}

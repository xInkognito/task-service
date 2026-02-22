package ru.cinimex.task.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.cinimex.task.feign.dto.UserResponse;

@FeignClient(name = "user-service", url = "${app.services.user-url}")
public interface UserClient {

    @GetMapping("/admin/users/{login}")
    UserResponse getUserByLogin(
            @PathVariable("login") String login,
            @RequestHeader("Authorization") String token
    );
}

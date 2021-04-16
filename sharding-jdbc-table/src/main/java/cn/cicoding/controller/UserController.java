package cn.cicoding.controller;

import cn.cicoding.model.User;
import cn.cicoding.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public Object list() {
        return userService.list();
    }

    @GetMapping("/add")
    public Object add() {
        for (long i = 0; i < 100; i++) {
            User user = new User();
            user.setCity("深圳");
            user.setName("李四" + i);
            user.setCreateTime(Date.from(LocalDateTime.now().minusMonths((long) (Math.random() * 4)).atZone(ZoneId.systemDefault()).toInstant()));
            userService.add(user);
        }
        return "success";
    }

    @GetMapping("/users/{id}")
    public Object get(@PathVariable Long id) {
        return userService.findById(id);
    }

    @GetMapping("/users/query")
    public Object get(String name) {
        return userService.findByName(name);
    }

}
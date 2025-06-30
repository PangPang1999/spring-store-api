package com.codewithmosh.store.admin;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @GetMapping("/hello")
    @Operation(summary = " admin test interface (only admin)")
    public String sayHello() {
        return "Hello Admin!";
    }
}

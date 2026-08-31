package com.formation.usermanagement.controller;

import com.formation.usermanagement.service.MonitoringService;
import com.formation.usermanagement.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestMonitoringController {

    private final MonitoringService monitoringService;

    @GetMapping("/register")
    public String testRegister() {
        monitoringService.incrementUserRegistration();
        return "✅ Inscription comptée !";
    }
}
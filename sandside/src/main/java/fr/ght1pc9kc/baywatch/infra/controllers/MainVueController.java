package fr.ght1pc9kc.baywatch.infra.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainVueController {
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("applicationName", "APPLICATION_NAME");
        return "index";
    }
}

package com.matheus.controle.ativos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro";
    }

    @GetMapping("/editar")
    public String editar() {
        return "editar";
    }

    @GetMapping("/visualizar")
    public String visualizar() {
        return "visualizar";
    }
}

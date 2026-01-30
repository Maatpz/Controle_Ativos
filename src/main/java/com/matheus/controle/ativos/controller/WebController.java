package com.matheus.controle.ativos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "forward:/cadastro.html";
    }

    @GetMapping("/editar")
    public String editar() {
        return "forward:/editar.html";
    }

    @GetMapping("/visualizar")
    public String visualizar() {
        return "forward:/visualizar.html";
    }
}

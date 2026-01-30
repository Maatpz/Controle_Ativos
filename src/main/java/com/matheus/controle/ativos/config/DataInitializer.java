package com.matheus.controle.ativos.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.matheus.controle.ativos.service.UsuarioService;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioService usuarioService;

    @Override
    public void run(String... args) throws Exception {

        usuarioService.initializeDefaultAdmin();
        System.out.println("Usuário iniciado");
    }
}

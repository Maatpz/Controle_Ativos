package com.matheus.controle.ativos.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.matheus.controle.ativos.model.Usuario;
import com.matheus.controle.ativos.model.enums.Role;
import com.matheus.controle.ativos.repository.UsuarioRepository;

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public List<Usuario>findAll(){
        return usuarioRepository.findAll();
    }

    public Optional<Usuario>findByUsername(String username){
        return usuarioRepository.findByUsername(username);
    }

    public Optional<Usuario>findByUsernameAndAtivo(String username, Boolean ativo){
        return usuarioRepository.findByUsernameAndAtivo(username, ativo);
    }

    public boolean existsByUsername(String username){
        return usuarioRepository.existsByUsername(username);
    }

    public Usuario save (Usuario usuario){

        if(usuario.getPassword() != null && !usuario.getPassword().isEmpty()){
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        return usuarioRepository.save(usuario);
    }

    public Usuario createUsuario(String username, String password, String nome, Role role ){
        if(existsByUsername(username)){
            throw new RuntimeException("Ja existe um usuario com esse username " + username);
        }
        Usuario usuario = new Usuario(username,password,nome,role);
        return save(usuario);
        
    }


}

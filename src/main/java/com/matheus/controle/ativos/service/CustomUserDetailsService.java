// package com.matheus.controle.ativos.service;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Optional;

// import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.userdetails.User;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UserDetailsService;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.stereotype.Service;

// import com.matheus.controle.ativos.model.Usuario;
// import com.matheus.controle.ativos.repository.UsuarioRepository;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// public class CustomUserDetailsService implements UserDetailsService {

//     private final UsuarioRepository usuarioRepository;

//     @Override
//     public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//         Optional<Usuario> usuario = usuarioRepository.findByUsernameIgnoreCaseAndAtivo(username, true);

//         if (usuario.isEmpty()) {
//             throw new UsernameNotFoundException("Usuario nao encontrado: " + username);
//         }

//         Usuario user = usuario.get();
//         List<GrantedAuthority> authorities = new ArrayList<>();
//         authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

//         return User.builder()
//                 .username(user.getUsername())
//                 .password(user.getPassword())
//                 .authorities(authorities)
//                 .accountExpired(false)
//                 .accountLocked(false)
//                 .credentialsExpired(false)
//                 .disabled(!Boolean.TRUE.equals(user.getAtivo()))
//                 .build();
//     }
// }

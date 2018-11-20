package br.edu.utfpr.pb.aula6.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.utfpr.pb.aula6.model.Usuario;

public interface UsuarioRepository 
			extends JpaRepository<Usuario, Long>{

	Usuario findByUsername(String username);
}

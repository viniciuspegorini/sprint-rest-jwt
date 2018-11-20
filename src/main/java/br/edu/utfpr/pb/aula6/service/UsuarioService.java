package br.edu.utfpr.pb.aula6.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import br.edu.utfpr.pb.aula6.model.Usuario;
import br.edu.utfpr.pb.aula6.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService{
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	public UsuarioService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository
				.findByUsername(username);
		if(usuario == null) {
			throw new UsernameNotFoundException(
					"Usuário não encontrado!");
		}
		return usuario;
	}

}








package br.edu.utfpr.pb.aula6.security.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.pb.aula6.AppConstant;
import br.edu.utfpr.pb.aula6.model.Usuario;
import br.edu.utfpr.pb.aula6.security.TokenUtils;
import br.edu.utfpr.pb.aula6.security.model.AuthenticationRequest;
import br.edu.utfpr.pb.aula6.security.model.AuthenticationResponse;
import br.edu.utfpr.pb.aula6.service.UsuarioService;

@RestController
@RequestMapping("auth")
public class AuthenticationController {
	
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private TokenUtils tokenUtils;
	@Autowired
	private UsuarioService usuarioService;
	
	@PostMapping(value = "")
	public ResponseEntity<?> authenticationRequest(
			@RequestBody AuthenticationRequest a) 
			throws AuthenticationException{
		
		Authentication authentication = this.authenticationManager
				.authenticate(
					new UsernamePasswordAuthenticationToken(
							a.getUsername(), 
							a.getPassword())
				);//{"username":"admin", "password": "123"}
		SecurityContextHolder.getContext()
				.setAuthentication(authentication);
	
		UserDetails userDetails = this.usuarioService
				.loadUserByUsername(a.getUsername());
		String token = this.tokenUtils.generateToken(userDetails);
		return ResponseEntity.ok(
				new AuthenticationResponse(token));
	}
	
	@GetMapping(value = "refresh")
	public ResponseEntity<?> authenticationRequest(
			HttpServletRequest request){
		
		String token = request.getHeader(AppConstant.TOKEN_HEADER);
		String username = this.tokenUtils.getUsernameFromToken(token);
		Usuario u = (Usuario) this.usuarioService
				.loadUserByUsername(username);
		
		if (this.tokenUtils.canTokenBeRefreshed(token, 
				u.getLastPasswordReset())) {
			String refreshedToken = 
					this.tokenUtils.refreshToken(token);
			return ResponseEntity.ok(
					new AuthenticationResponse(refreshedToken));
		}else {
			return ResponseEntity.badRequest().body(null);
		}
	}
	
}







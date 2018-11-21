#  Projeto Spring REST utilizando autenticação com Spring Security e JWT

Neste projeto é desenvolvida uma API REST utilizando Spring Boot. Os dados são persistidos em um banco de dados PostgreSQL utilizando Spring JPA. Para realizar a autenticação e autorização dos *endpoints* é utilizando Spring Security.


## Configurando o Spring Security com JWT

Inicialmente é necessário adicionar as dependências do Spring Security e JWT no arquivo **pom.xml**.

	<dependencies>
		...	
		<dependency>
		    <groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
		<dependency>
		<groupId>io.jsonwebtoken</groupId>
			<artifactId>jjwt</artifactId>
			<version>0.6.0</version>
		</dependency>
	</dependencies>
Após adicionadas as dependências serão criadas as classes de Usuário e Permissão. As quais devem implementar as interfaces **UserDetails** e **GrantedAuthority**, respectivamente. Ambas as classes estão no *package* **model**.
Então são criadas as classes **UsuarioRepository** e **PermissaoRepository**, as quais serão responsáveis pelos CRUDs de Usuario e Permissao, e também pelas consultas utilizadas para autenticação do usuário e busca de permissões. A classe **UsuarioRepository** deve implementar a interface JpaRepository e deve possuir um método para buscar o usuário pelo **username**:

	public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
		Usuario findByUsername(String username);
	}

No pacote **br.edu.utfpr.pb.aula6.security** estão as outras classes que fazem parte da autenticação e autorização do sistema.

### WebSecurityConfiguration
Classe responsável pelas configurações do Spring Security. O principal método dessa classe é o **configure()**, em que é configurado o objeto responsável pelo tratamento de acesso não autorizado. Também é configurado o tipo de sessão de usuário utilizada, além do controle de autorização para cada URL da API REST criada.

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable()
			.exceptionHandling() 
				//A classe EntryPointUnauthorizedHandler será responsável por retornar a mensagem de acesso negado.
				.authenticationEntryPoint(authenticationEntryPoint)
			.and()
			.sessionManagement()
				//Como a API REST não vai possuir session de usuário, a mesma é definida como stateless
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authorizeRequests()
				//A definição do perfil de cada usuário que terá autorização para cada endpoint da API
				.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.antMatchers("/auth/**").permitAll()
				.antMatchers("/categoria/**").hasAnyRole("USER")
			  .antMatchers("/produto/**").hasAnyRole("ADMIN")
				.anyRequest().authenticated();
		
		http.addFilterBefore(authenticationTokenFilterBean(), 
				UsernamePasswordAuthenticationFilter.class);
	}
### AuthenticationController
É o **controller** responsável por receber as requisições para autenticação/criação e renovação do token.

	@RestController
	@RequestMapping("auth") //receber as requisições em /auth
	public class AuthenticationController {		
		@Autowired private AuthenticationManager authenticationManager;
		@Autowired private TokenUtils tokenUtils;
		@Autowired private UsuarioService usuarioService;
		
		@PostMapping() // a URL '/auth' com o método POST, espera um objeto AuthenticationRequest no formato JSON ou seja {"username":"admin@admin.com", "password": "123"}
		public ResponseEntity<?> authenticationRequest(@RequestBody AuthenticationRequest a) throws AuthenticationException{
			
			//Realiza a autenticação por meio do Spring Security
			Authentication authentication = this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(a.getUsername(), a.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			UserDetails userDetails = this.usuarioService.loadUserByUsername(a.getUsername());
			
			//Gera o token de acordo com o usuário logado
			String token = this.tokenUtils.generateToken(userDetails);
			
			//Retorna um objeto do tipo AuthenticationResponse, que contém o **token**
			return ResponseEntity.ok(new AuthenticationResponse(token));
		}
		
		@GetMapping(value = "refresh") // O refresh retorna um novo token atualizado
		public ResponseEntity<?> authenticationRequest(HttpServletRequest request){
			//Recupera-se o token do cabeçalho da requisição		
			String token = request.getHeader(AppConstant.TOKEN_HEADER);
			//Recupera o username do token
			String username = this.tokenUtils.getUsernameFromToken(token);
			//Carrega o usuário do banco
			Usuario u = (Usuario) this.usuarioService.loadUserByUsername(username);
			//Verifica se o token ainda é válido e poderá ser atualizado gera o token e retorna, senão retorna um badrequest
			if (this.tokenUtils.canTokenBeRefreshed(token, u.getLastPasswordReset())) {
				String refreshedToken = this.tokenUtils.refreshToken(token);
				return ResponseEntity.ok(new AuthenticationResponse(refreshedToken));
			}else {
				return ResponseEntity.badRequest().body(null);
			}
		}
	}
	
### AuthenticationTokenFilter

### AuthenticationRequest
Objeto que vai conter o json enviado na requisição com o usuário e senha.
>{"username":"admin@admin.com", "password": "123"}
### AuthenticationResponse
Objeto que vai conter o token enviado na resposta para o cliente.
>{"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMCIsIm5hbWUiOiJQYWxtZWlyYXMiLCJpYXQiOjE1MTYyMzkwMjJ9.gtqLloY91FM97vUBl8nk4Ga6PyNP_x2LuAAHS1kt1MA"
### EntryPointUnauthorizedHandler
Classe criada para tratar as exceções de acesso negado. O método **commence()** presente na classe, no caso de acesso negado, retorna a mensagem traduzida (pt-BR) para o cliente, sobrescrevendo a mensagem padrão em inglês.
### TokenUtils
Classe com os métodos necessários para gerar o JWT.

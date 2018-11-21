
#  Projeto Spring REST utilizando autenticação com Spring Security e JWT

Neste projeto é desenvolvida uma API REST utilizando Spring Boot. Os dados são persistidos em um banco de dados PostgreSQL utilizando Spring JPA. Para realizar a autenticação e autorização dos *endpoints* é utilizando Spring Security.


## Configurando o Spring Security com JWT

Inicialmente é necessário adicionar as dependências do Spring Security e JWT no arquivo **pom.xml**.

```xml
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
```
Após adicionadas as dependências serão criadas as classes de Usuário e Permissão. As quais devem implementar as interfaces **UserDetails** e **GrantedAuthority**, respectivamente. Ambas as classes estão no *package* **model**.
Então são criadas as classes **UsuarioRepository** e **PermissaoRepository**, as quais serão responsáveis pelos CRUDs de Usuario e Permissao, e também pelas consultas utilizadas para autenticação do usuário e busca de permissões. A classe **UsuarioRepository** deve implementar a interface JpaRepository e deve possuir um método para buscar o usuário pelo **username**:
```java
	public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
		Usuario findByUsername(String username);
	}
```
No pacote **br.edu.utfpr.pb.aula6.security** estão as outras classes que fazem parte da autenticação e autorização do sistema.

### WebSecurityConfiguration
Classe responsável pelas configurações do Spring Security. O principal método dessa classe é o **configure()**, em que é configurado o objeto responsável pelo tratamento de acesso não autorizado. Também é configurado o tipo de sessão de usuário utilizada, além do controle de autorização para cada URL da API REST criada.
```java
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
```
### AuthenticationController
É o **controller** responsável por receber as requisições para autenticação/criação e renovação do token.
```java
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
```

### AuthenticationTokenFilter
Nessa clase o método **doFilter()** é responsável por verificar se o token está na requisição e manter o usuário antenticado durante a requisição.
```java
public class AuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {
    @Autowired private TokenUtils tokenUtils;
    @Autowired private UsuarioService userDetailsService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	    //Recupera a instancia de tokenUtils e userDetailService
    	tokenUtils = WebApplicationContextUtils.getRequiredWebApplicationContext(
                		this.getServletContext()).getBean(TokenUtils.class);
        userDetailsService = WebApplicationContextUtils.getRequiredWebApplicationContext(
                		this.getServletContext()).getBean(UsuarioService.class);
        //Recupera o response e o request
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE, PATCH");
        resp.setHeader("Access-Control-Max-Age", "3600");
        resp.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, " + AppConstant.TOKEN_HEADER);
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        /* Recupera o Token que veio na requisição
		   Na classe: AppConstant.TOKEN_HEADER é armazenada a constante com o TOKEN que deve
		vir na requisição, toda a requisição vinda do cliente deve conter esse token no Header.
		Assim o usuário será autenticado e a requisição pode continuar.
        */
        String authToken = httpRequest.getHeader(AppConstant.TOKEN_HEADER);
        //Recupera o nome do usuário do token
        String username = this.tokenUtils.getUsernameFromToken(authToken);
		
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //Carrega o usuário contido no token
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            //Verifica se o token é válido, se sim, então realiza a autenticação e continua a requisição.
            if (this.tokenUtils.validateToken(authToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null,userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // a requisição continua aqui, independente do usuário autenticado ou não (token válido), pois pode ser que alguns endpoints não solicitem de permissão.	
        chain.doFilter(request, response);
    }
}
```

### AuthenticationRequest
Objeto que vai conter o json enviado na requisição com o usuário e senha.
```json
	{"username":"admin@admin.com", "password": "123"}
```
### AuthenticationResponse
Objeto que vai conter o token enviado na resposta para o cliente.
```json
	{"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMCIsIm5hbWUiOiJQYWxtZWlyYXMiLCJpYXQiOjE1MTYyMzkwMjJ9.gtqLloY91FM97vUBl8nk4Ga6PyNP_x2LuAAHS1kt1MA"
```
### EntryPointUnauthorizedHandler
Classe criada para tratar as exceções de acesso negado. O método **commence()** presente na classe, no caso de acesso negado, retorna a mensagem traduzida (pt-BR) para o cliente, sobrescrevendo a mensagem padrão em inglês.
### TokenUtils
Classe com os métodos necessários para gerar o token utilizando JWT (json web token).

```java
@Component
public class TokenUtils {
	private final String AUDIENCE_WEB = "web"; //As AUDIENCE são os tipos de aplicação cliente
	private final String AUDIENCE_MOBILE = "mobile";
	private final String AUDIENCE_TABLET = "tablet";
	private String secret = "utfpr"; //chave utilizada para gerar o token
	private Long expiration = 604800L; //tempo de validade do token
	
	//Método que recupera o corpo do jwt
	private Claims getClaimsFromToken(String token) {
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(this.secret).parseClaimsJws(token).getBody();
		} catch (Exception e) {
			claims = null;
		}
		return claims;
	}
	//Método que recupera o nome do usuário do jwt
	public String getUsernameFromToken(String token) {
		String username;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			username = claims.getSubject();
		} catch (Exception e) {
			username = null;
		}
		return username;
	}
	//Método que recupera a data de criação do jwt
	public Date getCreatedDateFromToken(String token) {
		Date createdDate;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			createdDate = new Date((Long) claims.get("created"));
		} catch (Exception e) {
			createdDate = null;
		}
		return createdDate;
	}
	//Método que recupera a data de expiração do jwt
	public Date getExpirationDateFromToken(String token) {
		Date expirationDate;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			expirationDate = claims.getExpiration();
		} catch (Exception e) {
			expirationDate = null;
		}
		return expirationDate;
	}
	//Método que recupera o tipo de aplicação cliente que enviou o jwt
	public String getAudienceFromToken(String token) {
		String audience;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			audience = claims.getAudience();
		} catch (Exception e) {
			audience = null;
		}
		return audience;
	}
	//Método para gerar a data atual
	private Date generateCurrentDate() {
		return new Date(System.currentTimeMillis());
	}
	//Método para gerar a data de expiração
	private Date generateExpirationDate() {
		return new Date(System.currentTimeMillis() + this.expiration * 1000);
	}
	//Método para verificar se o token expirou, verificando se a data de expiração é de antes da data atual
	private Boolean isTokenExpired(String token) {
		final Date expiration = this.getExpirationDateFromToken(token);
		return expiration.before(this.generateCurrentDate());
	}
	/*Verifica se o token foi criado antes da última atualização de senha no banco de dados.
	Como o token pode ser válido por uma semana ou um mês, ou até mais, o usuário pode 
	ter trocado de senha nesse intervalo. Portanto, para essa funcionalidade ser efetiva 
	a cada troca de senha do usuário, deverá ser salvo no campo lastPasswordReset da tabela
	de usuário a data de modificação da senha.
	*/
	private Boolean isCreatedBeforeLastPasswordReset(Date created, Date lastPasswordReset) {
		return (lastPasswordReset != null && created.before(lastPasswordReset));
	}
	//Método para ignorar o tempo de expiração do token. Nesse método se o tipo de aplicação
	//for Mobile ou Tablet o tempo de expiração do token será ignorado.
	public Boolean ignoreTokenExpired(String token) {
		String audience = this.getAudienceFromToken(token);
		return (this.AUDIENCE_TABLET.equals(audience) || this.AUDIENCE_MOBILE.equals(audience)); 
	}
	//Método para gerar o corpo (claims) do token
	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("sub", userDetails.getUsername());
		claims.put("audience", this.AUDIENCE_WEB);
		claims.put("created", this.generateCurrentDate());
		return this.generateToken(claims);
	}
	//Método que utiliza o corpo do token gerado no método anterior e gera o jwt final.
	private String generateToken(Map<String, Object> claims) {
		return Jwts.builder().setClaims(claims).setExpiration(this.generateExpirationDate())
				.signWith(SignatureAlgorithm.HS256, this.secret).compact();
	}
	//Método que verifica se o token pode ser atualizado, ou seja, se ele ainda é válido
	// e se o usuário não alterou sua senha desde a geração do token
	public Boolean canTokenBeRefreshed(String token, Date lastPasswordReset) {
		final Date created = this.getCreatedDateFromToken(token);
		return (!(this.isCreatedBeforeLastPasswordReset(created, lastPasswordReset))
				&& (!(this.isTokenExpired(token)) || this.ignoreTokenExpired(token)));
	}
	//gera um novo token
	public String refreshToken(String token) {
		String refreshedToken;
		try {
			final Claims claims = this.getClaimsFromToken(token);
			claims.put("created", this.generateCurrentDate());
			refreshedToken = this.generateToken(claims);
		} catch (Exception e) {
			refreshedToken = null;
		}
		return refreshedToken;
	}
	//verifica se o token é valido durante as requisições do usuário
	public Boolean validateToken(String token, UserDetails userDetails) {
		Usuario user = (Usuario) userDetails;
		final String username = this.getUsernameFromToken(token);
		final Date created = this.getCreatedDateFromToken(token);
		return (username.equals(user.getUsername()) && !(this.isTokenExpired(token))
				&& !(this.isCreatedBeforeLastPasswordReset(created,user.getLastPasswordReset())));
	}
	
}
```

## Processo de autenticação
O processo de autenticação de um usuário ocorre na seguinte ordem:

```mermaid
graph LR;
A[Cliente] -- json com usuário e senha --> B[AuthController]
B -- realiza a autenticação --> C[SpringSecurity]
C -- gera o token --> D[TokenUtil]
D -- devolve o token --> B
B -- envia o token gerado --> A
```
Independente de um cliente Web ou cliente Mobile o processo sempre é o mesmo. Após o usuário autenticar-se e obter o token, a cada nova requisição o cliente sempre deverá enviar o  token no header e o método **doFilter()** da classe **AuthenticationTokenFilter** será utilizado para carregar o usuário autenticado na requisição. E, com base nas permissões do usuário e configurações presentes na classe **WebSecurityConfiguration** o usuário será autorizado ou não à acessar cada recurso do servidor.
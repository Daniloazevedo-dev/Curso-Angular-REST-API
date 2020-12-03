package curso.api.rest.security;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import curso.api.rest.ApplicationContextLoad;
import curso.api.rest.model.Usuario;
import curso.api.rest.repository.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
@Component
public class JWTTokenAutenticacaoService {
	
	/* Tem a validade do token 2 dias */
	private static final long EXPIRATION_TIME = 172800000; //acessar sites de conversão de dia para milisegundos
	
	/* Uma senha única para comppor a autenticação e ajudar na segurança*/
	private static String SECRET = "SenhaExtremamenteSecreta";
	
	/* prefixo padrão de Token */
	private static final String TOKEN_PREFIX = "Bearer";
	
	/* Resposta de cabeçalho */
	private static final String HEADER_STRING = "Authorization";
	
	/* Gerando Token de autenticação e adicionando ao cabeçalho a resposta Http */
	public void addAuthentication(HttpServletResponse response, String username) throws IOException{
		
		/* Montagem do Token */
		String JWT = Jwts.builder() /* Chama o gerador de Token */
					.setSubject(username).setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))/* Tempo de expiração */
					.signWith(SignatureAlgorithm.HS512, SECRET).compact(); /* Compactação e algoritimo gerador de senha */

		/* Junta o token com o prefixo */
		String token = TOKEN_PREFIX + " " + JWT; /* Beader 8787w87w87w87w87w8w7w8w7w*/
		
		/* Adiciona no cabeçalho http */
		response.addHeader(HEADER_STRING, token); /* Authorization: Beader 8787w87w87w87w87w8w7w8w7w */
		
		liberacaoCors(response);
		
		/* Escreve token como resposta no corpo de http */
		response.getWriter().write("{\"Authorization\": \"" + token + "\"}");
	} 
	
	/* Retorna o usuário validado com token ou caso não seja valido retorna null */
	public Authentication getAuthentication(HttpServletRequest request, HttpServletResponse response) {
		
		/* Pega o Token enviado no cabeçalho http */
		String token = request.getHeader(HEADER_STRING);
		
		try {
		
		if(token != null) {
			
			/* Faz a validação do token do usuário na requisição */
			String user = Jwts.parser().setSigningKey(SECRET) /* Beader 8787w87w87w87w87w8w7w8w7w */
					.parseClaimsJws(token.replace(TOKEN_PREFIX, "")) /* 8787w87w87w87w87w8w7w8w7w */
					.getBody().getSubject(); /* João Silva */
			
			if(user != null) {
				
				Usuario usuario = ApplicationContextLoad.getApplicationContext()
						.getBean(UsuarioRepository.class).findUserByLogin(user);
				
				if(usuario != null) {
					
					return new UsernamePasswordAuthenticationToken(
							usuario.getLogin(),
							usuario.getSenha(), 
							usuario.getAuthorities());
					
				} 
			}
		}
		
		} catch (io.jsonwebtoken.ExpiredJwtException e) {
			liberacaoCors(response);
			return null;
			
		}
		
		//fim da condição
		
		liberacaoCors(response);
		
		return null; // Não autorizado
		
		}

	private void liberacaoCors(HttpServletResponse response) {
		/* Liberando resposta para porta diferente do projeto angular*/
		if(response.getHeader("Access-Control-Allow-Origin") == null) {
			response.addHeader("Access-Control-Allow-Origin", "*");
		}
		
		if(response.getHeader("Access-Control-Allow-Headers") == null) {
			response.addHeader("Access-Control-Allow-Headers", "*");
		}
		
		if(response.getHeader("Access-Control-Request-Headers") == null) {
			response.addHeader("Access-Control-Request-Headers", "*");
		}
		
		if(response.getHeader("Access-Control-Allow-Methods") == null) {
			response.addHeader("Access-Control-Allow-Methods", "*");
		}
		
		if(response.getHeader("Access-Control-Request-Methods") == null) {
			response.addHeader("Access-Control-Request-Methods", "*");
		}
		
		
	}
}
	


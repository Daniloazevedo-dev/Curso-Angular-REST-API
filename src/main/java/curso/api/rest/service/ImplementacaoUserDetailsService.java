package curso.api.rest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import curso.api.rest.model.Usuario;
import curso.api.rest.repository.UsuarioRepository;

@Service
public class ImplementacaoUserDetailsService implements UserDetailsService{

	@Autowired
	private UsuarioRepository usuarioRepositoy;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		/* Consulta no banco o usuario */
		
		Usuario usuario = usuarioRepositoy.findUserByLogin(username);
		
		if(usuario == null) {
			throw new UsernameNotFoundException("Usuário não foi encontrado!");
		}
		
		
		return new User(usuario.getLogin(), usuario.getPassword(), usuario.getAuthorities());
	}

	public void insereAcessoPadrao(Long id) {
		
		/* Descobre a constraint de restrição */
		String constraint = usuarioRepositoy.consultaConstraintRole();
		
		if(constraint != null) {
			/* Remove a constraint */
			jdbcTemplate.execute(" alter table usuarios_role DROP CONSTRAINT " + constraint);
		}
		
		/* insere os acessos padrão */
		usuarioRepositoy.insereAcessoRolePadrao(id);
		
	}

} 

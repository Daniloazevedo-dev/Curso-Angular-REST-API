package curso.api.rest.controller;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import curso.api.rest.model.UserChart;
import curso.api.rest.model.UserReport;
import curso.api.rest.model.Usuario;
import curso.api.rest.repository.TelefoneRepository;
import curso.api.rest.repository.UsuarioRepository;
import curso.api.rest.service.ImplementacaoUserDetailsService;
import curso.api.rest.service.ServiceRelatorio;

@CrossOrigin
@RestController /* Arquitetura REST */
@RequestMapping(value = "/usuario")
public class IndexController {
	
	@Autowired /* se fosse CDI seria Inject */
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private ImplementacaoUserDetailsService implementacaoUserDetailsService;
	
	@Autowired
	private TelefoneRepository telefoneRepository;
	
	@Autowired
	private ServiceRelatorio serviceRelatorio;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/*Serviço RETful */  //Buscar por id
	@GetMapping(value = "/{id}", produces = "application/json")
	@CachePut("cacheuser")
	public ResponseEntity<Usuario> init(@PathVariable(value = "id") Long id) {
		
		Optional<Usuario> usuario = usuarioRepository.findById(id);
		
		return new ResponseEntity<Usuario>(usuario.get(), HttpStatus.OK);
	}
	
	/*Vamos supor que o carregamento de usuarios seja um processo lento e queremos 
	 * controlar ele com cache para agilizar o processo*/
	
	// Busca Todos
	@GetMapping(value = "/", produces = "application/json")
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Usuario>> usuario() throws InterruptedException{
	
		PageRequest page = PageRequest.of(0, 5, Sort.by("nome"));
		
		Page<Usuario> list = usuarioRepository.findAll(page);
		
		//List<Usuario> list = (List<Usuario>) usuarioRepository.findAll();
		
		//Thread.sleep(6000); /* Segura o codigo por 6 segundos simulando um processo lento */
	
		return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
	}
	
	@GetMapping(value = "/page/{pagina}", produces = "application/json")
	@CachePut("cacheusuarios")
	public ResponseEntity<Page<Usuario>> usuarioPagina(@PathVariable("pagina") int pagina) throws InterruptedException{
	
		PageRequest page = PageRequest.of(pagina, 5, Sort.by("nome"));
		
		Page<Usuario> list = usuarioRepository.findAll(page);
		
		//List<Usuario> list = (List<Usuario>) usuarioRepository.findAll();
		
		//Thread.sleep(6000); /* Segura o codigo por 6 segundos simulando um processo lento */
	
		return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
	}
	
	// END-PONIT consulta de usuario por Nome
		@GetMapping(value = "/usuarioPorNome/{nome}", produces = "application/json")
		@CachePut("cacheusuarios")
		public ResponseEntity<Page<Usuario>> usuarioPorNome(@PathVariable("nome") String nome) throws InterruptedException{
		
			PageRequest pageRequest = null;
			Page<Usuario> list = null;
					
			if(nome == null || (nome != null && nome.trim().isEmpty()) || nome.equalsIgnoreCase("undefined")) { /* Não informou o nome */
				
				pageRequest = PageRequest.of(0, 5, Sort.by("nome"));
				list = usuarioRepository.findAll(pageRequest);
			} else {
				pageRequest = PageRequest.of(0, 5, Sort.by("nome"));
				list = usuarioRepository.findUserByNamePage(nome, pageRequest);
			}
						
			return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
			
		}
		
		// END-PONIT consulta de usuario por Nome
				@GetMapping(value = "/usuarioPorNome/{nome}/page/{page}", produces = "application/json")
				@CachePut("cacheusuarios")
				public ResponseEntity<Page<Usuario>> usuarioPorNomePage(@PathVariable("nome") String nome, @PathVariable("page") int page) throws InterruptedException{
				
					PageRequest pageRequest = null;
					Page<Usuario> list = null;
							
					if(nome == null || (nome != null && nome.trim().isEmpty()) || nome.equalsIgnoreCase("undefined")) { /* Não informou o nome */
						
						pageRequest = PageRequest.of(page, 5, Sort.by("nome"));
						list = usuarioRepository.findAll(pageRequest);
					} else {
						pageRequest = PageRequest.of(page, 5, Sort.by("nome"));
						list = usuarioRepository.findUserByNamePage(nome, pageRequest);
					}
					
					return new ResponseEntity<Page<Usuario>>(list, HttpStatus.OK);
				}
	
	// Salva usuários e telefones
	@PostMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> cadastrar(@RequestBody Usuario usuario){
		
		/* Corrigi data, salvando com 1 dia a menos */
		int dia = usuario.getDataNascimento().getDate() + 1;
		usuario.getDataNascimento().setDate(dia);
		
		for(int pos = 0; pos < usuario.getTelefones().size(); pos ++) {
			
			usuario.getTelefones().get(pos).setUsuario(usuario); //Faz referencia, para os telefones para salvar no banco
		}
		
		String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
		usuario.setSenha(senhacriptografada);
		
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		implementacaoUserDetailsService.insereAcessoPadrao(usuarioSalvo.getId());
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);		
	}
	
	//Atualiza usuarios e telefone
	@PutMapping(value = "/", produces = "application/json")
	public ResponseEntity<Usuario> atualizar(@RequestBody Usuario usuario){
		
		/* outras rotinas */
		
			System.out.println("Data do front: " + usuario.getDataNascimento());
		
			/* Corrigi data, salvando com 1 dia a menos */
			int dia = usuario.getDataNascimento().getDate() + 1;
			usuario.getDataNascimento().setDate(dia);
			
			System.out.println("Data Atualizada: " + usuario.getDataNascimento());
		
			for(int pos = 0; pos < usuario.getTelefones().size(); pos ++) {
			
				usuario.getTelefones().get(pos).setUsuario(usuario); //Faz referencia, para os telefones para salvar no banco
			}
			
			
			Usuario userTemporario = usuarioRepository.findById(usuario.getId()).get();
			
			if(!userTemporario.getSenha().equals(usuario.getSenha())) { //senhas difrentes
				
				String senhacriptografada = new BCryptPasswordEncoder().encode(usuario.getSenha());
				usuario.setSenha(senhacriptografada);
				
			}
			
		Usuario usuarioSalvo = usuarioRepository.save(usuario);
		
		return new ResponseEntity<Usuario>(usuarioSalvo, HttpStatus.OK);		
	}
	
	//Deleta
	@DeleteMapping(value = "/{id}", produces = "application/text")
	public String delete(@PathVariable("id") Long id) {
		
		usuarioRepository.deleteById(id);
		
		return "ok";
	}
	
	@DeleteMapping(value = "/removerTelefone/{id}", produces = "application/text")
	public String deleteTelefone(@PathVariable("id") Long id) {
		telefoneRepository.deleteById(id);
		 return "ok";
	}
	
	@GetMapping(value = "/relatorio", produces = "application/text")
	public ResponseEntity<String> download(HttpServletRequest request) throws Exception{
		
		byte[] pdf = serviceRelatorio.gerarRelatorio("relatorio-usuario", new HashMap(),
				request.getServletContext());
		
		String base64Pdf = "data:application/pdf;base64," + Base64.encodeBase64String(pdf);
		
		return new ResponseEntity<String>(base64Pdf, HttpStatus.OK);
		
	}
	
	@PostMapping(value = "/relatorio/", produces = "application/text")
	public ResponseEntity<String> downloadRelatorioParam(HttpServletRequest request, @RequestBody UserReport userReport) throws Exception{
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		SimpleDateFormat dateFormatParam = new SimpleDateFormat("dd-MM-yyyy");
		
		String dataInicio = dateFormatParam.format(dateFormat.parse(userReport.getDataInicio()));
		String dataFim = dateFormatParam.format(dateFormat.parse(userReport.getDataFim()));
		
		Map<String,Object> params = new HashMap<String,Object>();
		
		params.put("DATA_INICIO", dataInicio);
		params.put("DATA_FIM", dataFim);
		
		byte[] pdf = serviceRelatorio.gerarRelatorio("relatorio-usuario-param", params,
				request.getServletContext());
		
		String base64Pdf = "data:application/pdf;base64," + Base64.encodeBase64String(pdf);
		
		return new ResponseEntity<String>(base64Pdf, HttpStatus.OK);
		
	}
	
	@GetMapping(value = "/grafico", produces = "application/json")
	public ResponseEntity<UserChart> grafico(){
		
		UserChart userChart = new UserChart();
		
		List<String> resultado = jdbcTemplate.queryForList("select array_agg(nome) from usuario where salario > 0 and nome <> '' union all select cast(array_agg(salario) as character varying[]) from usuario where salario > 0 and nome <> ''", String.class);
		
		if(!resultado.isEmpty()) {
			
			String nomes = resultado.get(0).replaceAll("\\{", "").replaceAll("\\}", "");
			String salario = resultado.get(1).replaceAll("\\{", "").replaceAll("\\}", "");
			
			userChart.setNome(nomes);
			userChart.setSalario(salario);
			
		}
		
		return new ResponseEntity<UserChart>(userChart, HttpStatus.OK);
		
	}
	
}

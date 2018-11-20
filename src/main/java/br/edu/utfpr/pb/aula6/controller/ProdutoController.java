package br.edu.utfpr.pb.aula6.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.utfpr.pb.aula6.model.Categoria;
import br.edu.utfpr.pb.aula6.repository.CategoriaRepository;

@RestController
@RequestMapping("/categoria")
public class ProdutoController {

	@Autowired
	private CategoriaRepository categoriaRepository;
	
	@GetMapping(value = {"/",""} )
	public List<Categoria> listar(){
		return categoriaRepository.findAll();
	}

	@GetMapping(value = "/{id}")
	public Categoria buscar(@PathVariable Long id) {
		return categoriaRepository.findOne(id);
	}
	
	@PostMapping("/")
	@ResponseStatus(value = HttpStatus.CREATED)
	public void inserir(@RequestBody Categoria categoria) {
		categoriaRepository.save(categoria);
	}
	
	@PutMapping("/")
	@ResponseStatus(value = HttpStatus.OK)
	public void atualizar(@RequestBody Categoria categoria) {
		categoriaRepository.save(categoria);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(value = HttpStatus.OK)
	public void remover(@PathVariable Long id) {
		categoriaRepository.delete(id);
	}
}





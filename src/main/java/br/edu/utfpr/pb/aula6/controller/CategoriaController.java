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

import br.edu.utfpr.pb.aula6.model.Produto;
import br.edu.utfpr.pb.aula6.repository.ProdutoRepository;

@RestController
@RequestMapping("/produto")
public class CategoriaController {

	@Autowired
	private ProdutoRepository produtoRepository;
	
	@GetMapping(value = {"/",""} )
	public List<Produto> listar(){
		return produtoRepository.findAll();
	}

	@GetMapping(value = "/{id}")
	public Produto buscar(@PathVariable Long id) {
		return produtoRepository.findOne(id);
	}
	
	@PostMapping("/")
	@ResponseStatus(value = HttpStatus.CREATED)
	public void inserir(@RequestBody Produto produto) {
		produtoRepository.save(produto);
	}
	
	@PutMapping("/")
	@ResponseStatus(value = HttpStatus.OK)
	public void atualizar(@RequestBody Produto produto) {
		produtoRepository.save(produto);
	}
	
	@DeleteMapping("/{id}")
	@ResponseStatus(value = HttpStatus.OK)
	public void remover(@PathVariable Long id) {
		produtoRepository.delete(id);
	}
}





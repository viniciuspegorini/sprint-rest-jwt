package br.edu.utfpr.pb.aula6.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.utfpr.pb.aula6.model.Categoria;

public interface CategoriaRepository extends 
						JpaRepository<Categoria, Long>{

}

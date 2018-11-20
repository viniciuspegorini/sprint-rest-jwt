package br.edu.utfpr.pb.aula6.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.utfpr.pb.aula6.model.Permissao;

public interface PermissaoRepository 
				extends JpaRepository<Permissao, Long>{

	Permissao findByPermissao(String permissao);
}

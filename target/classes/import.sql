INSERT INTO permissao (permissao) values('ROLE_USER');
INSERT INTO permissao (permissao) values('ROLE_ADMIN');

INSERT INTO usuario(nome, username, password) VALUES ('Administrador', 'admin@admin.com','$2a$10$.PVIfB07x.SfMYTcToxL0.yxcLWU0GbS2NUO1W1QAvqMm/TsFhVem');
INSERT INTO usuario(nome, username, password) VALUES ('Teste', 'teste@teste.com','$2a$10$.PVIfB07x.SfMYTcToxL0.yxcLWU0GbS2NUO1W1QAvqMm/TsFhVem');

INSERT INTO usuario_permissoes(usuario_id, permissoes_id) VALUES (1, 1);
INSERT INTO usuario_permissoes(usuario_id, permissoes_id) VALUES (1, 2);
INSERT INTO usuario_permissoes(usuario_id, permissoes_id) VALUES (2, 2);

INSERT INTO categoria (descricao) values('Informática');
INSERT INTO categoria (descricao) values('Eletrônicos');
INSERT INTO categoria (descricao) values('Telefonia');

INSERT INTO produto (nome, descricao, valor, idcategoria) values('Monitor 17pol Nell', 'Monitor 17pol. FHD(1920x1080)..', 795.82, 1);

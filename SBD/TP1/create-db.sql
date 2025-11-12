DROP DATABASE IF EXISTS VetCare;
CREATE DATABASE VetCare;
USE VetCare;

CREATE TABLE Clinicas (
	morada varchar(255) NOT NULL,
    coordenadas varchar(40) NOT NULL,
    
    PRIMARY KEY(morada, coordenadas)
);

CREATE TABLE TipoServicos (
	servico ENUM('consulta médica', 'exames complementares de diagnóstico', 'intervenções cirúrgicas', 'medicina preventiva', 'tratamentos terapêuticos') NOT NULL,
    preco int unsigned,
    precoCancel int unsigned,
    
    PRIMARY KEY(servico)
);

CREATE TABLE Veterinario (
	nLicenca int unsigned NOT NULL,
    nomeVet varchar(150) NOT NULL,
    
    PRIMARY KEY(nLicenca),
    CHECK (nomeVet REGEXP  '[A-Z \a-z \'''-]')
);

CREATE TABLE VeterinarioClinica (
	nLicenca int unsigned NOT NULL,
    morada varchar(255) NOT NULL,
    coordenadas varchar(40) NOT NULL,
    
    PRIMARY KEY(nLicenca, morada, coordenadas),
    FOREIGN KEY (morada, coordenadas) REFERENCES Clinicas(morada, coordenadas) ON DELETE CASCADE,
    FOREIGN KEY (nLicenca) REFERENCES Veterinario(nLicenca) ON DELETE CASCADE
);

CREATE TABLE Horario (
	idBloco int unsigned NOT NULL,
	dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    morada varchar(255) NOT NULL,
    coordenadas varchar(40) NOT NULL,
    nLicenca int unsigned NOT NULL,
    servico ENUM('consulta médica', 'exames complementares de diagnóstico', 'intervenções cirúrgicas', 'medicina preventiva', 'tratamentos terapêuticos') NOT NULL,
    
    PRIMARY KEY(idBloco),
    FOREIGN KEY (morada, coordenadas) REFERENCES Clinicas(morada, coordenadas) ON DELETE CASCADE,
    FOREIGN KEY (nLicenca) REFERENCES Veterinario(nLicenca) ON DELETE CASCADE,
    FOREIGN KEY (servico) REFERENCES TipoServicos(servico) ON DELETE CASCADE
);

CREATE TABLE ClassTaxonomica (
	raca varchar(50) NOT NULL,
    predGeneticas varchar(255),
    porte ENUM('pequeno', 'médio', 'grande'),
    comprAdulto int unsigned,
    regAlimentar ENUM('herbívoro', 'carnívoro', 'omnívoro'),
    cuidEspecificos varchar(255),
    peso int unsigned,
    expecVida TINYINT unsigned,
    padrAtivos ENUM('diurno', 'noturno', 'crepuscular'),
    vocalizacao varchar(50),
    nomeComum varchar(50) NOT NULL,
    nomeCientifico varchar(50) NOT NULL,
    
    PRIMARY KEY(raca)
);

CREATE TABLE Dono (
	nif CHAR(9) NOT NULL,
    nome varchar(255) NOT NULL,
    prefLingua varchar(30),
	capSocial int,
    contacto CHAR(9),
    tipo ENUM('empresa', 'pessoa'),
    freguesia varchar(50),
    conselho varchar(50),
    distrito ENUM('Lisboa', 'Porto', 'Setúbal', 'Braga', 'Aveiro', 'Faro', 'Leiria', 'Santarém', 'Coimbra', 'Viseu', 'Viana do Castelo', 'Vila Real', 'Castelo Branco', 'Évora', 'Beja', 'Guarda', 'Bragança', 'Portalegre', 'Madeira', 'Açores'),
    
    PRIMARY KEY(nif),
    CHECK (nome REGEXP  '[A-Z \a-z \'''-]'),
    CHECK ((tipo = 'empresa' AND capSocial IS NOT NULL) OR tipo = 'pessoa')
);

CREATE TABLE Animal (
	nUtente int unsigned NOT NULL,
    nome varchar(255) NOT NULL,
    idade TINYINT unsigned,
    cores varchar(255),
    caracDistint varchar(255),
    nTransponder CHAR(15),
    estReprodutivo ENUM('castrado', 'esterilizado', 'fértil'),
    sexo ENUM('feminino', 'masculino'),
    peso int unsigned,
    fotografia BLOB,
    dataNasc DATE NOT NULL,
    alergias varchar(255),
    raca varchar(50),
    nifDono CHAR(9),
    
    PRIMARY KEY(nUtente),
    FOREIGN KEY (nifDono) REFERENCES Dono(nif) ON DELETE CASCADE,
    FOREIGN KEY (raca) REFERENCES ClassTaxonomica(raca),
    CHECK (nome REGEXP  '[A-Z \a-z \'''-]'),
    CHECK (idade >= 0),
    CHECK (peso > 0)
);

CREATE TABLE Marcacoes (
	dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    morada varchar(255) NOT NULL,
    coordenadas varchar(40) NOT NULL,
    nUtente int unsigned NOT NULL,
    estado ENUM ('agendado', 'cancelado', 'rejeitado', 'reagendado'),
    opiniao ENUM ('adorei', 'gostei', 'não vou voltar'),
    comentario varchar(500),
    nLicenca int unsigned NOT NULL,
    porPagar DECIMAL(10,2) unsigned,
    servico ENUM('consulta médica', 'exames complementares de diagnóstico', 'intervenções cirúrgicas', 'medicina preventiva', 'tratamentos terapêuticos') NOT NULL,
    
    PRIMARY KEY(dia, hInicio, hFim, nUtente),
    FOREIGN KEY (morada, coordenadas) REFERENCES Clinicas(morada, coordenadas) ON DELETE CASCADE,
	FOREIGN KEY (nLicenca) REFERENCES Veterinario(nLicenca) ON DELETE CASCADE,
    FOREIGN KEY (servico) REFERENCES TipoServicos(servico) ON DELETE CASCADE,
    FOREIGN KEY (nUtente) REFERENCES Animal(nUtente) ON DELETE CASCADE
);

CREATE TABLE HorarioMarcacoes (
	dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    nUtente int unsigned NOT NULL,
    idBloco int unsigned NOT NULL,
    
    PRIMARY KEY(idBloco, dia, hInicio, hFim, nUtente),
	FOREIGN KEY (dia, hInicio, hFim, nUtente) REFERENCES Marcacoes(dia, hInicio, hFim, nUtente) ON DELETE CASCADE,
    FOREIGN KEY (idBloco) REFERENCES Horario(idBloco) ON DELETE CASCADE
);

CREATE TABLE ResultadosExames (
	tipo ENUM('radiografias', 'ecografias', 'análises clínicas'),
    resultado BLOB,
    dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    nUtente int unsigned NOT NULL,
    
    PRIMARY KEY(dia, hInicio, hFim, nUtente),
    FOREIGN KEY (dia, hInicio, hFim, nUtente) REFERENCES Marcacoes(dia, hInicio, hFim, nUtente) 
);

CREATE TABLE ResultadosDesparatizacao (
	interna bool,
    externa bool,
    produtos varchar(255),
    dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    nUtente int unsigned NOT NULL,
    
    PRIMARY KEY(dia, hInicio, hFim, nUtente),
    FOREIGN KEY (dia, hInicio, hFim, nUtente) REFERENCES Marcacoes(dia, hInicio, hFim, nUtente)
);

CREATE TABLE ResultadosVacinacao (
	tipo varchar(50),
    fabricante varchar(50),
    dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    nUtente int unsigned NOT NULL,
    
    PRIMARY KEY(dia, hInicio, hFim, nUtente),
    FOREIGN KEY (dia, hInicio, hFim, nUtente) REFERENCES Marcacoes(dia, hInicio, hFim, nUtente)
);

CREATE TABLE ResultadosExamesFisicos(
	temperatura TINYINT unsigned,
    peso int unsigned,
    freqCardiaca int unsigned,
    dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    nUtente int unsigned NOT NULL,
    
    PRIMARY KEY(dia, hInicio, hFim, nUtente),
    FOREIGN KEY (dia, hInicio, hFim, nUtente) REFERENCES Marcacoes(dia, hInicio, hFim, nUtente),
    CHECK (peso > 0 )
);

CREATE TABLE ResultadosCirugia(
	tipo varchar(50),
    notasPO varchar(500),
    dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    nUtente int unsigned NOT NULL,
    
    PRIMARY KEY(dia, hInicio, hFim, nUtente),
    FOREIGN KEY (dia, hInicio, hFim, nUtente) REFERENCES Marcacoes(dia, hInicio, hFim, nUtente)
);

CREATE TABLE ResultadosTerapia(
	tipo varchar(50),
    dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    nUtente int unsigned NOT NULL,
    
    PRIMARY KEY(dia, hInicio, hFim, nUtente),
    FOREIGN KEY (dia, hInicio, hFim, nUtente) REFERENCES Marcacoes(dia, hInicio, hFim, nUtente)
);

CREATE TABLE ResultadosConsulta(
	motivo varchar(100),
    sintomas varchar(255),
    diagnostico varchar(255),
    medicacao varchar(100),
    dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    nUtente int unsigned NOT NULL,
    
    PRIMARY KEY(dia, hInicio, hFim, nUtente),
    FOREIGN KEY (dia, hInicio, hFim, nUtente) REFERENCES Marcacoes(dia, hInicio, hFim, nUtente)
);

CREATE VIEW marcacoesDoDia AS
SELECT m.servico, a.nome, m.hInicio, m.hFim, v.nomeVet, m.morada, m.coordenadas
FROM Marcacoes AS m, Animal AS a, Veterinario AS v
WHERE dia= curdate() AND estado= 'agendado' AND m.nUtente = a.nUtente AND m.nLicenca = v.nLicenca
ORDER BY hInicio;

CREATE VIEW horarioDiaVeterinarios AS
SELECT h.nLicenca, v.nomeVet, h.servico, h.hInicio, h.hFim, h.morada, h.coordenadas
FROM Horario AS h, Veterinario AS v
WHERE dia= curdate() AND h.nLicenca = v.nLicenca
ORDER BY hInicio;
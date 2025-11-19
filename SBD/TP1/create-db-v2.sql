DROP DATABASE IF EXISTS VetCare;
CREATE DATABASE VetCare;
USE VetCare;

CREATE TABLE Moradas (
	idMorada varchar(255) UNIQUE NOT NULL,
    rua varchar(100) NOT NULL,
    pais varchar(50) NOT NULL,
    coordenadas varchar(40) UNIQUE NOT NULL,
    freguesia varchar(50),
    concelho varchar(50),
    distrito ENUM('Lisboa', 'Porto', 'Setúbal', 'Braga', 'Aveiro', 'Faro', 'Leiria', 'Santarém', 'Coimbra', 'Viseu', 'Viana do Castelo', 'Vila Real', 'Castelo Branco', 'Évora', 'Beja', 'Guarda', 'Bragança', 'Portalegre', 'Madeira', 'Açores'),

    PRIMARY KEY(idMorada)
);

CREATE TABLE Clinicas (
	idClinica varchar(255) UNIQUE NOT NULL,
	idMorada varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idClinica),
    FOREIGN KEY (idMorada) REFERENCES Moradas(idMorada) ON DELETE CASCADE
);

CREATE TABLE TipoServicos (
	servico ENUM('consulta médica', 'exames complementares de diagnóstico', 'intervenções cirúrgicas', 'medicina preventiva', 'tratamentos terapêuticos') NOT NULL,
    preco int unsigned,
    precoCancel int unsigned,

    PRIMARY KEY(servico)
);

CREATE TABLE Users (
	idUser varchar(255) UNIQUE NOT NULL,
    nomeUser varchar(255) NOT NULL,
    contacto CHAR(9) UNIQUE NOT NULL,
    nif CHAR(9) UNIQUE NOT NULL,
    veterinario BOOL NOT NULL,
    dono BOOL NOT NULL,
    recepcionista BOOL NOT NULL,
    idMorada varchar(255) NOT NULL,

    PRIMARY KEY(idUser),
    FOREIGN KEY (idMorada) REFERENCES Moradas(idMorada) ON DELETE CASCADE,
    CHECK (nomeUser REGEXP '^[A-Za-z \'\-]+$')
);

CREATE TABLE Veterinario (
	nLicenca int unsigned UNIQUE NOT NULL,
    idUser varchar(255) UNIQUE NOT NULL,
    idClinica varchar(255) NOT NULL,

    PRIMARY KEY(idUser),
    FOREIGN KEY (idUser) REFERENCES Users(idUser) ON DELETE CASCADE,
    FOREIGN KEY (idClinica) REFERENCES Clinicas(idClinica) ON DELETE CASCADE
);

CREATE TABLE Recepcionista (
	idUser varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idUser),
    FOREIGN KEY (idUser) REFERENCES Users(idUser) ON DELETE CASCADE
);

CREATE TABLE VeterinarioClinica (
	idUser varchar(255) NOT NULL,
    idClinica varchar(255) NOT NULL,

    PRIMARY KEY(idUser, idClinica),
    FOREIGN KEY (idUser) REFERENCES Veterinario(idUser) ON DELETE CASCADE,
    FOREIGN KEY (idClinica) REFERENCES Clinicas(idClinica) ON DELETE CASCADE
);

CREATE TABLE Horario (
	idBloco varchar(255) UNIQUE NOT NULL,
    idClinica varchar(255) NOT NULL,
    idUser varchar(255) NOT NULL,
	dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    servico ENUM('consulta médica', 'exames complementares de diagnóstico', 'intervenções cirúrgicas', 'medicina preventiva', 'tratamentos terapêuticos') NOT NULL,

    PRIMARY KEY(idBloco, idClinica),
	FOREIGN KEY (idClinica) REFERENCES Clinicas(idClinica) ON DELETE CASCADE,
    FOREIGN KEY (idUser) REFERENCES Veterinario(idUser) ON DELETE CASCADE,
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
	idUser varchar(255) UNIQUE NOT NULL,
    prefLingua varchar(30),
	capSocial int,
    tipoDono ENUM('empresa', 'pessoa'),

    PRIMARY KEY(idUser),
    FOREIGN KEY (idUser) REFERENCES Users(idUser) ON DELETE CASCADE,
    CHECK ((tipoDono = 'empresa' AND capSocial IS NOT NULL) OR tipoDono = 'pessoa')
);

CREATE TABLE Animal (
	nUtente varchar(255) UNIQUE NOT NULL,
    nome varchar(255) NOT NULL,
    idade TINYINT unsigned,
    cores varchar(255),
    caracDistint varchar(255),
    nTransponder CHAR(15) UNIQUE,
    estReprodutivo ENUM('castrado', 'esterilizado', 'fértil'),
    sexo ENUM('feminino', 'masculino'),
    peso int unsigned,
    fotografia BLOB,
    dataNasc DATE NOT NULL,
    alergias varchar(255),
    raca varchar(50),
    tutor varchar(255) NOT NULL,
    pai varchar(255),
    mae varchar(255),

    PRIMARY KEY(nUtente),
    FOREIGN KEY (tutor) REFERENCES Dono(idUser) ON DELETE CASCADE,
    FOREIGN KEY (raca) REFERENCES ClassTaxonomica(raca),
    FOREIGN KEY (mae) REFERENCES Animal(nUtente),
    FOREIGN KEY (pai) REFERENCES Animal(nUtente),
    CHECK (nome REGEXP  '^[A-Za-z \'\-]+$'),
    CHECK (idade >= 0),
    CHECK (peso > 0)
);

CREATE TABLE Marcacoes (
	idMarcacao varchar(255) UNIQUE NOT NULL,
    idClinica varchar(255) NOT NULL,
    idBloco varchar(255) NOT NULL,
	dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    nUtente varchar(255) NOT NULL,
    estado ENUM ('agendado', 'cancelado', 'rejeitado', 'reagendado'),
    opiniao ENUM ('adorei', 'gostei', 'não vou voltar'),
    comentario varchar(500),
    marcadoPor varchar(255) NOT NULL,
    veterinario varchar(255) NOT NULL,
    porPagar DECIMAL(10,2) unsigned,
    servico ENUM('consulta médica', 'exames complementares de diagnóstico', 'intervenções cirúrgicas', 'medicina preventiva', 'tratamentos terapêuticos') NOT NULL,

    PRIMARY KEY(idMarcacao),
    FOREIGN KEY (idClinica) REFERENCES Clinicas(idClinica) ON DELETE CASCADE,
    FOREIGN KEY (marcadoPor) REFERENCES Users(idUser) ON DELETE CASCADE,
	FOREIGN KEY (veterinario) REFERENCES Veterinario(idUser) ON DELETE CASCADE,
    FOREIGN KEY (servico) REFERENCES TipoServicos(servico) ON DELETE CASCADE,
    FOREIGN KEY (nUtente) REFERENCES Animal(nUtente) ON DELETE CASCADE,
    FOREIGN KEY (idBloco) REFERENCES Horario(idBloco) ON DELETE CASCADE
);


CREATE TABLE Resultados (
	idResult varchar(255) UNIQUE NOT NULL,
    idMarcacao varchar(255) NOT NULL,
    tipoResultado ENUM('exame', 'desparasitação', 'vacinação', 'exames físicos', 'cirurgia', 'terapia', 'consulta'),

    PRIMARY KEY(idResult),
    FOREIGN KEY (idMarcacao) REFERENCES Marcacoes(idMarcacao) ON DELETE CASCADE
);


CREATE TABLE ResultadosExames (
	tipoExame ENUM('radiografias', 'ecografias', 'análises clínicas') NOT NULL,
    resultado BLOB NOT NULL,
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultados(idResult)
);

CREATE TABLE ResultadosDesparatizacao (
	interna bool NOT NULL,
    externa bool NOT NULL,
    produtos varchar(255) NOT NULL,
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultados(idResult)
);

CREATE TABLE ResultadosVacinacao (
	tipoVacina varchar(50) NOT NULL,
    fabricante varchar(50) NOT NULL,
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultados(idResult)
);

CREATE TABLE ResultadosExamesFisicos(
	temperatura TINYINT unsigned NOT NULL,
    peso int unsigned NOT NULL,
    freqCardiaca int unsigned NOT NULL,
    freqRespiratoria int unsigned NOT NULL,
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultados(idResult),
    CHECK (peso > 0 AND freqCardiaca > 0 AND freqRespiratoria > 0)
);

CREATE TABLE ResultadosCirurgia(
	tipoCirurgia varchar(50) NOT NULL,
    notasPO varchar(500),
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultados(idResult)
);

CREATE TABLE ResultadosTerapia(
	tipoTerapia varchar(50) NOT NULL,
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultados(idResult)
);

CREATE TABLE ResultadosConsulta(
	motivo varchar(100) NOT NULL,
    sintomas varchar(255) NOT NULL,
    diagnostico varchar(255) NOT NULL,
    medicacao varchar(100),
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultados(idResult)
);

CREATE VIEW marcacoesDoDia AS
SELECT m.servico, a.nome, m.hInicio, m.hFim, u.nomeUser, m.idClinica
FROM Marcacoes AS m, Animal AS a, Users AS u
WHERE dia= curdate() AND estado= 'agendado' AND m.nUtente = a.nUtente AND m.veterinario = u.idUser
ORDER BY hInicio;

CREATE VIEW horarioDiaVeterinarios AS
SELECT h.idUser, u.nomeUser, h.servico, h.hInicio, h.hFim, h.idClinica
FROM Horario AS h, Veterinario AS v, Users as u
WHERE dia= curdate() AND h.idUser = u.idUser
ORDER BY hInicio;

DELIMITER $$

CREATE TRIGGER verificaMarcacaoExistenteInsert
BEFORE INSERT
ON Marcacoes
FOR EACH ROW
BEGIN
	IF EXISTS(SELECT * FROM Marcacoes WHERE idClinica= NEW.idClinica AND dia= NEW.dia AND hInicio= NEW.hInicio AND veterinario= NEW.veterinario)
	THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'Já existe uma marcação para os parâmetros indicados!';
	END IF;
END;

CREATE TRIGGER verificaMarcacaoExistenteUpdate
BEFORE UPDATE
ON Marcacoes
FOR EACH ROW
BEGIN
	IF EXISTS(SELECT * FROM Marcacoes WHERE idClinica= NEW.idClinica AND dia= NEW.dia AND hInicio= NEW.hInicio AND veterinario= NEW.veterinario AND idMarcacao <> NEW.idMarcacao)
	THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'Já existe uma marcação para os parâmetros indicados!';
	END IF;
END;

CREATE TRIGGER capitalEmpresaInsert
BEFORE INSERT
ON Dono
FOR EACH ROW
BEGIN
	IF NEW.tipoDono= 'empresa' AND NEW.capSocial IS NULL
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'A empresa tem de obirgatoriamente registar o seu capital social!';
	END IF;
END;

CREATE TRIGGER capitalEmpresaUpdate
BEFORE UPDATE
ON Dono
FOR EACH ROW
BEGIN
	IF NEW.tipoDono= 'empresa' AND NEW.capSocial IS NULL
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'A empresa tem de obirgatoriamente registar o seu capital social!';
	END IF;
END;

CREATE TRIGGER validarDataNascimentoInsert
BEFORE INSERT
ON Animal
FOR EACH ROW
BEGIN
	IF NEW.dataNasc > CURRENT_DATE()
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'A data de nascimento não é válida.';
	END IF;
END;

CREATE TRIGGER validarDataNascimentoUpdate
BEFORE UPDATE
ON Animal
FOR EACH ROW
BEGIN
	IF NEW.dataNasc > CURRENT_DATE()
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'A data de nascimento não é válida.';
	END IF;
END;

CREATE FUNCTION calculaIdade (dataNasc DATE) RETURNS SMALLINT DETERMINISTIC
BEGIN
	RETURN YEAR(curdate())-YEAR(dataNasc) - ((MONTH(dataNasc) > MONTH(curdate())) OR (MONTH(dataNasc) = MONTH(curdate())) AND (DAY(dataNasc) > DAY(curdate())));
END;

CREATE TRIGGER validarDiaUtilInsert
BEFORE INSERT
ON Horario
FOR EACH ROW
BEGIN
	IF DAYOFWEEK(NEW.dia) = 1 OR  DAYOFWEEK(NEW.dia)= 7
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'Um bloco do horário apenas se aplica a dias úteis!';
	END IF;
END;

CREATE TRIGGER validarDiaUtilUpdate
BEFORE UPDATE
ON Horario
FOR EACH ROW
BEGIN
	IF DAYOFWEEK(NEW.dia) = 1 OR  DAYOFWEEK(NEW.dia)= 7
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'Um bloco do horário apenas se aplica a dias úteis!';
	END IF;
END;

 CREATE TRIGGER validarMarcacaoHorarioInsert
 BEFORE INSERT
 ON Marcacoes
 FOR EACH ROW
 BEGIN
	IF NOT EXISTS(SELECT * FROM Horario WHERE NEW.servico= servico AND NEW.idClinica= idClinica AND NEW.dia= dia AND NEW.hInicio >= hInicio AND NEW.hFim <= hFim)
	THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'O serviço solicitado não está disponível nesta marcação!';
	END IF;
END;

 CREATE TRIGGER validarMarcacaoHorarioUpdate
 BEFORE UPDATE
 ON Marcacoes
 FOR EACH ROW
 BEGIN
	IF NOT EXISTS(SELECT * FROM Horario WHERE NEW.servico= servico AND NEW.idClinica= idClinica AND NEW.dia= dia AND NEW.hInicio >= hInicio AND NEW.hFim <= hFim)
	THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'O serviço solicitado não está disponível nesta marcação!';
	END IF;
END;

CREATE TRIGGER validarHorasMarcacoesInsert
BEFORE INSERT
ON Marcacoes
FOR EACH ROW
BEGIN
	IF NEW.hInicio >= NEW.hFim
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'A hora de início tem de ser menor que a hora de fim da marcação!';
	END IF;
END;

CREATE TRIGGER validarHorasMarcacoesUpdate
BEFORE UPDATE
ON Marcacoes
FOR EACH ROW
BEGIN
	IF NEW.hInicio >= NEW.hFim
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'A hora de início tem de ser menor que a hora de fim da marcação!';
	END IF;
END;

CREATE TRIGGER validarHorasHorarioInsert
BEFORE INSERT
ON Horario
FOR EACH ROW
BEGIN
	IF NEW.hInicio >= NEW.hFim
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'A hora de início tem de ser menor que a hora de fim no bloco do horário!';
	END IF;
END;

CREATE TRIGGER validarHorasHorarioUpdate
BEFORE UPDATE
ON Horario
FOR EACH ROW
BEGIN
	IF NEW.hInicio >= NEW.hFim
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'A hora de início tem de ser menor que a hora de fim no bloco do horário!';
	END IF;
END;

CREATE TRIGGER verificarTutorMarcacaoInsert
BEFORE INSERT
ON Marcacoes
FOR EACH ROW
BEGIN
    -- verificar se marcadoPor é um dono (tutor)
    IF EXISTS (SELECT 1 FROM Dono WHERE idUser = NEW.marcadoPor)
       AND NEW.servico <> 'consulta médica'
    THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Um tutor só pode marcar consultas!';
    END IF;
END;


CREATE TRIGGER verificarTutorMarcacaoUpdate
BEFORE UPDATE
ON Marcacoes
FOR EACH ROW
BEGIN
    -- verificar se marcadoPor é um dono (tutor)
    IF EXISTS (SELECT 1 FROM Dono WHERE idUser = NEW.marcadoPor)
       AND NEW.servico <> 'consulta médica'
    THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Um tutor só pode marcar consultas!';
    END IF;
END;

CREATE TRIGGER verificarMoradaInsert
BEFORE INSERT
ON Users
FOR EACH ROW
BEGIN
    IF EXISTS(SELECT * FROM Moradas WHERE NEW.idMorada= idMorada AND pais= 'Portugal' AND (distrito IS NULL OR concelho IS NULL OR freguesia IS NULL))
    THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Uma morada portuguesa tem de ter informação sobre distrito, conselho e freguesia!';
    END IF;
END;

CREATE TRIGGER verificarMoradaUpdate
BEFORE UPDATE
ON Users
FOR EACH ROW
BEGIN
    IF EXISTS(SELECT * FROM Moradas WHERE NEW.idMorada= idMorada AND pais= 'Portugal' AND (distrito IS NULL OR concelho IS NULL OR freguesia IS NULL))
    THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Uma morada portuguesa tem de ter informação sobre distrito, conselho e freguesia!';
    END IF;
END;
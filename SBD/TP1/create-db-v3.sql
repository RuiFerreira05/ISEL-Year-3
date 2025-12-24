DROP DATABASE IF EXISTS VetCare;
CREATE DATABASE VetCare;
USE VetCare;

-- Tabela que contém as clínicas da empresa.
CREATE TABLE Clinica (
	localidade varchar(255) UNIQUE NOT NULL,
    coordenadas varchar(40) UNIQUE NOT NULL,

    PRIMARY KEY(localidade)
);

-- Tabela que contém os tipos de serviços oferecidos bem como os seus preços e preços de cancelamento.
CREATE TABLE TipoServico (
	servico varchar(100) UNIQUE NOT NULL,
    preco int unsigned,
    precoCancel int unsigned,

    PRIMARY KEY(servico),
    CONSTRAINT CHK_Servico CHECK (servico="consulta médica" OR servico="exames complementares de diagnóstico" OR servico="intervenções cirúrgicas" OR servico="medicina preventiva" OR servico="tratamentos terapêuticos")
);

-- Tabela que contém todas as pessoas que podem estar relacionadas com as clínicas, 
-- nomeadamente veterinários, recepcionistas e donos.
CREATE TABLE Utilizador (
	idUser varchar(255) UNIQUE NOT NULL,
    pass varchar(255) NOT NULL,
    nomeUser varchar(255) NOT NULL,
    contacto CHAR(9) UNIQUE NOT NULL,
    nif CHAR(9) UNIQUE NOT NULL,
    veterinario BOOL NOT NULL,
    dono BOOL NOT NULL,
    recepcionista BOOL NOT NULL,
    gerente BOOL NOT NULL,
    pais varchar(40) NOT NULL,
    rua varchar(100) NOT NULL,
    freguesia varchar(50),
    concelho varchar(50),
    distrito varchar(255),

    PRIMARY KEY(idUser),
    CHECK (nomeUser REGEXP '^[A-Za-z \'\-]+$'),
    CONSTRAINT CHK_Distrito CHECK (distrito = "Lisboa" OR distrito="Porto" OR distrito="Setúbal" OR distrito="Braga" OR distrito="Aveiro" OR distrito="Faro" OR distrito="Leiria" OR distrito="Santarém" OR distrito="Coimbra" OR distrito="Viseu" OR distrito="Viana do Castelo" OR
    distrito="Vila Real" OR distrito="Castelo Branco" OR distrito="Évora" OR distrito="Beja" OR distrito="Guarda" OR distrito="Bragança" OR distrito="Portalegre" OR distrito="Madeira" OR distrito="Açores")
);

-- Tabela que contém todos os veterinários que trabalham nas clínicas da empresa.
-- Um veterinário pode trabalhar em várias clínicas.
CREATE TABLE Veterinario (
	nLicenca int unsigned UNIQUE NOT NULL,
    idUser varchar(255) UNIQUE NOT NULL,
    -- localidade varchar(255) NOT NULL,

    PRIMARY KEY(idUser),
    FOREIGN KEY (idUser) REFERENCES Utilizador(idUser) ON DELETE CASCADE
    -- FOREIGN KEY (localidade) REFERENCES Clinica(localidade) ON DELETE CASCADE
);

-- Tabela que contém todos os recepcionistas que trabalham nas clínicas da empresa.
-- Um recepcionista só pode trabalhar numa clínica.
CREATE TABLE Recepcionista (
	idUser varchar(255) UNIQUE NOT NULL,
    localidade varchar(255) NOT NULL,

    PRIMARY KEY(idUser),
    FOREIGN KEY (idUser) REFERENCES Utilizador(idUser) ON DELETE CASCADE,
    FOREIGN KEY (localidade) REFERENCES Clinica(localidade) ON DELETE CASCADE
);

CREATE TABLE Gerente (
	idUser varchar(255) UNIQUE NOT NULL,
    localidade varchar(255) NOT NULL,

    PRIMARY KEY(idUser),
    FOREIGN KEY (idUser) REFERENCES Utilizador(idUser) ON DELETE CASCADE,
    FOREIGN KEY (localidade) REFERENCES Clinica(localidade) ON DELETE CASCADE
);

-- Tabela que contém todos os veterinários que trabalham nas clínicas da empresa com informação da(s) clínica(s)
-- onde trabalham.
CREATE TABLE VeterinarioClinica (
	idUser varchar(255) NOT NULL,
    localidade varchar(255) NOT NULL,

    PRIMARY KEY(idUser, localidade),
    FOREIGN KEY (idUser) REFERENCES Veterinario(idUser) ON DELETE CASCADE,
    FOREIGN KEY (localidade) REFERENCES Clinica(localidade) ON DELETE CASCADE
);

-- Tabela que contém todos os blocos dos horários de todas as clínicas da empresa.
-- Mostra informação sobre hora de iníco e fim do bloco, a localidade da clínica onde vai ser realizado,
-- o veterinário responsável pelo mesmo e o tipo de serviço.
CREATE TABLE Horario (
	idBloco varchar(255) UNIQUE NOT NULL,
    localidade varchar(255) NOT NULL,
    idUser varchar(255) NOT NULL,
	dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    servico varchar(255) NOT NULL,

    PRIMARY KEY(idBloco, localidade),
	FOREIGN KEY (localidade) REFERENCES Clinica(localidade) ON DELETE CASCADE,
    FOREIGN KEY (idUser) REFERENCES Veterinario(idUser) ON DELETE CASCADE,
    FOREIGN KEY (servico) REFERENCES TipoServico(servico) ON DELETE CASCADE,
    CONSTRAINT CHK_Servico_H CHECK (servico="consulta médica" OR servico="exames complementares de diagnóstico" OR servico="intervenções cirúrgicas" OR servico="medicina preventiva" OR servico="tratamentos terapêuticos")
);

-- Tabela que contém informações sobre cada raça presente (informações que diferem de raça para raça).
CREATE TABLE Raca (
	nome varchar(50) UNIQUE NOT NULL,
    predGeneticas varchar(255),
    porte varchar(255),
    comprAdulto int unsigned,
    cuidEspecificos varchar(255),
    peso int unsigned,
    expecVida TINYINT unsigned,
    padrAtivos varchar(255),

    PRIMARY KEY(nome),
    CONSTRAINT CHK_Porte CHECK (porte="pequeno" OR porte="médio"OR porte="grande"),
    CONSTRAINT CHK_PadrAtivos CHECK (padrAtivos="diurno" OR padrAtivos="noturno" OR padrAtivos="crepuscular")
);

-- Tabela que contém informações sobre cada espécie presente (informações que diferem de espécie para espécie).
CREATE TABLE Especie (
	nomeComum varchar(50) NOT NULL,
    nomeCientifico varchar(50) UNIQUE NOT NULL,
    regAlimentar varchar(255),
    vocalizacao varchar(50),

    PRIMARY KEY(nomeCientifico),
    CONSTRAINT CHK_RegAlimentar CHECK (regAlimentar="herbívoro" OR regAlimentar="carnívoro" OR regAlimentar="omnívoro")
);

-- Tabela que contém informação sobre todas as raças pertencentes a cada espécie.
CREATE TABLE ClassTaxonomica (
	nomeCientifico varchar(50) NOT NULL,
    nome varchar(50) UNIQUE NOT NULL,

    PRIMARY KEY(nome),
    FOREIGN KEY (nomeCientifico) REFERENCES Especie(nomeCientifico) ON DELETE CASCADE,
    FOREIGN KEY (nome) REFERENCES Raca(nome) ON DELETE CASCADE
);

-- Tabela que contém informação sobre os tutores existentes para os animais que são acompanhados nas clínicas.
CREATE TABLE Dono (
	idUser varchar(255) UNIQUE NOT NULL,
    prefLingua varchar(30),
	capSocial int,
    tipoDono varchar(255) NOT NULL,

    PRIMARY KEY(idUser),
    FOREIGN KEY (idUser) REFERENCES Utilizador(idUser) ON DELETE CASCADE,
    CONSTRAINT CHK_TipoDono CHECK (tipoDono="empresa" OR tipoDono="pessoa"),
    CHECK ((tipoDono = 'empresa' AND capSocial IS NOT NULL) OR tipoDono = 'pessoa')

);

-- Tabela que contém informações sobre todos os animais que são acompanhados nas clínicas.
CREATE TABLE Animal (
	nUtente varchar(255) UNIQUE NOT NULL,
    nome varchar(255) NOT NULL,
    idade TINYINT unsigned,
    cores varchar(255),
    caracDistint varchar(255),
    nTransponder CHAR(15) UNIQUE,
    estReprodutivo varchar(50),
    sexo varchar(50) NOT NULL,
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
    FOREIGN KEY (raca) REFERENCES ClassTaxonomica(nome),
    FOREIGN KEY (mae) REFERENCES Animal(nUtente),
    FOREIGN KEY (pai) REFERENCES Animal(nUtente),
    CONSTRAINT CHK_EstReprodutivo CHECK (estReprodutivo="castrado" OR estReprodutivo="esterilizado" OR estReprodutivo="fértil"),
    CONSTRAINT CHK_Sexo CHECK (sexo="feminino" OR sexo="masculino"),
    CHECK (nome REGEXP  '^[A-Za-z \'\-]+$'),
    CHECK (idade >= 0),
    CHECK (peso > 0)
);

-- Tabela que contém todas as marcações realizadas para as clínicas, estejam por realizar ou não.
CREATE TABLE Marcacao (
	idMarcacao varchar(255) UNIQUE NOT NULL,
    localidade varchar(255) NOT NULL,
    idBloco varchar(255) NOT NULL,
	dia DATE NOT NULL,
    hInicio time NOT NULL,
    hFim time NOT NULL,
    nUtente varchar(255) NOT NULL,
    estado varchar(50) NOT NULL,
    opiniao varchar(50),
    comentario varchar(500),
    marcadoPor varchar(255) NOT NULL,
    veterinario varchar(255) NOT NULL,
    porPagar DECIMAL(10,2) unsigned,
    servico varchar(50) NOT NULL,

    PRIMARY KEY(idMarcacao, localidade), -- Não devia ter também localização??
    FOREIGN KEY (localidade) REFERENCES Clinica(localidade) ON DELETE CASCADE,
    FOREIGN KEY (marcadoPor) REFERENCES Utilizador(idUser) ON DELETE CASCADE,
	FOREIGN KEY (veterinario) REFERENCES Veterinario(idUser) ON DELETE CASCADE,
    FOREIGN KEY (servico) REFERENCES TipoServico(servico) ON DELETE CASCADE,
    FOREIGN KEY (nUtente) REFERENCES Animal(nUtente) ON DELETE CASCADE,
    FOREIGN KEY (idBloco) REFERENCES Horario(idBloco) ON DELETE CASCADE,
    CONSTRAINT CHK_Estado CHECK (estado="agendado" OR estado="cancelado" OR estado="rejeitado" OR estado="reagendado"),
    CONSTRAINT CHK_Opiniao CHECK (opiniao="adorei" OR opiniao="gostei" OR opiniao="não vou voltar"),
    CONSTRAINT CHK_Servico_M CHECK (servico="consulta médica" OR servico="exames complementares de diagnóstico" OR servico="intervenções cirúrgicas" OR servico="medicina preventiva" OR servico="tratamentos terapêuticos")
);

-- Tabela que contém todos os resultados correspondentes a marcações realizadas.
CREATE TABLE Resultado (
	idResult varchar(255) UNIQUE NOT NULL,
    idMarcacao varchar(255) NOT NULL,
    tipoResultado varchar(50) NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idMarcacao) REFERENCES Marcacao(idMarcacao) ON DELETE CASCADE,
    CONSTRAINT CHK_TipoResultado CHECK (tipoResultado="exame" OR tipoResultado="desparatização" OR tipoResultado="vacinação" OR tipoResultado="exames físicos" OR tipoResultado="cirurgia" OR tipoResultado="terapia" OR tipoResultado="consulta")
);

-- Tabela que contém todos os resultados de exames de todas as clínicas.
CREATE TABLE ResultExame (
	tipoExame varchar(50) NOT NULL,
    resultado BLOB NOT NULL,
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultado(idResult),
    CONSTRAINT CHK_TipoExame CHECK (tipoExame="radiografias" OR tipoExame="ecografias" OR tipoExame="análises clínicas")
);

-- Tabela que contém todos os resultados de desparatizações de todas as clínicas.
CREATE TABLE ResultDespar(
	interna bool NOT NULL,
    externa bool NOT NULL,
    produtos varchar(255) NOT NULL,
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultado(idResult)
);

-- Tabela que contém todos os resultados de vacinação de todas as clínicas.
CREATE TABLE ResultVacinacao (
	tipoVacina varchar(50) NOT NULL,
    fabricante varchar(50) NOT NULL,
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultado(idResult)
);

-- Tabela que contém todos os resultados de exames físicos de todas as clínicas.
CREATE TABLE ResultExamFisic(
	temperatura TINYINT unsigned NOT NULL,
    peso int unsigned NOT NULL,
    freqCardiaca int unsigned NOT NULL,
    freqRespiratoria int unsigned NOT NULL,
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultado(idResult),
    CHECK (peso > 0 AND freqCardiaca > 0 AND freqRespiratoria > 0)
);

-- Tabela que contém todos os resultados de cirurgias de todas as clínicas.
CREATE TABLE ResultCirurgia(
	tipoCirurgia varchar(50) NOT NULL,
    notasPO varchar(500),
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultado(idResult)
);

-- Tabela que contém todos os resultados de terapias de todas as clínicas.
CREATE TABLE ResultTerapia(
	tipoTerapia varchar(50) NOT NULL,
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultado(idResult)
);

-- Tabela que contém todos os resultados de consultas de todas as clínicas.
CREATE TABLE ResultConsulta(
	motivo varchar(100) NOT NULL,
    sintomas varchar(255) NOT NULL,
    diagnostico varchar(255) NOT NULL,
    medicacao varchar(100),
    idResult varchar(255) UNIQUE NOT NULL,

    PRIMARY KEY(idResult),
    FOREIGN KEY (idResult) REFERENCES Resultado(idResult)
);

-- Vista que mostra todas as marcações que vão acontecer no dia atual para todas as clínicas da empresa.
CREATE VIEW marcacoesDoDia AS
SELECT m.servico, a.nome, m.hInicio, m.hFim, u.nomeUser, m.localidade
FROM Marcacao AS m, Animal AS a, Utilizador AS u
WHERE dia= curdate() AND estado= 'agendado' AND m.nUtente = a.nUtente AND m.veterinario = u.idUser
ORDER BY hInicio;

-- Vista que mostra todos os blocos de horário correspondentes ao dia atual e o respetivo veterinário responsável.
CREATE VIEW horarioDiaVeterinarios AS
SELECT h.idUser, u.nomeUser, h.servico, h.hInicio, h.hFim, h.localidade
FROM Horario AS h, Veterinario AS v, Utilizador as u
WHERE dia= curdate() AND h.idUser = u.idUser
ORDER BY hInicio;

DELIMITER $$

-- Verifica se já existe uma marcação existente para o mesmo veterinário no mesmo dia, hora e clínica, antes de ser inserida uma nova marcação.
CREATE TRIGGER verificaMarcacaoExistenteInsert
BEFORE INSERT
ON Marcacao
FOR EACH ROW
BEGIN
	IF EXISTS(SELECT * FROM Marcacao WHERE localidade= NEW.localidade AND dia= NEW.dia AND hInicio= NEW.hInicio AND veterinario= NEW.veterinario)
	THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'Já existe uma marcação para os parâmetros indicados!';
	END IF;
END;

-- Verifica se já existe uma marcação existente para o mesmo veterinário no mesmo dia, hora e clínica, antes de ser alterada uma marcação já existente.
CREATE TRIGGER verificaMarcacaoExistenteUpdate
BEFORE UPDATE
ON Marcacao
FOR EACH ROW
BEGIN
	IF EXISTS(SELECT * FROM Marcacao WHERE localidade= NEW.localidade AND dia= NEW.dia AND hInicio= NEW.hInicio AND veterinario= NEW.veterinario AND idMarcacao <> NEW.idMarcacao)
	THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'Já existe uma marcação para os parâmetros indicados!';
	END IF;
END;

-- Verifica se uma empresa que se designa como tutora de um ou mais animais, tem informação sobre o capital social antes de ser adicionada.
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

-- Verifica se uma empresa que se designa como tutora de um ou mais animais, tem informação sobre o capital social antes de ser alterada.
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

-- Verifica se a data de nascimento a ser inserida é válida.
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

-- Verifica se a data de nascimento a ser alterada é válida.
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

-- Função que calcula a idade através de uma data de nascimento.
CREATE FUNCTION calculaIdade (dataNasc DATE) RETURNS SMALLINT DETERMINISTIC
BEGIN
	RETURN YEAR(curdate())-YEAR(dataNasc) - ((MONTH(dataNasc) > MONTH(curdate())) OR (MONTH(dataNasc) = MONTH(curdate())) AND (DAY(dataNasc) > DAY(curdate())));
END;

-- Verifica que o dia a ser inserido corresponde a um dia útil.
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

-- Verifica que o dia a ser alterado corresponde a um dia útil.
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

-- Verifica se a marcação a ser adicionada, está dentro de um bloco de horário (da clínica em questão) que fornece o serviço pedido existente.
 CREATE TRIGGER validarMarcacaoHorarioInsert
 BEFORE INSERT
 ON Marcacao
 FOR EACH ROW
 BEGIN
	IF NOT EXISTS(SELECT * FROM Horario WHERE NEW.servico= servico AND NEW.localidade= localidade AND NEW.dia= dia AND NEW.hInicio >= hInicio AND NEW.hFim <= hFim)
	THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'O serviço solicitado não está disponível nesta marcação!';
	END IF;
END;

-- Verifica se a marcação a ser alterada, está dentro de um bloco de horário (da clínica em questão) que fornece o serviço pedido existente.
 CREATE TRIGGER validarMarcacaoHorarioUpdate
 BEFORE UPDATE
 ON Marcacao
 FOR EACH ROW
 BEGIN
	IF NOT EXISTS(SELECT * FROM Horario WHERE NEW.servico= servico AND NEW.localidade= localidade AND NEW.dia= dia AND NEW.hInicio >= hInicio AND NEW.hFim <= hFim)
	THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'O serviço solicitado não está disponível nesta marcação!';
	END IF;
END;

-- Verifica se as horas de início e fim das marcações a ser adicionadas são válidas.
CREATE TRIGGER validarHorasMarcacoesInsert
BEFORE INSERT
ON Marcacao
FOR EACH ROW
BEGIN
	IF NEW.hInicio >= NEW.hFim
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'A hora de início tem de ser menor que a hora de fim da marcação!';
	END IF;
END;

-- Verifica se as horas de início e fim das marcações a ser alteradas são válidas.
CREATE TRIGGER validarHorasMarcacoesUpdate
BEFORE UPDATE
ON Marcacao
FOR EACH ROW
BEGIN
	IF NEW.hInicio >= NEW.hFim
    THEN
		SIGNAL SQLSTATE '45000'
		SET MESSAGE_TEXT = 'A hora de início tem de ser menor que a hora de fim da marcação!';
	END IF;
END;

-- Verifica se as horas de início e fim dos blocos de horário a ser adicionados são válidas.
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

-- Verifica se as horas de início e fim dos blocos de horário a ser alterados são válidas.
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

-- Verifica se a marcação a ser adicionada é marcada por um tutor é uma consulta, que é o único tipo de marcação que pode fazer.
CREATE TRIGGER verificarTutorMarcacaoInsert
BEFORE INSERT
ON Marcacao
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

-- Verifica se a marcação a ser alterada é marcada por um tutor é uma consulta, que é o único tipo de marcação que pode fazer.
CREATE TRIGGER verificarTutorMarcacaoUpdate
BEFORE UPDATE
ON Marcacao
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

-- Verifica se a morada a ser adicionada é portuguesa e, se fôr o caso, verifica se tem informação sobre distrito, concelho e freguesia.
CREATE TRIGGER verificarMoradaInsert
BEFORE INSERT
ON Utilizador
FOR EACH ROW
BEGIN
    IF NEW.pais= 'Portugal' AND (NEW.distrito IS NULL OR NEW.concelho IS NULL OR NEW.freguesia IS NULL)
    THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Uma morada portuguesa tem de ter informação sobre distrito, conselho e freguesia!';
    END IF;
END;

-- Verifica se a morada a ser alterada é portuguesa e, se fôr o caso, verifica se tem informação sobre distrito, concelho e freguesia.
CREATE TRIGGER verificarMoradaUpdate
BEFORE UPDATE
ON Utilizador
FOR EACH ROW
BEGIN
    IF NEW.pais= 'Portugal' AND (NEW.distrito IS NULL OR NEW.concelho IS NULL OR NEW.freguesia IS NULL)
    THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Uma morada portuguesa tem de ter informação sobre distrito, conselho e freguesia!';
    END IF;
END;
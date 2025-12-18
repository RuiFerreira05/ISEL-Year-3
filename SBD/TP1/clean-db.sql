USE VetCare;

-- ELIMINAÇÃO DA FUNÇÃO

DROP FUNCTION IF EXISTS calculaIdade;

-- ELIMINAÇÃO DOS TRIGGERS

DROP TRIGGER IF EXISTS verificaMarcacaoExistenteInsert;
DROP TRIGGER IF EXISTS verificaMarcacaoExistenteUpdate;
DROP TRIGGER IF EXISTS capitalEmpresaInsert;
DROP TRIGGER IF EXISTS capitalEmpresaUpdate;
DROP TRIGGER IF EXISTS validarDataNascimentoInsert;
DROP TRIGGER IF EXISTS validarDataNascimentoUpdate;
DROP TRIGGER IF EXISTS validarDiaUtilInsert;
DROP TRIGGER IF EXISTS validarDiaUtilUpdate;
DROP TRIGGER IF EXISTS validarMarcacaoHorarioInsert;
DROP TRIGGER IF EXISTS validarMarcacaoHorarioUpdate;
DROP TRIGGER IF EXISTS validarHorasMarcacoesInsert;
DROP TRIGGER IF EXISTS validarHorasMarcacoesUpdate;
DROP TRIGGER IF EXISTS validarHorasHorarioInsert;
DROP TRIGGER IF EXISTS validarHorasHorarioUpdate;
DROP TRIGGER IF EXISTS verificarTutorMarcacaoInsert;
DROP TRIGGER IF EXISTS verificarTutorMarcacaoUpdate;
DROP TRIGGER IF EXISTS verificarMoradaInsert;
DROP TRIGGER IF EXISTS verificarMoradaUpdate;

-- ELIMINAÇÃO DAS VISTAS

DROP VIEW IF EXISTS marcacoesDoDia;
DROP VIEW IF EXISTS horarioDiaVeterinarios;

-- ELIMINAÇÃO DAS TABELAS

DROP TABLE IF EXISTS ResultConsulta;
DROP TABLE IF EXISTS ResultTerapia;
DROP TABLE IF EXISTS ResultCirurgia;
DROP TABLE IF EXISTS ResultExamFisico;
DROP TABLE IF EXISTS ResultVacinacao;
DROP TABLE IF EXISTS ResultDespar;
DROP TABLE IF EXISTS ResultExame;
DROP TABLE IF EXISTS Resultado;
DROP TABLE IF EXISTS Marcacao;
DROP TABLE IF EXISTS Animal;
DROP TABLE IF EXISTS Dono;
DROP TABLE IF EXISTS ClassTaxonomica;
DROP TABLE IF EXISTS Especie;
DROP TABLE IF EXISTS Raca;
DROP TABLE IF EXISTS Horario;
DROP TABLE IF EXISTS VeterinarioClinica;
DROP TABLE IF EXISTS Recepcionista;
DROP TABLE IF EXISTS Veterinario;
DROP TABLE IF EXISTS Utilizador;
DROP TABLE IF EXISTS TipoServico;
DROP TABLE IF EXISTS Clinica;
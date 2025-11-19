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

DROP TABLE IF EXISTS ResultadosConsulta;
DROP TABLE IF EXISTS ResultadosTerapia;
DROP TABLE IF EXISTS ResultadosCirurgia;
DROP TABLE IF EXISTS ResultadosExamesFisicos;
DROP TABLE IF EXISTS ResultadosVacinacao;
DROP TABLE IF EXISTS ResultadosDesparatizacao;
DROP TABLE IF EXISTS ResultadosExames;
DROP TABLE IF EXISTS Resultados;
DROP TABLE IF EXISTS Marcacoes;
DROP TABLE IF EXISTS Animal;
DROP TABLE IF EXISTS Dono;
DROP TABLE IF EXISTS ClassTaxonomica;
DROP TABLE IF EXISTS Horario;
DROP TABLE IF EXISTS VeterinarioClinica;
DROP TABLE IF EXISTS Recepcionista;
DROP TABLE IF EXISTS Veterinario;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS TipoServicos;
DROP TABLE IF EXISTS Clinicas;
DROP TABLE IF EXISTS Moradas;
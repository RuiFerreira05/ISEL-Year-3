USE VetCare;

-- CLINICA
INSERT INTO Clinica (localidade, coordenadas) VALUES
('Póvoa de Santa Iria', '38.858059, -9.076871'),
('Almeirim', '39.201392, -8.628219'),
('Montijo', '38.70624434781486, -8.968442098691304');

-- TIPOSERVICO
INSERT INTO TipoServico (servico, preco, precoCancel) VALUES
('consulta médica', 30, 20),
('exames complementares de diagnóstico', 75, 50),
('medicina preventiva', 40, 30),
('intervenções cirúrgicas', 120, 90),
('tratamentos terapêuticos', 40, 30);

-- UTILIZADOR
-- Corrected 'Snatarém' to 'Santarém'. Ensure schema CHECK constraint is updated.
SET @user_vet_1001 = UUID();
INSERT INTO Utilizador (idUser, nomeUser, contacto, nif, veterinario, dono, recepcionista, pais, rua, freguesia, concelho, distrito)
VALUES (@user_vet_1001, 'Rui Ferreira', '912345678', '100100100', TRUE, FALSE, FALSE, 'Portugal', 'Av. Prof. Dr. Egas Moniz 70-12, Póvoa de Santa Iria', 'Póvoa de Santa Iria', 'Vila Franca de Xira', 'Lisboa');

SET @user_vet_1002 = UUID();
INSERT INTO Utilizador (idUser, nomeUser, contacto, nif, veterinario, dono, recepcionista, pais, rua, freguesia, concelho, distrito)
VALUES (@user_vet_1002, 'Matilde Goncalves', '912345679', '100200200', TRUE, FALSE, FALSE, 'Portugal', 'R. Paul Harris 21, Almeirim', 'Almeirim', 'Almeirim', 'Santarém');

SET @user_vet_1003 = UUID();
INSERT INTO Utilizador (idUser, nomeUser, contacto, nif, veterinario, dono, recepcionista, pais, rua, freguesia, concelho, distrito)
VALUES (@user_vet_1003, 'Marta Garcia', '912345680', '100300300', TRUE, FALSE, FALSE, 'Portugal', 'R. Prof. António Caleiro 117, 2870-358 Montijo', 'Montijo', 'Montijo', 'Setúbal');

SET @user_dono_1 = UUID();
INSERT INTO Utilizador (idUser, nomeUser, contacto, nif, veterinario, dono, recepcionista, pais, rua, freguesia, concelho, distrito)
VALUES (@user_dono_1, 'Joaquim Marques', '213456789', '123456789', FALSE, TRUE, FALSE, 'Portugal', 'Av. Prof. Dr. Egas Moniz 70-12, Póvoa de Santa Iria', 'Póvoa de Santa Iria', 'Vila Franca de Xira', 'Lisboa');

SET @user_dono_2 = UUID();
INSERT INTO Utilizador (idUser, nomeUser, contacto, nif, veterinario, dono, recepcionista, pais, rua, freguesia, concelho, distrito)
VALUES (@user_dono_2, 'Animal Clinic Lda', '213456788', '987654321', FALSE, TRUE, FALSE, 'Portugal', 'R. Paul Harris 21, Almeirim', 'Almeirim', 'Almeirim', 'Santarém');

SET @user_dono_3 = UUID();
INSERT INTO Utilizador (idUser, nomeUser, contacto, nif, veterinario, dono, recepcionista, pais, rua, freguesia, concelho, distrito)
VALUES (@user_dono_3, 'Clara Silva', '289123456', '456789123', FALSE, TRUE, FALSE, 'Portugal', 'R. Prof. António Caleiro 117, 2870-358 Montijo', 'Montijo', 'Montijo', 'Setúbal');

SET @user_rec_1 = UUID();
INSERT INTO Utilizador (idUser, nomeUser, contacto, nif, veterinario, dono, recepcionista, pais, rua, freguesia, concelho, distrito)
VALUES (@user_rec_1, 'Ana Sousa', '219876543', '200100200', FALSE, FALSE, TRUE, 'Portugal', 'Av. Prof. Dr. Egas Moniz 70-12, Póvoa de Santa Iria', 'Póvoa de Santa Iria', 'Vila Franca de Xira', 'Lisboa');

SET @user_rec_2 = UUID();
INSERT INTO Utilizador (idUser, nomeUser, contacto, nif, veterinario, dono, recepcionista, pais, rua, freguesia, concelho, distrito)
VALUES (@user_rec_2, 'Joao Almeida', '219876544', '200200200', FALSE, FALSE, TRUE, 'Portugal', 'R. Paul Harris 21, Almeirim', 'Almeirim', 'Almeirim', 'Santarém');

SET @user_rec_3 = UUID();
INSERT INTO Utilizador (idUser, nomeUser, contacto, nif, veterinario, dono, recepcionista, pais, rua, freguesia, concelho, distrito)
VALUES (@user_rec_3, 'Beatriz Costa', '219876545', '200300300', FALSE, FALSE, TRUE, 'Portugal', 'R. Prof. António Caleiro 117, 2870-358 Montijo', 'Montijo', 'Montijo', 'Setúbal');

-- VETERINARIO
INSERT INTO Veterinario (nLicenca, idUser) VALUES
(1001, @user_vet_1001),
(1002, @user_vet_1002),
(1003, @user_vet_1003);

-- RECEPCIONISTA
INSERT INTO Recepcionista (idUser, localidade) VALUES
(@user_rec_1, 'Póvoa de Santa Iria'),
(@user_rec_2, 'Almeirim'),
(@user_rec_3, 'Montijo');

-- VETERINARIOCLINICA
INSERT INTO VeterinarioClinica (idUser, localidade) VALUES
(@user_vet_1001, 'Póvoa de Santa Iria'),
(@user_vet_1002, 'Almeirim'),
(@user_vet_1003, 'Montijo');

-- HORARIO
SET @bloco_egas_1 = UUID();
INSERT INTO Horario (idBloco, localidade, idUser, dia, hInicio, hFim, servico)
VALUES (@bloco_egas_1, 'Póvoa de Santa Iria', @user_vet_1001, '2025-11-14', '09:00', '12:00', 'consulta médica');

SET @bloco_paul_1 = UUID();
INSERT INTO Horario (idBloco, localidade, idUser, dia, hInicio, hFim, servico)
VALUES (@bloco_paul_1, 'Almeirim', @user_vet_1002, '2025-11-17', '13:00', '17:00', 'exames complementares de diagnóstico');

SET @bloco_caleiro_1 = UUID();
INSERT INTO Horario (idBloco, localidade, idUser, dia, hInicio, hFim, servico)
VALUES (@bloco_caleiro_1, 'Montijo', @user_vet_1003, '2025-11-18', '10:00', '15:00', 'medicina preventiva');

-- TAXONOMY
INSERT INTO Especie (nomeComum, nomeCientifico, regAlimentar, vocalizacao) VALUES
('Cão', 'Canis lupus familiaris', 'carnívoro', 'latido'),
('Gato', 'Felis catus', 'carnívoro', 'miado'),
('Ave', 'Cacatua alba', 'herbívoro', 'grasnido');

INSERT INTO Raca (nome, predGeneticas, porte, comprAdulto, cuidEspecificos, peso, expecVida, padrAtivos) VALUES
('Labrador Retriever', 'problemas nas ancas', 'grande', 60, 'exercício diário', 30, 12, 'diurno'),
('Siamês', 'problemas respiratórios', 'pequeno', 25, 'limpeza ocular', 5, 14, 'crepuscular'),
('Cacatua', 'problemas nas penas', 'pequeno', 40, 'interação social', 1, 40, 'diurno');

INSERT INTO ClassTaxonomica (nomeCientifico, nome) VALUES
('Canis lupus familiaris', 'Labrador Retriever'),
('Felis catus', 'Siamês'),
('Cacatua alba', 'Cacatua');

-- DONO
INSERT INTO Dono (idUser, prefLingua, capSocial, tipoDono) VALUES
(@user_dono_1, 'pt-PT', NULL, 'pessoa'),
(@user_dono_2, 'pt-PT', 50000, 'empresa'),
(@user_dono_3, 'en-GB', NULL, 'pessoa');

-- ANIMAL
SET @animal_1 = UUID();
INSERT INTO Animal (nUtente, nome, idade, cores, caracDistint, nTransponder, estReprodutivo, sexo, peso, fotografia, dataNasc, alergias, raca, tutor, pai, mae)
VALUES (@animal_1, 'Bolt', 5, 'amarelo', 'cicatriz na pata', '123456789012345', 'castrado', 'masculino', 32, NULL, '2020-03-05', 'nenhuma', 'Labrador Retriever', @user_dono_1, NULL, NULL);

SET @animal_2 = UUID();
INSERT INTO Animal (nUtente, nome, idade, cores, caracDistint, nTransponder, estReprodutivo, sexo, peso, fotografia, dataNasc, alergias, raca, tutor, pai, mae)
VALUES (@animal_2, 'Luna', 3, 'branco e castanho', 'olho azul', '987654321098765', 'esterilizado', 'feminino', 4, NULL, '2022-07-20', 'pólen', 'Siamês', @user_dono_2, NULL, NULL);

SET @animal_3 = UUID();
INSERT INTO Animal (nUtente, nome, idade, cores, caracDistint, nTransponder, estReprodutivo, sexo, peso, fotografia, dataNasc, alergias, raca, tutor, pai, mae)
VALUES (@animal_3, 'Kiko', 10, 'branco', 'asa cortada', '555444333222111', 'fértil', 'masculino', 1, NULL, '2015-06-15', NULL, 'Cacatua', @user_dono_3, NULL, NULL);

-- MARCACAO
SET @marc_1 = UUID();
INSERT INTO Marcacao (idMarcacao, localidade, idBloco, dia, hInicio, hFim, nUtente, estado, opiniao, comentario, marcadoPor, veterinario, porPagar, servico)
VALUES (@marc_1, 'Póvoa de Santa Iria', @bloco_egas_1, '2025-11-14', '09:30', '10:00', @animal_1, 'agendado', NULL, NULL, @user_vet_1001, @user_vet_1001, 40.00, 'consulta médica');

SET @marc_2 = UUID();
INSERT INTO Marcacao (idMarcacao, localidade, idBloco, dia, hInicio, hFim, nUtente, estado, opiniao, comentario, marcadoPor, veterinario, porPagar, servico)
VALUES (@marc_2, 'Almeirim', @bloco_paul_1, '2025-11-17', '14:00', '14:30', @animal_2, 'agendado', NULL, NULL, @user_rec_2, @user_vet_1002, 75.00, 'exames complementares de diagnóstico');

SET @marc_3 = UUID();
INSERT INTO Marcacao (idMarcacao, localidade, idBloco, dia, hInicio, hFim, nUtente, estado, opiniao, comentario, marcadoPor, veterinario, porPagar, servico)
VALUES (@marc_3, 'Montijo', @bloco_caleiro_1, '2025-11-18', '11:00', '11:30', @animal_3, 'agendado', NULL, NULL, @user_vet_1003, @user_vet_1003, 25.00, 'medicina preventiva');

-- RESULTADO
SET @res1 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res1, @marc_1, 'consulta');

SET @res2 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res2, @marc_2, 'exame');

SET @res3 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res3, @marc_3, 'vacinação');

-- RESULTEXAME
INSERT INTO ResultExame (tipoExame, resultado, idResult) VALUES
('análises clínicas', 0x00, @res2);

SET @res4 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res4, @marc_1, 'exame');
INSERT INTO ResultExame (tipoExame, resultado, idResult) VALUES
('radiografias', 0x00, @res4);

SET @res5 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res5, @marc_2, 'exame');
INSERT INTO ResultExame (tipoExame, resultado, idResult) VALUES
('ecografias', 0x00, @res5);

-- RESULTDESPAR
SET @res6 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res6, @marc_1, 'desparatização');
INSERT INTO ResultDespar (interna, externa, produtos, idResult) VALUES
(TRUE, FALSE, 'Advocate', @res6);

SET @res7 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res7, @marc_2, 'desparatização');
INSERT INTO ResultDespar (interna, externa, produtos, idResult) VALUES
(FALSE, TRUE, 'Frontline', @res7);

SET @res8 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res8, @marc_3, 'desparatização');
INSERT INTO ResultDespar (interna, externa, produtos, idResult) VALUES
(TRUE, TRUE, 'Milbemax', @res8);

-- RESULTVACINACAO
INSERT INTO ResultVacinacao (tipoVacina, fabricante, idResult) VALUES
('Raiva', 'PfizerVet', @res3);

SET @res9 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res9, @marc_1, 'vacinação');
INSERT INTO ResultVacinacao (tipoVacina, fabricante, idResult) VALUES
('Leptospirose', 'Zoetis', @res9);

SET @res10 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res10, @marc_2, 'vacinação');
INSERT INTO ResultVacinacao (tipoVacina, fabricante, idResult) VALUES
('Tosse do Canil', 'Boehringer', @res10);

-- RESULTEXAMFISIC
SET @res11 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res11, @marc_1, 'exames físicos');
INSERT INTO ResultExamFisic (temperatura, peso, freqCardiaca, freqRespiratoria, idResult) VALUES
(38, 32, 90, 20, @res11);

SET @res12 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res12, @marc_2, 'exames físicos');
INSERT INTO ResultExamFisic (temperatura, peso, freqCardiaca, freqRespiratoria, idResult) VALUES
(39, 4, 110, 22, @res12);

SET @res13 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res13, @marc_3, 'exames físicos');
INSERT INTO ResultExamFisic (temperatura, peso, freqCardiaca, freqRespiratoria, idResult) VALUES
(40, 1, 120, 18, @res13);

-- RESULTCIRURGIA
SET @res14 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res14, @marc_1, 'cirurgia');
INSERT INTO ResultCirurgia (tipoCirurgia, notasPO, idResult) VALUES
('Esterilização', 'Sem complicações', @res14);

SET @res15 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res15, @marc_2, 'cirurgia');
INSERT INTO ResultCirurgia (tipoCirurgia, notasPO, idResult) VALUES
('Remoção de tumor', 'Recuperação completa', @res15);

SET @res16 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res16, @marc_3, 'cirurgia');
INSERT INTO ResultCirurgia (tipoCirurgia, notasPO, idResult) VALUES
('Limpeza dentária', 'Aplicado antibiótico', @res16);

-- RESULTTERAPIA
SET @res17 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res17, @marc_1, 'terapia');
INSERT INTO ResultTerapia (tipoTerapia, idResult) VALUES
('Tratamento de ferida', @res17);

SET @res18 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res18, @marc_2, 'terapia');
INSERT INTO ResultTerapia (tipoTerapia, idResult) VALUES
('Terapia laser', @res18);

SET @res19 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res19, @marc_3, 'terapia');
INSERT INTO ResultTerapia (tipoTerapia, idResult) VALUES
('Massagem muscular', @res19);

-- RESULTCONSULTA
INSERT INTO ResultConsulta (motivo, sintomas, diagnostico, medicacao, idResult) VALUES
('Check-up geral', 'Sem sintomas', 'Saudável', 'Vermífugo', @res1);

SET @res20 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res20, @marc_2, 'consulta');
INSERT INTO ResultConsulta (motivo, sintomas, diagnostico, medicacao, idResult) VALUES
('Tosse', 'Tosse seca', 'Tosse do canil', 'Antibiótico', @res20);

SET @res21 = UUID();
INSERT INTO Resultado (idResult, idMarcacao, tipoResultado) VALUES (@res21, @marc_3, 'consulta');
INSERT INTO ResultConsulta (motivo, sintomas, diagnostico, medicacao, idResult) VALUES
('Penas em mau estado', 'Perda de penas', 'Deficiência vitamínica', 'Suplementos', @res21);
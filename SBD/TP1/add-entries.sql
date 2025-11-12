USE VetCare;


-- TABLE: Clinicas

INSERT INTO Clinicas VALUES
('Av. Prof. Dr. Egas Moniz 70-12, Póvoa de Santa Iria', '38.858059, -9.076871'),
('R. Paul Harris 21, Almeirim', '39.201392, -8.628219'),
('R. Prof. António Caleiro 117, 2870-358 Montijo', '38.70624434781486, -8.968442098691304');


-- TABLE: TipoServicos

INSERT INTO TipoServicos VALUES
('consulta médica', 30, 30),
('exames complementares de diagnóstico', 60, 60),
('intervenções cirúrgicas', 120, 80),
('medicina preventiva', 50, 40),
('tratamentos terapêuticos', 40, 30);


-- TABLE: Veterinario

INSERT INTO Veterinario VALUES
(1001, 'Rui Ferreira'),
(1002, 'Matilde Gonçalves'),
(1003, 'Marta Pires');


-- TABLE: VeterinarioClinica

INSERT INTO VeterinarioClinica VALUES
(1001, 'Av. Prof. Dr. Egas Moniz 70-12, Póvoa de Santa Iria', '38.858059, -9.076871'),
(1002, 'R. Paul Harris 21, Almeirim', '39.201392, -8.628219'),
(1003, 'R. Prof. António Caleiro 117, 2870-358 Montijo', '38.70624434781486, -8.968442098691304');


-- TABLE: Horario

INSERT INTO Horario VALUES
(1, '2025-11-11', '09:00', '12:00', 'Av. Prof. Dr. Egas Moniz 70-12, Póvoa de Santa Iria', '38.858059, -9.076871', 1001, 'consulta médica'),
(2, '2025-11-11', '13:00', '17:00', 'R. Paul Harris 21, Almeirim', '39.201392, -8.628219', 1002, 'exames complementares de diagnóstico'),
(3, '2025-11-11', '10:00', '15:00', 'R. Prof. António Caleiro 117, 2870-358 Montijo', '38.70624434781486, -8.968442098691304', 1003, 'medicina preventiva');


-- TABLE: ClassTaxonomica

INSERT INTO ClassTaxonomica VALUES
('Labrador Retriever', 'problemas nas ancas', 'grande', 60, 'carnívoro', 'exercício diário', 30, 12, 'diurno', 'latido', 'Cão', 'Canis lupus familiaris'),
('Siamês', 'problemas respiratórios', 'pequeno', 25, 'carnívoro', 'limpeza ocular', 5, 14, 'noturno', 'miado', 'Gato', 'Felis catus'),
('Cacatua', 'problemas nas penas', 'pequeno', 40, 'herbívoro', 'interação social', 1, 40, 'diurno', 'grasnido', 'Ave', 'Cacatua alba');


-- TABLE: Dono

INSERT INTO Dono VALUES
('123456789', 'Joaquim Marques', 'pt-PT', NULL, '912345678', 'pessoa', 'Lumiar', 'Lisboa', 'Lisboa'),
('987654321', 'Animal Clinic Lda', 'pt-PT', 50000, '213456789', 'empresa', 'Bonfim', 'Porto', 'Porto'),
('456789123', 'Clara Silva', 'en-GB', NULL, '965432187', 'pessoa', 'Sé', 'Faro', 'Faro');


-- TABLE: Animal

INSERT INTO Animal VALUES
(1, 'Bolt', 5, 'amarelo', 'cicatriz na pata', '123456789012345', 'castrado', 'masculino', 32, NULL, '2020-03-05', 'nenhuma', 'Labrador Retriever', '123456789'),
(2, 'Luna', 3, 'branco e castanho', 'olho azul', '987654321098765', 'esterilizado', 'feminino', 4, NULL, '2022-07-20', 'pólen', 'Siamês', '987654321'),
(3, 'Kiko', 10, 'branco', 'asa cortada', '555444333222111', 'fértil', 'masculino', 1, NULL, '2015-06-15', NULL, 'Cacatua', '456789123');


-- TABLE: Marcacoes

INSERT INTO Marcacoes VALUES
('2025-11-11', '09:30', '10:00', 'Av. Prof. Dr. Egas Moniz 70-12, Póvoa de Santa Iria', '38.858059, -9.076871', 1, 'agendado', NULL, NULL, 1001, 40.00, 'consulta médica'),
('2025-11-11', '14:00', '15:00', 'R. Paul Harris 21, Almeirim', '39.201392, -8.628219', 2, 'agendado', NULL, NULL, 1002, 75.00, 'exames complementares de diagnóstico'),
('2025-11-11', '11:00', '11:30', 'R. Prof. António Caleiro 117, 2870-358 Montijo', '38.70624434781486, -8.968442098691304', 3, 'agendado', NULL, NULL, 1003, 25.00, 'medicina preventiva');


-- TABLE: HorarioMarcacoes

INSERT INTO HorarioMarcacoes VALUES
('2025-11-11', '09:30', '10:00', 1, 1),
('2025-11-11', '14:00', '15:00', 2, 2),
('2025-11-11', '11:00', '11:30', 3, 3);


-- TABLE: ResultadosExames

INSERT INTO ResultadosExames VALUES
('radiografias', NULL, '2025-11-11', '09:30', '10:00', 1),
('ecografias', NULL, '2025-11-11', '14:00', '15:00', 2),
('análises clínicas', NULL, '2025-11-11', '11:00', '11:30', 3);


-- TABLE: ResultadosDesparatizacao

INSERT INTO ResultadosDesparatizacao VALUES
(TRUE, FALSE, 'Advocate', '2025-11-11', '09:30', '10:00', 1),
(FALSE, TRUE, 'Frontline', '2025-11-11', '14:00', '15:00', 2),
(TRUE, TRUE, 'Milbemax', '2025-11-11', '11:00', '11:30', 3);


-- TABLE: ResultadosVacinacao

INSERT INTO ResultadosVacinacao VALUES
('Raiva', 'PfizerVet', '2025-11-11', '09:30', '10:00', 1),
('Leptospirose', 'Zoetis', '2025-11-11', '14:00', '15:00', 2),
('Tosse do Canil', 'Boehringer', '2025-11-11', '11:00', '11:30', 3);


-- TABLE: ResultadosExamesFisicos

INSERT INTO ResultadosExamesFisicos VALUES
(38, 32, 90, '2025-11-11', '09:30', '10:00', 1),
(39, 4, 110, '2025-11-11', '14:00', '15:00', 2),
(40, 1, 120, '2025-11-11', '11:00', '11:30', 3);


-- TABLE: ResultadosCirugia

INSERT INTO ResultadosCirugia VALUES
('Esterilização', 'Sem complicações', '2025-11-11', '14:00', '15:00', 2),
('Remoção de tumor', 'Recuperação completa', '2025-11-11', '09:30', '10:00', 1),
('Limpeza dentária', 'Aplicado antibiótico', '2025-11-11', '11:00', '11:30', 3);


-- TABLE: ResultadosTerapia

INSERT INTO ResultadosTerapia VALUES
('Tratamento de ferida', '2025-11-11', '09:30', '10:00', 1),
('Terapia laser', '2025-11-11', '14:00', '15:00', 2),
('Massagem muscular', '2025-11-11', '11:00', '11:30', 3);


-- TABLE: ResultadosConsulta

INSERT INTO ResultadosConsulta VALUES
('Check-up geral', 'Sem sintomas', 'Saudável', 'Vermífugo', '2025-11-11', '09:30', '10:00', 1),
('Tosse', 'Tosse seca', 'Tosse do canil', 'Antibiótico', '2025-11-11', '14:00', '15:00', 2),
('Penas em mau estado', 'Perda de penas', 'Deficiência vitamínica', 'Suplementos', '2025-11-11', '11:00', '11:30', 3);

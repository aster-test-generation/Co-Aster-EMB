insert into projects (project_id, title, start_date, end_date, status) values
(1, 'TRANSBSCS', '2020-09-28', '2020-11-04', 'COMPLETED'),
(2, 'SYNCH_BSCS_IMX', '2020-11-26', '2021-03-25', 'IN_PROGRESS'),
(3, 'TASYI9A LILVIRANDA', '2020-11-26', '2020-11-26', 'COMPLETED'),
(4, 'MACHYA_RANDONNEE', '2021-01-29', '2021-04-30', 'NOT_STARTED'),
(5, 'TATIB LEFTOUR', '2020-11-14', '2020-11-14', 'COMPLETED'),
(6, 'ChatBot', '2020-12-11', '2021-01-30', 'NOT_STARTED'),
(7, 'MyOoredoo', '2018-08-01', '2021-05-14', 'IN_PROGRESS'),
(8, 'GREENPLUME_UPGRADE', '2020-11-02', '2021-05-01', 'IN_PROGRESS'),
(9, 'COMMISION_AUTOMATION', '2020-06-01', '2021-03-02', 'IN_PROGRESS');

insert into locations (location_id, adr, postal_code, city) values
(1, 'RUE DE LA BOURSE', '2016', 'LAC2'),
(2, 'RUE DE BLA BLA', '2016', 'CHARGUIA');

insert into departments (department_id, department_name, location_id) values
(4, 'DWH', 1),
(5, 'Digital', 1),
(6, 'Billing', 1);


insert into employees (employee_id, first_name, last_name, email, phone, hiredate, job, salary, manager_id, department_id) values
(1, 'Selim', 'Horri', 'springabcxyzboot@gmail.com', '22125144', '2019-04-15', 'Billing', '5000.00', NULL, NULL),
(2, 'Badr', 'Idoudi', 'springabcxyzboot@gmail.com', '22125144', '2019-04-15', 'Digital', '5000.00', NULL, NULL),
(3, 'Imen', 'Touk', 'springabcxyzboot@gmail.com', '22125144', '2019-04-15', 'Data Warehouse', '5000.00', NULL, NULL),
(4, 'Soumaya', 'Hajjem', 'springabcxyzboot@gmail.com', '22125144', NULL, 'Chef service Billing', '6000.00', NULL, NULL),
(5, 'Nour', 'Larguech', 'springabcxyzboot@gmail.com', '22125144', NULL, 'Chef service Data Warehouse', '6000.00', NULL, NULL),
(6, 'Khdija', 'Ben Ghachame', 'springabcxyzboot@gmail.com', '22125144', '2559-01-01', 'Billing', '5000.50', NULL, NULL),
(7, 'Maryem', 'Tlemseni', 'springabcxyzboot@gmail.com', '22125144', NULL, 'Billing', '5000.00', NULL, NULL),
(8, 'Malek', 'Aissa', 'springabcxyzboot@gmail.com', '22125144', '2020-09-01', 'Billing', '5000.00', NULL, NULL),
(9, 'John', 'Doe', 'springabcxyzboot@gmail.com', '22125144', NULL, 'Chef service digital', '6000.00', NULL, NULL),
(10, 'Sana', 'Saanouni', 'springabcxyzboot@gmail.com', '22125144', NULL, 'Digital', '5000.00', NULL, NULL),
(11, 'Marwen', 'Mejri', 'springabcxyzboot@gmail.com', '22125144', NULL, 'Digital', '5000.60', NULL, NULL),
(12, 'Mayssa', 'Hassine', 'springabcxyzboot@gmail.com', '22125144', '2019-04-30', 'Data Warehouse', '5000.00', NULL, NULL),
(13, 'Mouna', 'Chaouachi', 'springabcxyzboot@gmail.com', '22125144', NULL, 'Data Warehouse', '5000.50', NULL, NULL),
(14, 'admin', 'admin', 'springabcxyzboot@gmail.com', '22125144', NULL, 'RH', '5000.00', NULL, NULL);

update employees
set manager_id = 4, department_id = 6
where employee_id = 1;

update employees
set manager_id = 9, department_id = 5
where employee_id = 2;

update employees
set manager_id = 5, department_id = 4
where employee_id = 3;

update employees
set manager_id = NULL, department_id = 6
where employee_id = 4;

update employees
set manager_id = NULL, department_id = 4
where employee_id = 5;

update employees
set manager_id = 4, department_id = 6
where employee_id = 6;

update employees
set manager_id = 4, department_id = 6
where employee_id = 7;

update employees
set manager_id = 4, department_id = 6
where employee_id = 8;

update employees
set manager_id = NULL, department_id = 5
where employee_id = 9;

update employees
set manager_id = 9, department_id = 5
where employee_id = 10;

update employees
set manager_id = 9, department_id = 5
where employee_id = 11;

update employees
set manager_id = 5, department_id = 4
where employee_id = 12;

update employees
set manager_id = 5, department_id = 4
where employee_id = 13;

update employees
set manager_id = NULL, department_id = NULL
where employee_id = 14;

insert into assignments (employee_id, project_id, commit_date, commit_emp_desc, commit_mgr_desc) values
(1, 1, '2020-11-26 10:50:09', NULL, 'init'),
(1, 1, '2020-11-26 13:14:22', 'set up some configs', 'you need to implement sec solution'),
(1, 1, '2020-12-12 16:49:42', 'implement customer by invoice', NULL),
(1, 1, '2020-12-12 17:04:14', 'suspend customers...', NULL),
(1, 1, '2020-12-12 17:04:30', 'suspe', NULL),
(1, 1, '2020-12-12 17:25:48', 'created new customer suspension', NULL),
(1, 2, '2020-11-26 10:51:59', NULL, 'init'),
(1, 2, '2020-11-26 13:14:22', 'generate xml file', 'check out marshaling correctness'),
(1, 2, '2020-12-12 11:57:18', 'files on CRMIMX', NULL),
(1, 2, '2020-12-12 12:13:51', '00000', NULL),
(1, 2, '2020-12-12 12:23:39', 'set up xml for CRMIMX1', NULL),
(1, 2, '2020-12-12 12:30:14', 'implement BSCSIMX2 business layer', NULL),
(1, 2, '2020-12-12 12:37:53', 'synchronize BSCSIMX2', NULL),
(1, 2, '2020-12-12 16:40:17', 'create a simple xml file for IMX CX', NULL),
(1, 2, '2020-12-12 16:43:48', 'synchronize xml and Java file', NULL),
(1, 2, '2020-12-17 19:29:17', 'take it easy with Spring Boot***********', NULL),
(1, 2, '2020-12-19 12:05:23', 'Generate new XML file for CRMIMX2', NULL),
(2, 5, '2020-11-26 10:52:32', NULL, 'init'),
(2, 5, '2020-12-12 15:10:28', 'samtan l ma9rouna', NULL),
(2, 5, '2020-12-12 15:10:57', 'sa9i l ma9rouna fel keskess', NULL),
(2, 5, '2020-12-12 15:12:10', '7ot salsa 3al ma9rouna', NULL),
(2, 6, '2020-12-19 11:04:29', NULL, 'init'),
(2, 6, '2020-12-19 11:16:53', 'set info', NULL),
(2, 6, '2020-12-19 11:17:12', 'set layers', NULL),
(2, 6, '2020-12-19 11:17:29', 'some front', NULL),
(2, 7, '2020-12-19 11:04:29', NULL, 'init'),
(2, 7, '2020-12-19 11:17:44', 'setup some classes', NULL),
(2, 7, '2020-12-19 11:17:58', 'implement some solutions', NULL),
(3, 4, '2020-12-13 19:55:14', NULL, 'init'),
(3, 4, '2020-12-17 11:20:47', 'ta7dhirat..........', NULL),
(3, 4, '2020-12-17 11:30:09', 'ta7dhirat ........$$**', NULL),
(3, 9, '2020-12-19 16:06:20', NULL, 'init'),
(6, 1, '2020-11-26 10:49:41', NULL, 'init'),
(6, 1, '2020-11-26 10:50:53', 'set UP DIFFERENT LAYERS', NULL),
(6, 1, '2020-12-12 15:16:55', 'import new libs', NULL),
(6, 1, '2020-12-12 15:17:31', 'set exception payload', NULL),
(10, 6, '2020-12-19 11:34:11', NULL, 'init'),
(10, 6, '2020-12-19 11:36:34', 'set some configs', NULL),
(10, 6, '2020-12-19 11:37:11', 'configure some properties', NULL),
(10, 7, '2020-12-19 11:38:00', 'configure some properties', NULL),
(10, 7, '2020-12-19 11:38:22', 'set configs', NULL),
(11, 6, '2020-12-19 11:34:29', NULL, 'init'),
(11, 6, '2020-12-19 11:40:50', 'set up a new container for deployment', NULL),
(11, 6, '2020-12-19 11:41:17', 'configure my new container', NULL),
(11, 7, '2020-12-19 11:41:57', NULL, 'init'),
(11, 7, '2020-12-19 11:42:33', 'containerize a service', NULL),
(11, 7, '2020-12-19 15:48:03', 'create a new container', NULL),
(12, 8, '2020-12-19 16:05:28', NULL, 'init'),
(12, 8, '2020-12-19 16:08:52', 'setting greenplume env locally', NULL),
(12, 8, '2020-12-19 16:09:52', 'open workspace', NULL),
(12, 9, '2020-12-19 16:05:41', NULL, 'init'),
(12, 9, '2020-12-19 16:11:00', 'design first functionality', NULL),
(12, 9, '2020-12-19 16:11:20', 'design second functionality', NULL),
(13, 8, '2020-12-19 16:05:55', NULL, 'init'),
(13, 8, '2020-12-19 16:11:59', 'set envirnment', NULL),
(13, 8, '2020-12-19 16:12:13', 'new click', NULL),
(13, 9, '2020-12-19 16:06:09', NULL, 'init'),
(13, 9, '2020-12-19 16:13:01', 'get first ids', NULL),
(13, 9, '2020-12-19 16:13:17', 'create new workspace', NULL);

insert into user_credentials (user_id, username, password, enabled, role, employee_id) values
(1, 'imentouk', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_EMP', 3),
(2, 'badridoudi', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_EMP', 2),
(3, 'selimhorri', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_EMP', 1),
(4, 'admin', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_ADMIN', 14),
(5, 'soumayahajjem', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_MGR', 4),
(6, 'nourlarguech', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_MGR', 5),
(7, 'johndoe', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_MGR', 9),
(8, 'kbenghachame', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_EMP', 6),
(9, 'malekaissa', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_EMP', 8),
(10, 'maryemtlemseni', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_EMP', 7),
(11, 'sanasaanouni', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_EMP', 10),
(12, 'marwenmejri', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_EMP', 11),
(13, 'mayssahassine', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_EMP', 12),
(14, 'mounachaouachi', '$2a$10$6pNV34gbMAEj6vuyVmQMdOfSKk.kuxOUOeucg78/cvOprSR3lsZL2', 1, 'ROLE_EMP', 13);


--
-- TOC entry 2474 (class 0 OID 33349)
-- Dependencies: 209
-- Data for Name: department; Type: TABLE DATA; Schema: company; Owner: -
--

INSERT INTO department (dname, dnumber, mgr_ssn, mgr_start_date) VALUES ('Headquarters', 1, '888665555', '1981-06-19');
INSERT INTO department (dname, dnumber, mgr_ssn, mgr_start_date) VALUES ('Research', 5, '333445555', '1988-05-22');
INSERT INTO department (dname, dnumber, mgr_ssn, mgr_start_date) VALUES ('Administration', 4, '987654321', '1995-01-01');


--
-- TOC entry 2475 (class 0 OID 33353)
-- Dependencies: 210
-- Data for Name: dependent; Type: TABLE DATA; Schema: company; Owner: -
--

INSERT INTO dependent (essn, dependent_name, sex, bdate, relationship) VALUES ('333445555', 'Alice', 'F', '1986-04-05', 'Daughter');
INSERT INTO dependent (essn, dependent_name, sex, bdate, relationship) VALUES ('333445555', 'Theodore', 'M', '1983-10-25', 'Son');
INSERT INTO dependent (essn, dependent_name, sex, bdate, relationship) VALUES ('333445555', 'Joy', 'F', '1958-05-03', 'Spouse');
INSERT INTO dependent (essn, dependent_name, sex, bdate, relationship) VALUES ('987654321', 'Abner', 'M', '1942-02-28', 'Spouse');
INSERT INTO dependent (essn, dependent_name, sex, bdate, relationship) VALUES ('123456789', 'Michael', 'M', '1988-01-04', 'Son');
INSERT INTO dependent (essn, dependent_name, sex, bdate, relationship) VALUES ('123456789', 'Alice', 'F', '1988-12-30', 'Daughter');
INSERT INTO dependent (essn, dependent_name, sex, bdate, relationship) VALUES ('123456789', 'Elizabeth', 'F', '1967-05-05', 'Spouse');


--
-- TOC entry 2476 (class 0 OID 33356)
-- Dependencies: 211
-- Data for Name: dept_locations; Type: TABLE DATA; Schema: company; Owner: -
--

INSERT INTO dept_locations (dnumber, dlocation) VALUES (1, 'Houston');
INSERT INTO dept_locations (dnumber, dlocation) VALUES (4, 'Stafford');
INSERT INTO dept_locations (dnumber, dlocation) VALUES (5, 'Bellaire');
INSERT INTO dept_locations (dnumber, dlocation) VALUES (5, 'Sugarland');
INSERT INTO dept_locations (dnumber, dlocation) VALUES (5, 'Houston');


--
-- TOC entry 2477 (class 0 OID 33359)
-- Dependencies: 212
-- Data for Name: employee; Type: TABLE DATA; Schema: company; Owner: -
--

INSERT INTO employee (fname, minit, lname, ssn, bdate, address, sex, salary, super_ssn, dno) VALUES ('James', 'E', 'Borg', '888665555', '1937-11-10', '450 Stone, Houston, TX', 'M', 55000.00, NULL, 1);
INSERT INTO employee (fname, minit, lname, ssn, bdate, address, sex, salary, super_ssn, dno) VALUES ('Ahmad', 'V', 'Jabbar', '987987987', '1969-03-29', '980 Dallas, Houston, TX', 'M', 25000.00, '987654321', 4);
INSERT INTO employee (fname, minit, lname, ssn, bdate, address, sex, salary, super_ssn, dno) VALUES ('Joyce', 'A', 'English', '453453453', '1972-07-31', '5631 Rice, Houston, TX', 'F', 25000.00, '333445555', 5);
INSERT INTO employee (fname, minit, lname, ssn, bdate, address, sex, salary, super_ssn, dno) VALUES ('Ramesh', 'K', 'Narayan', '666884444', '1962-09-15', '975 Fire Oak, Humble, TX', 'M', 38000.00, '333445555', 5);
INSERT INTO employee (fname, minit, lname, ssn, bdate, address, sex, salary, super_ssn, dno) VALUES ('Jennifer', 'S', 'Wallace', '987654321', '1941-06-20', '291 Berry, Bellaire, TX', 'F', 43000.00, '888665555', 4);
INSERT INTO employee (fname, minit, lname, ssn, bdate, address, sex, salary, super_ssn, dno) VALUES ('Alicia', 'J', 'Zelaya', '999887777', '1968-01-19', '3321 Castle, Spring, TX', 'F', 25000.00, '987654321', 4);
INSERT INTO employee (fname, minit, lname, ssn, bdate, address, sex, salary, super_ssn, dno) VALUES ('Franklin', 'T', 'Wong', '333445555', '1955-12-09', '638 Voss, Houston, TX', 'M', 40000.00, '888665555', 5);
INSERT INTO employee (fname, minit, lname, ssn, bdate, address, sex, salary, super_ssn, dno) VALUES ('John', 'B', 'Smith', '123456789', '1965-01-09', '731 Fondren, Houston, TX', 'M', 30000.00, '333445555', 5);


--
-- TOC entry 2478 (class 0 OID 33363)
-- Dependencies: 213
-- Data for Name: project; Type: TABLE DATA; Schema: company; Owner: -
--

INSERT INTO project (pname, pnumber, plocation, dnum) VALUES ('ProductX', 1, 'Bellaire', 5);
INSERT INTO project (pname, pnumber, plocation, dnum) VALUES ('ProductY', 2, 'Sugarland', 5);
INSERT INTO project (pname, pnumber, plocation, dnum) VALUES ('ProductZ', 3, 'Houston', 5);
INSERT INTO project (pname, pnumber, plocation, dnum) VALUES ('Computerization', 10, 'Stafford', 4);
INSERT INTO project (pname, pnumber, plocation, dnum) VALUES ('Reorganization', 20, 'Houston', 1);
INSERT INTO project (pname, pnumber, plocation, dnum) VALUES ('New Benefits', 30, 'Stafford', 4);


--
-- TOC entry 2479 (class 0 OID 33366)
-- Dependencies: 214
-- Data for Name: works_on; Type: TABLE DATA; Schema: company; Owner: -
--

INSERT INTO works_on (essn, pno, hours) VALUES ('123456789', 1, 32.5);
INSERT INTO works_on (essn, pno, hours) VALUES ('123456789', 2, 7.5);
INSERT INTO works_on (essn, pno, hours) VALUES ('666884444', 3, 40.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('453453453', 1, 20.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('333445555', 2, 10.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('333445555', 3, 10.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('333445555', 10, 10.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('333445555', 20, 10.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('453453453', 2, 20.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('999887777', 30, 30.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('999887777', 10, 10.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('987987987', 10, 35.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('987987987', 30, 5.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('987654321', 30, 20.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('987654321', 20, 15.0);
INSERT INTO works_on (essn, pno, hours) VALUES ('888665555', 20, 0.0);



--
-- TOC entry 2340 (class 2606 OID 33484)
-- Name: department_dname_key; Type: CONSTRAINT; Schema: company; Owner: -; Tablespace: 
--

ALTER TABLE ONLY department
    ADD CONSTRAINT department_dname_key UNIQUE (dname);


--
-- TOC entry 2342 (class 2606 OID 33486)
-- Name: department_pkey; Type: CONSTRAINT; Schema: company; Owner: -; Tablespace: 
--

ALTER TABLE ONLY department
    ADD CONSTRAINT department_pkey PRIMARY KEY (dnumber);


--
-- TOC entry 2344 (class 2606 OID 33488)
-- Name: dependent_pkey; Type: CONSTRAINT; Schema: company; Owner: -; Tablespace: 
--

ALTER TABLE ONLY dependent
    ADD CONSTRAINT dependent_pkey PRIMARY KEY (essn, dependent_name);


--
-- TOC entry 2346 (class 2606 OID 33490)
-- Name: dept_locations_pkey; Type: CONSTRAINT; Schema: company; Owner: -; Tablespace: 
--

ALTER TABLE ONLY dept_locations
    ADD CONSTRAINT dept_locations_pkey PRIMARY KEY (dnumber, dlocation);


--
-- TOC entry 2348 (class 2606 OID 33492)
-- Name: pkey_ssn; Type: CONSTRAINT; Schema: company; Owner: -; Tablespace: 
--

ALTER TABLE ONLY employee
    ADD CONSTRAINT pkey_ssn PRIMARY KEY (ssn);


--
-- TOC entry 2350 (class 2606 OID 33494)
-- Name: project_pkey; Type: CONSTRAINT; Schema: company; Owner: -; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_pkey PRIMARY KEY (pnumber);


--
-- TOC entry 2352 (class 2606 OID 33496)
-- Name: project_pname_key; Type: CONSTRAINT; Schema: company; Owner: -; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_pname_key UNIQUE (pname);


--
-- TOC entry 2354 (class 2606 OID 33498)
-- Name: works_on_pkey; Type: CONSTRAINT; Schema: company; Owner: -; Tablespace: 
--

ALTER TABLE ONLY works_on
    ADD CONSTRAINT works_on_pkey PRIMARY KEY (pno, essn);


--
-- TOC entry 2355 (class 2606 OID 33569)
-- Name: department_mgr_ssn_fkey; Type: FK CONSTRAINT; Schema: company; Owner: -
--

ALTER TABLE ONLY department
    ADD CONSTRAINT department_mgr_ssn_fkey FOREIGN KEY (mgr_ssn) REFERENCES employee(ssn) ON UPDATE CASCADE ON DELETE SET DEFAULT;


--
-- TOC entry 2356 (class 2606 OID 33574)
-- Name: dependent_essn_fkey; Type: FK CONSTRAINT; Schema: company; Owner: -
--

ALTER TABLE ONLY dependent
    ADD CONSTRAINT dependent_essn_fkey FOREIGN KEY (essn) REFERENCES employee(ssn);


--
-- TOC entry 2357 (class 2606 OID 33579)
-- Name: dept_locations_dnumber_fkey; Type: FK CONSTRAINT; Schema: company; Owner: -
--

ALTER TABLE ONLY dept_locations
    ADD CONSTRAINT dept_locations_dnumber_fkey FOREIGN KEY (dnumber) REFERENCES department(dnumber) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2358 (class 2606 OID 33584)
-- Name: dno_pkey; Type: FK CONSTRAINT; Schema: company; Owner: -
--

ALTER TABLE ONLY employee
    ADD CONSTRAINT dno_pkey FOREIGN KEY (dno) REFERENCES department(dnumber) ON UPDATE CASCADE ON DELETE SET DEFAULT;


--
-- TOC entry 2360 (class 2606 OID 33589)
-- Name: project_dnum_fkey; Type: FK CONSTRAINT; Schema: company; Owner: -
--

ALTER TABLE ONLY project
    ADD CONSTRAINT project_dnum_fkey FOREIGN KEY (dnum) REFERENCES department(dnumber) ON UPDATE CASCADE ON DELETE SET DEFAULT;


--
-- TOC entry 2359 (class 2606 OID 33594)
-- Name: super_ssn_fkey_to_ssn; Type: FK CONSTRAINT; Schema: company; Owner: -
--

ALTER TABLE ONLY employee
    ADD CONSTRAINT super_ssn_fkey_to_ssn FOREIGN KEY (super_ssn) REFERENCES employee(ssn) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- TOC entry 2361 (class 2606 OID 33599)
-- Name: works_on_essn_fkey; Type: FK CONSTRAINT; Schema: company; Owner: -
--

ALTER TABLE ONLY works_on
    ADD CONSTRAINT works_on_essn_fkey FOREIGN KEY (essn) REFERENCES employee(ssn) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2362 (class 2606 OID 33604)
-- Name: works_on_pno_fkey; Type: FK CONSTRAINT; Schema: company; Owner: -
--

ALTER TABLE ONLY works_on
    ADD CONSTRAINT works_on_pno_fkey FOREIGN KEY (pno) REFERENCES project(pnumber) ON UPDATE CASCADE ON DELETE CASCADE;

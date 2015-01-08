--
-- PostgreSQL database dump
--

-- Dumped from database version 9.3.5
-- Dumped by pg_dump version 9.3.5
-- Started on 2014-12-19 10:57:28 EST

--
-- TOC entry 8 (class 2615 OID 33150)
-- Name: company; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA company;
SET SCHEMA company;

--
-- TOC entry 209 (class 1259 OID 33349)
-- Name: department; Type: TABLE; Schema: company; Owner: -; Tablespace: 
--

CREATE TABLE department (
    dname character varying(15) NOT NULL,
    dnumber integer NOT NULL,
 --   mgr_ssn character(9) DEFAULT '888665555'::bpchar NOT NULL,
    mgr_ssn character(9) DEFAULT '888665555' NOT NULL,
    mgr_start_date date
);


--
-- TOC entry 210 (class 1259 OID 33353)
-- Name: dependent; Type: TABLE; Schema: company; Owner: -; Tablespace: 
--

CREATE TABLE dependent (
    essn character(9) NOT NULL,
    dependent_name character varying(15) NOT NULL,
    sex character(1),
    bdate date,
    relationship character varying(8)
);


--
-- TOC entry 211 (class 1259 OID 33356)
-- Name: dept_locations; Type: TABLE; Schema: company; Owner: -; Tablespace: 
--

CREATE TABLE dept_locations (
    dnumber integer NOT NULL,
    dlocation character varying(15) NOT NULL
);


--
-- TOC entry 212 (class 1259 OID 33359)
-- Name: employee; Type: TABLE; Schema: company; Owner: -; Tablespace: 
--

CREATE TABLE employee (
    fname character varying(15) NOT NULL,
    minit character(1),
    lname character varying(15) NOT NULL,
    ssn character(9) NOT NULL,
    bdate date,
    address character varying(30),
    sex character(1),
    salary numeric(10,2),
    super_ssn character(9),
    dno integer DEFAULT 1 NOT NULL
);


--
-- TOC entry 213 (class 1259 OID 33363)
-- Name: project; Type: TABLE; Schema: company; Owner: -; Tablespace: 
--

CREATE TABLE project (
    pname character varying(15) NOT NULL,
    pnumber integer NOT NULL,
    plocation character varying(15),
    dnum integer NOT NULL
);


--
-- TOC entry 214 (class 1259 OID 33366)
-- Name: works_on; Type: TABLE; Schema: company; Owner: -; Tablespace: 
--

CREATE TABLE works_on (
    essn character(9) NOT NULL,
    pno integer NOT NULL,
    hours numeric(3,1) NOT NULL
);




-- Completed on 2014-12-19 10:57:28 EST

--
-- PostgreSQL database dump complete
--


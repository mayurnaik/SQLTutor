--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: company; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA company;


ALTER SCHEMA company OWNER TO postgres;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: linked_admin_codes; Type: TABLE; Schema: public; Owner: DB_Manager; Tablespace: 
--

CREATE TABLE linked_admin_codes (
    email text NOT NULL,
    linked_admin_code text DEFAULT 'xgFabbA'::text NOT NULL
);


ALTER TABLE public.linked_admin_codes OWNER TO "DB_Manager";

--
-- Name: log; Type: TABLE; Schema: public; Owner: DB_Manager; Tablespace: 
--

CREATE TABLE log (
    "timestamp" timestamp with time zone DEFAULT now() NOT NULL,
    query text NOT NULL,
    schema character varying(100),
    email character varying(255),
    question character varying(255),
    correct_answer character varying(255),
    ip_address character varying(255),
    session_id character varying(255),
    parsed boolean,
    correct boolean
);


ALTER TABLE public.log OWNER TO "DB_Manager";

--
-- Name: password_change_requests; Type: TABLE; Schema: public; Owner: DB_Manager; Tablespace: 
--

CREATE TABLE password_change_requests (
    email text NOT NULL,
    "time" timestamp with time zone DEFAULT now() NOT NULL,
    salt text NOT NULL,
    id text NOT NULL
);


ALTER TABLE public.password_change_requests OWNER TO "DB_Manager";

--
-- Name: query_translation; Type: TABLE; Schema: public; Owner: DB_Manager; Tablespace: 
--

CREATE TABLE query_translation (
    id integer NOT NULL,
    query_id integer NOT NULL,
    nlp text NOT NULL
);


ALTER TABLE public.query_translation OWNER TO "DB_Manager";

--
-- Name: query_translation_id_seq; Type: SEQUENCE; Schema: public; Owner: DB_Manager
--

CREATE SEQUENCE query_translation_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.query_translation_id_seq OWNER TO "DB_Manager";

--
-- Name: query_translation_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: DB_Manager
--

ALTER SEQUENCE query_translation_id_seq OWNED BY query_translation.id;


--
-- Name: schema_options; Type: TABLE; Schema: public; Owner: DB_Manager; Tablespace: 
--

CREATE TABLE schema_options (
    visible_to_users boolean DEFAULT false NOT NULL,
    in_order_questions boolean DEFAULT true NOT NULL,
    schema text NOT NULL,
    owner text DEFAULT '[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]'::text NOT NULL
);


ALTER TABLE public.schema_options OWNER TO "DB_Manager";

--
-- Name: schema_questions; Type: TABLE; Schema: public; Owner: DB_Manager; Tablespace: 
--

CREATE TABLE schema_questions (
    question text NOT NULL,
    answer text NOT NULL,
    "order" bigint NOT NULL,
    id bigint NOT NULL,
    schema text NOT NULL
);


ALTER TABLE public.schema_questions OWNER TO "DB_Manager";

--
-- Name: schema_questions_id_seq; Type: SEQUENCE; Schema: public; Owner: DB_Manager
--

CREATE SEQUENCE schema_questions_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.schema_questions_id_seq OWNER TO "DB_Manager";

--
-- Name: schema_questions_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: DB_Manager
--

ALTER SEQUENCE schema_questions_id_seq OWNED BY schema_questions.id;


--
-- Name: translation_rating; Type: TABLE; Schema: public; Owner: DB_Manager; Tablespace: 
--

CREATE TABLE translation_rating (
    id integer NOT NULL,
    translation_id integer NOT NULL,
    username character varying(100) NOT NULL,
    rating smallint NOT NULL
);


ALTER TABLE public.translation_rating OWNER TO "DB_Manager";

--
-- Name: translation_rating_id_seq; Type: SEQUENCE; Schema: public; Owner: DB_Manager
--

CREATE SEQUENCE translation_rating_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.translation_rating_id_seq OWNER TO "DB_Manager";

--
-- Name: translation_rating_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: DB_Manager
--

ALTER SEQUENCE translation_rating_id_seq OWNED BY translation_rating.id;


--
-- Name: user; Type: TABLE; Schema: public; Owner: DB_Manager; Tablespace: 
--

CREATE TABLE "user" (
    creation_timestamp timestamp with time zone DEFAULT now() NOT NULL,
    email text NOT NULL,
    admin boolean DEFAULT false NOT NULL,
    developer boolean DEFAULT false NOT NULL,
    admin_code text,
    salt text DEFAULT 'password'::text NOT NULL,
    password text DEFAULT 'password'::text NOT NULL
);


ALTER TABLE public."user" OWNER TO "DB_Manager";

--
-- Name: id; Type: DEFAULT; Schema: public; Owner: DB_Manager
--

ALTER TABLE ONLY query_translation ALTER COLUMN id SET DEFAULT nextval('query_translation_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: DB_Manager
--

ALTER TABLE ONLY schema_questions ALTER COLUMN id SET DEFAULT nextval('schema_questions_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: DB_Manager
--

ALTER TABLE ONLY translation_rating ALTER COLUMN id SET DEFAULT nextval('translation_rating_id_seq'::regclass);


--
-- Data for Name: linked_admin_codes; Type: TABLE DATA; Schema: public; Owner: DB_Manager
--

COPY linked_admin_codes (email, linked_admin_code) FROM stdin;
[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]	xgFabbA
[-93, -57, -35, -25, -24, -100, 21, -49, 41, 90, 70, -42, -116, -105, 79, -41, -7, -64, 78, 54]	xgFabbA
[-112, 4, -124, -111, -127, 119, -67, -87, -109, -53, 104, 115, 102, -105, 62, 89, 122, 39, 29, 8]	xgFabbA
[34, 53, -6, -81, 51, 6, 93, 78, -31, 112, -27, 56, -72, 34, -122, 82, -31, -91, 34, 19]	xgFabbA
[-93, -57, -35, -25, -24, -100, 21, -49, 41, 90, 70, -42, -116, -105, 79, -41, -7, -64, 78, 54]	zArWo3U
[-112, 4, -124, -111, -127, 119, -67, -87, -109, -53, 104, 115, 102, -105, 62, 89, 122, 39, 29, 8]	dl-0TZg
[34, 53, -6, -81, 51, 6, 93, 78, -31, 112, -27, 56, -72, 34, -122, 82, -31, -91, 34, 19]	HHQbe78
[-127, 1, 4, 77, -7, 111, 110, -106, 83, -88, 64, -28, 66, 12, 64, 68, 12, -62, 57, -112]	xgFabbA
\.


--
-- Data for Name: log; Type: TABLE DATA; Schema: public; Owner: DB_Manager
--

COPY log ("timestamp", query, schema, email, question, correct_answer, ip_address, session_id, parsed, correct) FROM stdin;
\.


--
-- Data for Name: password_change_requests; Type: TABLE DATA; Schema: public; Owner: DB_Manager
--

COPY password_change_requests (email, "time", salt, id) FROM stdin;
[-112, 4, -124, -111, -127, 119, -67, -87, -109, -53, 104, 115, 102, -105, 62, 89, 122, 39, 29, 8]	2014-10-03 17:29:24.341+00	[38, 122, 92, 127, 88, -26, 13, 108]	[-116, -88, -6, -95, -32, -76, 2, -113, -115, 2, -56, 57, 22, -117, 103, -11, 63, -35, 36, -69]
\.


--
-- Data for Name: query_translation; Type: TABLE DATA; Schema: public; Owner: DB_Manager
--

COPY query_translation (id, query_id, nlp) FROM stdin;
\.


--
-- Name: query_translation_id_seq; Type: SEQUENCE SET; Schema: public; Owner: DB_Manager
--

SELECT pg_catalog.setval('query_translation_id_seq', 1, false);


--
-- Data for Name: schema_options; Type: TABLE DATA; Schema: public; Owner: DB_Manager
--

COPY schema_options (visible_to_users, in_order_questions, schema, owner) FROM stdin;
f	t	booktown	[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]
f	t	jobs	[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]
f	t	sales	[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]
f	t	business_trip	[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]
f	t	world	[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]
t	t	company	[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]
\.


--
-- Data for Name: schema_questions; Type: TABLE DATA; Schema: public; Owner: DB_Manager
--

COPY schema_questions (question, answer, "order", id, schema) FROM stdin;
Retrieve all authors.	SELECT * FROM authors;	18	28	booktown
Retrieve all employees.	SELECT * FROM employee	1	1	company
Retrieve the salary of the employee(s) named 'Ahmad'.	SELECT salary FROM employee WHERE fname = 'Ahmad';	2	29	company
Select all employee SSNs in the database.	SELECT ssn FROM employee	3	3	company
Retrieve all distinct salary values.	SELECT DISTINCT salary FROM employee	4	4	company
For every project located in 'Stafford', list the project number, the controlling department number, and the department manager's last name, address, and birthdate.	SELECT p.pnumber, d.dnumber, e.lname, e.address, e.bdate FROM project p, department d, employee e WHERE p.dnum = d.dnumber AND d.mgr_ssn = e.ssn AND p.plocation = 'Stafford';	20	52	company
Retrieve the full name of each employee.	SELECT fname, minit, lname FROM employee;	5	30	company
Retrieve the birth date and address of the employee(s) whose name is 'John B. Smith'.	SELECT bdate, address FROM employee WHERE fname='John' AND minit='B' AND lname='Smith';	6	42	company
Retrieve the first and last name as well as the address of all employees who work for the 'Research' department.	SELECT fname, lname, address FROM employee, department WHERE dname='Research' AND dno=dnumber;	7	44	company
Retrieve the full name of all employees who do not have supervisors.	SELECT fname, minit, lname FROM employee WHERE super_ssn IS NULL;	8	47	company
Find the full names of all employee who are directly supervised by 'Franklin Wong'.	SELECT e.fname, e.minit, e.lname FROM employee e, employee s WHERE s.fname= 'Franklin' AND s.lname = 'Wong' AND e.super_ssn = s.ssn;	9	41	company
For each employee, retrieve the employee's first and last name and the first and last name of his or her immediate supervisor.	SELECT e.fname, e.lname, s.fname, s.lname FROM employee AS E, employee AS S WHERE e.super_ssn = S.ssn;	10	45	company
Retrieve the employee(s) whose address is in Houston, Texas.	SELECT * FROM employee WHERE address LIKE '%Houston, TX%';	11	46	company
Retrieve all employees in department 5 whose salary is between $30,000 and $40,000.	SELECT * FROM employee WHERE (salary BETWEEN 30000 AND 40000) AND dno = 5;	12	35	company
Retrieve the last name of each employee and his or her supervisor.	SELECT e.lname AS employee_name, s.lname AS supervisor_name FROM employee e, employee s WHERE e.super_ssn = s.ssn;	13	48	company
Retrieve all employees whose salaries are greater than the salary of the manager of their department.	SELECT e.fname, e.lname FROM employee E, employee M, department D WHERE E.salary > M.salary AND e.dno = D.dnumber AND d.mgr_ssn = M.ssn;	14	39	company
Retrieve the first and last name of all employee(s) who work on a project.	SELECT DISTINCT fname, lname FROM employee, project, works_on WHERE ssn= essn AND pnumber = pno;	16	50	company
\.


--
-- Name: schema_questions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: DB_Manager
--

SELECT pg_catalog.setval('schema_questions_id_seq', 52, true);


--
-- Data for Name: translation_rating; Type: TABLE DATA; Schema: public; Owner: DB_Manager
--

COPY translation_rating (id, translation_id, username, rating) FROM stdin;
\.


--
-- Name: translation_rating_id_seq; Type: SEQUENCE SET; Schema: public; Owner: DB_Manager
--

SELECT pg_catalog.setval('translation_rating_id_seq', 1, false);


--
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: DB_Manager
--

COPY "user" (creation_timestamp, email, admin, developer, admin_code, salt, password) FROM stdin;
2014-06-20 10:21:33.484378+00	[115, 104, 16, -32, -71, -83, -106, 116, -68, -27, -44, 69, -55, -88, 51, -25, 98, -6, -33, 66]	t	t	xgFabbA	[30, 125, 41, 91, -110, -63, 72, 34]	[-72, 67, -87, 121, -108, -101, 17, -79, 89, 68, -52, -70, 122, -111, 77, -76, 60, -9, -5, 69]
2014-06-21 01:22:55.326233+00	[-93, -57, -35, -25, -24, -100, 21, -49, 41, 90, 70, -42, -116, -105, 79, -41, -7, -64, 78, 54]	t	t	zArWo3U	[-21, 102, 93, 60, 52, 123, 88, 109]	[-105, -75, -13, -59, 57, -68, 13, 88, 44, 20, -85, -79, -68, -57, 58, -15, -83, 52, -5, -37]
2014-08-26 17:46:33.793157+00	[34, 53, -6, -81, 51, 6, 93, 78, -31, 112, -27, 56, -72, 34, -122, 82, -31, -91, 34, 19]	t	f	HHQbe78	[71, -51, 117, -74, 73, -4, 81, 13]	[3, 97, -72, -35, -114, 7, 93, 49, 103, 23, -23, -124, 8, -56, 127, -62, 60, 89, -73, -117]
2014-10-03 17:23:54.485145+00	[-127, 1, 4, 77, -7, 111, 110, -106, 83, -88, 64, -28, 66, 12, 64, 68, 12, -62, 57, -112]	f	f	\N	[-70, -73, 40, -50, 107, -29, -108, 11]	[-120, 122, 87, 4, 69, 41, 119, 28, 56, -111, -86, 115, 61, 117, -42, 53, -103, 6, -8, -58]
2014-06-20 10:30:35.890718+00	[-112, 4, -124, -111, -127, 119, -67, -87, -109, -53, 104, 115, 102, -105, 62, 89, 122, 39, 29, 8]	t	t	dl-0TZg	[49, 108, 38, -43, -97, 103, 123, 94]	[-50, 108, 2, 37, 111, 117, 37, -81, -127, 14, 94, -25, -31, -10, -50, -90, -73, -73, 9, 21]
\.


--
-- Name: admin_code_key; Type: CONSTRAINT; Schema: public; Owner: DB_Manager; Tablespace: 
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT admin_code_key UNIQUE (admin_code);


--
-- Name: linked_admin_codes_pkey; Type: CONSTRAINT; Schema: public; Owner: DB_Manager; Tablespace: 
--

ALTER TABLE ONLY linked_admin_codes
    ADD CONSTRAINT linked_admin_codes_pkey PRIMARY KEY (linked_admin_code, email);


--
-- Name: query_translation_pkey; Type: CONSTRAINT; Schema: public; Owner: DB_Manager; Tablespace: 
--

ALTER TABLE ONLY query_translation
    ADD CONSTRAINT query_translation_pkey PRIMARY KEY (id);


--
-- Name: schema_options_pkey; Type: CONSTRAINT; Schema: public; Owner: DB_Manager; Tablespace: 
--

ALTER TABLE ONLY schema_options
    ADD CONSTRAINT schema_options_pkey PRIMARY KEY (schema);


--
-- Name: schema_questions_pkey; Type: CONSTRAINT; Schema: public; Owner: DB_Manager; Tablespace: 
--

ALTER TABLE ONLY schema_questions
    ADD CONSTRAINT schema_questions_pkey PRIMARY KEY (id);


--
-- Name: translation_rating_pkey; Type: CONSTRAINT; Schema: public; Owner: DB_Manager; Tablespace: 
--

ALTER TABLE ONLY translation_rating
    ADD CONSTRAINT translation_rating_pkey PRIMARY KEY (id);


--
-- Name: user_email_pkey; Type: CONSTRAINT; Schema: public; Owner: DB_Manager; Tablespace: 
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT user_email_pkey PRIMARY KEY (email);


--
-- Name: fki_translation_rating_translation_id_fkey; Type: INDEX; Schema: public; Owner: DB_Manager; Tablespace: 
--

CREATE INDEX fki_translation_rating_translation_id_fkey ON translation_rating USING btree (translation_id);


--
-- Name: linked_admin_codes_email_fkey; Type: FK CONSTRAINT; Schema: public; Owner: DB_Manager
--

ALTER TABLE ONLY linked_admin_codes
    ADD CONSTRAINT linked_admin_codes_email_fkey FOREIGN KEY (email) REFERENCES "user"(email) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: translation_rating_translation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: DB_Manager
--

ALTER TABLE ONLY translation_rating
    ADD CONSTRAINT translation_rating_translation_id_fkey FOREIGN KEY (translation_id) REFERENCES query_translation(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: log; Type: ACL; Schema: public; Owner: DB_Manager
--

REVOKE ALL ON TABLE log FROM PUBLIC;
REVOKE ALL ON TABLE log FROM "DB_Manager";
GRANT ALL ON TABLE log TO "DB_Manager";


--
-- Name: schema_options; Type: ACL; Schema: public; Owner: DB_Manager
--

REVOKE ALL ON TABLE schema_options FROM PUBLIC;
REVOKE ALL ON TABLE schema_options FROM "DB_Manager";
GRANT ALL ON TABLE schema_options TO "DB_Manager";


--
-- Name: schema_questions; Type: ACL; Schema: public; Owner: DB_Manager
--

REVOKE ALL ON TABLE schema_questions FROM PUBLIC;
REVOKE ALL ON TABLE schema_questions FROM "DB_Manager";
GRANT ALL ON TABLE schema_questions TO "DB_Manager";


--
-- PostgreSQL database dump complete
--


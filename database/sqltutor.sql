--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

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
-- Name: log; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE log (
    "timestamp" timestamp with time zone DEFAULT now() NOT NULL,
    query text NOT NULL,
    schema character varying(100),
    "user" character varying(255)
);


ALTER TABLE public.log OWNER TO postgres;

--
-- Name: query; Type: TABLE; Schema: public; Owner: DB_Manager; Tablespace: 
--

CREATE TABLE query (
    id integer NOT NULL,
    username character varying(100) NOT NULL,
    schema character varying(100) NOT NULL,
    created timestamp with time zone DEFAULT now() NOT NULL,
    sql text NOT NULL,
    user_description text,
    source text
);


ALTER TABLE public.query OWNER TO "DB_Manager";

--
-- Name: query_id_seq; Type: SEQUENCE; Schema: public; Owner: DB_Manager
--

CREATE SEQUENCE query_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.query_id_seq OWNER TO "DB_Manager";

--
-- Name: query_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: DB_Manager
--

ALTER SEQUENCE query_id_seq OWNED BY query.id;


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
    username text NOT NULL,
    salt bytea NOT NULL,
    creation_timestamp timestamp with time zone DEFAULT now() NOT NULL,
    password bytea NOT NULL
);


ALTER TABLE public."user" OWNER TO "DB_Manager";

--
-- Name: id; Type: DEFAULT; Schema: public; Owner: DB_Manager
--

ALTER TABLE ONLY query ALTER COLUMN id SET DEFAULT nextval('query_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: DB_Manager
--

ALTER TABLE ONLY query_translation ALTER COLUMN id SET DEFAULT nextval('query_translation_id_seq'::regclass);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: DB_Manager
--

ALTER TABLE ONLY translation_rating ALTER COLUMN id SET DEFAULT nextval('translation_rating_id_seq'::regclass);


--
-- Name: id_pkey; Type: CONSTRAINT; Schema: public; Owner: DB_Manager; Tablespace: 
--

ALTER TABLE ONLY query
    ADD CONSTRAINT id_pkey PRIMARY KEY (id);


--
-- Name: query_translation_pkey; Type: CONSTRAINT; Schema: public; Owner: DB_Manager; Tablespace: 
--

ALTER TABLE ONLY query_translation
    ADD CONSTRAINT query_translation_pkey PRIMARY KEY (id);


--
-- Name: translation_rating_pkey; Type: CONSTRAINT; Schema: public; Owner: DB_Manager; Tablespace: 
--

ALTER TABLE ONLY translation_rating
    ADD CONSTRAINT translation_rating_pkey PRIMARY KEY (id);


--
-- Name: user_username_pkey; Type: CONSTRAINT; Schema: public; Owner: DB_Manager; Tablespace: 
--

ALTER TABLE ONLY "user"
    ADD CONSTRAINT user_username_pkey PRIMARY KEY (username);


--
-- Name: fki_translation_rating_translation_id_fkey; Type: INDEX; Schema: public; Owner: DB_Manager; Tablespace: 
--

CREATE INDEX fki_translation_rating_translation_id_fkey ON translation_rating USING btree (translation_id);


--
-- Name: query_translation_query_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: DB_Manager
--

ALTER TABLE ONLY query_translation
    ADD CONSTRAINT query_translation_query_id_fkey FOREIGN KEY (query_id) REFERENCES query(id);


--
-- Name: translation_rating_translation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: DB_Manager
--

ALTER TABLE ONLY translation_rating
    ADD CONSTRAINT translation_rating_translation_id_fkey FOREIGN KEY (translation_id) REFERENCES query_translation(id);


--
-- Name: user_fkey; Type: FK CONSTRAINT; Schema: public; Owner: DB_Manager
--

ALTER TABLE ONLY query
    ADD CONSTRAINT user_fkey FOREIGN KEY (username) REFERENCES "user"(username);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- Name: log; Type: ACL; Schema: public; Owner: postgres
--

REVOKE ALL ON TABLE log FROM PUBLIC;
REVOKE ALL ON TABLE log FROM postgres;
GRANT ALL ON TABLE log TO postgres;
GRANT ALL ON TABLE log TO PUBLIC;


--
-- PostgreSQL database dump complete
--


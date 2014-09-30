#!/usr/bin/env python
from __future__ import with_statement, print_function
import subprocess
import sys
import re
import os
from collections import OrderedDict
import argparse
import sqlite3
import operator
import itertools

ALL_RULES = [
    'TransformationRule',
    'InRelationshipLabelRule',
    'DescribingAttributeLabelRule',
    'JoinLabelRule',
    'MergeCompositeAttributeRule',
    'ValueTypeInferenceRule',
    'TableEntityRefNeedsIdRule',
    'AllAttributesLiteralLabelRule',
    'AttributeLiteralLabelRule',
    'BetweenLiteralsRule',
    'BinaryComparisonRule',
    'NumberLiteralRule',
    'SelectLabelRule',
    'WhereLiteralRule',
    'ColumnReferenceLiteralRule',
    'DefaultIsNullRule',
    'InRelationshipLoweringRule',
    'TableEntityRefLiteralRule',
    'SimplifyConjunctionsRule',
    'FixVerbTenseRule',
    'InvalidDeterminerRule',
    'ConjunctScopeComputationRule',
    'DefaultColumnLabelRule',
    'DefaultTableLabelRule',
    'AnaphoricPronounRule',
    'DeterminerRedundancyRule',
    'RangeToBetweenRule',
    'SimplifyRepeatedAttributesRule',
    'SingleReferenceAnaphoraRule'
]

IGNORE_RULES = [
    'DefaultColumnLabelRule',
    'DefaultTableLabelRule',
    'ConjunctScopeComputationRule',
    'InvalidDeterminerRule',
    'FixVerbTenseRule',
    'SimplifyConjunctionsRule'
]

SOURCES = OrderedDict()
SOURCES['textbook'] = 'CompanyExperiment1Test'
SOURCES['generation-company'] = 'Experiment3CompanyTest'
SOURCES['generation-businesstrip'] = 'Experiment3BusinessTripTest'
SOURCES['guidance'] = 'Experiment4CompanyTest'


class QueryStats(object):
    def __init__(self, query):
        self.query = query
        self.rules = OrderedDict()

    def applied(self, rule):
        self.rules[rule] = None


class RuleStats(object):
    def __init__(self):
        self.rules = OrderedDict()
        for rule in ALL_RULES:
            self.rules[rule] = 0

    def applied(self, rule, times=1):
        try:
            self.rules[rule] += times
        except KeyError:
            self.rules[rule] = times

    def print_stats(self):
        for rule, count in self.rules.iteritems():
            print('{}: {}'.format(rule, count))


def run_init(args):
    db = args.db
    create_schema(db)


def create_schema(outfile):
    with sqlite3.connect(outfile) as conn:
        conn.execute('''CREATE TABLE rule_application (
            source STRING NOT NULL,
            query STRING NOT NULL,
            query_number INTEGER NOT NULL,
            rule STRING NOT NULL,
            PRIMARY KEY(source, query, rule))
        ''')


def run_update(args):
    db = args.db
    source = args.sourcename
    with sqlite3.connect(db) as conn:
        stats = read_stats(args.logfile)
        conn.execute('DELETE FROM rule_application WHERE source=?', (source,))
        qnum = 0
        for query, qstats in stats.iteritems():
            qnum += 1
            for rule in qstats.rules.iterkeys():
                conn.execute('INSERT INTO rule_application (source, query, query_number, rule) VALUES (?, ?, ?, ?)',
                             (source, query, qnum, rule))


def read_stats(logfile):
    stats = OrderedDict()
    qstats = None
    with open(logfile) as fp:
        for line in fp:
            query = get_query(line)
            if query:
                qstats = stats[query] = QueryStats(query)
                continue
            rule = get_applied(line)
            if rule:
                qstats.applied(rule)
    return stats


def get_query_rules(conn, source, query):
    q = 'SELECT rule FROM rule_application WHERE source=? AND query=?'
    return conn.execute(q, (source, query))


def run_report(args):
    db = args.db
    with sqlite3.connect(db) as conn:
        if not args.sources:
            run_global_report(conn, unique_queries=args.unique_queries)
        else:
            for source in args.sources:
                run_source_report(conn, source, show_rules=args.show_rules)
                print()


def get_source_counts(conn, source=None, query=None):
    source_query = (source, query)
    q = 'SELECT COUNT(*) FROM rule_application WHERE source=? AND query=?'
    total = conn.execute(q, source_query).fetchone()[0]
    q += ' AND rule NOT IN (\'' + '\',\''.join(IGNORE_RULES) + '\')'
    paper = conn.execute(q, source_query).fetchone()[0]
    return total, paper


def run_source_report(conn, source, show_rules=False):
    print('Source: {}'.format(source))
    q = 'SELECT DISTINCT query, query_number FROM rule_application WHERE source=? ORDER BY query_number ASC'
    cur = conn.execute(q, (source,))
    for query, num in cur:
        print('Query {}: {}'.format(num, query))
        if show_rules:
            map(print, itertools.imap(operator.itemgetter(0), get_query_rules(conn, source, query)))
        total, paper_total = get_source_counts(conn, source, query)
        print('Rules applied: {} ({})'.format(paper_total, total))
    print()
    run_global_report(conn, source=source)


def run_global_report(conn, unique_queries=False, source=None):
    """Collects and reports rule application counts, either globally or for a particular source.

    :param conn: sqlite3.Connection the database connections
    :param unique_queries: bool if only unique queries should be considered for global counts
    :param source: basestring|None if counts should come from a single source
    """
    stats = RuleStats()
    if unique_queries:
        queries = set()
        for query, rule in conn.execute('SELECT DISTINCT query, rule FROM rule_application'):
            queries.add(query)
            stats.applied(rule)
        print('Unique queries: {}'.format(len(queries)))
    else:
        args = []
        q = 'SELECT rule, COUNT(*) FROM rule_application'
        if source:
            args.append(source)
            q += ' WHERE source=?'
        q += ' GROUP BY rule'
        for row in conn.execute(q, args):
            stats.applied(row[0], times=int(row[1]))
    stats.print_stats()

def run_all_tests(args):
    """:param args: argparser.Namespace"""
    db = args.db
    if not os.path.isfile('build.xml'):
        raise RuntimeError('build.xml does not exist, generate it from Eclipse first')
    if not os.path.isfile(db):
        print('Creating db file: {}'.format(db))
        main(['init', '--db', db])
    for source, testname in SOURCES.iteritems():
        print('Running test case: {}'.format(testname))
        ant_args = ('ant', testname)
        retcode = subprocess.call(ant_args)
        if retcode != 0:
            raise RuntimeError('Ant run failed with code: {}'.format(retcode))
        print('Updating results for {}'.format(source))
        main(['update', source])



def get_query(line):
    m = re.match(r'^.*TestBase - Query: (?P<query>.+)$', line)
    return m.group('query') if m else None


def get_applied(line):
    m = re.match(r'^.*Applied rule: (?P<rule>[^\s]+)', line)
    return m.group('rule') if m else None


def _parser():
    parent = argparse.ArgumentParser()
    parent.add_argument('--db', default='sqltutorrules.db')

    sub = parent.add_subparsers()

    init = sub.add_parser('init', description='Create the database')
    """:type : argparse.ArgumentParser"""
    init.set_defaults(func=run_init)

    update = sub.add_parser('update', description='Update the database from a log file')
    """:type : argparse.ArgumentParser"""
    update.add_argument('sourcename', help='Source (test case) name for these results.')
    update.add_argument('logfile', nargs='?', default='sqltutor.debug.log')
    update.set_defaults(func=run_update)

    report = sub.add_parser('report', description='Report current results')
    report.add_argument('--unique-queries', action='store_true', default=False,
                        help='Only consider unique queries for global counts')
    report.add_argument('--show-rules', action='store_true', default=False,
                        help='Show the rules used for per-source reports')
    report.add_argument('sources', nargs='*', default=None,
                        help='Report for each source (report is global otherwise)')
    report.set_defaults(func=run_report)

    run = sub.add_parser('run', description='Run all tests and update the results')
    run.set_defaults(func=run_all_tests)

    return parent


def main(args=None):
    if args is None: args = sys.argv[1:]
    parser = _parser()
    args = parser.parse_args(args)
    args.func(args)


if __name__ == '__main__':
    main()

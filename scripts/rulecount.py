#!/usr/bin/env python
from __future__ import with_statement, print_function
import subprocess
import sys
import re
import os
import collections
from collections import OrderedDict
import argparse
import sqlite3


def default_paper_name(name):
    name = re.sub(r'Rule$', '', name)
    name = re.sub(r'([a-z])([A-Z])', r'\1 \2', name)
    return name

class RuleDef(object):
    def __init__(self, name, category='L', paper_name=None):
        self.name = name
        self.paper_name = paper_name if paper_name else default_paper_name(name)
        self.category = category

    def __repr__(self):
        return 'RuleDef({}, paper_name={}, category={})'.format(
            map(repr, (self.name, self.paper_name, self.category)))

ALL_RULES = [
    RuleDef('TransformationRule', 'L', paper_name='Select List Format'),
    RuleDef('InRelationshipLabelRule', 'G'),
    RuleDef('DescribingAttributeLabelRule', 'A', paper_name='Describing Attribute'),
    RuleDef('JoinLabelRule', 'A', paper_name='Join Relationship'),
    RuleDef('MergeCompositeAttributeRule', 'A'),
    RuleDef('ValueTypeInferenceRule', 'A'),
    RuleDef('TableEntityRefNeedsIdRule', 'G', paper_name='Entity Ref Needs Id'),
    RuleDef('AllAttributesLiteralLabelRule', 'L'),
    RuleDef('AttributeLiteralLabelRule', 'L'),
    RuleDef('BetweenLiteralsRule', 'L'),
    RuleDef('BinaryComparisonRule', 'L'),
    RuleDef('NumberLiteralRule', 'L'),
    RuleDef('SelectLabelRule', 'L'),
    RuleDef('WhereLiteralRule', 'L'),
    RuleDef('ColumnReferenceLiteralRule', 'L'),
    RuleDef('DefaultIsNullRule', 'L'),
    RuleDef('InRelationshipLoweringRule', 'L'),
    RuleDef('TableEntityRefLiteralRule', 'L', paper_name='Entity Ref Lowering'),
    RuleDef('SimplifyConjunctionsRule', 'G'),
    RuleDef('FixVerbTenseRule', 'C'),
    RuleDef('InvalidDeterminerRule', 'C'),
    RuleDef('ConjunctScopeComputationRule', 'D'),
    RuleDef('DefaultColumnLabelRule', 'D'),
    RuleDef('DefaultTableLabelRule', 'D'),
    RuleDef('AnaphoricPronounRule', 'G'),
    RuleDef('DeterminerRedundancyRule', 'G'),
    RuleDef('RangeToBetweenRule', 'A'),
    RuleDef('SimplifyRepeatedAttributesRule', 'G'),
    RuleDef('SingleReferenceAnaphoraRule', 'G')
]

def get_rule_def(name):
    for rule in ALL_RULES:
        if rule.name == name:
            return rule
    return None

# establish map from implementation name to name used in the paper
RULE_TO_PAPER_NAME = OrderedDict()
for rule in ALL_RULES:
    RULE_TO_PAPER_NAME[rule.name] = rule.paper_name

def get_paper_name(rule):
    return get_rule_def(rule).paper_name


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
        print('{}\t{}'.format('Rule', 'Count'))
        for rule, count in self.rules.iteritems():
            print('{}\t{}'.format(rule, count))


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
        conn.commit()


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
    """Reads the rule application stats from a log file.
    :returns a dict mapping query -> QueryStats"""
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


def get_source_counts(conn, source=None, query=None):
    source_query = (source, query)
    q = 'SELECT COUNT(*) FROM rule_application WHERE source=? AND query=?'
    total = conn.execute(q, source_query).fetchone()[0]
    q += ' AND rule NOT IN (\'' + '\',\''.join(IGNORE_RULES) + '\')'
    paper = conn.execute(q, source_query).fetchone()[0]
    return total, paper


def run_query_report(args):
    db = args.db
    with sqlite3.connect(db) as conn:
        if not args.sources:
            query_report(conn, unique_queries=args.unique_queries, show_rules=args.show_rules)
        else:
            for source in args.sources:
                query_report(conn, source=source, unique_queries=args.unique_queries, show_rules=args.show_rules)


def query_report(conn, source=None, unique_queries=False, show_rules=False):
    """Generate a report aggregated by query.

    :param conn: the database connection
    :param source: optional source to restrict the report to
    :param unique_queries: if only unique queries should be counted, has no effect if a single source is given
    :param show_rules: if the individual rules used should be included in the output
    """
    seen = set()
    qargs = []
    headers = ['Source', 'Query #', 'Core', 'Total', 'Query']
    if show_rules:
        headers.append('Rules')

    q = 'SELECT DISTINCT source, query, query_number FROM rule_application'
    if source:
        q += ' WHERE source=?'
        qargs.append(source)
    q += ' ORDER BY source DESC, query_number ASC'

    cur = conn.execute(q, qargs)
    print('\t'.join(headers))
    for source, query, query_number in cur:
        # skip if unique
        if unique_queries:
            if query in seen:
                continue
            seen.add(query)

        total, paper_total = get_source_counts(conn, source, query)
        output = [source, query_number, paper_total, total, query]
        if show_rules:
            rules = '|'.join(r[0] for r in get_query_rules(conn, source, query))
            output.append(rules)
        print('\t'.join(map(str, output)))


def run_rules_report(args):
    db = args.db
    with sqlite3.connect(db) as conn:
        if not args.sources:
            rules_report(conn, unique_queries=args.unique_queries)
        else:
            for source in args.sources:
                print('Source: {}'.format(source), file=sys.stderr)
                rules_report(conn, unique_queries=args.unique_queries, source=source)


def rules_report(conn, unique_queries=False, source=None):
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
        print('Unique queries: {}'.format(len(queries)), file=sys.stderr)
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
        main(['--db', db, 'init'])
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
    common = argparse.ArgumentParser(add_help=False)
    common.add_argument('--db', default='sqltutorrules.db',
                        help='Choose the SQLite database file')

    parent = argparse.ArgumentParser(parents=[common])
    # parent.add_argument('--db', default='sqltutorrules.db')

    sub = parent.add_subparsers()

    init = sub.add_parser('init', help='Initialize the database file',
                          description='Creates the database file and schema.')
    """:type : argparse.ArgumentParser"""
    init.set_defaults(func=run_init)

    update = sub.add_parser('update', help='Update the database from a log file',
                            description='Reads a log file and updates the database entries of a source.')
    """:type : argparse.ArgumentParser"""
    update.add_argument('--logfile', default='sqltutor.debug.log',
                        help='The logfile location to read results from')
    update.add_argument('sourcename', help='Source (test case) name for these results.')
    update.set_defaults(func=run_update)

    report_parent = argparse.ArgumentParser(add_help=False)
    report_parent.add_argument('--unique-queries', action='store_true', default=False,
                               help='Only consider unique queries for global counts')
    report_parent.add_argument('sources', nargs='*', default=None,
                               help='Report for each source (report is global otherwise)')

    report = sub.add_parser('report', help='Generate reports, see subcommand help.',
                            description='Generate rule application reports, either per query or per rule.')
    report_subs = report.add_subparsers()
    report_queries = report_subs.add_parser('queries', parents=[report_parent],
                                            help='Generate per-query reports')
    report_queries.add_argument('--show-rules', action='store_true', default=False,
                                help='Show the rules used for per-source reports')
    report_queries.set_defaults(func=run_query_report)

    report_rules = report_subs.add_parser('rules', parents=[report_parent],
                                          help='Generate per-rule reports')
    report_rules.set_defaults(func=run_rules_report)

    run = sub.add_parser('run', help='Run tests and gather results',
                         description='Run all tests and update the results.')
    run.set_defaults(func=run_all_tests)

    return parent


def main(args=None):
    if args is None: args = sys.argv[1:]
    parser = _parser()
    args = parser.parse_args(args)
    args.func(args)


if __name__ == '__main__':
    main()

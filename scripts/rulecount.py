#!/usr/bin/env python
from __future__ import with_statement, print_function
import sys, re
from collections import OrderedDict, namedtuple

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

class RuleStats(object):
	def __init__(self):
		self._init_gstats()
		self.qstats = None

	def _init_gstats(self):
		self.gstats = OrderedDict()
		for rule in ALL_RULES:
			self.gstats[rule] = 0

	def update_stats(self):
		if self.qstats:
			for rule in self.qstats.iterkeys():
				try:
					self.gstats[rule] += 1
				except KeyError:
					self.gstats[rule] = 1
		self.qstats = OrderedDict()


	def applied(self, rule):
		self.qstats[rule] = None

	def print_qstats(self):
		if self.qstats is not None:
			rules = self.qstats.keys()
			print('\n'.join(rules))
			main_rules = filter(lambda r: not is_ignored_rule(r), rules)
			print('Rules applied: {} ({})'.format(len(main_rules), len(rules)))

	def print_gstats(self):
		for rule, count in self.gstats.iteritems():
			print('{}: {}'.format(rule, count))


def is_cleanup_rule(rule):
	return bool(re.match(r'(SimplifyConjunctions|FixVerbTense|InvalidDeterminer)Rule', rule))

def is_default_rule(rule):
	return bool(re.match(r'(Default(Column|Table)Label|ConjunctScopeComputation)Rule', rule))

def is_ignored_rule(rule):
	return is_cleanup_rule(rule) or is_default_rule(rule)



def get_query(line):
	m = re.match(r'^.*TestBase - Query: (?P<query>.+)$', line)
	return m.group('query') if m else None

def get_applied(line):
	m = re.match(r'^.*Applied rule: (?P<rule>[^\s]+)', line)
	return m.group('rule') if m else None

def main(args=None):
	if args is None: args = sys.argv[1:]
	infile = args.pop(0) if args else 'sqltutor.debug.log'

	qstats = None
	stats = RuleStats()

	def end_query():
		stats.print_qstats()
		stats.update_stats()


	with open(infile) as fp:
		for line in fp:
			query = get_query(line)
			if query:
				end_query()
				print('Query: {}'.format(query))
				continue
			rule = get_applied(line)
			if rule:
				stats.applied(rule)
	end_query()
	stats.print_gstats()




if __name__ == '__main__':
	main()

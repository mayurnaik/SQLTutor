#!/usr/bin/env python
from __future__ import print_function, with_statement
import sys
import matplotlib.figure
from matplotlib.figure import Figure, Axes
import matplotlib.pyplot as plt
import numpy as np
import argparse
import csv
import collections
import itertools as it

from rulecount import IGNORE_RULES, get_paper_name

RuleCount = collections.namedtuple('RuleCount', ('rule', 'count'))

def create_plot(args):
    data = read_input(args.inputfile)

    # optionally filter and sort
    if not args.all_rules:
        data = [d for d in data if d.rule not in IGNORE_RULES]
    if args.sort:
        data.sort(cmp=lambda x, y: cmp(x.count, y.count))

    rules = [get_paper_name(d.rule) for d in data]
    counts = [d.count for d in data]
    fig = plt.figure()
    """:type : Figure"""
    ax = fig.add_subplot(111)
    """:type : Axes"""
    ax.set_yticks(range(len(rules)))
    ax.set_yticklabels(rules)
    rects = ax.barh(range(len(rules)), counts, align='center', hatch=None, fill=True, color='gray')
    ax.set_xlabel('Query Translation')

    # draw values by bars
    for i, rect in enumerate(rects):
        y = rect.get_y() + rect.get_height() / 4.0
        val = counts[i]
        if val <= 40:
            color = 'black'
            x = rect.get_width() + 0.5
        else:
            color = 'white'
            x = rect.get_width() - 2
        ax.text(x, y, str(counts[i]), color=color)

    # draw mean
    mean = np.mean(counts)
    print('Mean: {}'.format(mean), file=sys.stderr)

    fig.tight_layout(pad=0.0, rect=(0, 0, 0.925, 1))

    if args.show:
        plt.show(fig)
    else:
        fig.savefig(args.outputfile, format='pdf')


def read_input(inputfile):
    counts = []
    reader = csv.reader(inputfile, delimiter='\t')
    for i, data in it.izip(it.count(0), reader):
        if i == 0 and data[0] == 'Rule':
            continue
        count = RuleCount(data[0], int(data[1]))
        counts.append(count)
    return counts


def make_parser():
    p = argparse.ArgumentParser()
    p.add_argument('inputfile', nargs='?', default='-', type=argparse.FileType(mode='r'),
                   help='The input file to read (defaults to standard in)')
    p.add_argument('--all-rules', action='store_true', default=False,
                   help='Include rules that would normally be excluded (defaults and cleanups)')
    p.add_argument('-o', '--output-file', dest='outputfile', default='rulecounts.pdf',
                   help='The output file to write')
    p.add_argument('--no-sort', dest='sort', action='store_false', default=True,
                   help='Do not sort rules by number of applications')
    p.add_argument('-s', '--show', dest='show', default=False, action='store_true',
                   help='Show the figure instead of saving to a file')
    return p


def main(args=None):
    if args is None: args = sys.argv[1:]
    parser = make_parser()
    args = parser.parse_args(args)
    create_plot(args)

if __name__ == '__main__':
    main()

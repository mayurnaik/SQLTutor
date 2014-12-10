#!/bin/bash
infile="$1"
infile_loc="$(dirname "$(readlink -f "$infile")")"
schemaname="$2"

if [ ! -f "$infile" ]; then
	echo "Input file must exist: $infile" >&2
	echo "Usage: $0 INFILE [SCHEMA]" >&2
	echo "Example: $0 example.sql > example.fixed.sql"
	exit 1
fi
if [ -z "$schemaname" ]; then
	schemaname="$(basename "$infile" | cut -d. -f-1)"
fi


cat "$infile" | \
	awk -v schemaname="$schemaname" '{print}
	/SET search_path = public/{
		print "DROP SCHEMA IF EXISTS " schemaname " CASCADE;\n";
		print "CREATE SCHEMA " schemaname ";\n";
		print "SET search_path = " schemaname ";\n";
	}' |
	sed -r "s/public\./$schemaname./g" |
	sed -r 's@\$\$PATH\$\$@'"$infile_loc"'@g'



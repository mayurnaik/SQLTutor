#!/bin/bash

groupId=at.sti2.iris

SCRIPTDIR=`dirname "$0"`
SCRIPTDIR=`( cd "$SCRIPTDIR" && pwd )`

for jfile in "$SCRIPTDIR"/iris*.jar; do
	fname=`basename "$jfile"`
	if [[ "$fname" =~ (.+)-([0-9][0-9\.]+)\.jar ]]; then
		artifactId="${BASH_REMATCH[1]}"
		version="${BASH_REMATCH[2]}"
		mvn install:install-file -Dpackaging=jar -DgroupId="$groupId" \
		                         -DartifactId="$artifactId" -Dversion="$version" \
		                         -Dfile="$jfile" -DpomFile="$SCRIPTDIR/pom-${artifactId}-${version}.xml"
	else
		echo "Unexpected file: $fname" >&2
	fi
done

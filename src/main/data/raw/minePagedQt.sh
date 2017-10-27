#!/bin/bash
touch startedMiningQt
pageSize=500
for i in {0..305}
do
	for status in abandoned deferred merged
	do
		calls=0
		if [ $i -eq 0 ] 
		then 
			if [ ! -f "qt_changes_${i}_${status}.json" ]
			then
				echo "Retrieving page $i for Qt $status"
				timeout 15m curl "https://codereview.qt-project.org/changes/?q=is:${status}&o=LABELS&o=DETAILED_LABELS&o=ALL_FILES&o=DETAILED_ACCOUNTS&o=ALL_REVISIONS&n=${pageSize}" -o "qt_changes_${i}_${status}.json"
				calls=`expr $calls + 1`
			else
				echo "Skipping page $i for Qt, already retrieved"
			fi
		else
			if [ -f "qt_changes_$(expr $i - 1)_${status}.json" ] && grep --quiet "_more_changes" "qt_changes_$(expr $i - 1)_${status}.json"
			then
				if [ ! -f "qt_changes_${i}_${status}.json" ]
				then
					echo "Retrieving page $i for Qt $status"
					sortkey=$(grep -Eo "\"_sortkey\": \".*\"" "qt_changes_$(expr $i - 1)_${status}.json"| tail -1 | sed "s/\"_sortkey\": \"\(.*\)\"/\1/")
					timeout 15m curl "https://codereview.qt-project.org/changes/?q=is:${status}&o=LABELS&o=DETAILED_LABELS&o=ALL_FILES&o=DETAILED_ACCOUNTS&o=ALL_REVISIONS&N=${sortkey}&n=${pageSize}" -o "qt_changes_${i}_${status}.json"
					calls=`expr $calls + 1`
				else
					echo "Skipping page $i for Qt, already retrieved"
				fi
			fi
		fi
		if [ $calls -gt 0 ]
		then
			sleep 1s
		fi
	done
done
echo "Done mining, cleaning up retrieved JSON files"
# remove non-JSON characters at beginning of new JSON files
find . -cnewer startedMiningQt -iname "qt_changes_*.json" -exec sed -i "/)]}'/d" {} \;
rm startedMiningQt

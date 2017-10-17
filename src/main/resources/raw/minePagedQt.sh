#!/bin/bash
pageSize=500
for i in {1..305}
do
	for status in abandoned deferred merged
	do
		if [ $i -eq 0 ] 
		then 
			if [ ! -f "qt_changes_${i}_${status}.json" ]
			then
				echo "Retrieving page $i for Qt"
				curl "https://codereview.qt-project.org/changes/?q=is:${status}&o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&n=${pageSize}" -o "qt_changes_${i}_${status}.json"
			else
				echo "Skipping page $i for Qt, already retrieved"
				sleep 1s
			fi
		else
			if [ -f "qt_changes_$(expr $i - 1)_${status}.json" ] && grep --quiet "_more_changes" "qt_changes_$(expr $i - 1)_${status}.json"
			then
				if [ ! -f "qt_changes_${i}_${status}.json" ]
				then
					echo "Retrieving page $i for Qt"
					sortkey=$(grep -Eo "\"_sortkey\": \".*\"" "qt_changes_$(expr $i - 1)_${status}.json"| tail -1 | sed "s/\"_sortkey\": \"\(.*\)\"/\1/")
					curl "https://codereview.qt-project.org/changes/?q=is:${status}&o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&N=${sortkey}&n=${pageSize}" -o "qt_changes_${i}_${status}.json"
				else
					echo "Skipping page $i for Qt, already retrieved"
				fi
			else
				sleep 1s
			fi
		fi
	done
done
# remove non-JSON characters at beginning of JSON file
sed -i.bak "/)]}'/d" *.json

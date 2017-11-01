#!/bin/bash
pageSize=100
for i in {0..1200}
do
	calls=0
	if [ $i -eq 0 ] 
	then 
		if [ ! -f "eclipse_changes_${i}.json" ]
		then
			echo "Retrieving page $i for Eclipse $status"
			timeout 15m curl "https://git.eclipse.org/r/changes/?q=is:closed&o=LABELS&o=DETAILED_LABELS&o=ALL_FILES&o=DETAILED_ACCOUNTS&o=ALL_REVISIONS&o=MESSAGES&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "eclipse_changes_${i}.json"
			calls=`expr $calls + 1`
			sed -i "/)]}'/d" "eclipse_changes_${i}.json"
		else
			echo "Skipping page $i for Eclipse, already retrieved"
		fi
	else
		if [ -f "eclipse_changes_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "eclipse_changes_$(expr $i - 1).json"
		then
			if [ ! -f "eclipse_changes_${i}.json" ]
			then
				echo "Retrieving page $i for Eclipse $status"
				timeout 15m curl "https://git.eclipse.org/r/changes/?q=is:closed&o=LABELS&o=DETAILED_LABELS&o=ALL_FILES&o=DETAILED_ACCOUNTS&o=ALL_REVISIONS&o=MESSAGES&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "eclipse_changes_${i}.json"
				calls=`expr $calls + 1`
				sed -i "/)]}'/d" "eclipse_changes_${i}.json"
			else
				echo "Skipping page $i for Eclipse, already retrieved"
			fi
		fi
	fi
	if [ $calls -gt 0 ]
	then
		sleep 1s
	fi
done

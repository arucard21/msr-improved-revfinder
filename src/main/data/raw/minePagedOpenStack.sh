#!/bin/bash
pageSize=500
for year in $(seq 2017 -1 2000)
do
  for month in {01..12}
  do
	if [ $month == 12 ]
	then
		start="${year}-${month}"
		end="$(expr $year + 1)-01"
	else
		start="${year}-${month}"
		end="${year}-$(expr $month + 1)"
	fi
	for i in {0..99}
	do
		calls=0
		if [ $i -eq 0 ] || ( [ -f "openstack_changes_${start}_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "openstack_changes_${start}_$(expr $i - 1).json" )
		then
			if [ ! -f "openstack_changes_${start}_${i}.json" ]
			then
				echo "Retrieving page $i for OpenStack for ${start}"
				timeout 15m curl "https://review.openstack.org/changes/?q=is:closed+before:${end}-01+after:${start}-01&o=LABELS&o=DETAILED_LABELS&o=ALL_FILES&o=DETAILED_ACCOUNTS&o=ALL_REVISIONS&o=MESSAGES&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "openstack_changes_${start}_${i}.json"
				calls=`expr $calls + 1`
				sed -i "/)]}'/d" "openstack_changes_${start}_${i}.json"
			else
				echo "Skipping page $i for OpenStack, already retrieved"
			fi
		fi
		if [ $calls -gt 0 ]
		then
			sleep 1s
		fi
	done
  done
done

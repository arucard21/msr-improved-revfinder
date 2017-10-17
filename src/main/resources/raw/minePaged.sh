#!/bin/bash
pageSize=500
for year in $(seq 2018 -1 2000)
do
  for i in {0..99}
  do
  	# check if previous retrieved page contains the field "_more_changes" which indicates that more data is available (unless this is the first page we're trying to retrieve)
  	if [ $i -eq 0 ] || ( [ -f "android_changes_$(expr $year - 1)_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "android_changes_$(expr $year - 1)_$(expr $i - 1).json" )
  	then
  		# make sure we don't retrieve data that we already retrieved
  		if [ ! -f "android_changes_$(expr $year - 1)_${i}.json" ]
  		then
  			echo "Retrieving page $i for Android"
  			curl "https://android-review.googlesource.com/changes/?q=is:closed+before:${year}-01-01+after:$(expr $year - 1)-01-01&o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "android_changes_$(expr $year - 1)_${i}.json"
  		else
  			echo "Skipping page $i for Android, already retrieved"
  			sleep 1s
  		fi
  	# sleep for a bit since all data has been retrieved for this project. This avoids us hammering a single project with requests
  	else
  		sleep 1s
  	fi
  	if [ $i -eq 0 ] || ( [ -f "chromium_changes_$(expr $year - 1)_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "chromium_changes_$(expr $year - 1)_$(expr $i - 1).json" )
  	then
  		if [ ! -f "chromium_changes_$(expr $year - 1)_${i}.json" ]
  		then
  			echo "Retrieving page $i for Chromium"
  			curl "https://chromium-review.googlesource.com/changes/?q=is:closed+before:${year}-01-01+after:$(expr $year - 1)-01-01&o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "chromium_changes_$(expr $year - 1)_${i}.json"
  		else
  			echo "Skipping page $i for Chromium, already retrieved"
  			sleep 1s
  		fi
  	else
  		sleep 1s
  	fi
  	if [ $i -eq 0 ] || ( [ -f "openstack_changes_$(expr $year - 1)_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "openstack_changes_$(expr $year - 1)_$(expr $i - 1).json" )
  	then
  		if [ ! -f "openstack_changes_$(expr $year - 1)_${i}.json" ]
  		then
  			echo "Retrieving page $i for OpenStack"
  			curl "https://review.openstack.org/changes/?q=is:closed+before:${year}-01-01+after:$(expr $year - 1)-01-01&o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "openstack_changes_$(expr $year - 1)_${i}.json"
  		else
  			echo "Skipping page $i for OpenStack, already retrieved"
  			sleep 1s
  		fi
  	else
  		sleep 1s
  	fi
  done
done
# remove non-JSON characters at beginning of JSON file
sed -i.bak "/)]}'/d" *.json

#!/bin/bash
pageSize=500
for i in {0..99}
do
	if [ $i -eq 0 ] || ( [ -f "android_changes_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "android_changes_$(expr $i - 1).json" )
	then
		if [ ! -f "android_changes_${i}.json" ]
		then
			echo "Retrieving page $i for Android"
			curl "https://android-review.googlesource.com/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "android_changes_${i}.json"
		else
			echo "Skipping page $i for Android, already retrieved"
		fi
	fi
	if [ $i -eq 0 ] || ( [ -f "chromium_changes_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "chromium_changes_$(expr $i - 1).json" )
	then
		if [ ! -f "chromium_changes_${i}.json" ]
		then
			echo "Retrieving page $i for Chromium"
			curl "https://chromium-review.googlesource.com/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "chromium_changes_${i}.json"
		else
			echo "Skipping page $i for Chromium, already retrieved"
		fi
	fi
	if [ $i -eq 0 ] || ( [ -f "openstack_changes_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "openstack_changes_$(expr $i - 1).json" )
	then
		if [ ! -f "openstack_changes_${i}.json" ]
		then
			echo "Retrieving page $i for OpenStack"
			curl "https://review.openstack.org/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "openstack_changes_${i}.json"
		else
			echo "Skipping page $i for OpenStack, already retrieved"
		fi
	fi
	if [ $i -eq 0 ] 
	then 
		if [ ! -f "qt_changes_${i}.json" ]
		then
			echo "Retrieving page $i for Qt"
			curl "https://codereview.qt-project.org/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&n=${pageSize}" -o "qt_changes_${i}.json"
		else
			echo "Skipping page $i for Qt, already retrieved"
		fi
	else
		if [ -f "qt_changes_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "qt_changes_$(expr $i - 1).json"
		then
			if [ ! -f "qt_changes_${i}.json" ]
			then
				echo "Retrieving page $i for Qt"
				sortkey=$(grep -Eo "\"_sortkey\": \".*\"" "qt_changes_$(expr $i - 1).json"| tail -1 | sed "s/\"_sortkey\": \"\(.*\)\"/\1/")
				curl "https://codereview.qt-project.org/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&N=${sortkey}&n=${pageSize}" -o "qt_changes_${i}.json"
			else
				echo "Skipping page $i for Qt, already retrieved"
			fi
		fi
	fi
	sleep 3s
done

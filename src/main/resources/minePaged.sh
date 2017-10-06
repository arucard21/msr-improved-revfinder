#!/bin/bash
pageSize=500
for i in {0..99}
do
	if [ $i -eq 0 ] || ( [ -f "android_changes_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "android_changes_$(expr $i - 1).json" )
	then
		curl "https://android-review.googlesource.com/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "android_changes_${i}.json"
	fi
	if [ $i -eq 0 ] || ( [ -f "chromium_changes_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "chromium_changes_$(expr $i - 1).json" )
	then
		curl "https://chromium-review.googlesource.com/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "chromium_changes_${i}.json"
	fi
	if [ $i -eq 0 ] || ( [ -f "openstack_changes_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "openstack_changes_$(expr $i - 1).json" )
	then
		curl "https://review.openstack.org/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&S=$(expr $i \* $pageSize)&n=${pageSize}" -o "openstack_changes_${i}.json"
	fi
	if [ $i -eq 0 ] 
	then 
		curl "https://codereview.qt-project.org/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&n=${pageSize}" -o "qt_changes_${i}.json"
	else
		if [ -f "qt_changes_$(expr $i - 1).json" ] && grep --quiet "_more_changes" "qt_changes_$(expr $i - 1).json"
		then
			sortkey=$(grep -Eo "\"_sortkey\": \".*\"" "qt_changes_$(expr $i - 1).json"| tail -1 | sed "s/\"_sortkey\": \"\(.*\)\"/\1/")
			curl "https://codereview.qt-project.org/changes/?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS&N=${sortkey}&n=${pageSize}" -o "qt_changes_${i}.json"
		fi
	fi
done

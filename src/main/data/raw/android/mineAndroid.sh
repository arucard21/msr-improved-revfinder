#!/bin/bash
projectsFile="projectURLs.txt"
refs="android_refs.txt"
changeNumbers="android_changeNumbers.txt"
noReview="android_noReview.txt"
retrieved="android_retrieved.txt"
changes="android_changes_all.json"
response="android_response.json"

# curl "https://android-review.googlesource.com/projects/" | grep "\"url\"\: " | sed "s/^.*\"url\"\: \"https:\/\/android.googlesource.com\/\(.*\)\/.*$/\https:\/\/android-review.googlesource.com\/\1.git/" > $projectsFile
# 
# while read -r gitURL
# do 
# 	git ls-remote $gitURL | grep refs/changes/ >> $refs 
# 	sleep 0.5s
# done < $projectsFile
# 
# sed "s/^.*refs\/changes\/[0-9]\+\/\([0-9]\+\)\/.*$/\1/" $refs | uniq > $changeNumbers

if [ ! -f $noReview ]
then 
	touch $noReview
fi

if [ ! -f $retrieved ]
then 
	touch $retrieved
fi

count=0
while read -r changeNumber
do 
	if [ ! $(grep "^$changeNumber$" $noReview) ]
	then 
		if [ ! $(grep "^$changeNumber$" $retrieved) ]
		then
			if curl --output $response --silent --fail "https://android-review.googlesource.com/changes/${changeNumber}?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS"
			then
				if [ -s $changes ] 
				then
					echo "," >> $changes
				fi
				echo $changeNumber >> $retrieved
				cat $response | sed "/)]}'/d" >> $changes
			else
				if [ $(curl -s -o /dev/null -w "%{http_code}" "https://android-review.googlesource.com/changes/${changeNumber}?o=ALL_REVISIONS&o=ALL_FILES&o=ALL_COMMITS&o=MESSAGES&o=DETAILED_ACCOUNTS") -eq 429 ]
				then
					exit -1
				fi
				echo $changeNumber >> $noReview
			fi
			sleep 1s
		fi
	fi
	echo "Amount processed: ${count}"
	count=$(expr $count + 1)
done < $changeNumbers

# add characters to make it a valid JSON array
sed -i "1i \[" $changes
echo "]" >> $changes
rm $response

#!/usr/bin/python3

import urllib.request
import json

gerrit_list = ['https://android-review.googlesource.com/', 'https://review.openstack.org/', 'https://chromium-review.googlesource.com/' ,'https://codereview.qt-project.org/' ]
query_list = ['accounts/?q=is:inactive', 'accounts/?q=status','changes/?q=status:open&o=REVIEWED' ]

i = input('Choose project to mine: 0) Android, 1) Openstack, 2) Chromium, 3) Qt\n')
j = input('Choose data to mine: 0) Inactive developers, 1) Developers\' status, 2) Open changes that have been reviewed\n')

url =  gerrit_list[int(i)] + query_list[int(j)]
response = urllib.request.urlopen(url).read()[4:]

try:
    data = json.loads(response.decode("utf-8"))
    filename = 'data'+ i + j + '.json'
    out = open(filename, 'w')
    json.dump(data, out)
except ValueError as e:
    print(e)



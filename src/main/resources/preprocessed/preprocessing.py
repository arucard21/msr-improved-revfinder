import json
import os.path

projects = ["android", "chromium", "openstack", "qt"]

for gerrit_projects in projects:
    for i in range(0, 100):
        filename = gerrit_projects + "_changes_" + str(i) + ".json"
        in_dir = "../raw/" + filename
        out_dir = filename

        if os.path.isfile(in_dir):
            print("process: " + in_dir)
            with open(in_dir) as data_file:
                data = json.load(data_file)

            dropped = 0
            output = []
            for record in data:
                id = record['id']
                project = record['project']
                change_id = record['change_id']
                status = record['status']
                created = record['created'][0:19]
                updated = record['updated'][0:19]

                if 'insertions' in record:
                    insertions = record['insertions']
                else:
                    insertions = -1

                if 'deletions' in record:
                    deletions = record['deletions']
                else:
                    deletions = -1

                _number = record['_number']
                owner_id = record['owner']['_account_id']
                if 'current_revision' not in record:
                    dropped += 1
                    continue
                current_revision = record['current_revision']

                messages = record['messages']
                reviewers = []
                clean_messages = []
                for msg in messages:
                    msg_id = msg['id']

                    if 'author' not in msg:
                        dropped += 1
                        continue

                    if '_account_id' not in msg['author']:
                        dropped += 1
                        continue
                    msg_author_id = msg['author']['_account_id']
                    reviewers.append(msg_author_id)
                    msg_date = msg['date'][0:19]
                    msg_type = ("review", "self")[owner_id == msg_author_id]

                    clean_msg = {
                        'id': msg_id,
                        'author': msg_author_id,
                        'type': msg_type,
                        'date': msg_date,
                    }
                    clean_messages.append(clean_msg)

                reviewers = list(set(reviewers))

                if owner_id in reviewers:
                    reviewers.remove(owner_id)

                revisions = record['revisions']
                clean_revisions = []
                for rev_id in revisions:
                    rev = revisions[rev_id]

                    if 'kind' in rev:
                        rev_kind = rev['kind']
                    else:
                        rev_kind = record['kind']

                    rev_author_date = rev['commit']['author']['date'][0:19]
                    rev_committer_date = rev['commit']['committer']['date'][0:19]
                    if 'uploader' in rev:
                        rev_uploader = rev['uploader']['_account_id']
                    else:
                        rev_uploader = None

                    files = rev['files']
                    file_names = []
                    for file in files:
                        file_names.append(file)

                    clean_rev = {
                        'id': rev_id,
                        'kind': rev_kind,
                        'author_date': rev_author_date,
                        'committer_date': rev_committer_date,
                        'uploader': rev_uploader,
                        'files': file_names
                    }
                    clean_revisions.append(clean_rev)

                result = {
                    'id': id,
                    'change_id': change_id,
                    'owner': owner_id,
                    'status': status,
                    'created': created,
                    'updated': updated,
                    'insertions': insertions,
                    'deletions': deletions,
                    'current_revision': current_revision,
                    'reviewers': reviewers,

                    'messages': clean_messages,
                    'revisions': clean_revisions,

                    'project': project,
                    'number': _number,
                }
                output.append(result)

            if dropped:
                print('   ' + str(dropped) + "dropped")

            with open(filename, 'w') as outfile:
                json.dump(output, outfile)

        else:
            break
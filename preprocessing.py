import json

with open('chromium_changes.json') as data_file:
    data = json.load(data_file)

output = []
for record in data:
    id = record['id']
    project = record['project']
    change_id = record['change_id']
    status = record['status']
    created = record['created'][0:19]
    updated = record['updated'][0:19]
    insertions = record['insertions']
    deletions = record['deletions']
    _number = record['_number']
    owner_id = record['owner']['_account_id']
    current_revision = record['current_revision']

    messages = record['messages']
    reviewers = []
    clean_messages = []
    for msg in messages:
        msg_id = msg['id']
        msg_author_id = msg['real_author']['_account_id']
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
    reviewers.remove(owner_id)

    revisions = record['revisions']
    clean_revisions = []
    for rev_id in revisions:
        rev = revisions[rev_id]
        rev_kind = rev['kind']
        rev_created = rev['created'][0:19]
        rev_uploader = rev['uploader']['_account_id']

        files = rev['files']
        file_names = []
        for file in files:
            file_names.append(file)

        clean_rev = {
            'id': rev_id,
            'kind': rev_kind,
            'created': rev_created,
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

with open('chromium_cleaned.json', 'w') as outfile:
    json.dump(output, outfile)
"""Checks that the app data is a verbatim copy of the author's text (no edits by the tooling)."""
import json, re, sys
src = open('data_src/duas_source.txt', encoding='utf-8').read()
src = src[src.index('1۔ ہدایت، تقویٰ'):]
data = json.load(open('www/data/duas.json', encoding='utf-8'))['duas']
squash = lambda s: re.sub(r'\s+', ' ', s)
S = squash(src); fails = []
for d in data:
    for k in ('title', 'arabic', 'urdu', 'ref'):
        if squash(d[k]) not in S: fails.append((d['id'], k))
    if f"{d['id']}۔ {d['title']}" not in src: fails.append((d['id'], 'numbered-title'))
print('entries:', len(data), '| verbatim-check failures:', fails)
js = open('www/data/duas.js', encoding='utf-8').read()
assert json.loads(js[len('window.DUAS_DATA='):-1]) == json.load(open('www/data/duas.json', encoding='utf-8')), 'duas.js != duas.json'
print('duas.js matches duas.json')
sys.exit(1 if fails else 0)

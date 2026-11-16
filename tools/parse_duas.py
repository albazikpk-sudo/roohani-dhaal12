import re, json, sys
src, out = sys.argv[1], sys.argv[2]
lines = [l.rstrip('\n') for l in open(src, encoding='utf-8')]
start = next(i for i,l in enumerate(lines) if l.strip().startswith('1۔ ہدایت، تقویٰ'))
lines = lines[start:]
entries, cur, nxt = [], None, 1
for l in lines:
    s = l.strip()
    m = re.match(r'^(\d+)۔\s*(.+)$', s)
    if m and int(m.group(1)) == nxt:
        cur = {'n': nxt, 'title': m.group(2), 'arabic': [], 'urdu': '', 'ref': ''}
        entries.append(cur); nxt += 1; cur['_mode'] = 'ar'; continue
    if cur is None or not s: continue
    if s.startswith('ترجمہ:'):
        cur['urdu'] = s[len('ترجمہ:'):].strip(); cur['_mode'] = 'ur'
    elif s.startswith('حوالہ:'):
        cur['ref'] = s[len('حوالہ:'):].strip(); cur['_mode'] = 'rf'
    elif s.startswith('حصہ '):
        continue
    elif cur['_mode'] == 'ar': cur['arabic'].append(s)
    elif cur['_mode'] == 'ur': cur['urdu'] += ' ' + s
    elif cur['_mode'] == 'rf': cur['ref'] += ' ' + s
for e in entries:
    e['arabic'] = ' '.join(e['arabic']); del e['_mode']
bad = [e['n'] for e in entries if not (e['arabic'] and e['urdu'] and e['ref'])]
print(len(entries), 'entries; incomplete:', bad)
json.dump(entries, open(out, 'w', encoding='utf-8'), ensure_ascii=False, indent=1)

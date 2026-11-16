import json, sys
raw = json.load(open(sys.argv[1], encoding='utf-8'))
R = lambda a,b: list(range(a,b+1))
cats = [
 ('morning','صبح کے اذکار','🌅', [14,17,25,26,61,109,110,111,112,113,120,146,172]),
 ('evening','شام کے اذکار','🌇', [14,25,26,61,110,111,112,113]),
 ('evil_eye','نظرِ بد سے حفاظت','🧿', [56,59]),
 ('magic_shaitan','شیاطین اور جادو سے حفاظت','🛡️', [41,43,53,58,59,94,98,111,112,121]),
 ('illness','بیماری اور شفا','🌿', [24,25,54,55,56,57,59,88,108,142,143]),
 ('daily','روزمرہ زندگی کی دعائیں','🏠', [39,40,46,49,50,58,71,75,76,77,78,79,87,89,90,91,92,93,94,95,96,97,121,122,135,136,137,138,140,18,147]),
 ('sleep','سونے اور جاگنے کے اذکار','🌙', [42,68,69,70,72,107,156,165]),
 ('salah','نماز سے متعلق اذکار','🕌', R(31,38)+[44,45,73,99,100,106]+R(114,118)+R(123,129)+R(162,164)),
 ('distress','پریشانی، قرض اور مشکلات','🤲', [14,15,19,20,42,62,63,64,65,66,80,81,86,129,132,133,145,155]),
 ('istighfar','استغفار اور توبہ','🕊️', [5,35,45,61,104,105,119,126]),
 ('janaza','جنازہ، قبرستان اور تعزیت','⚰️', R(82,85)+[148,149,150,168]),
]
assigned = set()
out_cats = []
for cid,name,icon,ids in cats:
    ids = sorted(set(i for i in ids if 1 <= i <= 172))
    assigned |= set(ids)
    out_cats.append({'id':cid,'name':name,'icon':icon,'ids':ids})
other = [e['n'] for e in raw if e['n'] not in assigned]
out_cats.append({'id':'other','name':'دیگر مسنون دعائیں','icon':'📿','ids':other})
duas = []
for e in raw:
    cs = [c['id'] for c in out_cats if e['n'] in c['ids']]
    duas.append({'id':e['n'],'title':e['title'],'arabic':e['arabic'],'urdu':e['urdu'],'ref':e['ref'],'cats':cs})
json.dump({'duas':duas,'cats':out_cats}, open('www/data/duas.json','w',encoding='utf-8'), ensure_ascii=False)
# JS version so file:// loading works without fetch
open('www/data/duas.js','w',encoding='utf-8').write('window.DUAS_DATA='+json.dumps({'duas':duas,'cats':out_cats},ensure_ascii=False)+';')
print({c['id']:len(c['ids']) for c in out_cats})

(function(){
'use strict';
var D = window.DUAS_DATA, DUAS = D.duas, CATS = D.cats;
var BYID = {}; DUAS.forEach(function(d){ BYID[d.id]=d; });
var CATBY = {}; CATS.forEach(function(c){ CATBY[c.id]=c; });
var $view = document.getElementById('view'), $title = document.getElementById('pageTitle'),
    $back = document.getElementById('backBtn'), $toast = document.getElementById('toast');

/* ---------- storage (safe) ---------- */
var mem = {};
function load(k, def){ try{ var v = localStorage.getItem('rd_'+k); return v==null?def:JSON.parse(v); }catch(e){ return k in mem?mem[k]:def; } }
function save(k, v){ mem[k]=v; try{ localStorage.setItem('rd_'+k, JSON.stringify(v)); }catch(e){} }
var settings = load('settings', {ar:30, ur:19, theme:'light'});
var favs = load('favs', []);
function today(){ var d=new Date(); return d.getFullYear()+'-'+(d.getMonth()+1)+'-'+d.getDate(); }
function readToday(){ var r = load('read', {}); return r.date===today() ? r.ids : []; }
function setRead(ids){ save('read', {date:today(), ids:ids}); }

function applySettings(){
  var r = document.documentElement;
  r.style.setProperty('--ar-size', settings.ar+'px');
  r.style.setProperty('--ur-size', settings.ur+'px');
  r.setAttribute('data-theme', settings.theme);
}
applySettings();

/* ---------- helpers ---------- */
function esc(s){ return String(s).replace(/[&<>"]/g, function(c){ return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]; }); }
function toast(msg){ $toast.textContent = msg; $toast.classList.add('show'); clearTimeout(toast.t); toast.t=setTimeout(function(){ $toast.classList.remove('show'); },2000); }
var native = window.Android || null;
function nat(fn){ try{ if(native && native[fn]) return native[fn].apply(native, Array.prototype.slice.call(arguments,1)); }catch(e){} }

/* ---------- search normalisation (offline) ---------- */
function norm(s){
  return String(s||'')
    .replace(/[٠-٩]/g, function(c){ return c.charCodeAt(0)-0x660; })
    .replace(/[۰-۹]/g, function(c){ return c.charCodeAt(0)-0x6F0; })
    .replace(/[ً-ٰٟۖ-ۭـ]/g,'')
    .replace(/[آأإٱ]/g,'ا')
    .replace(/[يىےی]/g,'ی')
    .replace(/ك/g,'ک')
    .replace(/[هہھةە]/g,'ہ')
    .replace(/[ؤ]/g,'و').replace(/[ئ]/g,'ی')
    .replace(/[^؀-ۿa-z0-9\s]/gi,' ')
    .replace(/\s+/g,' ').trim().toLowerCase();
}
var INDEX = DUAS.map(function(d){ return {id:d.id, t:norm(d.title), a:norm(d.arabic), u:norm(d.urdu), r:norm(d.ref), all:norm(d.title+' '+d.arabic+' '+d.urdu+' '+d.ref)}; });
function search(q){
  var nq = norm(q); if(!nq) return [];
  var toks = nq.split(' '), seen = {}, out = [];
  var flat = nq.replace(/ /g,'');
  CATS.forEach(function(c){
    if(norm(c.name).replace(/ /g,'').indexOf(flat)!==-1){ c.ids.forEach(function(i){ if(!seen[i]){seen[i]=1; out.push(i);} }); }
  });
  var scored = [];
  INDEX.forEach(function(x){
    if(seen[x.id]) return;
    var ok = toks.every(function(t){ return x.all.indexOf(t)!==-1; });
    if(ok){ var s = toks.reduce(function(a,t){ return a + (x.t.indexOf(t)!==-1?3:0) + (x.a.indexOf(t)!==-1?2:0); },0); scored.push([s,x.id]); }
  });
  scored.sort(function(a,b){ return b[0]-a[0] || a[1]-b[1]; });
  return out.concat(scored.map(function(s){ return s[1]; }));
}
window.__rdSearch = search; // used by automated tests

/* ---------- dua text / actions ---------- */
function duaText(d){ return d.title+'\n\n'+d.arabic+'\n\n'+d.urdu+'\n\nحوالہ: '+d.ref+'\n\n— روحانی ڈھال (مؤلف مفتی محمد سہیل البازی)'; }
function copyText(t){
  if(native && native.copyText){ nat('copyText', t); return true; }
  if(navigator.clipboard && navigator.clipboard.writeText){ navigator.clipboard.writeText(t); return true; }
  var ta=document.createElement('textarea'); ta.value=t; document.body.appendChild(ta); ta.select();
  var ok=false; try{ ok=document.execCommand('copy'); }catch(e){} document.body.removeChild(ta); return ok;
}
function shareText(t){
  if(native && native.shareText){ nat('shareText', t); return; }
  if(navigator.share){ navigator.share({text:t}).catch(function(){}); return; }
  copyText(t); toast('شیئر دستیاب نہیں؛ دعا کاپی کر دی گئی');
}
function isFav(id){ return favs.indexOf(id)!==-1; }
function toggleFav(id){ var i=favs.indexOf(id); if(i===-1) favs.push(id); else favs.splice(i,1); save('favs',favs); return i===-1; }

/* ---------- views ---------- */
var views = 0;
function setTitle(t, showBack){ $title.textContent=t; $back.style.visibility = showBack?'visible':'hidden'; document.title=t; }
function listItems(ids, ctx){
  if(!ids.length) return '<div class="empty">یہاں ابھی کچھ موجود نہیں۔</div>';
  var rd = readToday();
  return ids.map(function(id,i){ var d=BYID[id];
    return '<button class="list-item" data-go="#/dua/'+id+'/'+ctx+'"><span class="num">'+(i+1)+'</span><span>'+esc(d.title)+'</span>'+(rd.indexOf(id)!==-1?'<span class="done">✓</span>':'')+'</button>';
  }).join('');
}
function home(){
  setTitle('روحانی ڈھال', false);
  var tiles = CATS.map(function(c){ return '<button class="tile" data-go="#/cat/'+c.id+'"><span class="ic">'+c.icon+'</span>'+esc(c.name)+'<small>'+c.ids.length+' دعائیں</small></button>'; }).join('');
  $view.innerHTML =
   '<section class="hero"><svg class="logo" viewBox="0 0 120 140"><path d="M60 6 L108 24 V70 C108 100 86 123 60 134 C34 123 12 100 12 70 V24 Z" fill="#0b5d3b" stroke="#e6c872" stroke-width="5"/><path d="M72 38 A26 26 0 1 0 72 92 A20 20 0 1 1 72 38 Z" fill="#e6c872"/></svg><h1>روحانی ڈھال</h1><p>مؤلف مفتی محمد سہیل البازی</p></section>'+
   '<input id="q" class="searchbar" type="search" placeholder="🔍 دعا تلاش کریں (عنوان، عربی، اردو، موضوع، حوالہ)" enterkeyhint="search">'+
   '<div class="grid"><button class="tile wide" data-go="#/daily"><span class="ic">✅</span>آج کے اذکار<small>روزانہ چیک لسٹ</small></button>'+
   '<button class="tile" data-go="#/favs"><span class="ic">⭐</span>پسندیدہ دعائیں<small>'+favs.length+' محفوظ</small></button>'+
   '<button class="tile" data-go="#/cat/all"><span class="ic">📖</span>تمام دعائیں<small>'+DUAS.length+' دعائیں</small></button>'+tiles+'</div>'+
   '<p class="note" style="text-align:center">ایپ مکمل آف لائن ہے۔ انٹرنیٹ کی ضرورت نہیں۔</p>';
  var q=document.getElementById('q');
  q.addEventListener('keydown', function(e){ if(e.key==='Enter' && q.value.trim()){ location.hash='#/search/'+encodeURIComponent(q.value.trim()); } });
}
function catView(id){
  var ids, name;
  if(id==='all'){ ids = DUAS.map(function(d){return d.id;}); name='تمام دعائیں'; }
  else { var c=CATBY[id]; if(!c){ location.hash='#/home'; return; } ids=c.ids; name=c.name; }
  setTitle(name, true); $view.innerHTML = listItems(ids, id);
}
function searchView(q){
  setTitle('تلاش', true);
  var ids = search(q);
  $view.innerHTML = '<input id="q2" class="searchbar" type="search" value="'+esc(q)+'" enterkeyhint="search"><p class="note">'+ids.length+' نتائج</p>'+
    (ids.length? listItems(ids,'s:'+encodeURIComponent(q)) : '<div class="empty">کوئی دعا نہیں ملی۔ کسی اور لفظ سے تلاش کریں۔</div>');
  var q2=document.getElementById('q2'); q2.addEventListener('keydown', function(e){ if(e.key==='Enter' && q2.value.trim()){ location.replace('#/search/'+encodeURIComponent(q2.value.trim())); } });
}
function favsView(){ setTitle('پسندیدہ دعائیں', true);
  $view.innerHTML = favs.length? listItems(favs,'favs') : '<div class="empty">ابھی کوئی پسندیدہ دعا محفوظ نہیں۔<br>کسی دعا کی اسکرین پر «پسندیدہ میں شامل کریں» دبائیں۔</div>'; }
function dailyIds(){ var s={}, out=[]; CATBY.morning.ids.concat(CATBY.evening.ids).forEach(function(i){ if(!s[i]){s[i]=1; out.push(i);} }); return out; }
function dailyView(){
  setTitle('آج کے اذکار', true);
  var m=CATBY.morning.ids, e=CATBY.evening.ids, rd=readToday(), all=dailyIds();
  var n = all.filter(function(i){ return rd.indexOf(i)!==-1; }).length;
  $view.innerHTML = '<p>آج مکمل: <b>'+n+' / '+all.length+'</b></p><div class="progress"><i style="width:'+Math.round(100*n/all.length)+'%"></i></div>'+
    '<h3 class="section-title">صبح کے اذکار</h3>'+listItems(m,'daily')+
    '<h3 class="section-title">شام کے اذکار</h3>'+listItems(e,'daily')+
    '<button class="btn" id="resetRead" style="width:100%;margin-top:10px">آج کی چیک لسٹ نئی کریں</button>';
  document.getElementById('resetRead').onclick=function(){ setRead([]); dailyView(); };
}
function ctxIds(ctx){
  if(ctx==='favs') return favs.slice();
  if(ctx==='daily') return dailyIds();
  if(ctx==='all') return DUAS.map(function(d){return d.id;});
  if(ctx && ctx.indexOf('s:')===0) return search(decodeURIComponent(ctx.slice(2)));
  if(CATBY[ctx]) return CATBY[ctx].ids;
  return DUAS.map(function(d){return d.id;});
}
function duaView(id, ctx){
  var d = BYID[id]; if(!d){ location.hash='#/home'; return; }
  var ids = ctxIds(ctx), pos = ids.indexOf(id), rd = readToday();
  setTitle('دعا', true); views++;
  var prev = pos>0 ? ids[pos-1] : null, next = pos>=0 && pos<ids.length-1 ? ids[pos+1] : null;
  var meta = '<div class="meta"><div><b>حوالہ:</b> '+esc(d.ref)+'</div><div><b>تحقیق:</b> مؤلف کی تحقیق کے مطابق</div></div>';
  $view.innerHTML =
   '<article class="dua"><h3>'+esc(d.title)+'</h3>'+
   '<div class="arabic" lang="ar" dir="rtl">'+esc(d.arabic)+'</div>'+
   '<div class="urdu" lang="ur" dir="rtl">'+esc(d.urdu)+'</div>'+meta+
   '<div class="actions">'+
   '<button class="btn primary" id="bCopy">📋 کاپی دعا</button><button class="btn primary" id="bShare">📤 شیئر دعا</button>'+
   '<button class="btn'+(isFav(id)?' on':'')+'" id="bFav">'+(isFav(id)?'★ پسندیدہ میں ہے':'☆ پسندیدہ میں شامل کریں')+'</button>'+
   '<button class="btn'+(rd.indexOf(id)!==-1?' on':'')+'" id="bRead">'+(rd.indexOf(id)!==-1?'✓ آج پڑھ لی':'آج پڑھ لی؟ نشان لگائیں')+'</button></div></article>'+
   '<div class="nav"><button class="btn" id="bNext"'+(next?'':' disabled')+'>‹ اگلی دعا</button><span class="count">'+(pos>=0?(pos+1)+' / '+ids.length:'')+'</span><button class="btn" id="bPrev"'+(prev?'':' disabled')+'>پچھلی دعا ›</button></div>';
  document.getElementById('bCopy').onclick=function(){ copyText(duaText(d)); toast('دعا کامیابی سے کاپی ہوگئی۔'); };
  document.getElementById('bShare').onclick=function(){ shareText(duaText(d)); };
  document.getElementById('bFav').onclick=function(){ var on=toggleFav(id); this.className='btn'+(on?' on':''); this.textContent=on?'★ پسندیدہ میں ہے':'☆ پسندیدہ میں شامل کریں'; toast(on?'پسندیدہ میں شامل ہو گئی':'پسندیدہ سے ہٹا دی گئی'); };
  document.getElementById('bRead').onclick=function(){ var r=readToday().slice(), i=r.indexOf(id); if(i===-1) r.push(id); else r.splice(i,1); setRead(r); var on=i===-1; this.className='btn'+(on?' on':''); this.textContent=on?'✓ آج پڑھ لی':'آج پڑھ لی؟ نشان لگائیں'; };
  if(next) document.getElementById('bNext').onclick=function(){ location.replace('#/dua/'+next+'/'+ctx); };
  if(prev) document.getElementById('bPrev').onclick=function(){ location.replace('#/dua/'+prev+'/'+ctx); };
}
function settingsView(){
  setTitle('ترتیبات', true);
  $view.innerHTML =
   '<div class="row"><span>عربی متن کا سائز</span><input type="range" id="sAr" min="22" max="52" value="'+settings.ar+'"></div>'+
   '<div class="row"><span>اردو ترجمے کا سائز</span><input type="range" id="sUr" min="15" max="32" value="'+settings.ur+'"></div>'+
   '<div class="row"><span>ڈارک موڈ</span><button class="btn" id="sTheme" style="min-width:110px">'+(settings.theme==='dark'?'آن ہے':'بند ہے')+'</button></div>'+
   '<div class="dua" style="margin-top:14px"><div class="arabic">اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى</div><div class="urdu">نمونہ متن: اے اللہ! میں تجھ سے ہدایت اور تقویٰ مانگتا ہوں۔</div></div>'+
   '<button class="btn" id="sAbout" style="width:100%;margin-top:16px">ایپ کے بارے میں</button>';
  document.getElementById('sAr').oninput=function(){ settings.ar=+this.value; save('settings',settings); applySettings(); };
  document.getElementById('sUr').oninput=function(){ settings.ur=+this.value; save('settings',settings); applySettings(); };
  document.getElementById('sTheme').onclick=function(){ settings.theme=settings.theme==='dark'?'light':'dark'; save('settings',settings); applySettings(); this.textContent=settings.theme==='dark'?'آن ہے':'بند ہے'; };
  document.getElementById('sAbout').onclick=function(){ location.hash='#/about'; };
}
function aboutView(){
  setTitle('تعارف', true);
  $view.innerHTML = '<div class="dua"><h3>روحانی ڈھال</h3><div class="urdu">مؤلف مفتی محمد سہیل البازی<br><br>یہ ایپ قرآن و حدیث سے ثابت شدہ دعاؤں اور اذکار کا آف لائن مجموعہ ہے۔ ہر دعا کے ساتھ عربی متن، اردو ترجمہ اور حوالہ موجود ہے۔ ایپ کے بنیادی استعمال کے لیے انٹرنیٹ، سرور یا اکاؤنٹ کی ضرورت نہیں۔ پسندیدہ دعائیں اور روزانہ کی چیک لسٹ صرف آپ کے فون میں محفوظ رہتی ہیں۔<br><br>اشتہارات ایپ کی اسکرین کے نچلے حصے میں الگ دکھائے جاتے ہیں اور دعا یا حدیث کا حصہ نہیں ہیں۔</div><div class="meta">ورژن 1.0.0 · '+DUAS.length+' دعائیں</div></div>';
}

/* ---------- router ---------- */
var lastKind = 'home';
function route(){
  var h = location.hash || '#/home', p = h.slice(2).split('/');
  var kind = p[0] || 'home';
  // limited interstitial opportunity: only when returning to a list/home page, never on a dua screen
  if(kind!=='dua' && lastKind==='dua' && views>=8){ views=0; nat('maybeShowInterstitial'); }
  lastKind = kind;
  window.scrollTo(0,0);
  if(kind==='home') home();
  else if(kind==='cat') catView(p[1]);
  else if(kind==='dua') duaView(+p[1], p.slice(2).join('/'));
  else if(kind==='search') searchView(decodeURIComponent(p[1]||''));
  else if(kind==='favs') favsView();
  else if(kind==='daily') dailyView();
  else if(kind==='settings') settingsView();
  else if(kind==='about') aboutView();
  else home();
}
document.addEventListener('click', function(e){ var t=e.target.closest('[data-go]'); if(t){ location.hash=t.getAttribute('data-go'); } });
$back.addEventListener('click', function(){ if(history.length>1) history.back(); else location.hash='#/home'; });
document.getElementById('settingsBtn').addEventListener('click', function(){ location.hash='#/settings'; });
window.addEventListener('hashchange', route);
window.__rdBack = function(){ if((location.hash||'#/home')==='#/home' || location.hash==='') return false; history.back(); return true; };
route();
setTimeout(function(){ var s=document.getElementById('splash'); s.classList.add('hide'); setTimeout(function(){ s.remove(); },450); }, 1400);
})();

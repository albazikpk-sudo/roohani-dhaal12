import os, math
from PIL import Image, ImageDraw, ImageFont
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GREEN=(11,93,59); GREEN2=(15,122,78); GOLD=(230,200,114); GOLD2=(201,162,74)
FONT_B='/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf'
def shield_icon(size, with_text=False, round_mask=False, bg=True):
    S=size*4
    im=Image.new('RGBA',(S,S),(0,0,0,0)); d=ImageDraw.Draw(im)
    if bg:
        if round_mask: d.ellipse((0,0,S-1,S-1),fill=GREEN)
        else: d.rounded_rectangle((0,0,S-1,S-1),radius=int(S*0.22),fill=GREEN)
    cx=S/2; top=S*(0.10 if with_text else 0.2); h=S*(0.58 if with_text else 0.6); w=S*(0.44 if with_text else 0.42)
    pts=[]
    pts+= [(cx,top),(cx+w/2,top+h*0.1),(cx+w/2,top+h*0.5)]
    for t in range(0,21):
        a=t/20; pts.append((cx+w/2*(1-a)**1.0*math.cos(0)+0, top+h*0.5+h*0.5*math.sin(a*math.pi/2)*1.0 - 0))
    # simple shield polygon
    poly=[(cx,top),(cx+w/2,top+h*0.12),(cx+w/2,top+h*0.55),(cx+w*0.3,top+h*0.85),(cx,top+h),(cx-w*0.3,top+h*0.85),(cx-w/2,top+h*0.55),(cx-w/2,top+h*0.12)]
    d.polygon(poly,fill=GREEN2,outline=GOLD)
    d.line(poly+[poly[0]],fill=GOLD,width=max(2,int(S*0.012)),joint='curve')
    # crescent
    r=w*0.28; ccx=cx-w*0.04; ccy=top+h*0.42
    d.ellipse((ccx-r,ccy-r,ccx+r,ccy+r),fill=GOLD)
    d.ellipse((ccx-r*0.72+r*0.38,ccy-r*0.82,ccx+r*1.0+r*0.38,ccy+r*0.82),fill=GREEN2)
    # star
    sx=cx+w*0.2; sy=ccy-r*0.05; R=r*0.34
    st=[]
    for i in range(10):
        ang=-math.pi/2+i*math.pi/5; rr=R if i%2==0 else R*0.42
        st.append((sx+rr*math.cos(ang),sy+rr*math.sin(ang)))
    d.polygon(st,fill=GOLD)
    if with_text:
        f=ImageFont.truetype(FONT_B,int(S*0.14))
        txt='روحانی ڈھال'
        tw=d.textlength(txt,font=f,features=['-kern'],direction='rtl',language='ur')
        d.text((cx-tw/2,S*0.72),txt,font=f,fill=GOLD,direction='rtl',language='ur')
    return im.resize((size,size),Image.LANCZOS)

res=os.path.join(ROOT,'android/app/src/main/res')
dens={'mdpi':48,'hdpi':72,'xhdpi':96,'xxhdpi':144,'xxxhdpi':192}
for k,px in dens.items():  # legacy (API 24-25) launcher icons; API 26+ uses the adaptive vector
    os.makedirs(f'{res}/mipmap-{k}',exist_ok=True)
    shield_icon(px).save(f'{res}/mipmap-{k}/ic_launcher.png')
    shield_icon(px,round_mask=True).save(f'{res}/mipmap-{k}/ic_launcher_round.png')
os.makedirs(f'{ROOT}/store-assets',exist_ok=True)
shield_icon(512,with_text=True).save(f'{ROOT}/store-assets/play_store_icon_512.png')
os.makedirs(f'{ROOT}/www/img',exist_ok=True)
shield_icon(512,with_text=True).save(f'{ROOT}/www/img/logo.png')
# Splash artwork (1080x1920)
sp=Image.new('RGB',(1080,1920),GREEN); lg=shield_icon(520,with_text=False,bg=False); sp.paste(lg,(280,520),lg)
d=ImageDraw.Draw(sp)
f1=ImageFont.truetype(FONT_B,130); f2=ImageFont.truetype(FONT_B,56)
for txt,f,y,col in (('روحانی ڈھال',f1,1100,GOLD),('مؤلف مفتی محمد سہیل البازی',f2,1290,(243,231,192))):
    tw=d.textlength(txt,font=f,direction='rtl',language='ur'); d.text(((1080-tw)/2,y),txt,font=f,fill=col,direction='rtl',language='ur')
sp.save(f'{ROOT}/store-assets/splash_1080x1920.png')
print('icons ok')

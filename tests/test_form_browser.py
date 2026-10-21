#!/usr/bin/env python3
"""Real browser smoke checks for the offline Angel Life form (synthetic only)."""
import os
import re
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BROWSER = next((p for p in (
    "/usr/bin/google-chrome", "/usr/bin/google-chrome-stable",
    "/usr/bin/chromium", "/usr/bin/chromium-browser"
) if Path(p).exists()), None)
if not BROWSER:
    from shutil import which
    BROWSER = which("google-chrome") or which("chromium")
if not BROWSER:
    raise RuntimeError("Chromium/Google Chrome is required for browser smoke tests.")

html = (ROOT / "index.html").read_text(encoding="utf-8")
assert html.count("</body>") == 1
smoke_js = r"""
<script>
window.addEventListener('load', function () {
  var failures=[];
  var ok=function(cond,message){if(!cond)failures.push(message);};
  try {
    var $=function(s){return document.querySelector(s)};
    var $$=function(s){return Array.from(document.querySelectorAll(s))};
    ok(document.documentElement.getAttribute('data-angel-v4-ready')==='yes','redesign boot');
    ok($$('.sign-card canvas').length===11,'all signatures present');
    ok($$('.sign-card .print-signature').length===11,'all print signatures present');
    ok($$('.signature-divider').length===3,'incident signer sections distinct');
    ok($$('input[name="sexo-paciente"]').length===2,'sex F/M');
    ok($('#vitals .bp input[type="number"]')!==null,'blood pressure split');
    ok($('#vitals .bp input[type="number"]').length===undefined,'BP element');
    ok($$('.bp input').length===2,'both blood pressure entries');
    ok($('input[inputmode="numeric"][type="tel"]')!==null,'doctor number phone keypad');
    var plan=$('.plan-selection');
    ok(!!plan,'plan selector');
    var plans=plan?Array.from(plan.options).map(function(x){return x.text;}):[];
    ['Vital · First Medical','Vital · MMM Multi Health','Vital · Plan de Salud Menonita',
     'Vital · Triple-S Salud','MCS Life / MCS Clásico',
     'Compromiso Doctor Center — Arecibo','Otro (escribir)','Plan privado (escribir)']
    .forEach(function(s){ok(plans.includes(s),'plan: '+s);});
    if(plan){
      plan.value='Otro (escribir)';
      plan.dispatchEvent(new Event('change',{bubbles:true}));
      ok(!$('#plan-manual-wrap').hidden,'manual plan appears');
      var manual=$('#plan-manual');
      manual.value='Plan de prueba sintético';
      manual.dispatchEvent(new Event('input',{bubbles:true}));
      ok(manual.nextElementSibling.textContent==='Plan de prueba sintético','print mirror');
    }
    var categories=$$('#assessment .choice span').map(function(e){return e.textContent;});
    ['Alerta','Orientado','Sibilancias','Anisocoria','Blando','Ritmo cardíaco',
     'ACAA','ATH Móvil','Cash / Efectivo','CPR / RCP',
     'Ayuda obstétrica','Tratamiento en','Ambulancia']
      .forEach(function(s){
        ok(categories.includes(s) || $$('#assessment h3').some(function(e){return e.textContent===s}),
           'option: '+s);
      });
    var lic=$('input[aria-label="Número de licencia del médico"]');
    ok(!!lic,'medical license field');
    var names=$$('#incident-signatures h3').map(function(e){return e.textContent});
    ok(names.includes('Paciente no acepta servicios'),'service refusal clear');
    ok(names.includes('Paciente no acepta transportación'),'transport refusal clear');
    var first=$('.sign-card canvas');
    if(first){
      var rect=first.getBoundingClientRect();
      var props={pointerId:19,pointerType:'touch',isPrimary:true,bubbles:true,clientX:rect.x+15,clientY:rect.y+15};
      first.dispatchEvent(new PointerEvent('pointerdown',props));
      first.dispatchEvent(new PointerEvent('pointermove',Object.assign({},props,{clientX:rect.x+80,clientY:rect.y+40})));
      first.dispatchEvent(new PointerEvent('pointerup',props));
      ok(first.parentElement.querySelector('.print-signature').getAttribute('src').startsWith('data:image/png'),'ink recorded in print image');
      first.parentElement.querySelector('[data-clear-signature]').click();
      ok(first.parentElement.querySelector('.print-signature').getAttribute('src')==='', 'erase clears ink');
    }
    var hipaa=$('[data-tab="hipaa"]');hipaa.click();
    ok(!$('#hipaa').hidden && $('#incidente').hidden,'tabs switch visibility');
    $('[data-tab="incidente"]').click();
    ok(!$('#incidente').hidden && $('#hipaa').hidden,'incident can be reopened');
    window.dispatchEvent(new Event('beforeprint'));
    ok($$('.print-value').length>60,'printable values mirrored');
    ok($$('.choice input').length>50,'options rendered, not collapsed into single text');
  } catch(e) {
    failures.push('uncaught: '+String(e)+' '+(e.stack||''));
  }
  var p=document.createElement('pre');
  p.id='angel-browser-smoke';
  p.textContent=failures.length?'SMOKE_FAIL: '+failures.join(' | '):'SMOKE_PASS';
  document.body.appendChild(p);
});
</script>
"""
with tempfile.TemporaryDirectory(prefix="angel-browser-") as directory:
    p=Path(directory)/"index.html"
    # Keep assets accessible to the test page where possible; no PHI used.
    p.write_text(html.replace("</body>", smoke_js+"</body>"), encoding="utf-8")
    cmd=[BROWSER,"--headless","--no-sandbox","--disable-gpu",
         "--disable-dev-shm-usage","--virtual-time-budget=4000",
         "--dump-dom",p.as_uri()]
    result=subprocess.run(cmd, text=True, capture_output=True, timeout=70)
    match=re.search(r'<pre id="angel-browser-smoke">([^<]+)</pre>',result.stdout)
    if result.returncode or not match or "SMOKE_PASS" not in match.group(1):
        print("BROWSER SMOKE FAILURE")
        print("status:",result.returncode)
        print("test:",match.group(1) if match else "test harness did not render")
        print("stderr:",result.stderr[-2500:])
        sys.exit(1)
    print(match.group(1))
    # Also exercise Chrome's real PDF rendering path with synthetic form data.
    pdf=Path(directory)/"printed-incident.pdf"
    pdf_cmd=[BROWSER,"--headless","--no-sandbox","--disable-gpu",
             "--disable-dev-shm-usage","--virtual-time-budget=3500",
             "--no-pdf-header-footer","--print-to-pdf="+str(pdf),p.as_uri()]
    printed=subprocess.run(pdf_cmd,text=True,capture_output=True,timeout=80)
    if printed.returncode or not pdf.exists() or pdf.stat().st_size<12000 or pdf.read_bytes()[:4]!=b"%PDF":
        print("PDF_RENDER_FAIL",printed.returncode,printed.stderr[-1500:])
        sys.exit(1)
    print("PDF_RENDER_PASS",pdf.stat().st_size,"bytes")

#!/usr/bin/env python3
"""Runs a command, captures its REAL output, saves it as <out>.txt and renders a terminal-style screenshot <out>.svg (text-based, viewable on GitHub).
Usage: render_svg.py <workdir> <out-basename> <command...>"""
import subprocess, sys, os, html, time
workdir, out, cmd = sys.argv[1], sys.argv[2], " ".join(sys.argv[3:])
env = dict(os.environ); env.pop("JAVA_TOOL_OPTIONS", None)
t0 = time.time()
res = subprocess.run(cmd, shell=True, cwd=workdir, capture_output=True, text=True, env=env)
text = (res.stdout + res.stderr).rstrip("\n")
open(out + ".txt", "w").write(text + "\n")
lines = ["$ " + cmd] + text.split("\n")
cw, lh, pad, bar = 8.4, 19, 16, 32
w = int(max(len(l) for l in lines) * cw + pad * 2 + 4); w = max(w, 560)
h = bar + pad * 2 + lh * len(lines)
o = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{w}" height="{h}" viewBox="0 0 {w} {h}" font-family="DejaVu Sans Mono, Consolas, monospace" font-size="14">',
     f'<rect width="{w}" height="{h}" rx="8" fill="#1e1e1e"/><rect width="{w}" height="{bar}" rx="8" fill="#3a3a3a"/>',
     '<circle cx="18" cy="16" r="6" fill="#ff5f56"/><circle cx="40" cy="16" r="6" fill="#ffbd2e"/><circle cx="62" cy="16" r="6" fill="#27c93f"/>',
     f'<text x="84" y="21" fill="#cccccc" font-size="13">Terminal - {html.escape(os.path.basename(os.path.abspath(workdir)))}</text>']
y = bar + pad + 13
for i, l in enumerate(lines):
    col = "#9cdcfe" if i == 0 else ("#6a9955" if l.rstrip().endswith(("PASS", "MATCH")) else ("#f48771" if l.rstrip().endswith("DIFFERENT") or "Exception" in l else "#d4d4d4"))
    wt = ' font-weight="bold"' if i == 0 else ""
    o.append(f'<text x="{pad}" y="{y}" fill="{col}" xml:space="preserve"{wt}>{html.escape(l)}</text>'); y += lh
o.append("</svg>")
open(out + ".svg", "w").write("\n".join(o))
print(f"{out}.svg  exit={res.returncode}  {time.time()-t0:.1f}s  lines={len(lines)}")

"""
Run this ONCE to download all 151 Pokemon sprites.
Usage:  python download_sprites.py
"""

import os, time, socket, struct, zlib, sys

# ── Pure SSL bypass using http.client directly ────────────────────────────────
import http.client, ssl

SAVE_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                        "src", "main", "resources", "sprites")
os.makedirs(SAVE_DIR, exist_ok=True)

# Completely unverified SSL context
ctx = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
ctx.check_hostname = False
ctx.verify_mode    = ssl.CERT_NONE

HOSTS = [
    ("raw.githubusercontent.com",
     "/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/{id}.png"),
    ("raw.githubusercontent.com",
     "/PokeAPI/sprites/master/sprites/pokemon/{id}.png"),
]

def fetch(host, path):
    try:
        conn = http.client.HTTPSConnection(host, 443, context=ctx, timeout=12)
        conn.request("GET", path, headers={"User-Agent": "Mozilla/5.0", "Host": host})
        r = conn.getresponse()
        if r.status == 200:
            data = r.read()
            conn.close()
            return data if len(data) > 500 else None
        conn.close()
        return None
    except Exception as e:
        return None

total, ok, fail = 151, 0, []

print(f"Saving to: {SAVE_DIR}\n")

for pid in range(1, total + 1):
    dest = os.path.join(SAVE_DIR, f"{pid}.png")
    if os.path.exists(dest) and os.path.getsize(dest) > 500:
        print(f"  skip #{pid:3d} (cached)")
        ok += 1
        continue

    data = None
    for host, path_tpl in HOSTS:
        data = fetch(host, path_tpl.format(id=pid))
        if data:
            break

    if data:
        with open(dest, "wb") as f:
            f.write(data)
        print(f"  ✓ #{pid:3d}  ({len(data)//1024} KB)")
        ok += 1
    else:
        print(f"  ✗ #{pid:3d}  FAILED")
        fail.append(pid)

    time.sleep(0.04)

print(f"\nDone: {ok}/{total} sprites downloaded to:\n  {SAVE_DIR}")
if fail:
    print(f"Failed: {fail}")
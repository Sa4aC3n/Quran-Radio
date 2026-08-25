with open('main.js', 'r', encoding='utf-8') as f:
    js = f.read()

import re

# Find occurrences of http requests or api
matches = re.findall(r'(https?://[^\s"\'`<>]+)', js)
print("URLs found in main.js:")
for m in sorted(set(matches)):
    print("  ", m)

# Find all occurrences of api paths like /radios, /categories, /reciters, etc.
paths = re.findall(r'[\'"`](/[a-zA-Z0-9_\-/\.]+[\'"`])', js)
print("\nPaths found:")
for p in sorted(set(paths)):
    if any(k in p for k in ['api', 'radio', 'library', 'station', 'category', 'reciter', 'read', 'popular']):
        print("  ", p)

# Search surrounding code for https://api.mp3quran.net/api_2
for idx in [m.start() for m in re.finditer('api_2', js)]:
    print("\n--- Context around api_2 ---")
    print(js[max(0, idx-200):min(len(js), idx+500)])

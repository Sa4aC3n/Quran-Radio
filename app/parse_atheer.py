import re, urllib.request, json

with open('main.js', 'r', encoding='utf-8') as f:
    js = f.read()

print("Analyzing main.js for API and routes...")
# Find all occurrences of api
for m in re.finditer(r'api\.mp3quran\.net', js):
    start = max(0, m.start() - 150)
    end = min(len(js), m.end() + 250)
    print("MATCH:\n", js[start:end])
    print("=" * 50)

# Check all string literals ending in api endpoints
endpoints = re.findall(r'https?://[a-zA-Z0-9_\-\.\:\/]+', js)
for ep in sorted(set(endpoints)):
    print("Found URL:", ep)

# Check how atheer loads radios, categories, and library
keywords = ['categories', 'radios', 'recent', 'featured', 'library', 'station', 'reciters']
for kw in keywords:
    pos = [m.start() for m in re.finditer(kw, js)]
    print(f"Occurrences of '{kw}': {len(pos)}")
    for p in pos[:3]:
        print(f"  [{kw}] {js[max(0, p-50):min(len(js), p+150)]}")

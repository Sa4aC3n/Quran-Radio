import urllib.request, json

headers = {'User-Agent': 'Mozilla/5.0'}

# 1. Fetch categories
cats_url = 'https://api.mp3quran.net/api_2/atheer/radios_cats?language=ar'
print("Fetching categories from:", cats_url)
req = urllib.request.Request(cats_url, headers=headers)
with urllib.request.urlopen(req, timeout=10) as resp:
    cats_data = json.loads(resp.read().decode('utf-8'))
    print("Categories response keys:", cats_data.keys() if isinstance(cats_data, dict) else "List")
    reads = cats_data.get('reads', cats_data.get('categories', []))
    print(f"Total categories: {len(reads)}")
    for c in reads:
        print("Category:", c)

# 2. For each category, fetch radios
all_radios = []
for c in reads:
    cat_id = c.get('id')
    cat_name = c.get('name')
    r_url = f"https://api.mp3quran.net/api_2/atheer/radios?radio_cat={cat_id}&language=ar"
    print(f"\nFetching radios for Category {cat_id} ({cat_name})...")
    try:
        req = urllib.request.Request(r_url, headers=headers)
        with urllib.request.urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            radios = data.get('radios', data.get('reads', []))
            print(f"  Found {len(radios)} radios in {cat_name}")
            for r in radios:
                r['category_id'] = cat_id
                r['category_name'] = cat_name
                all_radios.append(r)
                if len(all_radios) <= 3:
                    print("  Sample radio:", r)
    except Exception as e:
        print("  Error fetching cat:", cat_id, e)

print(f"\n======================================")
print(f"TOTAL ATHEER RADIOS EXTRACTED: {len(all_radios)}")

with open('atheer_raw_radios.json', 'w', encoding='utf-8') as f:
    json.dump(all_radios, f, ensure_ascii=False, indent=2)
print("Saved atheer_raw_radios.json")

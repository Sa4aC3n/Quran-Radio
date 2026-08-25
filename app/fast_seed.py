import json, subprocess, concurrent.futures

with open('atheer_raw_radios.json', 'r', encoding='utf-8') as f:
    radios = json.load(f)

print(f"Total radios: {len(radios)}")

def curl_check(url):
    if not url or not url.startswith('http'):
        return False, 0
    cmd = ['curl', '-I', '-s', '-k', '--max-time', '3', '-A', 'Mozilla/5.0', url]
    try:
        res = subprocess.run(cmd, capture_output=True, text=True, timeout=4)
        out = res.stdout
        if 'HTTP/' in out:
            first_line = out.splitlines()[0]
            parts = first_line.split()
            if len(parts) >= 2 and parts[1].isdigit():
                code = int(parts[1])
                return code in [200, 206, 301, 302, 307, 308, 400, 401], code
        return False, 0
    except Exception:
        return False, 0

def map_category(cat_id, cat_name):
    c = cat_name.strip()
    if cat_id == 1 or "قراء" in c:
        return "RECITER", "محطات القراء"
    elif cat_id == 2 or "تفسير" in c:
        return "TAFSIR", "التفسير وعلوم القرآن"
    elif cat_id == 3 or "ترجمة" in c:
        return "ISLAMIC", "ترجمة معاني القرآن الكريم"
    elif cat_id == 4 or "سيرة" in c or "قصص" in c:
        return "ISLAMIC", "السيرة النبوية والقصص"
    elif cat_id == 5 or "تلاوات" in c or "خاشع" in c:
        return "RECITATION", "تلاوات متميزة وخاشعة"
    elif cat_id == 6 or "رقية" in c:
        return "ISLAMIC", "الرقية الشرعية"
    elif cat_id == 7 or "فتاوى" in c:
        return "ISLAMIC", "الفتاوى والدروس"
    elif cat_id == 8 or "أذكار" in c or "أدعية" in c:
        return "ADHKAR", "الأذكار والأدعية"
    elif cat_id == 10 or "عشر" in c:
        return "QURAN", "القراءات العشر"
    elif cat_id == 11 or "مواسم" in c or "رمضان" in c or "حج" in c:
        return "ISLAMIC", "مواسم الخير"
    elif cat_id == 12 or "سنة" in c or "حديث" in c:
        return "SUNNAH", "السنة النبوية والأحاديث"
    elif cat_id == 13 or "سعودية" in c or "إذاعة القرآن الكريم" in c:
        return "OFFICIAL", "إذاعات رسمية"
    return "QURAN", "إذاعات القرآن الكريم"

def process(r):
    raw_id = r.get('id')
    raw_url = r.get('URL', '').strip()
    name = r.get('name', '').strip()
    cat_name = r.get('category_name', '')
    cat_id = r.get('category_id')

    primary_url = raw_url
    backup_url = None
    if 'backup.qurango.net' in raw_url:
        primary_url = raw_url.replace('backup.qurango.net', 'qurango.net')
        backup_url = raw_url
    elif 'qurango.net' in raw_url:
        primary_url = raw_url
        backup_url = raw_url.replace('qurango.net', 'backup.qurango.net')

    p_ok, p_code = curl_check(primary_url)
    b_ok, b_code = False, 0
    if backup_url:
        b_ok, b_code = curl_check(backup_url)

    is_verified = p_ok or b_ok
    best_stream = primary_url if p_ok else (backup_url if b_ok else primary_url)
    fallback_stream = backup_url if (p_ok and b_ok) else (primary_url if (not p_ok and b_ok) else None)

    category_enum, category_title = map_category(cat_id, cat_name)
    reciter_name = name if category_enum == "RECITER" else None

    return {
        "id": f"atheer_{raw_id}",
        "atheerRawId": raw_id,
        "name": name,
        "nameAr": name,
        "nameEn": "",
        "country": "المملكة العربية السعودية" if ("السعودية" in name or cat_id == 13) else "العالم الإسلامي",
        "countryCode": "SA" if ("السعودية" in name or cat_id == 13) else "GLOBAL",
        "city": "",
        "category": category_enum,
        "categoryTitle": category_title,
        "reciter": reciter_name,
        "reciterId": f"reciter_{raw_id}" if reciter_name else None,
        "streamUrl": best_stream,
        "fallbackStreamUrl": fallback_stream,
        "streams": [
            {
                "id": f"atheer_{raw_id}_primary",
                "stationId": f"atheer_{raw_id}",
                "url": primary_url,
                "format": "MP3",
                "priority": 1,
                "isPrimary": True,
                "isBackup": False,
                "isActive": p_ok,
                "status": "ONLINE" if p_ok else "OFFLINE",
                "healthScore": 100 if p_ok else 40
            }
        ] + ([
            {
                "id": f"atheer_{raw_id}_backup",
                "stationId": f"atheer_{raw_id}",
                "url": backup_url,
                "format": "MP3",
                "priority": 2,
                "isPrimary": False,
                "isBackup": True,
                "isActive": b_ok,
                "status": "ONLINE" if b_ok else "OFFLINE",
                "healthScore": 95 if b_ok else 30
            }
        ] if backup_url else []),
        "sourceName": "Atheer Radio",
        "sourceWebsite": "https://www.atheer-radio.com/library",
        "sourceUrl": f"https://www.atheer-radio.com/library/{cat_id}",
        "logoUrl": f"https://www.atheer-radio.com/assets/imgs/cat{cat_id}.png" if cat_id else "",
        "description": f"بث مباشر لـ {name} عبر شبكة إذاعات أثير راديو.",
        "isVerified": is_verified,
        "streamStatus": "ONLINE" if is_verified else "OFFLINE",
        "bitrate": "128 kbps",
        "isFeatured": cat_id in [1, 2, 5, 13] and raw_id in [1, 2, 3, 4, 5, 116, 10904, 109082],
        "priority": 50
    }

results = []
with concurrent.futures.ThreadPoolExecutor(max_workers=30) as executor:
    results = list(executor.map(process, radios))

results.sort(key=lambda x: x.get('atheerRawId', 0))
verified_count = sum(1 for r in results if r['isVerified'])
print(f"DONE in seconds! Total: {len(results)}, Verified: {verified_count}")

with open('atheer_stations_seed.json', 'w', encoding='utf-8') as f:
    json.dump(results, f, ensure_ascii=False, indent=2)

print("Saved atheer_stations_seed.json")

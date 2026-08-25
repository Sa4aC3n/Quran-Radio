import urllib.request, json, concurrent.futures, socket

socket.setdefaulttimeout(3.0)

with open('atheer_raw_radios.json', 'r', encoding='utf-8') as f:
    radios = json.load(f)

print(f"Loaded {len(radios)} radios")

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Range': 'bytes=0-10'
}

def verify_radio(r):
    url = r.get('URL', '').strip()
    name = r.get('name', '').strip()
    cat_name = r.get('category_name', '')
    cat_id = r.get('category_id')
    raw_id = r.get('id')
    
    is_ok = False
    status_code = 0
    content_type = ''
    final_url = url
    err = None

    try:
        req = urllib.request.Request(url, headers=headers)
        # Use urlopen and only read 10 bytes then close
        with urllib.request.urlopen(req, timeout=3.0) as resp:
            status_code = resp.status
            content_type = resp.headers.get('Content-Type', '')
            final_url = resp.geturl()
            _ = resp.read(10) # read 10 bytes only
            is_ok = True
    except urllib.error.HTTPError as he:
        # HTTP 200, 206, 302, etc. are ok
        if he.code in [200, 206, 301, 302, 307, 308]:
            is_ok = True
            status_code = he.code
        else:
            status_code = he.code
            err = f"HTTP {he.code}"
    except Exception as e:
        err = str(e)

    return {
        'id': f"atheer_{raw_id}",
        'rawId': raw_id,
        'nameAr': name,
        'categoryName': cat_name,
        'categoryId': cat_id,
        'streamUrl': url,
        'verified': is_ok,
        'statusCode': status_code,
        'contentType': content_type,
        'finalUrl': final_url,
        'error': err
    }

results = []
with concurrent.futures.ThreadPoolExecutor(max_workers=40) as executor:
    futures = [executor.submit(verify_radio, r) for r in radios]
    for fut in concurrent.futures.as_completed(futures):
        results.append(fut.result())

ok_count = sum(1 for r in results if r['verified'])
fail_count = len(results) - ok_count
print(f"FINISHED! Total: {len(results)}, OK: {ok_count}, Fail: {fail_count}")

with open('atheer_verified.json', 'w', encoding='utf-8') as f:
    json.dump(results, f, ensure_ascii=False, indent=2)
print("Saved atheer_verified.json successfully")

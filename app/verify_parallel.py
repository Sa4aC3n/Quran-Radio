import urllib.request, json, concurrent.futures, re

with open('atheer_raw_radios.json', 'r', encoding='utf-8') as f:
    radios = json.load(f)

print(f"Total raw radios: {len(radios)}")

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
}

def verify_single(r):
    url = r.get('URL', '').strip()
    name = r.get('name', '').strip()
    cat = r.get('category_name', '')
    cat_id = r.get('category_id')
    raw_id = r.get('id')
    
    # Try head then get
    status_code = 0
    content_type = ''
    final_url = url
    is_ok = False
    err_str = None
    
    for method in ['HEAD', 'GET']:
        if is_ok:
            break
        try:
            req = urllib.request.Request(url, headers=headers, method=method)
            with urllib.request.urlopen(req, timeout=5) as resp:
                status_code = resp.status
                content_type = resp.headers.get('Content-Type', '')
                final_url = resp.geturl()
                is_ok = True
        except Exception as e:
            err_str = str(e)
            if method == 'GET':
                pass

    return {
        'id': f"atheer_{raw_id}",
        'rawId': raw_id,
        'nameAr': name,
        'categoryName': cat,
        'categoryId': cat_id,
        'streamUrl': url,
        'verified': is_ok,
        'statusCode': status_code,
        'contentType': content_type,
        'finalUrl': final_url,
        'error': err_str if not is_ok else None
    }

results = []
with concurrent.futures.ThreadPoolExecutor(max_workers=30) as executor:
    futures = [executor.submit(verify_single, r) for r in radios]
    for fut in concurrent.futures.as_completed(futures):
        results.append(fut.result())

ok_count = sum(1 for r in results if r['verified'])
fail_count = len(results) - ok_count
print(f"Parallel verification done! Total: {len(results)}, OK: {ok_count}, Fail: {fail_count}")

with open('atheer_verified.json', 'w', encoding='utf-8') as f:
    json.dump(results, f, ensure_ascii=False, indent=2)

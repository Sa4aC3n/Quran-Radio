import urllib.request, json, time, re

with open('atheer_raw_radios.json', 'r', encoding='utf-8') as f:
    radios = json.load(f)

print(f"Total raw radios to verify: {len(radios)}")

headers = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
    'Range': 'bytes=0-2048'
}

def verify_stream(url):
    try:
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=6) as resp:
            status = resp.status
            content_type = resp.headers.get('Content-Type', '')
            # Check redirect
            final_url = resp.geturl()
            return {
                'ok': True,
                'status_code': status,
                'content_type': content_type,
                'final_url': final_url,
                'error': None
            }
    except Exception as e:
        # Retry with GET without range
        try:
            req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
            with urllib.request.urlopen(req, timeout=6) as resp:
                return {
                    'ok': True,
                    'status_code': resp.status,
                    'content_type': resp.headers.get('Content-Type', ''),
                    'final_url': resp.geturl(),
                    'error': None
                }
        except Exception as e2:
            return {
                'ok': False,
                'status_code': 0,
                'content_type': '',
                'final_url': url,
                'error': str(e2)
            }

verified_results = []
ok_count = 0
fail_count = 0

for i, r in enumerate(radios):
    url = r.get('URL', '').strip()
    name = r.get('name', '').strip()
    cat = r.get('category_name', '')
    
    res = verify_stream(url)
    if res['ok']:
        ok_count += 1
    else:
        fail_count += 1
    
    r_item = {
        'id': f"atheer_{r.get('id')}",
        'raw_id': r.get('id'),
        'nameAr': name,
        'categoryName': cat,
        'categoryId': r.get('category_id'),
        'streamUrl': url,
        'verified': res['ok'],
        'statusCode': res['status_code'],
        'contentType': res['content_type'],
        'finalUrl': res['final_url'],
        'error': res['error']
    }
    verified_results.append(r_item)
    if (i + 1) % 25 == 0 or i == len(radios) - 1:
        print(f"Progress: {i+1}/{len(radios)} | OK: {ok_count}, Fail: {fail_count}")

print(f"\nVerification Finished! Total: {len(verified_results)}, OK: {ok_count}, Fail: {fail_count}")

with open('atheer_verified.json', 'w', encoding='utf-8') as f:
    json.dump(verified_results, f, ensure_ascii=False, indent=2)

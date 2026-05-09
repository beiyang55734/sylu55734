import requests

url = "https://open.docmee.cn/api/user/createApiToken"
headers = {
    "Api-Key": "ak_r4xSLQvrrsvpvdLVlw",
    "Content-Type": "application/json"
}
data = {
    "uid": "test_user",
    "limit": 10,
    "timeOfHours": 2
}

try:
    response = requests.post(url, headers=headers, json=data)
    print(f"Status Code: {response.status_code}")
    print(f"Response: {response.text}")
    result = response.json()
    if result.get("code") == 0:
        print(f"\nToken创建成功!")
        print(f"Token: {result.get('data', {}).get('token')}")
        print(f"过期时间: {result.get('data', {}).get('expireTime')}秒")
    else:
        print(f"\nToken创建失败: {result.get('msg')}")
except Exception as e:
    print(f"Error: {e}")
import requests

def go():
    session = requests.Session()
    url = "http://localhost/src/test/resources/test.html"
    for i in range(10):
        response = session.get(url)
        print(response.headers)
        print(response.text)

if __name__ == "__main__":
    go()

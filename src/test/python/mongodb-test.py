import unittest
import http.client
import urllib.parse

class TestMongoDB(unittest.TestCase):

    host = "localhost"
    port = 80
    #resource = "/test-classes/test.html"

    def test_post(self):
        params = urllib.parse.urlencode({'text':'lorem ipsum'})
        headers = {"Content-type": "application/x-www-form-urlencoded", "Accept": "text/plain"}
        conn = http.client.HTTPConnection(self.host, self.port)
        conn.request("POST", "/mongodb", params, headers)
        response = conn.getresponse()
        print(response.status, response.reason)
        self.assertEqual(response.status, 200)
        data = response.read()
        print(data)
        conn.close()

if __name__ == "__main__":
    unittest.main()

import unittest
import http.client

class TestETag(unittest.TestCase):

    host = "localhost"
    port = 80

    def test_head(self):
        conn = http.client.HTTPConnection(self.host, self.port)
        conn.request("HEAD", "/")
        response = conn.getresponse()
        self.assertEqual(response.status, 200)

    def test_get(self):
        conn = http.client.HTTPConnection(self.host, self.port)
        conn.request("GET", "/src/test/resources/test.html")
        response = conn.getresponse()
        self.assertEqual(response.status, 200)

    def test_etag(self):
        # simple request returns eTag
        conn = http.client.HTTPConnection(self.host, self.port)
        resource = "/src/test/resources/test.html"
        conn.request("GET", resource)
        response = conn.getresponse()
        self.assertEqual(response.status, 200)

        # simple request with valid If-Match eTag
        conn = http.client.HTTPConnection(self.host, self.port)
        etag = response.headers.get('eTag')
        headers = {"If-Match": etag}
        conn.request("GET", resource, headers=headers)
        response = conn.getresponse()
        self.assertEqual(response.status, 200)

        # request with missing If-Match eTag
        conn = http.client.HTTPConnection(self.host, self.port)
        headers = {'If-Match': '"9999"'}
        conn.request("GET", resource, headers=headers)
        response = conn.getresponse()
        self.assertEqual(response.status, 412)

    def test_not_found(self):
        conn = http.client.HTTPConnection(self.host, self.port)
        conn.request("GET", "not-there")
        response = conn.getresponse()
        self.assertEqual(response.status, 404)

if __name__ == '__main__':
    unittest.main()

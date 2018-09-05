import unittest
import http.client

class TestETag(unittest.TestCase):

    host = "localhost"
    port = 80
    resource = "/test-classes/test.html"

    def test_head(self):
        conn = http.client.HTTPConnection(self.host, self.port)
        conn.request("HEAD", "/")
        response = conn.getresponse()
        self.assertEqual(response.status, 200)
        conn.close()

    def test_get(self):
        conn = http.client.HTTPConnection(self.host, self.port)
        conn.request("GET", self.resource)
        response = conn.getresponse()
        self.assertEqual(response.status, 200)
        conn.close()

    def test_etag(self):
        # simple request returns eTag
        conn = http.client.HTTPConnection(self.host, self.port)
        conn.request("GET", self.resource)
        response = conn.getresponse()
        self.assertEqual(response.status, 200)
        conn.close()

        # simple request with valid If-Match eTag
        conn = http.client.HTTPConnection(self.host, self.port)
        etag = response.headers.get('eTag')
        headers = {"If-Match": etag}
        conn.request("GET", self.resource, headers=headers)
        response = conn.getresponse()
        self.assertEqual(response.status, 200)
        conn.close()

        # request with missing If-Match eTag
        conn = http.client.HTTPConnection(self.host, self.port)
        headers = {'If-Match': '"9999"'}
        conn.request("GET", self.resource, headers=headers)
        response = conn.getresponse()
        self.assertEqual(response.status, 412)
        conn.close()

    def test_not_found(self):
        conn = http.client.HTTPConnection(self.host, self.port)
        conn.request("GET", "not-there")
        response = conn.getresponse()
        self.assertEqual(response.status, 404)
        conn.close()

    def test_if_none_match(self):
        # TODO
        pass

    # TODO gets ignored if used in combination with if-none-match
    def test_if_modified_since(self):
        # TODO
        pass

if __name__ == '__main__':
    unittest.main()

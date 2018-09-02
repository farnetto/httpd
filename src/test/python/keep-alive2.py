import unittest
import http.client

class TestKeepAlive(unittest.TestCase):

    host = "localhost"
    port = 80
    resource = "/src/test/resources/test.html"

    def test_keep_alive(self):
        conn = http.client.HTTPConnection(self.host, self.port)
        for i in range(10):
            print("request #" + str(i))
            # ask server to close connection after 8th request
            if i == 8:
                conn.request("GET", resource
            conn.request("GET", )
            response = conn.getresponse()
            response.read()
            self.assertEqual(response.status, 200)
        conn.close()

if __name__ == "__main__":
    unittest.main()

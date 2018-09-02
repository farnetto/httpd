import unittest
import http.client

class TestKeepAlive(unittest.TestCase):

    host = "localhost"
    port = 80
    resource = "/src/test/resources/test.html"

    def test_keep_alive(self):
        conn = http.client.HTTPConnection(self.host, self.port)
        for i in range(8):
            print("request #" + str(i))
            conn.request("GET", self.resource)
            response = conn.getresponse()
            response.read()
            self.assertEqual(response.status, 200)
        # ask server to close connection
        conn.request("GET", self.resource, headers={"Connection":"close"})
        response = conn.getresponse()
        response.read()
        self.assertEqual(response.status, 200)
        # now the connection should be closed
        conn.request("GET", self.resource)
        try:
            response = conn.getresponse()
            self.fail('exception expected')
        except http.client.RemoteDisconnected:
            # correct
            pass
        except ConnectionAbortedError:
            pass

if __name__ == "__main__":
    unittest.main()

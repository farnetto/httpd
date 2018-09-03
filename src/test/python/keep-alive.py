import unittest
import http.client

#
# Tests the keep-alive functionality
#
class TestKeepAlive(unittest.TestCase):

    host = "localhost"
    port = 80
    resource = "/test-classes/test.html"

    def test_keep_alive(self):
        conn = http.client.HTTPConnection(self.host, self.port)
        # get resource a few times to check if the connection stays open
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
            # also correct
            pass

if __name__ == "__main__":
    unittest.main()

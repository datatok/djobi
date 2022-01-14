import json
from http.server import HTTPServer, BaseHTTPRequestHandler

class web_server(BaseHTTPRequestHandler):

    def do_POST(self):
        self.do_GET()

    def do_GET(self):
        self.send_response(200)
        print(self.path)

        responseStr = ""

        if self.path == '/get_json':
            responseStr = "[{\"letter\" : \"A\", \"count\" : 12},{\"letter\" : \"B\", \"count\" : 1}, {\"letter\" : \"C\", \"count\" : 2}]"

        elif self.path.startswith("/ReportDownload/Download.aspx"):
            responseStr = open("./report_1.csv.zip", "rb").read()
            self.end_headers()
            self.wfile.write(responseStr)
            return
        else:
            responseStr = "Not Found"

        self.end_headers()
        self.wfile.write(bytes(responseStr, 'utf-8'))

PORT = 8080
server_address = ("", PORT)

httpd = HTTPServer(server_address, web_server)

httpd.serve_forever()
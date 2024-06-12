// The serverUrl constant holds the URL of the server.
// This URL is used by the JavaScript files to make API requests.
// If the server's URL changes, you only need to update this constant.
// This constant is used by the WebUI to make API requests and to connect to the WebSocket server.
export const serverUrl = 'localhost:8080';
export const serverUrlWithHTTPS = 'https://' + serverUrl;
export const serverUrlWithWS = 'ws://' + serverUrl;
export const serverUrlWithWSS = 'wss://' + serverUrl;
export const serverUrlWithHTTP = 'http://' + serverUrl;

// If you are using Https, you need to change this HTTP to HTTPS.
export const serverUrlForAPI = 'http://' + serverUrl;
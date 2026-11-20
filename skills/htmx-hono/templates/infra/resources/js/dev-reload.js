// Dev only: reloads the page when the server says the browser bundle changed (cross.devreload).
// Outside dev mode /dev-reload is a 404, and EventSource does not retry a non-200 response: one request, then quiet.
new EventSource("/dev-reload").addEventListener("reload", () => location.reload());

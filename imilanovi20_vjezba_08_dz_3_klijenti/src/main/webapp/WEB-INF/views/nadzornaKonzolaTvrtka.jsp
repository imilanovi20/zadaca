<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Vježba 8 - zadaća 3 - Nadzorna kontrola tvrtka</title>
    </head>
    <body>
        <h1>Vježba 8 - zadaća 3 - Nadzorna kontrola tvrtka</h1>
        <br />
		<div>
			<p>
				Poruka: <span id="poruka"></span>
			</p>
		</div>
		<script type="text/javascript">
			var wsocket;
			function connect() {
				var adresa = window.location.pathname;
				var dijelovi = adresa.split("/");
				adresa = "ws://" + window.location.hostname + ":"
						+ window.location.port + "/" + dijelovi[1]
						+ "/ws/tvrtka";
				if ('WebSocket' in window) {
					wsocket = new WebSocket(adresa);
				} else if ('MozWebSocket' in window) {
					wsocket = new MozWebSocket(adresa);
				} else {
					alert('WebSocket nije podržan od web preglednika.');
					return;
				}
				wsocket.onmessage = onMessage;
			}

			function onMessage(evt) {
				var poruka = evt.data;
				var porukaElem = document.getElementById("poruka");
				porukaElem.innerHTML = poruka;
			}

			window.addEventListener("load", connect, false);
		</script>
    
    </body>
</html>

Market Pulse
============

A **Play** application with **Akka** back-end and **WebSocket** event streaming.

Running locally
---------------

To launch the server in development mode:

    $ ./activator run

To **POST** a trade from the command line:

    $ curl -X POST -H 'Content-Type: application/json' -d '{"userId": "134256", "currencyFrom": "EUR", "currencyTo": "GBP", "amountSell": 1000, "amountBuy": 747.10, "rate": 0.7471, "timePlaced" : "24-JAN-15 10:27:44", "originatingCountry" : "FR"}' http://localhost:9000/api/trades

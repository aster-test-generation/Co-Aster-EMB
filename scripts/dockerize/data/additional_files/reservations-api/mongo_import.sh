#!/bin/bash
mongoimport --host db --port 27017 --db reservations-api --collection users --file /fixtures/init.json --jsonArray
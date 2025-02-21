#!/usr/bin/env python

import os
import pandas as pd

SCRIPT_LOCATION = os.path.dirname(os.path.realpath(__file__))
SUTS_LOCATION = os.path.join(SCRIPT_LOCATION, 'dockerize/data/sut.csv')
GENERATOR_LOCATION = os.path.join(SCRIPT_LOCATION, 'dockerize/docker_generator.py')

suts = pd.read_csv(SUTS_LOCATION)
dockerized_suts = suts[suts['Dockerized'] == True]
EXPOSE_PORT = 8080

for _, sut in dockerized_suts.iterrows():
    os.system(f"python {GENERATOR_LOCATION} {sut['NAME']} {EXPOSE_PORT}")

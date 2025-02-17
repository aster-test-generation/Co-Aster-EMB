import os

import pandas as pd

# should be callable without ANY input
# execute whatever is in "dockerize"
### for example, read EMB/scripts/dockerize/data/sut.csv for list of names, and run docker-generator/py on each of them
# outPut of files should be under EMB/dockerize
# no Docker should be run, just creating files
# can assume EMB/dist has already been created (do not run it here)
# then, output under EMB/dockerize must be put under Git
# additional files links must point correctly, do not duplicate files

suts = pd.read_csv('./dockerize/data/sut.csv')
dockerized_suts = suts[suts['Dockerized'] == True]

EXPOSE_PORT = 8080

for _, sut in dockerized_suts.iterrows():
    # call command to run docker-generator.py
    os.system(f"python dockerize/docker_generator.py {sut['NAME']} {EXPOSE_PORT}")

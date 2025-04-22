source("analyze.R")

suts <- function(){

  # This generated table should NOT be added to Git, as will change based on papers
  TABLE = "./table_suts.tex"

  #Names here should match what in data.csv
  SUTS = c(
### REST
# "bibliothek",
"blogapi",
# "catwatch",
# "cwa-verification",
"familie-ba-sak",
# "features-service",
# "genome-nexus",
# "gestaohospital",
# "languagetool",
"market",
"ocvn",
"pay-publicapi",
# "person-controller",
"proxyprint",
"reservations-api",
# "rest-ncs",
# "rest-news",
# "rest-scs",
# "restcountries",
"scout-api",
# "session-service",
"tiltaksgjennomforing-api",
"tracking-system",
# "user-management",
# "youtube-mock",
### GraphQL
    # "petclinic-graphql",
    # "patio-api",
    # "timbuctoo",
    #"graphql-ncs",
    #"graphql-scs",
### RPC
    #"thrift-ncs",
    #"thrift-scs",
    #"grpc-ncs",
    #"grpc-scs",
    #"signal-registration",
#     "ind0",
#     "ind1",
    ""
  )

  latex(TABLE,SUTS)
}


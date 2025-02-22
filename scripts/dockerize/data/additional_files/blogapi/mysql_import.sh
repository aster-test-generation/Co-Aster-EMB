mysql -u root -proot blogapi < /docker-entrypoint-initdb.d/blogapi.sql
mysql -u root -proot blogapi < /docker-entrypoint-initdb.d/data.sql

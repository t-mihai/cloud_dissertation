

::build the image
docker build -t ase.ism/cloud-biometrics .


::create the network
docker network create dissertation_net

::create the db container
docker container run --name mysqldb --network dissertation_net -e MYSQL_ROOT_PASSWORD=root1234 -e MYSQL_DATABASE=dissertation_db -d mysql:latest

::todo: synt the deplyment of the 2 contianers


::crete the app container
docker container run --network dissertation_net --name cloud_biometrics -p 8083:8082 -d ase.ism/cloud-biometrics:latest

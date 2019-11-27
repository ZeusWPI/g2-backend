docker-local:
	sudo docker-compose -f local.docker-compose.yml up -d
docker-staging:
	sudo docker build -t g2-backend . && \
	sudo docker run -d -p "3333:3000" -e "DATABASE_URL=mysql://host.docker.internal:3306/g2_dev?user=g2_dev_user&password=mstFVVS4ASEDMZlx0TWsWABmo&serverTimezone=UTC" g2-backend

docker-local:
	sudo docker-compose -f local.docker-compose.yml up -d
docker-staging:
	sudo docker build -t g2-backend-img . && \
	sudo docker run -it -p "3333:3000" \
		-e "DATABASE_URL=mysql://host.docker.internal:3306/g2_dev?user=g2_dev_user&password=mstFVVS4ASEDMZlx0TWsWABmo&serverTimezone=UTC" \
		-e "APP_HOST=10.0.20.20:3333" \
		-e "DEV=false" \
		--name g2-backend g2-backend-img /bin/bash

run-jar-with-dev:
	java -Dconf=dev-config.edn -jar target/uberjar/g2.jar migrate && \
	java -Dconf=dev-config.edn -jar target/uberjar/g2.jar

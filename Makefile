# Simple Makefile for common Docker tasks

up:
	docker-compose -f docker-compose.yml up -d

down:
	docker-compose -f docker-compose.yml down -v

up-app:
	docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d --build

down-app:
	docker-compose -f docker-compose.yml -f docker-compose.app.yml down -v

logs:
	docker-compose logs -f

restart:
	make down-app && make up-app

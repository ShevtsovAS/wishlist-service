# Simple Makefile for common Docker tasks

up:
	docker-compose -f docker-compose.yml up -d

# Down with volume deletion (WILL DELETE DATABASE DATA)
down:
	docker-compose -f docker-compose.yml down -v

# Down without volume deletion (PRESERVES DATABASE DATA)
down-keep-data:
	docker-compose -f docker-compose.yml down

up-app:
	docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d --build

# Down with volume deletion (WILL DELETE DATABASE DATA)
down-app:
	docker-compose -f docker-compose.yml -f docker-compose.app.yml down -v

# Down without volume deletion (PRESERVES DATABASE DATA)
down-app-keep-data:
	docker-compose -f docker-compose.yml -f docker-compose.app.yml down

up-debug:
	docker-compose -f docker-compose.yml -f docker-compose.debug.yml up -d --build

# Down with volume deletion (WILL DELETE DATABASE DATA)
down-debug:
	docker-compose -f docker-compose.yml -f docker-compose.debug.yml down -v

# Down without volume deletion (PRESERVES DATABASE DATA) 
down-debug-keep-data:
	docker-compose -f docker-compose.yml -f docker-compose.debug.yml down

logs:
	docker-compose logs -f

# Restart with database reset
restart:
	make down-app && make up-app

# Restart while preserving database data
restart-keep-data:
	make down-app-keep-data && make up-app

# Rebuild and restart only the app container without touching the database
rebuild-app-only:
	docker-compose -f docker-compose.yml -f docker-compose.app.yml up -d --build --no-deps app

#!/bin/sh
docker login -u $DOCKER_USER -p $DOCKER_PASS
docker login -u $HEROKU_USERNAME -p $HEROKU_API_KEY registry.heroku.com
mvn docker:build
mvn docker:push
IMAGE_ID=$(docker inspect registry.heroku.com/$HEROKU_APP_NAME/web --format={{.Id}})
curl -X PATCH \
  https://api.heroku.com/apps/$HEROKU_APP_NAME/formation \
  -H "Accept: application/vnd.heroku+json; version=3.docker-releases" \
  -H "Authorization: Bearer $HEROKU_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{
  "updates": [
    {
      "type": "web",
      "docker_image": "'$IMAGE_ID'"
    }
  ]
}'

#!/bin/bash

echo "Creating tusich..."
response=$(curl -X POST http://localhost:1234/tusich -d @tusich.json --header "Content-Type: application/json" --header "X-Request-Id: kek")
echo $response
tusich_1_id=$(echo $response | jq -r '.id')

echo "Creating achievement..."
sed -i "s/\"tusichId\": .*/\"tusichId\": $tusich_1_id/" achievement.json
response=$(curl -X POST http://localhost:1234/achievement -d @achievement.json --header "Content-Type: application/json" --header "X-Request-Id: kek")
echo $response
achievement_1_id=$(echo $response | jq -r '.id')

echo "Creating user..."
response=$(curl -X POST http://localhost:1234/user -d @user.json --header "Content-Type: application/json" --header "X-Request-Id: kek")
echo $response
user_1_id=$(echo $response | jq -r '.id')

echo "Creating user 2..."
curl -X POST http://localhost:1234/user -d @user.json --header "Content-Type: application/json" --header "X-Request-Id: kek"

echo "Creating userAchievement..."
sed -i "s/\"userId\": .*,/\"userId\": $user_1_id,/" userAchievement.json
sed -i "s/\"achievementId\": .*/\"achievementId\": $achievement_1_id/" userAchievement.json
response=$(curl -X POST http://localhost:1234/user/$user_1_id/achievement -d @userAchievement.json --header "Content-Type: application/json" --header "X-Request-Id: kek")
echo $response

echo "Creating same userAchievement..."
curl -X POST http://localhost:1234/user/$user_1_id/achievement -d @userAchievement.json --header "Content-Type: application/json" --header "X-Request-Id: kek"

echo "Getting all users..."
curl -X GET http://localhost:1234/user --header "Content-Type: application/json" --header "X-Request-Id: kek"

#[{"id":1,"firstName":"Insaf","lastName":"Fayzrakhmanov","achievements":[{"id":1,"userId":1,"achievement":{"id":1,"name":"Погладить курочку","tusichId":1},"level":2}]},{"id":1,"firstName":"Scala","lastName":"Engineer","achievements":[]}]


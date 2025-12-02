#!/bin/bash

echo ""
echo "Applying migration ClientReturnsList"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /clientReturnsList                       controllers.ClientReturnsListController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "clientReturnsList.title = clientReturnsList" >> ../conf/messages.en
echo "clientReturnsList.heading = clientReturnsList" >> ../conf/messages.en

echo "Migration ClientReturnsList completed"

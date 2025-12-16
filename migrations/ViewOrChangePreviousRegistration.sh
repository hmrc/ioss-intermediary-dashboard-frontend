#!/bin/bash

echo ""
echo "Applying migration ViewOrChangePreviousRegistration"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /viewOrChangePreviousRegistration                        controllers.ViewOrChangePreviousRegistrationController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /viewOrChangePreviousRegistration                        controllers.ViewOrChangePreviousRegistrationController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeViewOrChangePreviousRegistration                  controllers.ViewOrChangePreviousRegistrationController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeViewOrChangePreviousRegistration                  controllers.ViewOrChangePreviousRegistrationController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "viewOrChangePreviousRegistration.title = ViewOrChangePreviousRegistration" >> ../conf/messages.en
echo "viewOrChangePreviousRegistration.heading = ViewOrChangePreviousRegistration" >> ../conf/messages.en
echo "viewOrChangePreviousRegistration.intermediary number 1 = Intermediary number 2" >> ../conf/messages.en
echo "viewOrChangePreviousRegistration.aaa = bbb" >> ../conf/messages.en
echo "viewOrChangePreviousRegistration.checkYourAnswersLabel = ViewOrChangePreviousRegistration" >> ../conf/messages.en
echo "viewOrChangePreviousRegistration.error.required = Select viewOrChangePreviousRegistration" >> ../conf/messages.en
echo "viewOrChangePreviousRegistration.change.hidden = ViewOrChangePreviousRegistration" >> ../conf/messages.en

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryViewOrChangePreviousRegistration: Arbitrary[ViewOrChangePreviousRegistration] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(ViewOrChangePreviousRegistration.values.toSeq)";\
    print "    }";\
    next }1' ../test-utils/generators/ModelGenerators.scala > tmp && mv tmp ../test-utils/generators/ModelGenerators.scala

echo "Migration ViewOrChangePreviousRegistration completed"

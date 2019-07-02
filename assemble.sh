#! /bin/bash

./gradlew clean check assemble

if [[ $? != 0 ]]; then
  exit
fi

mv build/distributions/*.zip ../elastic-test/plugins/
#!/bin/bash

./gradlew clean build -x :keanu-python:build --daemon uploadArchives -PnexusUser=$NEXUS_USER -PnexusPassword=$NEXUS_PASSWORD -Psigning.keyId=$SIGNING_KEY_ID -Psigning.password=$SIGNING_PASSWORD -Psigning.secretKeyRingFile=../deployment/secret-keys-keanu.gpg --info --stacktrace

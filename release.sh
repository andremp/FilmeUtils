#!/bin/bash
rm release.properties
VERSION=$(cat pom.xml | grep "<version>.*-SNAPSHOT" | grep -o "[0-9.]*")
VERSION_FILE=src/main/java/filmeUtils/ArgumentsParser.java
echo $VERSION
sed -i "s/VERSION = \"[0-9\.]*\";/VERSION = \"$VERSION\";/g" $VERSION_FILE
git add $VERSION_FILE
git commit -m"New version"
mvn3 clean package release:prepare
mvn3 release:perform 

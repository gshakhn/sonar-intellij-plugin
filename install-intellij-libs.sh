#!/bin/sh

INTELLIJ_HOME=$1
IDEA_VERSION=$2

if [ -z "$INTELLIJ_HOME" ]
then
  echo "Please provide the path to Intellij home directory. For example: install-intellij-libs.sh /Applications/Nika-IU-111.228.app"
  exit 1
fi

if [ ! -d "$INTELLIJ_HOME" ]
then
  echo "Directory does not exist: $INTELLIJ_HOME"
  exit 1
fi

echo 'Installing Intellij artifacts to Maven local repository'
echo "Intellij home: $INTELLIJ_HOME"
echo "Intellij version: $IDEA_VERSION"

mvn install:install-file -Dfile="$INTELLIJ_HOME/lib/openapi.jar" -DgroupId=com.intellij -DartifactId=openapi -Dversion=$IDEA_VERSION -Dpackaging=jar
mvn install:install-file -Dfile="$INTELLIJ_HOME/lib/util.jar" -DgroupId=com.intellij -DartifactId=util -Dversion=$IDEA_VERSION -Dpackaging=jar
mvn install:install-file -Dfile="$INTELLIJ_HOME/lib/extensions.jar" -DgroupId=com.intellij -DartifactId=extensions -Dversion=$IDEA_VERSION -Dpackaging=jar
mvn install:install-file -Dfile="$INTELLIJ_HOME/lib/annotations.jar" -DgroupId=com.intellij -DartifactId=annotations -Dversion=$IDEA_VERSION -Dpackaging=jar
mvn install:install-file -Dfile="$INTELLIJ_HOME/lib/forms_rt.jar" -DgroupId=com.intellij -DartifactId=forms_rt -Dversion=$IDEA_VERSION -Dpackaging=jar

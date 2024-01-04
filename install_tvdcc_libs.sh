#!/bin/sh

# set the directory where SQLcl libraries are stored
if [[ "$1" = "" ]]; then
    TVDCC_DIR="$HOME/tvdcc"
else
    TVDCC_DIR="$1"
fi

# check db* CODECOP CLI root directory
if ! test -f "${TVDCC_DIR}/tvdcc.jar"; then
    echo "Error: ${TVDCC_DIR} is not a valid path to the root path of db* CODECOP CLI."
    echo "       Please pass a valid path to this script."
    exit 1
fi

# define versions according usage in pom.xml
TVDCC_VERSION="4.4.3-SNAPSHOT"
PLSQL_VERSION="4.2.2-SNAPSHOT"

# install JAR files into local Maven repository, these libs are not available in public Maven repositories
mvn install:install-file -Dfile=$TVDCC_DIR/tvdcc.jar -DgeneratePom=true \
        -DgroupId=trivadis.tvdcc -DartifactId=tvdcc -Dversion=$TVDCC_VERSION -Dpackaging=jar
mvn install:install-file -Dfile=$TVDCC_DIR/lib/plsql-${PLSQL_VERSION}.jar -DgeneratePom=true \
        -DgroupId=trivadis.oracle -DartifactId=plsql -Dversion=$PLSQL_VERSION -Dpackaging=jar

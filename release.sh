#!/bin/sh

#Few global variables
ARTIFACT_ID="actuator-libvirt"

if [ $# != 1 ]; then
    echo "Usage: $0 request|perform"
    exit 1
fi


function getVersionToRelease {
    CURRENT_VERSION=`mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"`
    echo ${CURRENT_VERSION%%-SNAPSHOT}
}

function getVersion {
    mvn ${MVN_ARGS} org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v "\[INFO\]"    
}
function getBranch {
    git symbolic-ref --short HEAD
}

case $1 in
request)        
    VERSION=$(getVersionToRelease)
    RELEASE_BRANCH="release/$VERSION"        
    git checkout -b ${RELEASE_BRANCH} || exit 1

    echo "-- Prepare the code for release ${VERSION} in branch ${RELEASE_BRANCH} --"
    echo $VERSION > .version
    git add .version    
    ./bump_release.sh code $VERSION || exit 1
    
    git commit -m "Prepare the code for release ${VERSION}" -a
    git push origin ${RELEASE_BRANCH} || exit 1
    git checkout develop
    echo "Branch $RELEASE_BRANCH is ready for the releasing process"
    ;;
perform)    
    if [ $(hostname) != "btrp" ]; then
            echo "This script must be executed on btrp.inria.fr"
            exit 1
    fi    
    if [ ! -f .version ]; then
        echo "Missing .version file"
        exit 1
    fi
    VERSION=$(cat .version)
        
    echo "-- Prepare the release $VERSION --"
    mvn -B release:prepare || exit 1

    echo "-- Perform the release --"
    mvn release:perform || exit 1
    rm .version #To prevent for an infinite loop
    
    DEV_HEAD=$(git rev-parse HEAD)
    RELEASE_BRANCH="release/${VERSION}"
    # The current tree looks like:
    # * HEAD -> next dev version, it is a detached head due to jenkins git plugin
    # * (tag ...) -> the tag made by maven release:prepare on the released version == ${DEV_HEAD}~1
    # * ($RELEASE_BRANCH) -> the pointer on the released branch that was set by the client with ./release.sh request
    
    # merge the version changes back into develop so that folks are working against the new release
    git checkout develop
    echo "-- Integrate the next version ($DEV_HEAD) into the develop branch --"
    git merge --no-ff -m "integrate the development version generated by maven" ${DEV_HEAD} || exit 1
    NEW_VERSION=$(getVersion)
    ./bump_release.sh code ${NEW_VERSION}
    git commit -m "code prepared for development version ${NEW_VERSION}" -a

    echo "-- Integrate release ${VERSION} into the master branch --"
    git checkout master
    git merge --no-ff ${DEV_HEAD}~1 -m "integrate release ${VERSION} to master"

    echo "-- Push the changes and the tags --"
    git branch -D ${RELEASE_BRANCH}
    git push --all && git push --tags
    git push origin --delete ${RELEASE_BRANCH}
    echo "-- Generate the javadoc for release ${VERSION} --"
    ./release_javadoc.sh /usr/share/nginx/html

    echo "-- Notify the website for release ${VERSION} --"
    ./bump_release.sh site ${VERSION}
    ;;
    *)
        echo "Unsupported operation '$1'"
        exit 1
esac
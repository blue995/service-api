#!/bin/bash

TAG="$1"

if [ -z "$TAG" ]
then
    echo "Tag to merge with is missing"
    exit 1
fi

git checkout master
git pull
git checkout service-api-with-tfs
git merge "$TAG"
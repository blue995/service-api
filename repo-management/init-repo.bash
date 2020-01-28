#!/bin/bash

CURRENT_DIR=$PWD
BASEDIR=$(dirname "$0")

cd "$BASEDIR"/..
git remote rename origin patched
git remote add origin https://github.com/reportportal/service-api.git
git fetch origin
git branch develop --set-upstream-to origin/develop
git branch master
git branch master --set-upstream-to origin/master
git checkout service-api-with-tfs
cd "$CURRENT_DIR"
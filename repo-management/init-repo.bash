#!/bin/bash

CURRENT_DIR=$PWD
BASEDIR=$(dirname "$0")

cd "$BASEDIR"/..
git remote rename origin patched
git remote add origin https://github.com/reportportal/service-api.git
git fetch origin
git checkout --track origin/master
git checkout service-api-with-tfs
cd "$CURRENT_DIR"
#!/bin/bash

CURRENT_DIR=.
BASEDIR=$(dirname "$0")

cd "$BASEDIR"/..
git remote add main https://github.com/reportportal/service-api.git
git fetch main
git checkout -t main/master
git checkout service-api-with-tfs
cd "$CURRENT_DIR"
#!/bin/bash

git remote add main https://github.com/reportportal/service-api.git
git fetch main
git checkout -t main/master
git checkout service-api-with-tfs
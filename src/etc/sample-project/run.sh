#!/bin/bash
# This script is largely targeted at fresh Ubuntu docker images
# Before attempting to install git, make sure to run:
#  apt-get update
#  apt-get install git
#  git config --global user.name "jrh3k5"
#  git config --global user.email "jrh3k5@gmail.com"
#  apt-get install default-jdk
#  apt-get install maven
#  git clone https://github.com/jrh3k5/flume-agent-maven-plugin.git
#
#  From the root directory of the project, run:
#    mvn clean install
#  ...to get the plugin installed in the local repo

mvn flume-agent:run

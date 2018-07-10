#!/bin/bash

function strjoin () (IFS=$'\xEE\x80\x80'; echo "$*");

args=$(strjoin "$@")

./gradlew run -PrunArgs="$args"
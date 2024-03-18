#!/usr/bin/env bash
#
# Creates the deployment package for a .zip file archive.
#

set -euo pipefail

if [[ "${VERBOSE:-}" == "yes" ]]; then
  set -x
fi

this_script="$0"

echo "Executing $this_script" "$@"

function usage {
  cat <<EOM
Usage: $this_script [-app modules]

Options:
  -app modules: Project modules under the apps directory. Example: -app operations,notifications
EOM
}

function fail_with_error {
  echo "$1" >&2
  echo
  usage
  exit 1
}

function parse_list {
  IFS=',' ; read -r -a "$2" <<< "$1"
}

function package_app {
  app="$1"

  if [[ ! -d apps/$app ]]; then
    echo "Error: Application $app does not exist" >&2
    exit 1
  fi

  if [[ $app == "operations" ]]; then
    package_with_dependencies "$app"
  elif [[ $app == "notifications" ]]; then
    package_no_dependencies "$app"
  else
    echo "Error: Unsupported application: $app" >&2
    exit 1
  fi
}

function package_with_dependencies {
  app="$1"
  working_dir=$(pwd)

  sbt "project $app" clean npmPackage
  mkdir -p dist/"$app"
  cp -r apps/"$app"/target/scala-3.3.3/npm-package/* dist/"$app"
  cd dist/"$app" ; npm -s install --package-lock=false promise-mysql@5.2.0 ; zip -r ../"$app".zip .
  cd "$working_dir"
}

function package_no_dependencies {
  app="$1"

  sbt "project $app" clean npmPackage
  zip -rj dist/"$app".zip apps/"$app"/target/scala-3.3.3/npm-package/*
}

if [ $# -eq 0 ]; then
  fail_with_error "Incorrect number of input arguments: $0 $*"
fi

while (( $# > 0 )); do
  case "$1" in
    -help)
      echo "Create the deployment package for a .zip file archive for different project modules."
      echo
      usage
      exit 0
      ;;
    -app)
      parse_list "$2" app_modules;
      shift 2
      ;;
    -*) # unsupported parameters
      fail_with_error "Error: Unsupported parameter $1"
      ;;
    *) # unsupported positional arguments
      fail_with_error "Error: Unsupported positional parameter $1"
      ;;
  esac
done

if [[ -n ${app_modules:-} ]]; then
  echo "Packaging applications..."
  for (( n=0; n < ${#app_modules[*]}; n++)); do
    package_app "${app_modules[n]}"
  done
fi

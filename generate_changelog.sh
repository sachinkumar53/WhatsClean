#!/bin/bash

# Get the latest tag
latest_tag=$(git describe --tags --abbrev=0)

# If there's no tag, assume the first commit
if [ -z "$latest_tag" ]; then
  latest_tag=$(git rev-list --max-parents=0 HEAD)
fi

# Generate change log from the latest tag to HEAD
git log --pretty=format:"- %s (%h)" $latest_tag..HEAD > changelog.txt

echo "Generated changelog.txt"

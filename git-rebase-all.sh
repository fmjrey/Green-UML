#!/bin/bash

echo "Branch: fix-create-diagram"
git checkout fix-create-diagram
git rebase master

echo "Branch: ccvisu-3.0"
git checkout ccvisu-3.0
git rebase master

echo "Branch: version-qualifier"
git checkout version-qualifier
git rebase master

echo "Branch: fmjrey"
git checkout fmjrey
git rebase master


#!/bin/bash
# sync.sh
# AntiGravity Auto-Sync Script for Conductor Documentation

echo -e "\033[0;36mStarting documentation sync...\033[0m"

# 1. Pull latest changes from remote
echo -e "\033[0;33mFetching latest changes from GitHub...\033[0m"
git pull --rebase origin main
if [ $? -ne 0 ]; then
    echo -e "\033[0;31mError: Failed to pull remote changes. Please resolve any conflicts manually.\033[0m"
    exit 1
fi

# 2. Check for local modifications
status=$(git status --porcelain)
if [ -z "$status" ]; then
    echo -e "\033[0;32mNo local changes detected. Workspace is in sync with GitHub.\033[0m"
    exit 0
fi

echo -e "\033[0;33mDetected local changes:\033[0m"
echo "$status"

# 3. Stage changes
echo -e "\033[0;33mStaging changes...\033[0m"
git add .

# 4. Commit changes
timestamp=$(date +"%Y-%m-%d %H:%M:%S")
commitMsg="docs: automatic sync updates ($timestamp)"
echo -e "\033[0;33mCommitting changes with message: '$commitMsg'...\033[0m"
git commit -m "$commitMsg"

# 5. Push to GitHub
echo -e "\033[0;33mPushing changes to GitHub...\033[0m"
git push origin main
if [ $? -eq 0 ]; then
    echo -e "\033[0;32mSuccess! Documentation is successfully synchronized with GitHub.\033[0m"
else
    echo -e "\033[0;31mError: Failed to push changes to GitHub.\033[0m"
fi

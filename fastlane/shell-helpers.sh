#!/usr/bin/env bash
#
# shell-helpers.sh — Shared color definitions and logging helpers.
#
# Source this file from other scripts:
#   source "$(dirname "${BASH_SOURCE[0]}")/shell-helpers.sh"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

info()    { echo -e "${GREEN}[INFO]${NC}  $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*" >&2; }
section() { echo -e "\n${CYAN}${BOLD}=== $* ===${NC}"; }

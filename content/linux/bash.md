# Bash

## Port checking

Check what's running on port 8083.

` netstat -ltnp | grep -w ':8083'`

## Read pid startup command

`cat /proc/{PID}/cmdline`

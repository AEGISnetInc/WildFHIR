#!/usr/bin/env bash
while [ ! -f /tmp/server_can_shutdown.txt ] # <-- created by 99_last_processed_file.sh
do
  sleep 2
done
rm -f /tmp/server_can_shutdown.txt
mysqladmin shutdown
sleep 15


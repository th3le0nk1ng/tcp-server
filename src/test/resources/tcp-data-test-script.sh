#!/bin/bash
for i in {1..2000000}
do
  head /dev/urandom | LC_CTYPE=C tr -dc 0-9 | head -c 9 | nc localhost 4000
done;

#!/bin/bash

password=$1
cmd_report=$2
cmd_kinit="kinit hdfs"
expect -c "
	set timeout 30
	spawn $cmd_kinit
	expect \"Password*\"
	send \"$password\r\"
	spawn $cmd_report
	expect eof
	exit
"

#/bin/bash

password=$1
filepath=$2
goalpath=$3

cmd="scp $filepath $goalpath"
expect -c "
	set timeout 30
	spawn $cmd
	expect {
		\"yes/no\" { send \"yes\r\";exp_continue;}
		\"password:\" { send \"$password\r\" }
	}
	expect eof
	exit"

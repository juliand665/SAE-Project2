#/bin/bash

expected=$(cat src/$1.java | head -n 2 | awk '{print $2}')
result=$(java ch.ethz.sae.Verifier $1 | tail -n 2 | awk '{print $2}')

paste <(echo "$expected") <(echo "$result") | awk -v name="$1" '{
	red = "\033[0;31m"
	green = "\033[0;32m"
	orange = "\033[0;33m"
	nc = "\033[0m"

	if ($1 == $2) {
		print green "[" name "] " nc $1
	} else {
		color = (index($2, "NOT")>0) ? orange : red
		print color "[" name "] " nc "expected: " $1 " got: " $2
	}
}'

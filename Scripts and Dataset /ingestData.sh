#!/bin/bash
input="datasetJSON.txt"
i=1
while IFS= read -r var
do
##  echo "$var"
  curl -X POST http://localhost:8080/index -H "Content-type: application/json" -d "$var"
  echo "---------------------------""$i"
  i=$((i+1))
done < "$input"

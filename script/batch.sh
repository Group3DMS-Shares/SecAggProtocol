#!/bin/bash
exp=10
for ((i=0;i<exp;++i)) 
do
  mkdir $i
  script/dropouts.sh &
  script/gradients.sh &
  script/users.sh &
  wait
  mv log_users $i/
  mv log_dropouts $i/
  mv log_gradients $i/
done     

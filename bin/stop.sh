#!/bin/sh 

PROC_NAME=SL_SCHEDULER
PID=$PROC_NAME.pid


# program is running
if [ -f "$PID" ]; then
  NEW_PID=`cat < $PID`
  if [ ! -z "$NEW_PID" ]; then
    if [ $NEW_PID -gt 0 ]; then
      echo "Attempting to signal the process to stop through OS signal."
      kill -15 $NEW_PID >/dev/null 2>&1
      sleep 3;
      if ps -p $NEW_PID > /dev/null; then
        echo "$PROC_NAME is still be running with $NEW_PID"
        kill -9 $NEW_PID
        echo "$PROC_NAME force stop!"
        rm $PID;
        else
          rm $PID
      fi
    fi
  fi
  
  else
   echo "$PID is not exist!"

fi

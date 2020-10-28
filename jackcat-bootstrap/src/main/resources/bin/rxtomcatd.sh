#!/bin/sh

# Copyright 2019 dunwoo.com - 顿悟源码
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

usage()
{
  echo "Usage: ${0##*/} {start|stop|restart} "
  exit 1
}

# 检查参数
[ $# -gt 0 ] || usage


# 检查是否存在 java 可执行命令
RUNJAVA=`which java`

if [ -z "$RUNJAVA" ];then
  echo "Cannot find a Java JDK. Please set either set RUNJAVA or put java (>=1.5) in your PATH." >&2
  exit 1
fi


pid=

# 检查是否正在运行
function running() {
  pid=`ps auxww| grep "com.dunwoo.tomcat.startup.Bootstrap" | grep -v grep | awk '{print $2}'`
  if [ -n "$pid" ];then
    return 0
  fi
  return 1
}

# 设置程序执行的家目录
PRGDIR=`dirname "$0"`
RXTOMCAT_BASE=`cd "$PRGDIR/.." >/dev/null; pwd`

TMPDIR=$RXTOMCAT_BASE/work

# 设置 classpath 和 jvm 参数
RXTOMCAT_START="bootstrap.jar"

MAIN_CALSS="com.dunwoo.tomcat.startup.Bootstrap"

CLASSPATH="$RXTOMCAT_BASE"/bin/bootstrap.jar

LOGGING_CONFIG="$RXTOMCAT_BASE"/bin/logback.xml

JAVA_OPTS="-server -Xms512M -Xmx512M"

ACTION=$1
# Do the action
case "$ACTION" in
  start)
    echo "Starting RxTomcat ... "

    if running; then
      echo "Already Running $pid!"
      exit 1
    fi

    nohup "$RUNJAVA" $JAVA_OPTS \
      -classpath $CLASSPATH \
      -Dlogback.configurationFile="$LOGGING_CONFIG" \
      -Drxtomcat.base="$RXTOMCAT_BASE" \
      -Djava.io.tmpdir="$TMPDIR" \
      $MAIN_CALSS \
      >/dev/null 2>&1 &

    pid=$!
    echo "OK - $pid"
    ;;

  stop)
    echo "Stopping RxTomcat ... "
    
    if running; then
      kill "$pid" 2>/dev/null
      echo OK
    else
      echo "ERROR: RxTomcat not running."
      exit 1
    fi
    ;;

  restart)
    if running; then
      echo "Stopping RxTomcat ... "
      kill "$pid" 2>/dev/null
      echo OK
    fi
    sleep 1
    RXTOMCAT_SH=$0
    if [ ! -f $RXTOMCAT_SH ]; then
      if [ ! -f $RXTOMCAT_BASE/bin/rxtomcatd.sh ]; then
        echo "$RXTOMCAT_BASE/bin/rxtomcatd.sh does not exist."
        exit 1
      fi
      RXTOMCAT_SH=$RXTOMCAT_BASE/bin/rxtomcatd.sh
    fi
    
    "$RXTOMCAT_SH" start "$@"
    ;;

  *)
    usage
    ;;
esac

exit 0

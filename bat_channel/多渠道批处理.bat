::设置编码方式(65001 UTF-8;936 GBK;437 英语)
chcp 65001

::双冒号表示注释(setlocal enabledelayedexpansion是延迟变量赋值使用)
@echo off&setlocal enabledelayedexpansion
echo ☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆==Start==☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆
echo=
::先删除文件夹，再创建文件夹
for %%i in (*.apk) do (
  ::~ni 表示无后缀文件名
  if exist %~dp0\%%~ni (
     echo %%~ni文件夹已存在,执行清空
     echo=
     del /q /s %~dp0\%%~ni
     echo=
  ) else (
     echo %%~ni文件夹不存在,执行创建
     echo=
     md %~dp0\%%~ni
     echo=
  )
)
::找到当前目录下所有apk文件
for %%i in (*.apk) do (
  ::读取当前所有渠道
  for /f "tokens=1,* delims=_" %%a in (config.txt) do (
     set channel=%%a
     ::去除TAB
     set channel=!channel:	=!
     if not "!channel!"=="" (
        ::去除空格
        set channel=!channel: =!
  	   	if not "!channel!"=="" (
      		set key=%%b
            ::去除TAB
            set key=!key:	=!  
            if not "!key!"=="" (  
                ::去除空格
            	set key=!key: =!
            	if not "!key!"=="" (
            		echo 创建渠道：%%~ni_!channel!_!key!.apk
            		echo=
            		::~fi表示文件全路径
             		java -jar walle-cli-all.jar put -c !channel! -e count_key=!key! %%~fi %~dp0\%%~ni\%%~ni_!channel!.apk
            	)	
            ) 		                  
  		)
     )
  )
)
echo ☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆==End==☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆☆
pause
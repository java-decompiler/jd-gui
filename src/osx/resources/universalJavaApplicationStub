#!/bin/bash
##################################################################################
#                                                                                #
# universalJavaApplicationStub                                                   #
#                                                                                #
# A BASH based JavaApplicationStub for Java Apps on Mac OS X                     #
# that works with both Apple's and Oracle's plist format.                        #
#                                                                                #
# Inspired by Ian Roberts stackoverflow answer                                   #
# at http://stackoverflow.com/a/17546508/1128689                                 #
#                                                                                #
# @author    Tobias Fischer                                                      #
# @url       https://github.com/tofi86/universalJavaApplicationStub              #
# @date      2020-03-19                                                          #
# @version   3.0.6                                                               #
#                                                                                #
##################################################################################
#                                                                                #
# The MIT License (MIT)                                                          #
#                                                                                #
# Copyright (c) 2014-2020 Tobias Fischer                                         #
#                                                                                #
# Permission is hereby granted, free of charge, to any person obtaining a copy   #
# of this software and associated documentation files (the "Software"), to deal  #
# in the Software without restriction, including without limitation the rights   #
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell      #
# copies of the Software, and to permit persons to whom the Software is          #
# furnished to do so, subject to the following conditions:                       #
#                                                                                #
# The above copyright notice and this permission notice shall be included in all #
# copies or substantial portions of the Software.                                #
#                                                                                #
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR     #
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,       #
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE    #
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER         #
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,  #
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE  #
# SOFTWARE.                                                                      #
#                                                                                #
##################################################################################



# function 'stub_logger()'
#
# A logger which logs to the macOS Console.app using the 'syslog' command
#
# @param1  the log message
# @return  void
################################################################################
function stub_logger() {
	syslog -s -k \
		Facility com.apple.console \
		Level Notice \
		Sender "$(basename "$0")" \
		Message "[$$][${CFBundleName:-$(basename "$0")}] $1"
}



# set the directory abspath of the current
# shell script with symlinks being resolved
############################################

PRG=$0
while [ -h "$PRG" ]; do
	ls=$(ls -ld "$PRG")
	link=$(expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null)
	if expr "$link" : '^/' 2> /dev/null >/dev/null; then
		PRG="$link"
	else
		PRG="$(dirname "$PRG")/$link"
	fi
done
PROGDIR=$(dirname "$PRG")
stub_logger "[StubDir] $PROGDIR"



# set files and folders
############################################

# the absolute path of the app package
cd "$PROGDIR"/../../ || exit 11
AppPackageFolder=$(pwd)

# the base path of the app package
cd .. || exit 12
AppPackageRoot=$(pwd)

# set Apple's Java folder
AppleJavaFolder="${AppPackageFolder}"/Contents/Resources/Java

# set Apple's Resources folder
AppleResourcesFolder="${AppPackageFolder}"/Contents/Resources

# set Oracle's Java folder
OracleJavaFolder="${AppPackageFolder}"/Contents/Java

# set Oracle's Resources folder
OracleResourcesFolder="${AppPackageFolder}"/Contents/Resources

# set path to Info.plist in bundle
InfoPlistFile="${AppPackageFolder}"/Contents/Info.plist

# set the default JVM Version to a null string
JVMVersion=""
JVMMaxVersion=""



# function 'plist_get()'
#
# read a specific Plist key with 'PlistBuddy' utility
#
# @param1  the Plist key with leading colon ':'
# @return  the value as String or Array
################################################################################
plist_get(){
	/usr/libexec/PlistBuddy -c "print $1" "${InfoPlistFile}" 2> /dev/null
}

# function 'plist_get_java()'
#
# read a specific Plist key with 'PlistBuddy' utility
# in the 'Java' or 'JavaX' dictionary (<dict>)
#
# @param1  the Plist :Java(X):Key with leading colon ':'
# @return  the value as String or Array
################################################################################
plist_get_java(){
	plist_get ${JavaKey:-":Java"}$1
}



# read Info.plist and extract JVM options
############################################

# read the program name from CFBundleName
CFBundleName=$(plist_get ':CFBundleName')

# read the icon file name
CFBundleIconFile=$(plist_get ':CFBundleIconFile')


# check Info.plist for Apple style Java keys -> if key :Java is present, parse in apple mode
/usr/libexec/PlistBuddy -c "print :Java" "${InfoPlistFile}" > /dev/null 2>&1
exitcode=$?
JavaKey=":Java"

# if no :Java key is present, check Info.plist for universalJavaApplication style JavaX keys -> if key :JavaX is present, parse in apple mode
if [ $exitcode -ne 0 ]; then
	/usr/libexec/PlistBuddy -c "print :JavaX" "${InfoPlistFile}" > /dev/null 2>&1
	exitcode=$?
	JavaKey=":JavaX"
fi


# read 'Info.plist' file in Apple style if exit code returns 0 (true, ':Java' key is present)
if [ $exitcode -eq 0 ]; then
	stub_logger "[PlistStyle] Apple"

	# set Java and Resources folder
	JavaFolder="${AppleJavaFolder}"
	ResourcesFolder="${AppleResourcesFolder}"

	APP_PACKAGE="${AppPackageFolder}"
	JAVAROOT="${AppleJavaFolder}"
	USER_HOME="$HOME"


	# read the Java WorkingDirectory
	JVMWorkDir=$(plist_get_java ':WorkingDirectory' | xargs)
	# set Working Directory based upon PList value
	if [[ ! -z ${JVMWorkDir} ]]; then
		WorkingDirectory="${JVMWorkDir}"
	else
		# AppPackageRoot is the standard WorkingDirectory when the script is started
		WorkingDirectory="${AppPackageRoot}"
	fi
	# expand variables $APP_PACKAGE, $JAVAROOT, $USER_HOME
	WorkingDirectory=$(eval echo "${WorkingDirectory}")


	# read the MainClass name
	JVMMainClass="$(plist_get_java ':MainClass')"

	# read the SplashFile name
	JVMSplashFile=$(plist_get_java ':SplashFile')

	# read the JVM Properties as an array and retain spaces
	IFS=$'\t\n'
	JVMOptions=($(xargs -n1 <<<$(plist_get_java ':Properties' | grep " =" | sed 's/^ */-D/g' | sed -E 's/ = (.*)$/="\1"/g')))
	unset IFS
	# post processing of the array follows further below...

	# read the ClassPath in either Array or String style
	JVMClassPath_RAW=$(plist_get_java ':ClassPath' | xargs)
	if [[ $JVMClassPath_RAW == *Array* ]] ; then
		JVMClassPath=.$(plist_get_java ':ClassPath' | grep "    " | sed 's/^ */:/g' | tr -d '\n' | xargs)
	else
		JVMClassPath=${JVMClassPath_RAW}
	fi
	# expand variables $APP_PACKAGE, $JAVAROOT, $USER_HOME
	JVMClassPath=$(eval echo "${JVMClassPath}")

	# read the JVM Options in either Array or String style
	JVMDefaultOptions_RAW=$(plist_get_java ':VMOptions' | xargs)
	if [[ $JVMDefaultOptions_RAW == *Array* ]] ; then
		JVMDefaultOptions=$(plist_get_java ':VMOptions' | grep "    " | sed 's/^ */ /g' | tr -d '\n' | xargs)
	else
		JVMDefaultOptions=${JVMDefaultOptions_RAW}
	fi

	# read StartOnMainThread and add as -XstartOnFirstThread
	JVMStartOnMainThread=$(plist_get_java ':StartOnMainThread')
	if [ "${JVMStartOnMainThread}" == "true" ]; then
		JVMDefaultOptions+=" -XstartOnFirstThread"
	fi

	# read the JVM Arguments in either Array or String style (#76) and retain spaces
	IFS=$'\t\n'
	MainArgs_RAW=$(plist_get_java ':Arguments' | xargs)
	if [[ $MainArgs_RAW == *Array* ]] ; then
		MainArgs=($(xargs -n1 <<<$(plist_get_java ':Arguments' | tr -d '\n' | sed -E 's/Array \{ *(.*) *\}/\1/g' | sed 's/  */ /g')))
	else
		MainArgs=($(xargs -n1 <<<$(plist_get_java ':Arguments')))
	fi
	unset IFS
	# post processing of the array follows further below...

	# read the Java version we want to find
	JVMVersion=$(plist_get_java ':JVMVersion' | xargs)
	# post processing of the version string follows below...


# read 'Info.plist' file in Oracle style
else
	stub_logger "[PlistStyle] Oracle"

	# set Working Directory and Java and Resources folder
	JavaFolder="${OracleJavaFolder}"
	ResourcesFolder="${OracleResourcesFolder}"
	WorkingDirectory="${OracleJavaFolder}"

	APP_ROOT="${AppPackageFolder}"

	# read the MainClass name
	JVMMainClass="$(plist_get ':JVMMainClassName')"

	# read the SplashFile name
	JVMSplashFile=$(plist_get ':JVMSplashFile')

	# read the JVM Options as an array and retain spaces
	IFS=$'\t\n'
	JVMOptions=($(plist_get ':JVMOptions' | grep "    " | sed 's/^ *//g'))
	unset IFS
	# post processing of the array follows further below...

	# read the ClassPath in either Array or String style
	JVMClassPath_RAW=$(plist_get ':JVMClassPath')
	if [[ $JVMClassPath_RAW == *Array* ]] ; then
		JVMClassPath=.$(plist_get ':JVMClassPath' | grep "    " | sed 's/^ */:/g' | tr -d '\n' | xargs)
		# expand variables $APP_PACKAGE, $JAVAROOT, $USER_HOME
		JVMClassPath=$(eval echo "${JVMClassPath}")

	elif [[ ! -z ${JVMClassPath_RAW} ]] ; then
		JVMClassPath=${JVMClassPath_RAW}
		# expand variables $APP_PACKAGE, $JAVAROOT, $USER_HOME
		JVMClassPath=$(eval echo "${JVMClassPath}")

	else
		#default: fallback to OracleJavaFolder
		JVMClassPath="${JavaFolder}/*"
		# Do NOT expand the default 'AppName.app/Contents/Java/*' classpath (#42)
	fi

	# read the JVM Default Options
	JVMDefaultOptions=$(plist_get ':JVMDefaultOptions' | grep -o " \-.*" | tr -d '\n' | xargs)

	# read the Main Arguments from JVMArguments key as an array and retain spaces (see #46 for naming details)
	IFS=$'\t\n'
	MainArgs=($(xargs -n1 <<<$(plist_get ':JVMArguments' | tr -d '\n' | sed -E 's/Array \{ *(.*) *\}/\1/g' | sed 's/  */ /g')))
	unset IFS
	# post processing of the array follows further below...

	# read the Java version we want to find
	JVMVersion=$(plist_get ':JVMVersion' | xargs)
	# post processing of the version string follows below...
fi


# (#75) check for undefined icons or icon names without .icns extension and prepare
# an osascript statement for those cases when the icon can be shown in the dialog
DialogWithIcon=""
if [ ! -z ${CFBundleIconFile} ]; then
	if [[ ${CFBundleIconFile} == *.icns ]] && [[ -f "${ResourcesFolder}/${CFBundleIconFile}" ]] ; then
		DialogWithIcon=" with icon path to resource \"${CFBundleIconFile}\" in bundle (path to me)"
	elif [[ ${CFBundleIconFile} != *.icns ]] && [[ -f "${ResourcesFolder}/${CFBundleIconFile}.icns" ]] ; then
		CFBundleIconFile+=".icns"
		DialogWithIcon=" with icon path to resource \"${CFBundleIconFile}\" in bundle (path to me)"
	fi
fi


# JVMVersion: post processing and optional splitting
if [[ ${JVMVersion} == *";"* ]]; then
	minMaxArray=(${JVMVersion//;/ })
	JVMVersion=${minMaxArray[0]//+}
	JVMMaxVersion=${minMaxArray[1]//+}
fi
stub_logger "[JavaRequirement] JVM minimum version: ${JVMVersion}"
stub_logger "[JavaRequirement] JVM maximum version: ${JVMMaxVersion}"

# MainArgs: replace occurences of $APP_ROOT with its content
MainArgsArr=()
for i in "${MainArgs[@]}"
do
	MainArgsArr+=("$(eval echo "$i")")
done

# JVMOptions: replace occurences of $APP_ROOT with its content
JVMOptionsArr=()
for i in "${JVMOptions[@]}"
do
	JVMOptionsArr+=("$(eval echo "$i")")
done


# internationalized messages
############################################

LANG=$(defaults read -g AppleLocale)
stub_logger "[Language] $LANG"

# French localization
if [[ $LANG == fr* ]] ; then
	MSG_ERROR_LAUNCHING="ERREUR au lancement de '${CFBundleName}'."
	MSG_MISSING_MAINCLASS="'MainClass' n'est pas spécifié.\nL'application Java ne peut pas être lancée."
	MSG_JVMVERSION_REQ_INVALID="La syntaxe de la version de Java demandée est invalide: %s\nVeuillez contacter le développeur de l'application."
	MSG_NO_SUITABLE_JAVA="La version de Java installée sur votre système ne convient pas.\nCe programme nécessite Java %s"
	MSG_JAVA_VERSION_OR_LATER="ou ultérieur"
	MSG_JAVA_VERSION_LATEST="(dernière mise à jour)"
	MSG_JAVA_VERSION_MAX="à %s"
	MSG_NO_SUITABLE_JAVA_CHECK="Merci de bien vouloir installer la version de Java requise."
	MSG_INSTALL_JAVA="Java doit être installé sur votre système.\nRendez-vous sur java.com et suivez les instructions d'installation..."
	MSG_LATER="Plus tard"
	MSG_VISIT_JAVA_DOT_COM="Java by Oracle"
	MSG_VISIT_ADOPTOPENJDK="Java by AdoptOpenJDK"

# German localization
elif [[ $LANG == de* ]] ; then
	MSG_ERROR_LAUNCHING="FEHLER beim Starten von '${CFBundleName}'."
	MSG_MISSING_MAINCLASS="Die 'MainClass' ist nicht spezifiziert!\nDie Java-Anwendung kann nicht gestartet werden!"
	MSG_JVMVERSION_REQ_INVALID="Die Syntax der angeforderten Java-Version ist ungültig: %s\nBitte kontaktieren Sie den Entwickler der App."
	MSG_NO_SUITABLE_JAVA="Es wurde keine passende Java-Version auf Ihrem System gefunden!\nDieses Programm benötigt Java %s"
	MSG_JAVA_VERSION_OR_LATER="oder neuer"
	MSG_JAVA_VERSION_LATEST="(neuste Unterversion)"
	MSG_JAVA_VERSION_MAX="bis %s"
	MSG_NO_SUITABLE_JAVA_CHECK="Stellen Sie sicher, dass die angeforderte Java-Version installiert ist."
	MSG_INSTALL_JAVA="Auf Ihrem System muss die 'Java'-Software installiert sein.\nBesuchen Sie java.com für weitere Installationshinweise."
	MSG_LATER="Später"
	MSG_VISIT_JAVA_DOT_COM="Java von Oracle"
	MSG_VISIT_ADOPTOPENJDK="Java von AdoptOpenJDK"

# Simplifyed Chinese localization
elif [[ $LANG == zh* ]] ; then
	MSG_ERROR_LAUNCHING="无法启动 '${CFBundleName}'."
	MSG_MISSING_MAINCLASS="没有指定 'MainClass'！\nJava程序无法启动!"
	MSG_JVMVERSION_REQ_INVALID="Java版本参数语法错误: %s\n请联系该应用的开发者。"
	MSG_NO_SUITABLE_JAVA="没有在系统中找到合适的Java版本！\n必须安装Java %s才能够使用该程序！"
	MSG_JAVA_VERSION_OR_LATER="及以上版本"
	MSG_JAVA_VERSION_LATEST="（最新版本）"
	MSG_JAVA_VERSION_MAX="最高为 %s"
	MSG_NO_SUITABLE_JAVA_CHECK="请确保系统中安装了所需的Java版本"
	MSG_INSTALL_JAVA="你需要在Mac中安装Java运行环境！\n访问 java.com 了解如何安装。"
	MSG_LATER="稍后"
	MSG_VISIT_JAVA_DOT_COM="Java by Oracle"
	MSG_VISIT_ADOPTOPENJDK="Java by AdoptOpenJDK"

# English default localization
else
	MSG_ERROR_LAUNCHING="ERROR launching '${CFBundleName}'."
	MSG_MISSING_MAINCLASS="'MainClass' isn't specified!\nJava application cannot be started!"
	MSG_JVMVERSION_REQ_INVALID="The syntax of the required Java version is invalid: %s\nPlease contact the App developer."
	MSG_NO_SUITABLE_JAVA="No suitable Java version found on your system!\nThis program requires Java %s"
	MSG_JAVA_VERSION_OR_LATER="or later"
	MSG_JAVA_VERSION_LATEST="(latest update)"
	MSG_JAVA_VERSION_MAX="up to %s"
	MSG_NO_SUITABLE_JAVA_CHECK="Make sure you install the required Java version."
	MSG_INSTALL_JAVA="You need to have JAVA installed on your Mac!\nVisit java.com for installation instructions..."
	MSG_LATER="Later"
	MSG_VISIT_JAVA_DOT_COM="Java by Oracle"
	MSG_VISIT_ADOPTOPENJDK="Java by AdoptOpenJDK"
fi



# function 'get_java_version_from_cmd()'
#
# returns Java version string from 'java -version' command
# works for both old (1.8) and new (9) version schema
#
# @param1  path to a java JVM executable
# @return  the Java version number as displayed in 'java -version' command
################################################################################
function get_java_version_from_cmd() {
	# second sed command strips " and -ea from the version string
	echo $("$1" -version 2>&1 | awk '/version/{print $3}' | sed -E 's/"//g;s/-ea//g')
}


# function 'extract_java_major_version()'
#
# extract Java major version from a version string
#
# @param1  a Java version number ('1.8.0_45') or requirement string ('1.8+')
# @return  the major version (e.g. '7', '8' or '9', etc.)
################################################################################
function extract_java_major_version() {
	echo $(echo "$1" | sed -E 's/^1\.//;s/^([0-9]+)(-ea|(\.[0-9_.]{1,7})?)(-b[0-9]+-[0-9]+)?[+*]?$/\1/')
}


# function 'get_comparable_java_version()'
#
# return comparable version for a Java version number or requirement string
#
# @param1  a Java version number ('1.8.0_45') or requirement string ('1.8+')
# @return  an 8 digit numeral ('1.8.0_45'->'08000045'; '9.1.13'->'09001013')
################################################################################
function get_comparable_java_version() {
	# cleaning: 1) remove leading '1.'; 2) remove build string (e.g. '-b14-468'); 3) remove 'a-Z' and '-*+' (e.g. '-ea'); 4) replace '_' with '.'
	local cleaned=$(echo "$1" | sed -E 's/^1\.//g;s/-b[0-9]+-[0-9]+$//g;s/[a-zA-Z+*\-]//g;s/_/./g')
	# splitting at '.' into an array
	local arr=( ${cleaned//./ } )
	# echo a string with left padded version numbers
	echo "$(printf '%02s' ${arr[0]})$(printf '%03s' ${arr[1]})$(printf '%03s' ${arr[2]})"
}


# function 'is_valid_requirement_pattern()'
#
# check whether the Java requirement is a valid requirement pattern
#
# supported requirements are for example:
# - 1.6       requires Java 6 (any update)      [1.6, 1.6.0_45, 1.6.0_88]
# - 1.6*      requires Java 6 (any update)      [1.6, 1.6.0_45, 1.6.0_88]
# - 1.6+      requires Java 6 or higher         [1.6, 1.6.0_45, 1.8, 9, etc.]
# - 1.6.0     requires Java 6 (any update)      [1.6, 1.6.0_45, 1.6.0_88]
# - 1.6.0_45  requires Java 6u45                [1.6.0_45]
# - 1.6.0_45+ requires Java 6u45 or higher      [1.6.0_45, 1.6.0_88, 1.8, etc.]
# - 9         requires Java 9 (any update)      [9.0.*, 9.1, 9.3, etc.]
# - 9*        requires Java 9 (any update)      [9.0.*, 9.1, 9.3, etc.]
# - 9+        requires Java 9 or higher         [9.0, 9.1, 10, etc.]
# - 9.1       requires Java 9.1 (any update)    [9.1.*, 9.1.2, 9.1.13, etc.]
# - 9.1*      requires Java 9.1 (any update)    [9.1.*, 9.1.2, 9.1.13, etc.]
# - 9.1+      requires Java 9.1 or higher       [9.1, 9.2, 10, etc.]
# - 9.1.3     requires Java 9.1.3               [9.1.3]
# - 9.1.3*    requires Java 9.1.3 (any update)  [9.1.3]
# - 9.1.3+    requires Java 9.1.3 or higher     [9.1.3, 9.1.4, 9.2.*, 10, etc.]
# - 10-ea     requires Java 10 (early access release)
#
# unsupported requirement patterns are for example:
# - 1.2, 1.3, 1.9       Java 2, 3 are not supported
# - 1.9                 Java 9 introduced a new versioning scheme
# - 6u45                known versioning syntax, but unsupported
# - 9-ea*, 9-ea+        early access releases paired with */+
# - 9., 9.*, 9.+        version ending with a .
# - 9.1., 9.1.*, 9.1.+  version ending with a .
# - 9.3.5.6             4 part version number is unsupported
#
# @param1  a Java requirement string ('1.8+')
# @return  boolean exit code: 0 (is valid), 1 (is not valid)
################################################################################
function is_valid_requirement_pattern() {
	local java_req=$1
	java8pattern='1\.[4-8](\.[0-9]+)?(\.0_[0-9]+)?[*+]?'
	java9pattern='(9|1[0-9])(-ea|[*+]|(\.[0-9]+){1,2}[*+]?)?'
	# test matches either old Java versioning scheme (up to 1.8) or new scheme (starting with 9)
	if [[ ${java_req} =~ ^(${java8pattern}|${java9pattern})$ ]]; then
		return 0
	else
		return 1
	fi
}



# determine which JVM to use
############################################

# default Apple JRE plugin path (< 1.6)
apple_jre_plugin="/Library/Java/Home/bin/java"
apple_jre_version=$(get_java_version_from_cmd "${apple_jre_plugin}")
# default Oracle JRE plugin path (>= 1.7)
oracle_jre_plugin="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java"
oracle_jre_version=$(get_java_version_from_cmd "${oracle_jre_plugin}")


# first check system variable "$JAVA_HOME" -> has precedence over any other System JVM
stub_logger '[JavaSearch] Checking for $JAVA_HOME ...'
if [ -n "$JAVA_HOME" ] ; then
	stub_logger "[JavaSearch] ... found JAVA_HOME with value $JAVA_HOME"

	# PR 26: Allow specifying "$JAVA_HOME" relative to "$AppPackageFolder"
	# which allows for bundling a custom version of Java inside your app!
	if [[ $JAVA_HOME == /* ]] ; then
		# if "$JAVA_HOME" starts with a Slash it's an absolute path
		JAVACMD="$JAVA_HOME/bin/java"
	else
		# otherwise it's a relative path to "$AppPackageFolder"
		JAVACMD="$AppPackageFolder/$JAVA_HOME/bin/java"
	fi
	JAVACMD_version=$(get_comparable_java_version $(get_java_version_from_cmd "${JAVACMD}"))
else
	stub_logger "[JavaSearch] ... didn't found JAVA_HOME"
fi


# check for any other or a specific Java version
# also if $JAVA_HOME exists but isn't executable
if [ -z "${JAVACMD}" ] || [ ! -x "${JAVACMD}" ] ; then
	stub_logger "[JavaSearch] Checking for JavaVirtualMachines on the system ..."
	# reset variables
	JAVACMD=""
	JAVACMD_version=""

	# first check whether JVMVersion string is a valid requirement string
	if [ ! -z "${JVMVersion}" ] && ! is_valid_requirement_pattern ${JVMVersion} ; then
		MSG_JVMVERSION_REQ_INVALID_EXPANDED=$(printf "${MSG_JVMVERSION_REQ_INVALID}" "${JVMVersion}")
		# log exit cause
		stub_logger "[EXIT 4] ${MSG_JVMVERSION_REQ_INVALID_EXPANDED}"
		# display error message with AppleScript
		osascript -e "tell application \"System Events\" to display dialog \"${MSG_ERROR_LAUNCHING}\n\n${MSG_JVMVERSION_REQ_INVALID_EXPANDED}\" with title \"${CFBundleName}\" buttons {\" OK \"} default button 1${DialogWithIcon}"
		# exit with error
		exit 4
	fi
	# then check whether JVMMaxVersion string is a valid requirement string
	if [ ! -z "${JVMMaxVersion}" ] && ! is_valid_requirement_pattern ${JVMMaxVersion} ; then
		MSG_JVMVERSION_REQ_INVALID_EXPANDED=$(printf "${MSG_JVMVERSION_REQ_INVALID}" "${JVMMaxVersion}")
		# log exit cause
		stub_logger "[EXIT 5] ${MSG_JVMVERSION_REQ_INVALID_EXPANDED}"
		# display error message with AppleScript
		osascript -e "tell application \"System Events\" to display dialog \"${MSG_ERROR_LAUNCHING}\n\n${MSG_JVMVERSION_REQ_INVALID_EXPANDED}\" with title \"${CFBundleName}\" buttons {\" OK \"} default button 1${DialogWithIcon}"
		# exit with error
		exit 5
	fi


	# find installed JavaVirtualMachines (JDK + JRE)
	allJVMs=()
	# read JDK's from '/usr/libexec/java_home -V' command
	while read -r line; do
		version=$(echo $line | awk -F $',' '{print $1;}')
		path=$(echo $line | awk -F $'" ' '{print $2;}')
		path+="/bin/java"
		allJVMs+=("$version:$path")
	done < <(/usr/libexec/java_home -V 2>&1 | grep '^[[:space:]]')
	# unset while loop variables
	unset version path

	# add Apple JRE if available
	if [ -x "${apple_jre_plugin}" ] ; then
		allJVMs+=("$apple_jre_version:$apple_jre_plugin")
	fi

	# add Oracle JRE if available
	if [ -x "${oracle_jre_plugin}" ] ; then
		allJVMs+=("$oracle_jre_version:$oracle_jre_plugin")
	fi

	# debug output
	for i in "${allJVMs[@]}"
	do
		stub_logger "[JavaSearch] ... found JVM: $i"
	done


	# determine JVMs matching the min/max version requirement
	minC=$(get_comparable_java_version ${JVMVersion})
	maxC=$(get_comparable_java_version ${JVMMaxVersion})
	matchingJVMs=()

	for i in "${allJVMs[@]}"
	do
		# split JVM string at ':' delimiter to retain spaces in $path substring
		IFS=: arr=($i) ; unset IFS
		# [0] JVM version number
		ver=${arr[0]}
		# comparable JVM version number
		comp=$(get_comparable_java_version $ver)
		# [1] JVM path
		path="${arr[1]}"
		# construct string item for adding to the "matchingJVMs" array
		item="$comp:$ver:$path"

		# pre-requisite: current version number needs to be greater than min version number
		if [ "$comp" -ge "$minC" ] ; then

			# perform max version checks if max version requirement is present
			if [ ! -z ${JVMMaxVersion} ] ; then

				# max version requirement ends with '*' modifier
				if [[ ${JVMMaxVersion} == *\* ]] ; then

					# use the '*' modifier from the max version string as wildcard for a 'starts with' comparison
					# and check whether the current version number starts with the max version wildcard string
					if [[ ${ver} == ${JVMMaxVersion} ]]; then
						matchingJVMs+=("$item")

					# or whether the current comparable version is lower than the comparable max version
					elif [ "$comp" -le "$maxC" ] ; then
						matchingJVMs+=("$item")
					fi

				# max version requirement ends with '+' modifier -> always add this version if it's greater than $min
				# because a max requirement with + modifier doesn't make sense
				elif [[ ${JVMMaxVersion} == *+ ]] ; then
					matchingJVMs+=("$item")

				# matches 6 zeros at the end of the max version string (e.g. for 1.8, 9)
				# -> then the max version string should be treated like with a '*' modifier at the end
				#elif [[ ${maxC} =~ ^[0-9]{2}0{6}$ ]] && [ "$comp" -le $(( ${maxC#0} + 999 )) ] ; then
				#	matchingJVMs+=("$item")

				# matches 3 zeros at the end of the max version string (e.g. for 9.1, 10.3)
				# -> then the max version string should be treated like with a '*' modifier at the end
				#elif [[ ${maxC} =~ ^[0-9]{5}0{3}$ ]] && [ "$comp" -le "${maxC}" ] ; then
				#	matchingJVMs+=("$item")

				# matches standard requirements without modifier
				elif [ "$comp" -le "$maxC" ]; then
					matchingJVMs+=("$item")
				fi

			# no max version requirement:

			# min version requirement ends with '+' modifier
			# -> always add the current version because it's greater than $min
			elif [[ ${JVMVersion} == *+ ]] ; then
				matchingJVMs+=("$item")

			# min version requirement ends with '*' modifier
			# -> use the '*' modifier from the min version string as wildcard for a 'starts with' comparison
			#    and check whether the current version number starts with the min version wildcard string
			elif [[ ${JVMVersion} == *\* ]] ; then
				if [[ ${ver} == ${JVMVersion} ]] ; then
					matchingJVMs+=("$item")
				fi

			# compare the min version against the current version with an additional * wildcard for a 'starts with' comparison
			# -> e.g. add 1.8.0_44 when the requirement is 1.8
			elif [[ ${ver} == ${JVMVersion}* ]] ; then
					matchingJVMs+=("$item")
			fi
		fi
	done
	# unset for loop variables
	unset arr ver comp path item

	# debug output
	for i in "${matchingJVMs[@]}"
	do
		stub_logger "[JavaSearch] ... ... matches all requirements: $i"
	done


	# sort the matching JavaVirtualMachines by version number
	# https://stackoverflow.com/a/11789688/1128689
	IFS=$'\n' matchingJVMs=($(sort -nr <<<"${matchingJVMs[*]}"))
	unset IFS


	# get the highest matching JVM
	for ((i = 0; i < ${#matchingJVMs[@]}; i++));
	do
		# split JVM string at ':' delimiter to retain spaces in $path substring
		IFS=: arr=(${matchingJVMs[$i]}) ; unset IFS
		# [0] comparable JVM version number
		comp=${arr[0]}
		# [1] JVM version number
		ver=${arr[1]}
		# [2] JVM path
		path="${arr[2]}"

		# use current value as JAVACMD if it's executable
		if [ -x "$path" ] ; then
			JAVACMD="$path"
			JAVACMD_version=$comp
			break
		fi
	done
	# unset for loop variables
	unset arr comp ver path
fi

# log the Java Command and the extracted version number
stub_logger "[JavaCommand] '$JAVACMD'"
stub_logger "[JavaVersion] $(get_java_version_from_cmd "${JAVACMD}")${JAVACMD_version:+ / $JAVACMD_version}"



if [ -z "${JAVACMD}" ] || [ ! -x "${JAVACMD}" ] ; then

	# different error messages when a specific JVM was required
	if [ ! -z "${JVMVersion}" ] ; then
		# display human readable java version (#28)
		java_version_hr=$(echo ${JVMVersion} | sed -E 's/^1\.([0-9+*]+)$/ \1/g' | sed "s/+/ ${MSG_JAVA_VERSION_OR_LATER}/;s/*/ ${MSG_JAVA_VERSION_LATEST}/")
		MSG_NO_SUITABLE_JAVA_EXPANDED=$(printf "${MSG_NO_SUITABLE_JAVA}" "${java_version_hr}").

		if [ ! -z "${JVMMaxVersion}" ] ; then
			java_version_hr=$(extract_java_major_version ${JVMVersion})
			java_version_max_hr=$(echo ${JVMMaxVersion} | sed -E 's/^1\.([0-9+*]+)$/ \1/g' | sed "s/+//;s/*/ ${MSG_JAVA_VERSION_LATEST}/")
			MSG_NO_SUITABLE_JAVA_EXPANDED="$(printf "${MSG_NO_SUITABLE_JAVA}" "${java_version_hr}") $(printf "${MSG_JAVA_VERSION_MAX}" "${java_version_max_hr}")"
		fi

		# log exit cause
		stub_logger "[EXIT 3] ${MSG_NO_SUITABLE_JAVA_EXPANDED}"

		# display error message with AppleScript
		osascript -e "tell application \"System Events\" to display dialog \"${MSG_ERROR_LAUNCHING}\n\n${MSG_NO_SUITABLE_JAVA_EXPANDED}\n${MSG_NO_SUITABLE_JAVA_CHECK}\" with title \"${CFBundleName}\"  buttons {\" OK \", \"${MSG_VISIT_JAVA_DOT_COM}\", \"${MSG_VISIT_ADOPTOPENJDK}\"} default button 1${DialogWithIcon}" \
				-e "set response to button returned of the result" \
				-e "if response is \"${MSG_VISIT_JAVA_DOT_COM}\" then open location \"https://www.java.com/download/\"" \
				-e "if response is \"${MSG_VISIT_ADOPTOPENJDK}\" then open location \"https://adoptopenjdk.net/releases.html\""
		# exit with error
		exit 3

	else
		# log exit cause
		stub_logger "[EXIT 1] ${MSG_ERROR_LAUNCHING}"
		# display error message with AppleScript
		osascript -e "tell application \"System Events\" to display dialog \"${MSG_ERROR_LAUNCHING}\n\n${MSG_INSTALL_JAVA}\" with title \"${CFBundleName}\" buttons {\"${MSG_LATER}\", \"${MSG_VISIT_JAVA_DOT_COM}\", \"${MSG_VISIT_ADOPTOPENJDK}\"} default button 1${DialogWithIcon}" \
					-e "set response to button returned of the result" \
					-e "if response is \"${MSG_VISIT_JAVA_DOT_COM}\" then open location \"https://www.java.com/download/\"" \
					-e "if response is \"${MSG_VISIT_ADOPTOPENJDK}\" then open location \"https://adoptopenjdk.net/releases.html\""
		# exit with error
		exit 1
	fi
fi



# MainClass check
############################################

if [ -z "${JVMMainClass}" ]; then
	# log exit cause
	stub_logger "[EXIT 2] ${MSG_MISSING_MAINCLASS}"
	# display error message with AppleScript
	osascript -e "tell application \"System Events\" to display dialog \"${MSG_ERROR_LAUNCHING}\n\n${MSG_MISSING_MAINCLASS}\" with title \"${CFBundleName}\" buttons {\" OK \"} default button 1${DialogWithIcon}"
	# exit with error
	exit 2
fi



# execute $JAVACMD and do some preparation
############################################

# enable drag&drop to the dock icon
export CFProcessPath="$0"

# remove Apples ProcessSerialNumber from passthru arguments (#39)
if [[ "$*" == -psn* ]] ; then
	ArgsPassthru=()
else
	ArgsPassthru=("$@")
fi

# change to Working Directory based upon Apple/Oracle Plist info
cd "${WorkingDirectory}" || exit 13
stub_logger "[WorkingDirectory] ${WorkingDirectory}"

# execute Java and set
# - classpath
# - splash image
# - dock icon
# - app name
# - JVM options / properties (-D)
# - JVM default options (-X)
# - main class
# - main class arguments
# - passthrough arguments from Terminal or Drag'n'Drop to Finder icon
stub_logger "[Exec] \"$JAVACMD\" -cp \"${JVMClassPath}\" -splash:\"${ResourcesFolder}/${JVMSplashFile}\" -Xdock:icon=\"${ResourcesFolder}/${CFBundleIconFile}\" -Xdock:name=\"${CFBundleName}\" ${JVMOptionsArr:+$(printf "'%s' " "${JVMOptionsArr[@]}") }${JVMDefaultOptions:+$JVMDefaultOptions }${JVMMainClass}${MainArgsArr:+ $(printf "'%s' " "${MainArgsArr[@]}")}${ArgsPassthru:+ $(printf "'%s' " "${ArgsPassthru[@]}")}"
exec "${JAVACMD}" \
		-cp "${JVMClassPath}" \
		-splash:"${ResourcesFolder}/${JVMSplashFile}" \
		-Xdock:icon="${ResourcesFolder}/${CFBundleIconFile}" \
		-Xdock:name="${CFBundleName}" \
		${JVMOptionsArr:+"${JVMOptionsArr[@]}" }\
		${JVMDefaultOptions:+$JVMDefaultOptions }\
		"${JVMMainClass}"\
		${MainArgsArr:+ "${MainArgsArr[@]}"}\
		${ArgsPassthru:+ "${ArgsPassthru[@]}"}
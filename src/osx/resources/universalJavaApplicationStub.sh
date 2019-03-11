#!/bin/sh
##################################################################################
#                                                                                #
# universalJavaApplicationStub                                                   #
#                                                                                #
#                                                                                #
# A shellscript JavaApplicationStub for Java Apps on Mac OS X                    #
# that works with both Apple's and Oracle's plist format.                        #
#                                                                                #
# Inspired by Ian Roberts stackoverflow answer                                   #
# at http://stackoverflow.com/a/17546508/1128689                                 #
#                                                                                #
#                                                                                #
# @author    Tobias Fischer                                                      #
# @url       https://github.com/tofi86/universalJavaApplicationStub              #
# @date      2015-05-15                                                          #
# @version   0.9.0                                                               #
#                                                                                #
#                                                                                #
##################################################################################
#                                                                                #
#                                                                                #
# The MIT License (MIT)                                                          #
#                                                                                #
# Copyright (c) 2015 Tobias Fischer                                              #
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




#
# resolve symlinks
#####################

PRG=$0

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
        PRG="$link"
    else
        PRG="`dirname "$PRG"`/$link"
    fi
done

# set the directory abspath of the current shell script
PROGDIR=`dirname "$PRG"`




#
# set files and folders
############################################

# the absolute path of the app package
cd "$PROGDIR"/../../
AppPackageFolder=`pwd`

# the base path of the app package
cd ..
AppPackageRoot=`pwd`

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



#
# read Info.plist and extract JVM options
############################################


# read the program name from CFBundleName
CFBundleName=`/usr/libexec/PlistBuddy -c "print :CFBundleName" "${InfoPlistFile}"`

# read the icon file name
CFBundleIconFile=`/usr/libexec/PlistBuddy -c "print :CFBundleIconFile" "${InfoPlistFile}"`


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


# read Info.plist in Apple style if exit code returns 0 (true, :Java key is present)
if [ $exitcode -eq 0 ]; then

	# set Java and Resources folder
	JavaFolder="${AppleJavaFolder}"
	ResourcesFolder="${AppleResourcesFolder}"

	APP_PACKAGE="${AppPackageFolder}"
	JAVAROOT="${AppleJavaFolder}"
	USER_HOME="$HOME"


	# read the Java WorkingDirectory
	JVMWorkDir=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:WorkingDirectory" "${InfoPlistFile}" 2> /dev/null | xargs`
	
	# set Working Directory based upon Plist info
	if [[ ! -z ${JVMWorkDir} ]]; then
		WorkingDirectory="${JVMWorkDir}"
	else
		# AppPackageRoot is the standard WorkingDirectory when the script is started
		WorkingDirectory="${AppPackageRoot}"
	fi

	# expand variables $APP_PACKAGE, $JAVAROOT, $USER_HOME
	WorkingDirectory=`eval "echo ${WorkingDirectory}"`


	# read the MainClass name
	JVMMainClass=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:MainClass" "${InfoPlistFile}" 2> /dev/null`

	# read the JVM Options
	JVMOptions=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:Properties" "${InfoPlistFile}" 2> /dev/null | grep " =" | sed 's/^ */-D/g' | tr '\n' ' ' | sed 's/  */ /g' | sed 's/ = /=/g' | xargs`

	# read StartOnMainThread
	JVMStartOnMainThread=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:StartOnMainThread" "${InfoPlistFile}" 2> /dev/null`
	if [ "${JVMStartOnMainThread}" == "true" ]; then
		echo ${JVMStartOnMainThread} > ~/Desktop/test.txt
		JVMOptions+=" -XstartOnFirstThread"
	fi

	# read the ClassPath in either Array or String style
	JVMClassPath_RAW=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:ClassPath" "${InfoPlistFile}" 2> /dev/null`
	if [[ $JVMClassPath_RAW == *Array* ]] ; then
		JVMClassPath=.`/usr/libexec/PlistBuddy -c "print ${JavaKey}:ClassPath" "${InfoPlistFile}" 2> /dev/null | grep "    " | sed 's/^ */:/g' | tr -d '\n' | xargs`
	else
		JVMClassPath=${JVMClassPath_RAW}
	fi
	# expand variables $APP_PACKAGE, $JAVAROOT, $USER_HOME
	JVMClassPath=`eval "echo ${JVMClassPath}"`

	# read the JVM Default Options
	JVMDefaultOptions=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:VMOptions" "${InfoPlistFile}" 2> /dev/null | xargs`

	# read the JVM Arguments
	JVMArguments=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:Arguments" "${InfoPlistFile}" 2> /dev/null | xargs`

    # read the Java version we want to find
    JVMVersion=`/usr/libexec/PlistBuddy -c "print ${JavaKey}:JVMVersion" "${InfoPlistFile}" 2> /dev/null | xargs`

# read Info.plist in Oracle style
else

	# set Working Directory and Java and Resources folder
	JavaFolder="${OracleJavaFolder}"
	ResourcesFolder="${OracleResourcesFolder}"
	WorkingDirectory="${OracleJavaFolder}"

	APP_ROOT="${AppPackageFolder}"

	# read the MainClass name
	JVMMainClass=`/usr/libexec/PlistBuddy -c "print :JVMMainClassName" "${InfoPlistFile}" 2> /dev/null`

	# read the JVM Options
	JVMOptions=`/usr/libexec/PlistBuddy -c "print :JVMOptions" "${InfoPlistFile}" 2> /dev/null | grep " -" | tr -d '\n' | sed 's/  */ /g' | xargs`
	# replace occurances of $APP_ROOT with it's content
	JVMOptions=`eval "echo ${JVMOptions}"`

	JVMClassPath="${JavaFolder}/*"

	# read the JVM Default Options
	JVMDefaultOptions=`/usr/libexec/PlistBuddy -c "print :JVMDefaultOptions" "${InfoPlistFile}" 2> /dev/null | grep -o "\-.*" | tr -d '\n' | xargs`

	# read the JVM Arguments
	JVMArguments=`/usr/libexec/PlistBuddy -c "print :JVMArguments" "${InfoPlistFile}" 2> /dev/null | tr -d '\n' | sed -E 's/Array \{ *(.*) *\}/\1/g' | sed 's/  */ /g' | xargs`
	# replace occurances of $APP_ROOT with it's content
	JVMArguments=`eval "echo ${JVMArguments}"`
fi




#
# find installed Java versions
#################################

# first check system variable "$JAVA_HOME"
if [ -n "$JAVA_HOME" ] ; then
	JAVACMD="$JAVA_HOME/bin/java"
	
# check for specified JVMversion in "/usr/libexec/java_home" symlinks
elif [ ! -z ${JVMVersion} ] && [ -x /usr/libexec/java_home ] && /usr/libexec/java_home -F; then

	if /usr/libexec/java_home -F -v ${JVMVersion}; then
		JAVACMD="`/usr/libexec/java_home -F -v ${JVMVersion} 2> /dev/null`/bin/java"
	else
		# display error message with applescript
		osascript -e "tell application \"System Events\" to display dialog \"ERROR launching '${CFBundleName}'\n\nNo suitable Java version found on your system!\nThis program requires Java ${JVMVersion}\nMake sure you install the required Java version.\" with title \"${CFBundleName}\" buttons {\" OK \"} default button 1 with icon path to resource \"${CFBundleIconFile}\" in bundle (path to me)"
		# exit with error
		exit 3
	fi

# otherwise check "/usr/libexec/java_home" symlinks
elif [ -x /usr/libexec/java_home ] && /usr/libexec/java_home -F; then
	JAVACMD="`/usr/libexec/java_home 2> /dev/null`/bin/java"

# otherwise check Java standard symlink (old Apple Java)
elif [ -h /Library/Java/Home ]; then
	JAVACMD="/Library/Java/Home/bin/java"

# fallback: public JRE plugin (Oracle Java)
else
	JAVACMD="/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java"
fi

# fallback fallback: /usr/bin/java
# but this would prompt to install deprecated Apple Java 6




#
# execute JAVA commandline and do some pre-checks
####################################################

# display error message if MainClassName is empty
if [ -z ${JVMMainClass} ]; then
	# display error message with applescript
	osascript -e "tell application \"System Events\" to display dialog \"ERROR launching '${CFBundleName}'!\n\n'MainClass' isn't specified!\nJava application cannot be started!\" with title \"${CFBundleName}\" buttons {\" OK \"} default button 1 with icon path to resource \"${CFBundleIconFile}\" in bundle (path to me)"
	# exit with error
	exit 2


# check whether $JAVACMD is a file and executable
elif [ -f "$JAVACMD" ] && [ -x "$JAVACMD" ] ; then

	# enable drag&drop to the dock icon
	export CFProcessPath="$0"

	# change to Working Directory based upon Apple/Oracle Plist info
	cd "${WorkingDirectory}"

	# execute Java and set
	#	- classpath
	#	- dock icon
	#	- application name
	#	- JVM options
	#	- JVM default options
	#	- main class
	#	- JVM arguments
	exec "$JAVACMD" \
			-cp "${JVMClassPath}" \
			-Xdock:icon="${ResourcesFolder}/${CFBundleIconFile}" \
			-Xdock:name="${CFBundleName}" \
			${JVMOptions:+$JVMOptions }\
			${JVMDefaultOptions:+$JVMDefaultOptions }\
			${JVMMainClass}\
			${JVMArguments:+ $JVMArguments}


else

	# display error message with applescript
	osascript -e "tell application \"System Events\" to display dialog \"ERROR launching '${CFBundleName}'!\n\nYou need to have JAVA installed on your Mac!\nVisit http://java.com for more information...\" with title \"${CFBundleName}\" buttons {\" OK \"} default button 1 with icon path to resource \"${CFBundleIconFile}\" in bundle (path to me)"

	# and open java.com
	open http://java.com

	# exit with error
	exit 1
fi

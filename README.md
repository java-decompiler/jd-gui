# JD-GUI

JD-GUI, a standalone graphical utility that displays Java sources from CLASS files.

![](http://jd.benow.ca/img/screenshot17.png)

- Java Decompiler projects home page: [http://java-decompiler.github.io](http://java-decompiler.github.io)
- Java Decompiler Wikipedia page: [http://en.wikipedia.org/wiki/Java_Decompiler](http://en.wikipedia.org/wiki/Java_Decompiler)
- JD-GUI source code: [https://github.com/java-decompiler/jd-gui](https://github.com/java-decompiler/jd-gui)

## Description
JD-GUI is a standalone graphical utility that displays Java source codes of 
".class" files. You can browse the reconstructed source code with the JD-GUI
for instant access to methods and fields.

## How to build JD-GUI ?
```
> ./gradlew build 
```
generate _"build/libs/jd-gui-x.y.z.jar"_ and _"build/distributions/jd-gui-osx-x.y.z.tar"_
```
> iexplore https://sourceforge.net/projects/launch4j/files/launch4j-3/3.12/launch4j-3.12-win32.zip/download
> unzip launch4j-3.12-win32.zip
> ./gradlew -DLAUNCH4J_HOME=.../path/to/launch4j-3.12-win32 build
```
generate _"build/distributions/jd-gui-windows-x.y.z.zip"_
```
> ./gradlew buildDeb
```
generate _"build/distributions/jd-gui-x.y.z.deb"_ for Ubuntu and Debian
```
> ./gradlew buildRpm
```
generate _"build/distributions/jd-gui-x.y.z.rpm"_ for RedHat, CentOS and Fedora

## How to launch JD-GUI ?
- Double-click on _"jd-gui-x.y.z.jar"_
- Double-click on _"JD-GUI"_ application from Mac OSX
- Double-click on _"jd-gui.exe"_ application from Windows
- Execute _"java -jar jd-gui-x.y.z.jar"_ or _"java -classpath jd-gui-x.y.z.jar org.jd.gui.App"_

## How to use JD-GUI ?
- Open a file with menu "File > Open File..."
- Open recent files with menu "File > Recent Files"
- Drag and drop files from your file explorer

## How to extend JD-GUI ?
```
> ./gradlew idea 
```
generate Idea Intellij project
```
> ./gradlew eclipse
```
generate Eclipse project
```
> java -classpath jd-gui-x.y.z.jar;myextension1.jar;myextension2.jar org.jd.gui.App
```
launch JD-GUI with your extensions

## How to uninstall JD-GUI ?
- Java: Delete "jd-gui-x.y.z.jar" and "jd-gui.cfg".
- Mac OSX: Drag and drop "JD-GUI" application into the trash.
- Windows: Delete "jd-gui.exe" and "jd-gui.cfg".

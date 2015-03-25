#JD-GUI

JD-GUI, a standalone graphical utility that displays Java sources from CLASS files.

![](http://jd.benow.ca/img/screenshot17.png)

- Java Decompiler projects home page: [http://jd.benow.ca](http://jd.benow.ca)
- Java Decompiler Wikipedia page: [http://en.wikipedia.org/wiki/Java_Decompiler](http://en.wikipedia.org/wiki/Java_Decompiler)
- JD-GUI source code: [https://github.com/java-decompiler/jd-gui](https://github.com/java-decompiler/jd-gui)

##Description
JD-GUI is a standalone graphical utility that displays Java source codes of 
".class" files. You can browse the reconstructed source code with the JD-GUI
for instant access to methods and fields.

##How to build JD-GUI ?
```
> gradle build 
```
generate _"build/libs/jd-gui-x.y.z.jar"_
```
> gradle build installOsxDist
```
generate _"build/install/jd-gui-osx/JD-GUI.app"_

##How to launch JD-GUI ?
- Double-click on _"jd-gui-x.y.z.jar"_
- Double-click on _"JD-GUI"_ application under OSX
- Execute _"java -jar jd-gui-x.y.z.jar"_ or _"java -classpath jd-gui-x.y.z.jar jd.gui.App"_

##How to use JD-GUI ?
- Open a file with menu "File > Open File..."
- Open recent files with menu "File > Recent Files"
- Drag and drop files from your file explorer

##How to extend JD-GUI ?
```
> gradle idea 
```
generate Idea Intellij project
```
> gradle eclipse
```
generate Eclipse project
```
> java -classpath jd-gui-x.y.z.jar;myextension1.jar;myextension2.jar jd.gui.App
```
launch JD-GUI with your extensions

##Uninstallation
- Delete "jd-gui-x.y.z.jar" and "jd-gui.cfg".
- Drag and drop "JD-GUI" application into the trash.

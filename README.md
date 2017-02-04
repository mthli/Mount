App Mount
===

![App Mount](https://github.com/mthli/Mount/blob/master/app/src/main/res/mipmap-xhdpi/ic_launcher.png "App Mount")

mount/umount apps that you don't like on Android, **without** root.

mount: available for use as normal.

umount: unavailable for use, but the data and actual package file remain.

You can run app temporary, when screen off app will be automatically umounted.

**Support Android Lollipop+**.

## Tutorial | 教程

 0. [Download](https://github.com/mthli/Mount/releases "mthli/Mount/releases") and install App Mount;
 
    [下载](https://github.com/mthli/Mount/releases "mthli/Mount/releases")并安装应用挂载器；

 1. System Settings > Accounts, remove all accounts here;

    系统设置 > 帐号，移除当前所有帐号；

 2. Run adb command below in your computer, once and for all:
 
    在电脑上运行如下 adb 命令，除非卸载重装，否则只需要授权一次即可：

    `adb shell dpm set-device-owner io.github.mthli.mount/.app.MountReceiver`

 3. Goto App Mount's settings, allow UsageAccess/NotificationAccess for ignore;
 
    前往应用挂载器的设置界面，为忽略选项进行授权；

 4. In App Mount, click list item, dialog right-top switch mount/umount.
 
    在应用挂载器中点击列表项，在出现的对话框右上角进行挂载/弹出操作。

Cheers🍻 

## Thanks

 - [ReactiveX/RxJava](https://github.com/ReactiveX/RxJava "ReactiveX/RxJava"), *Apache License, Version 2.0*.

 - [ReactiveX/RxAndroid](https://github.com/ReactiveX/RxAndroid "ReactiveX/RxAndroid"), *Apache License, Version 2.0*.

 - [satyan/sugar](https://github.com/satyan/sugar "satyan/sugar"), *The MIT License*.

## License

    App Mount, mount/umount apps that you don't like on Android, without root.
    Copyright (C) 2017 Matthew Lee

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
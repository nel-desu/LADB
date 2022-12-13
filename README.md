自用 ADB 工具，可以在手机上完成一些简单的操作，基于 [LADB](https://github.com/tytydraco/LADB) 修改

### 连接设备

```
adb connect x.x.x.x
```

根据 IP 地址无线连接设备

```
adb disconnect
```

断开所有连接

### 安装卸载

```
adb uninstall x.x.x
```

通过输入包名卸载程序

```
adb install -r x.x.x
```

点击 `安装` 按钮会打开文件选择，需要选择一个 `.apk` 文件进行安装

### 屏幕截图

```
adb shell screencap -p /sdcard/screenshot.png
```

截取屏幕图像到设备本地

```
adb pull /sdcard/screenshot.png (screenshotFile.absolutePath)
```

然后拉取到本机

### 查看日志

```
adb logcat
```

日志会显示在界面中，同时输出到临时文件

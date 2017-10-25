# IntelliJ CPU Usage Indicator plugin
        
CPU Usage Indicator in the status bar, just like the Memory Indicator.<br/>
Also contains actions for performance problems diagnostics.<br/>
- left click on the panel generates a thread dump (useful when the IDE is doing something on the background and you want to know what).<br/>
- configuration GUI for Performance Watcher (IDE's bundled automatic thread dumper for frozen UI). 
You can configure it to dump even for shorter freezes than is the default: 5 second.  
<br/>

Note:<br/>
-it is painted in a background thread, not EDT, so it will update even when the GUI is stuck<br/>
-getting the process usage is a quite expensive operation :(<br/>


![screenshot](https://github.com/krasa/CpuUsageIndicator/blob/master/cpuUsage.png)

![screenshot](https://github.com/krasa/CpuUsageIndicator/blob/master/dumps.png)


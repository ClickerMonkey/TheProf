TheProf
=======

![Stable](http://i4.photobucket.com/albums/y123/Freaklotr4/stage_stable.png)

A simple Java Profiler that outputs the performance of your application into CSV files.

### Download

* [Latest Version](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/TheProf/raw/master/build/theprof.jar)

### How does it work?

In a properties file, you specify the classes/packages/methods to include/exclude from profiling. When a class is first loaded
and the class is marked as profilable, byte code will be injected in the methods of the class.

### What does it measure per method?

1. Number of invocations
2. The total execution time of the method (via average, variance, min, max, and importance)
3. The method execution time - this is the total time in the method MINUS the time of any method calls made that are also being profiled. This helps figure out whether a method is slow or whether it's fine, and the method's it calls are the problem.  (via average, variance, min, max, and importance)

### How is variance calculated?

It's not true variance, it's the average of the difference between the min, max, and average.

### How is importance calculated and why is it important?

Importance is "# of invocations" * "average time", this helps you determine which methods should be improved.

### What is delay?

Because the JVM will wait until a method is executed X number of times before it tries to improve it's execution speed (with JIT), you can set an invocation delay to avoid tracking statistics for the first X method invocations.

### What's the format of the configuration file?

```xml
<?xml version="1.0" encoding="UTF-8"?>
<theprof>
  <properties>
    <property name="mode" value="CSV" /> <!-- GUI, CONSOLE -->
  </properties>
   	
  <inclusion-filters>
    <inclusion pattern="org.magnos.entity.Entity" /> <!-- include class to include methods -->
    <inclusion pattern="org.magnos.entity.Entity#get(Component)" delay="100000" /> <!-- set delay -->
    <inclusion pattern="org.magnos.entity.Entity#set(Component,Object)" delay="9000" />
  </inclusion-filters>
  	
  <exclusion-filters>
    <exclusion pattern="org.magnos.entity.EntityCore" /> <!-- is included based on first pattern, but we want' to ignore it -->
    <exclusion pattern="org.magnos.entity.EntityUtility" />
  </exclusion-filters>
</theprof>
```

### How do I use this?

You add the following VM arguments:
> -javaagent:theprof.jar=theprof.xml

Where theprof.jar is the location of a TheProf build, and theprof.properties is the location of the properties file containing the class to include or exclude.

### Where do the profiling statistics go?

In the CWD directory of the application, a stats directory will be created which contains a CSV file for each profiled class and a CSV file that has all statistics.

### TODO

* Add a live statistics viewing option

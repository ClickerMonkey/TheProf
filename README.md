TheProf
=======

A simple Java Profiler that outputs the performance of your application into CSV files.

### How does it work?

In a properties file, you specify the classes/packages to include/exclude from profiling. When a class is first loaded
and the class is marked as profilable, byte code will be injected in the methods of the class.

### What does it measure per method?

1. Number of invocations
2. The total execution time of the method (via average, variance, min, max, and importance)
3. The method execution time - this is the total time in the method MINUS the time of any method calls made that are also being profiled. This helps figure out whether a method is slow or whether it's fine, and the method's it calls are the problem.  (via average, variance, min, max, and importance)

### How is variance calculated?

It's not true variance, it's meerly the average of the difference between the min, max, and average.

### How is importance calculated and why is it important?

Importance is "# of invocations" * "average time", this helps you determine which methods should be improved.

### What's the format of the properties file?

> org.magnos.=true  
> org.magnos.prof.=false

Where any class that starts with "org.magnos." will be profiled, except if it starts with "org.magnos.prof."

### How do I use this?

You add the following VM arguments:
> -javaagent:theprof.jar=theprof.properties

Where theprof.jar is the location of a TheProf build, and theprof.properties is the location of the properties file containing the class to include or exclude.

### Where do the profiling statistics go?

In the CWD directory of the application, a stats directory will be created which contains a CSV file for each profiled class and a CSV file that has all statistics.

### TODO

* Add a live statistics viewing option
* Add a property to delay statistical measurement until a method has been called X number of times (to avoid skewing average and maximum with measurements that involve class loading and non-JIT compiled code).

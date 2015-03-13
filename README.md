Roucairol-Carvalho 
==================
Implement a mutual exclusion service using Roucairol and Carvalho’s distributed mutual exclusion
algorithm. Your service should provide two function calls to the application: cs-enter and cs-leave.
The first function call cs-enter allows an application to request permission to start executing its
critical section. The function call is blocking and returns only when the invoking application can
execute its critical section. The second function call cs-leave allows an application to inform the
service that it has finished executing its critical section.
Implementation Details: Design your program so that each process or node consists of two
separate modules–one module that implements the application (requests and executes critical sections)
and one module that implements the mutual exclusion algorithm (coordinates critical section
executions of all processes so that they do not overlap). Intuitively, the two modules interact using
cs-enter and cs-leave function calls. Each module in turn may be implemented using one or more
threads. It should be possible to swap your application module with our own application module and
your program should still compile and run correctly!

####Testing:
Design a mechanism to test the correctness of your implementation. Your testing mechanism
should ascertain that at most one process is in its critical section at any time. It should
be as automated as possible and should require minimal human intervention. For example, visual
inspection of log files to verify the correctness of the execution will not be acceptable. You will be
graded on how accurate and automated your testing mechanism is

##Complexity

##Install

This library has the implementation based on Dimacs graph input


  
##Project Contributor(s)

* Dinesh Appavoo ([@DineshAppavoo](https://twitter.com/DineshAppavoo))
* Rahul Nair
* Anukul Kumar

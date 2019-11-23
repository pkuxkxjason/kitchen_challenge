CSS Coding Challenge
====================

How to run?
-----------
Use the following command to run:
```
./gradlew run
```

How to run unit tests?
----------------------
Use the following command to run all unit tests:
```
./gradlew test
```

How to run stress tests?
------------------------
Use **TestUtils.java** to generate large test cases for stress testing. We can handle **millions** of operations per 
second. Here's the instruction to do stress testing:
* Lower **ORDER_DISPATCH_DURATION** in **OrdersDispatcher.java**
* Raise **DRIVERS_PER_SECONDS** in **DriverDispatcher.java**
  * The current algorithm to generate Possion is not very scalable.

Extra Credit
------------
* Pass a custom **Evaluator** to **Shelf** to customize the decay formula.
* Orders and drivers are both dispatched asynchronously using 2 **ScheduledExecutorService**.

Design Considerations
---------------------
* Streaming GSON
  * We use streaming GSON to avoid reading the whole file and keeping it in memory.

* Shelf selection
  * We sort the shelves by their **coefficient** so that we can simply loop through to select the suitable one. 
  Given that the number of shelves tends to be small, this is the best solution. If we need to support more 
  shelves, switching to a faster lookup solution might be desirable.

* Remove expired orders
  * Each shelf keeps a **PriorityQueue** of orders based on their expiration. The orders are removed lazily to 
  achieve amortized O(logn).

* Order lifecyle
  * We could make it safer by using different classes to represent the lifecycle of the order. However, this will 
  lead to more data-copying. We traded-off between safety for performance here.

* Generic
  * We could also use Generic to distinguish between different types of **Order**.
  * **Pros**
    * Compile-time safety.
  * **Cons**
    * Harder to scale to different types of orders and shelves.

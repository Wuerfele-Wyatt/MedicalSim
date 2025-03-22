This is a program simulating both a single server and dual server hospital
Patients arrive at the facility with a specific ailment, either bleeding, heart problems, or gastro problems.
If one of the servers is empty and no one is in line they move immediately to that server. Otherwise
they are sorted in the line based on ailment first, then by time spent in line. Heart patients go first, then
bleeders, then gastro. After patients spent a random amount of time in line they may die and balk.
 This time is based off of their ailment and is calculated using standard deviation.
There is also a different amount of time it takes to treat them based off ailment.
The simulation has the following events
Event Type                      Description
1                               Patient Arrives ar facility
2                               Patient enters line at facility
3                               Patient enters service bay 1
4                               Patient enters service bay 2
5                               Patient leaves service bay 1
6                               Patient leaves service bay 2
7                               Patient dies, balks, and leaves waiting line
8                               Simulation shut down

The simulation has two main structures
The event queue, a set of ordered (time lower to higher) of linked lists w/ event objects
The waiting line, a set of linked (via linked list) objects representing patients

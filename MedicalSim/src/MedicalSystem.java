/*This is a program simulating both a single server and dual server hospital
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
 */



 import java.util.ArrayList;

 public class MedicalSystem {
     public static void main(String[] args) {
         double bigTime = 0.0; //simulation main clock in minutes
         double eventTime; //event time
         double deltaTime;
 
         //set balk times for each ailment
         double balkTime = 0.0;
 
         //create event manager and queue simulation manager
         GenericManager<Event> EventQueue = new GenericManager<Event>();
         GenericManager<Customer> MyQueue = new GenericManager<Customer>();
 
         int myBalkId = 0; //unique balk id
         int numInQueue;
         int numInEvent; //number of events in event queue
         int balkAilmentStore;
         double bleedThruSystem = 0, heartThruSystem = 0, gasThruSystem = 0,
                 bleedThruLine = 0, heartThruLine = 0, gasThruLine = 0,
                 bleedThruFac = 0, heartThruFac = 0, gasThruFac = 0;
         double totalTimeInLine =0.0,
                 totalTimeInSystem = 0.0,
                 totalTimeInServers = 0.0,
                 totalTimeInServers2 = 0.0, totalTimeInLine2 = 0.0, totalTimeInSystem2 = 0.0;
         double ttil, ttis;
         int myBCust; //this is the customer id from a balk event
         int bleedBalkCount = 0, heartBalkCount = 0, gasBalkCount = 0;//the number of customers balked
         boolean busy1 = true;
         boolean busy2 = false;
         Customer served1 = new Customer(-9, generateAilment()); //customer in server 1
         Customer served2 = new Customer(-9, generateAilment()); //customer in server 2
 
         //create service time
         double deltaTimeServe = 0;
 
         //create arrive times
         double deltaTimeArrive;
         Customer newCust = new Customer(-9, generateAilment());//this is a work customer obj for those entering queue
         Customer workCust = new Customer(-9, generateAilment());//this is a work customer object for those coming from queue
 
         //create last event of simulation
         Event workEvent = new Event(8, 6000.0, 0);
         //add the event in queue
         numInEvent = EventQueue.addInOrder(workEvent);
         //add the arrival for the first customers
         deltaTimeArrive = TimeToArriveOrServe(0.05);//customers arrive at a rate of 3/60 min
 
         //the event time is current time plus delta time
         eventTime = bigTime + deltaTimeArrive;
         System.out.println("The first customer arrives at " + eventTime);
 
         //create event for the first customer to arrive
         workEvent = new Event(1, eventTime, 0);
 
         //store event in queue
         numInEvent = EventQueue.addInOrder(workEvent);
 
         //begin processing events
         //get first event off of event queue
         workEvent = EventQueue.getValue(0);
         while (workEvent.getEventType() != 8) {
             //valid event requires time update
             deltaTime = workEvent.getTime() - bigTime;
             //update everyone with this deltatime
             ttil = UpdateCustomer(MyQueue, deltaTime);
             totalTimeInLine += ttil;
             totalTimeInLine2 += ttil * ttil;
 
             //update everyone in servers
             ttis = UpdateServers(served1, busy1, served2, busy2, deltaTime);
 
             //get event type and process it. First update time
             totalTimeInServers += ttis;
             totalTimeInServers2 += ttis * ttis;
             bigTime = workEvent.getTime();
 
             if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                 System.out.println("*****THE TIME IS NOW " + bigTime + "*****");
                 System.out.println();
             }
             //get number in customer queue
             numInQueue = MyQueue.getCount();
             switch (workEvent.getEventType()) {
                 case 1: //customer arrives at facility
                     //generate cust obj
                     //if servers are buys put customer in line event
                     //if servers not buys, put customer in server
 
                     if ((!busy1) && (numInQueue <= 0)) {
                         //server 1 no tbusy and no one in queue
                         if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                             System.out.println("cust goes into SERVER1");
                             System.out.println();
                         }
                         newCust = new Customer(-9, generateAilment());
                         //set arrival time for customer
                         newCust.setArrive(bigTime);
                         //put this customer in server 1
                         busy1 = true;
                         served1 = newCust;
                         //generate the finished server event for this customer
                         if (newCust.getAilment() == 0) {
                             deltaTimeServe = TimeToArriveOrServe(0.03);//heart - 2/60 minutes
                         } else if (newCust.getAilment() == 1) {
                             deltaTimeServe = TimeToArriveOrServe(0.1);//bleed - 6/60 minutes
                         } else if (newCust.getAilment() == 2) {
                             deltaTimeServe = TimeToArriveOrServe(0.067);//gastro - 4/60 minutes
                         }
                         eventTime = deltaTimeServe + bigTime;
                         workEvent = new Event(5, eventTime, -9);
 
                         //put this event in event queue
                         numInEvent = EventQueue.addInOrder(workEvent);
                     }//done in server 1
 
                     else if ((busy1) && (!busy2) && (numInQueue <= 0)) {
                         //server 2 is open and no one in queue
                         if ((bigTime >= 500.00) && bigTime <= 600.00) {
                             System.out.println("customer goes into SERVER2");
                             System.out.println();
                         }
                         newCust = new Customer(-9, generateAilment());
                         //set arrival time for customer
                         newCust.setArrive(bigTime);
                         //put this customer in server 1
                         busy2 = true;
                         served2 = newCust;
                         //generate the finished server event for this customer
                         if (newCust.getAilment() == 0) {
                             deltaTimeServe = TimeToArriveOrServe(0.03);//heart - 2/60 minutes
                         } else if (newCust.getAilment() == 1) {
                             deltaTimeServe = TimeToArriveOrServe(0.1);//bleed - 6/60 minutes
                         } else if (newCust.getAilment() == 3) {
                             deltaTimeServe = TimeToArriveOrServe(0.067);//gastro - 4/60 minutes
                         }
                         eventTime = deltaTimeServe + bigTime;
                         workEvent = new Event(6, eventTime, -9);
 
                         //put this event in event queue
                         numInEvent = EventQueue.addInOrder(workEvent);
                     }//done in server 2
 
                     else if (busy1 && busy2) {
                         //both servers are busy
                         //first generate customer, note customer must have unique id
                         if ((bigTime >= 500.00) && bigTime <= 600.00) {
                             System.out.println("customer goes into line");
                             System.out.println();
                         }
                         myBalkId++;
                         newCust = new Customer(myBalkId, generateAilment());
 
                         //set arrival time
                         newCust.setArrive(bigTime);
 
                         //not put the customer in line and sort the line
                         MyQueue.addAtEnd(newCust);
                         MyQueue.manageAndSort();
 
                         //now create customer balk event based on ailment
                         //TODO use standard deviation to calculate balk time
                         if (newCust.getAilment() == 0) {
                             balkTime = bigTime + generateCriticalPeriod(35.0, 10.0);
                             //heart, mean of 35 minutes, std of 10 minutes
                         } else if (newCust.getAilment() == 1) {
                             balkTime = bigTime + generateCriticalPeriod(60.0, 20.0);
                             //bleed, mean of 60 minutes, std of 20 minutes
                         } else if (newCust.getAilment() == 2) {
                             balkTime = bigTime + generateCriticalPeriod(80.0, 30.0);
                             //gastro, mean of 80 minutes, std of 30 minutes
                         }
 
                         //now create event
                         workEvent = new Event(7, balkTime, myBalkId);
                         //add event to event queue
                         numInEvent = EventQueue.addInOrder(workEvent);
 
                     }//the customer is in the line
                     //generate event for the next customer tro arrive
                     deltaTimeArrive = TimeToArriveOrServe(0.05);//customers arrive at a rate of 3/60 min
                     //the event time is current time plus delta time
                     eventTime = bigTime + deltaTimeArrive;
 
                     if ((bigTime >= 500.00) && bigTime <= 600.00) {
                         System.out.println("The next cust arrives at " + eventTime);
                         System.out.println();
                     }
                     //create event for next customer to arrive
                     workEvent = new Event(1, eventTime, 0);
                     //store this event in queue
                     numInEvent = EventQueue.addInOrder(workEvent);
                     break;
 
                 case 2://customer enters line at facility
                     //generate balk event for this customer
                     System.out.println("This is event 2. Already incorporated in arrival event." +
                             "If you see this there is an error");
                     System.out.println();
                     break;
 
                 case 3: //customer enters service bay 1
                     //decrement the number in line
                     //generate completion time and departure event for cust
                     //set server to busy
                     numInQueue = MyQueue.getCount();
                     if (!busy1 && numInQueue > 0) {
                         //the customer can enter bay 1
                         if ((bigTime >= 500.00) && bigTime <= 600.00) {
                             System.out.println("First cust in line enters SERVER1");
                             System.out.println();
                         }
                         workCust = MyQueue.getValue(0);
                         myBCust = workCust.getMyBalk();//get the cust balk event
                         PurgeEvent(EventQueue, myBCust);//purge event from queue
 
 
                         //delete customer from the queue and put them in server
                         MyQueue.remove(0);
                         busy1 = true;
                         served1 = workCust;
                         //generate the finished server event for this customer
                         if (served1.getAilment() == 0) {
                             deltaTimeServe = TimeToArriveOrServe(0.03);//heart - 2/60 minutes
                             heartThruLine++;
                         } else if (served1.getAilment() == 1) {
                             deltaTimeServe = TimeToArriveOrServe(0.1);//bleed - 6/60 minutes
                             bleedThruLine++;
                         } else if (served1.getAilment() == 2) {
                             deltaTimeServe = TimeToArriveOrServe(0.067);//gastro - 4/60 minutes
                             gasThruLine++;
                         }
                         eventTime = deltaTimeServe + bigTime;
                         workEvent = new Event(5, eventTime, -9);
                         //put this event in event queue
                         numInEvent = EventQueue.addInOrder(workEvent);
                     }//end of enter service bay 1
                     //either we are busy and have had an event collision or there is no one in line
                     else {
                         System.out.println("in event 3 customer enters service bay 1" +
                                 "unable to process event ERROR");
                         System.out.println();
                     }
                     break;
 
                 case 4: //customer enters service bay 2
                     //decrement the number in line
                     //generate completion time and departure event for cust
                     //set server to busy
                     numInQueue = MyQueue.getCount();
                     if (!busy2 && numInQueue > 0) {
                         //the customer can enter bay 2
                         if ((bigTime >= 500.00) && bigTime <= 600.00) {
                             System.out.println("First cust in line enters SERVER2");
                             System.out.println();
                         }
                         workCust = MyQueue.getValue(0);
                         myBCust = workCust.getMyBalk();//get the cust balk event
                         PurgeEvent(EventQueue, myBCust);//purge event from queue
 
 
                         //delete customer from the queue and put them in server
                         MyQueue.remove(0);
                         busy2 = true;
                         served2 = workCust;
                         //generate the finished server event for this customer
                         if (served2.getAilment() == 0) {
                             deltaTimeServe = TimeToArriveOrServe(0.03);//heart - 2/60 minutes
                             heartThruLine++;
                         } else if (served2.getAilment() == 1) {
                             deltaTimeServe = TimeToArriveOrServe(0.1);//bleed - 6/60 minutes
                             bleedThruLine++;
                         } else if (served2.getAilment() == 2) {
                             deltaTimeServe = TimeToArriveOrServe(0.067);//gastro - 4/60 minutes
                             gasThruLine++;
                         }
                         eventTime = deltaTimeServe + bigTime;
                         workEvent = new Event(6, eventTime, -9);
                         //put this event in event queue
                         numInEvent = EventQueue.addInOrder(workEvent);
                     }//end of enter service bay 2
                     //either we are busy and have had an event collision or there is no one in line
                     else {
                         System.out.println("in event 4 customer enters service bay 2" +
                                 "unable to process event ERROR");
                         System.out.println();
                     }
                     break;
 
                 case 5: //customer leaves service bay 1
                 //update the number of customers through the system
                     //set server to not busy
                     //if there are people in line generate an enter service bay 1 event
                     busy1 = false;
                     if (served1.getAilment() == 0) {
                         heartThruSystem++;
                     }
                     else if (served1.getAilment() == 1) {
                         bleedThruSystem++;
                     }
                     else if (served1.getAilment() == 2) {
                         gasThruSystem++;
                     }
                     numInQueue = MyQueue.getCount();
                     if ((bigTime >= 500.00) && bigTime <= 600.00) {
                         System.out.println("Customer leaves server 1 w/ numInQueue " + numInQueue);
                         System.out.println();
                     }
                     if(numInQueue>0){
                         //there are customers in the line, generate a customer enter service bay 1 not at bigTime
                         workEvent = new Event(3, bigTime+0.01, -9);
 
                         //put this event in event queue
                         numInEvent = EventQueue.addInOrder(workEvent);
                     }
                     break;
 
                 case 6://customer leaves service bay 2
                     //update the number of customers through the system
                     //set server to not busy
                     //if there are people in line generate an enter service bay 1 event
                     busy2 = false;
                     if (served2.getAilment() == 0) {
                         heartThruSystem++;
                     } else if (served2.getAilment() == 1) {
                         bleedThruSystem++;
                     } else if (served2.getAilment() == 2) {
                         gasThruSystem++;
                     }
                     numInQueue = MyQueue.getCount();
                     if ((bigTime >= 500.00) && bigTime <= 600.00) {
                         System.out.println("Customer leaves server 2 w/ numInQueue " + numInQueue);
                         System.out.println();
                     }
                     if(numInQueue>0){
                         //there are customers in the line, generate a customer enter service bay 2 not at bigTime
                         workEvent = new Event(4, bigTime+0.01, -9);
 
                         //put this event in event queue
                         numInEvent = EventQueue.addInOrder(workEvent);
                     }
                     break;
 
                 case 7://customer dies and balks
                     //customer dies and leaves waiting line
                     //delete customer from line
                     //delete customer from queue
                     myBCust = workEvent.getMyCust();
                     //get this customer out of the line
                     balkAilmentStore = OutaHere(MyQueue, myBCust);
                     if (balkAilmentStore == 0) {
                         heartThruLine++;
                         heartBalkCount++;
                     } else if (balkAilmentStore == 1) {
                         bleedThruLine++;
                         bleedBalkCount++;
                     } else if (balkAilmentStore == 2) {
                         gasThruLine++;
                         gasBalkCount++;
                     }
                     //add this to the customers that have gone through the system
 
 
 
                     break;
 
                 case 8://shutdown event
                     System.out.println(" this event is type 8 and we are in the switch statement TROUBLE!");
                     System.out.println();
                     continue;
 
                 default:
                     System.out.println("this is a bad event type " + workEvent.getEventType() +
                             " at time " + workEvent.getTime());
                     System.out.println();
             }//end of switch case
             //this event is processed - delete it from event queue
             EventQueue.remove(0);
             //now to next event
             if ((bigTime >= 500.00) && (bigTime <= 600.00)) {
                 System.out.println("*****The Time is " + bigTime + "*****");
                 System.out.println();
             }
             workEvent = EventQueue.getValue(0);
         }//end of sim while
         System.out.println("*****Printing statistics for this run*****");
         //TODO print statistics for Number of heart Patients Serviced
         double avgBleed = totalTimeInLine/bleedThruLine;
         double avgHeart = totalTimeInLine/heartThruLine;
         double avgGas = totalTimeInLine/gasThruLine;
 
         double bleed2 = bleedThruLine*bleedThruLine;
         double heart2 = heartThruLine*heartThruLine;
         double gas2 = gasThruLine*gasThruLine;
 
         double varBleed = bleed2/totalTimeInLine-avgBleed*avgBleed;
         double varHeart = heart2/totalTimeInLine-avgHeart*avgHeart;
         double varGas = gas2/totalTimeInLine-avgGas*avgGas;
 
 
         System.out.println("Number of bleeders serviced: " + bleedThruSystem);
         System.out.println("Number of heart patients serviced: " + heartThruSystem);
         System.out.println("Number of gastros serviced: " + gasThruSystem);
         System.out.println();
         System.out.println("Number of bleeders lost: " + bleedBalkCount);
         System.out.println("Number of heart patients lost: " + heartBalkCount);
         System.out.println("Number of gastros lost: " + gasBalkCount);
         System.out.println();
         System.out.println("Average time a bleeder spent in queue " +avgBleed);
         System.out.println("Average time a heart patient spent in queue " +avgHeart);
         System.out.println("Average time gastros spent in queue " +avgGas);
         System.out.println();
         System.out.println("Variance for bleeders: " + varBleed);
         System.out.println("Variance for heart patients: " + varHeart);
         System.out.println("Variance for gastros: " + varGas);
 
     }//end of main
 
     public static int OutaHere(GenericManager<Customer> CustLine, int balkId) {
         //removes balking customer from Queue and return ailment type
         int i, numInLine, cBalkId;
         Customer workCust = new Customer(-9, generateAilment());
         //prepare to traverse customer line
         numInLine = CustLine.getCount();
         workCust = CustLine.getValue(0);
         cBalkId = workCust.getMyBalk();
         i = 0;
 
         while ((cBalkId != balkId) && (i <= (numInLine - 1))) {
             workCust = CustLine.getValue(i);
             cBalkId = workCust.getMyBalk();
             i++;
         }//end of while
 
         //remove customer i from line
         if (i == 0) {
             CustLine.remove(0);
         } else if ((cBalkId == balkId) && (i > 0)) {
             CustLine.remove(i - 1);
         }
         return workCust.getAilment();
     }//end of OutaHere
 
     public static void PurgeEvent(GenericManager<Event> EventQueue, int balkId) {
         //removes balking event from Event Queue
         int i, numInQueue, eBalkId;
         Event workEvent = new Event(1, 1.0, 1);
         //prepare to traverse event queue
         numInQueue = EventQueue.getCount();
         workEvent = EventQueue.getValue(0);
         eBalkId = workEvent.getMyCust();
         i = 0;
 
         while ((eBalkId != balkId) && (i <= (numInQueue - 1))) {
             workEvent = EventQueue.getValue(i);
             eBalkId = workEvent.getMyCust();
             i++;
         }//end of while
         //remove customer i from line
         if (eBalkId == balkId) {
             EventQueue.remove(i - 1);
         }
     }//end of PurgeEvent
 
     public static void PrintCustQueue(GenericManager<Customer> MyQueue) {
         int numInQueue;
         Customer workCust = new Customer(123, 0);
         numInQueue = MyQueue.getCount() - 1;
 
         for (int i = 0; i <= numInQueue; i++) {
             workCust = MyQueue.getValue(i);
         }
     }//end of PrintCustQueue
 
     public static void PrintEventQueue(GenericManager<Event> MyQueue) {
         int numInEvent;
         Event workEvent = new Event(1, 1.0, 1);
         numInEvent = MyQueue.getCount() - 1;
 
         for (int i = 0; i <= numInEvent; i++) {
             workEvent = MyQueue.getValue(i);
         }
     }//end of PrintEventQueue
 
     public static double TimeToArriveOrServe(double rate) {
         //random process to determine time to arrive or the service time
         double deltaTime;
         double bigx;
         bigx = Math.random();
         while (bigx > 0.9) {
             bigx = Math.random();
         }
         deltaTime = -Math.log(1.0 - bigx) / rate;
         //System.out.println("DEBUG: in time to arrive with rate " + rate + " the delta time is " + deltaTime +
         // " bigx is " + bigx);
         return deltaTime;
     }//end of TimeToArriveOrServe
 
     public static double UpdateCustomer(GenericManager<Customer> custLine, double deltaTime) {
         //this function adds all time spent for a customer in line for deltatime
         double lineTime = 0.0;
         int custInLine;
         custInLine = custLine.getCount();
 
         if (custInLine == 0) {
             return lineTime;
         } else {
             return lineTime = deltaTime * custInLine;
         }
     }//end of UpdateCustomer
 
     public static double UpdateServers(Customer s1, boolean b1, Customer s2, boolean b2,
                                        double deltaTime) {
         //this function updates the time to customers in servers
         double serveTime = 0.0;
         if (b1 && b2) {
             return serveTime = 2 * deltaTime;
         } else if (b1 || b2) {
             serveTime = deltaTime;
         }
         return serveTime;
     }//end of UpdateServers
 
     public static int generateAilment(){
     int ailment;
     int x;//random variant
 
         x = (int) (Math.random() * 100);
 
         if(x<=30) ailment = 0;//heart
         else if(x<=80) ailment = 1;//bleed
         else ailment = 2;//gastro
         return ailment;
     }//end of generateAilment
 
     public static double generateCriticalPeriod(double mean, double stDev){
         double critical;
 
         double u1 = Math.random();
         double u2 = Math.random();
 
         double z = Math.sqrt(-2 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
         critical = mean + z *stDev;
         return critical;
     }
 }//end of MedicalSystem
 
 class Customer implements Comparable {
     /*Customer class stores time the customer enters line, time in server
         and time in system. Also keeps a unique id for balk event
          */
     protected double timeInLine;
     protected double timeInServer;
     protected double timeInSystem;
     protected double timeArrive;
     protected int myBalk; //unique balk id
     protected int ailment;//0 for heart, 1 for bleed, 2 for gastro
 
     //create customer constructor
     public Customer(int x, int t) {
         timeInLine = timeInServer = timeInSystem = 0;
         myBalk = x;
         ailment = t;
     }//end of customer constructor
 
     public int compareTo(Object o) {
         if(getAilment() < ((Customer) o).getAilment()){
             if (getTimeInLine() > ((Customer) o).getTimeInLine()) {
                 return 1; //if ailments are in order and time in line is in order
             }
             else if (getTimeInLine() < ((Customer) o).getTimeInLine()) {
                 return -1; //if ailments are in order but time in line is not
             }
         }
         else if(getAilment() > ((Customer) o).getAilment()){
             return -1; //ailments are out of order
         }
 
         return 0;
     }//end of compareTo
 
     public void setArrive(double x) {
         //the time of arrival is set from x
         timeArrive = x;
     }//end of set arrive
 
     public void setInLine(double x) {
         //we add value of x. It is the del time
         timeInLine += x;
     }//end of setInLine
 
     public void setInServer(double x) {
         timeInServer += x;
     }//end of setInServer
 
     public void setBalk(int x) {
         myBalk = x;
     }//end of setBalk
 
     public void setAilment(int x){
         ailment = x;
     }
 
     //getter methods
     public double getTimeInLine() {
         return timeInLine;
     }
 
     public double getTimeInServer() {
         return timeInServer;
     }
 
     public double getTimeInSystem() {
         return timeInSystem;
     }
 
     public double getTimeArrive() {
         return timeArrive;
     }
 
     public int getMyBalk() {
         return myBalk;
     }
 
     public int getAilment(){return ailment;}
 
 }//end of Customer class
 
 class Event implements Comparable {
     /* Event holds event type, event time and a pointer to customer
     if a balk event occurs
      */
     protected int eventType; //event type
     protected double time; //time of the event
     protected int myCust; //unique id for balking customer
 
     public Event(int e, double t, int c) {
         eventType = e;
         time = t;
         if (e == 7) {
             //balk event
             myCust = c;
         } else {
             //not a balk event
             myCust = -9;
         }
     }//end of Event constructor
 
     public int compareTo(Object o) {
         if (getTime() > ((Event) o).getTime()) {
             return 1;//if time a > time b
         } else if (getTime() < ((Event) o).getTime()) {
             return -1;//if time a < time b
         } else return 0;
 
     }//end of compareTo
 
     public int getEventType() {
         return eventType;
     }
 
     public double getTime() {
         return time;
     }
 
     public int getMyCust() {
         return myCust;
     }
 }//end of Event
 
 class GenericManager<T extends Comparable> {
     protected ArrayList<T> myList = new ArrayList<T>();
     protected int count;//next available value in list
 
     public GenericManager() {
         count = 0;
     }//end of constructor
 
     public int addAtEnd(T x) {
         //places values at the end of list
         myList.add(count++, x);
         return count;
     }//end of addAtEnd
 
     public int getCount() {
         return count;
     }
 
     public int addInOrder(T x) {
         int i;
         //this adds objects from smallest to largest
         if ((count == 0) || ((x.compareTo(myList.get(0))) == -1)
                 || (x.compareTo(myList.get(0)) == 0)) {
             //if less than or equal to first entry
             myList.add(0, x);
         } else if ((x.compareTo(myList.get(count - 1)) == 1
                 || (x.compareTo(myList.get(count - 1)) == 0))) {
             //x is greater than last entry
             myList.add(count, x);
         } else {
             //the object is greater than the first and less than the last
             i = 0;
             while ((i < count) && (x.compareTo(myList.get(i)) == 1)) {
                 i++;
             }
             myList.add(i, x);
         }
         count++;
         return count;
     }//end of addInOrder
 
     public int addAtFront(T x) {
         // add object to front of list
         myList.add(0, x);
         count++;
         return count;
     }//end of addAtFront
 
     public T getValue(int i) {
         // gets value from list
         if (i < count) return myList.get(i);
         else {
             return myList.get(0);
         }
     }//end of getValue
 
     public void manageAndSort() {
         //generic sort that sorts everything but the objects
         //will sort an array of flat objects based on compareTo func
         T xsave, ysave, a, b;
         int isw = 1, xlast = myList.size();
         while (isw == 1) {
             isw = 0;
             for (int i = 0; i <= xlast - 2; i++) {
                 a = myList.get(i);
                 b = myList.get(i + 1);
                 switch (a.compareTo(b)) {
                     case 1://objects are in proper order
                         break;
                     case -1://objects are out of order
                         xsave = myList.get(i);
                         ysave = myList.get(i + 1);
                         myList.remove(i);
                         myList.add(i, ysave);
                         myList.remove(i + 1);
                         myList.add((i + 1), xsave);
                         isw = 1;
                         break;
                     default://objects are equal
                 }//end of switch
             }//end of for
         }//end of while
     }//end of manageAndSort
 
     public void remove(int i) {
         //removes value from index i
         if ((i >= 0) && (i <= count - 1)) {
             myList.remove(i);
             count--;
         }
     }//end of remove
 }//end of  GenericManager
 
 
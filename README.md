# Elevator Saga Solution

It is a solution to Elevator Saga. 

* http://play.elevatorsaga.com/

The code is [elevator-saga.js](./elevator-saga.js).

The success rate is as follows.

```
Challenge  1: 100.00 (200/200)
Challenge  2:  83.50 (167/200)
Challenge  3:  96.50 (193/200)
Challenge  4: 100.00 (200/200)
Challenge  5:  93.00 (186/200)
Challenge  6: 100.00 (200/200)
Challenge  7:  95.00 (190/200)
Challenge  8:  99.00 (198/200)
Challenge  9:  99.50 (199/200)
Challenge 10:  58.50 (117/200)
Challenge 11:  97.00 (194/200)
Challenge 12:  86.50 (173/200)
Challenge 13:  37.50 (75/200)
Challenge 14:  45.00 (90/200)
Challenge 15:  95.00 (190/200)
Challenge 16: 100.00 (200/200)
Challenge 17:  86.50 (173/200)
Challenge 18:  40.00 (80/200)
```

## Solution

The following is the implementation content.

### Basic

* The information that the button of the floor was pressed is kept as floor × direction (up or down).
    * Move a nearby stop elevator.
    * Once it stops on that floor, clear the information.
    * Check the information before passing through the floor, stop on the floor if the same button as the traveling direction is pressed and the passenger can still ride.
    * When the destination floor disappears, move to the floor closest to the current floor on the floor where the button was pressed.
* When the destination floor button in the elevator is pushed, reset the order in which it stops on the floor to the optimum order.

### Advanced

* From the destination candidate, exclude the floor where another elevator is about to stop next.
* At the timing when the passenger passes through the floor, if the passenger is 0, the movement destination is re-determined with the latest information.
* In Challenge #6 #7, move a lot of people on board.

Please check the details at the [code](./elevator-saga.js).

## Auto play

I created a tool to automatically run Elevator Saga.

* [Elevator Saga auto play](./elevator-saga-auto-paly/README.md).

You can measure benchmarks with this.

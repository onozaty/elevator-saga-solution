# elevator-saga-auto-play

This is an application that automatic run Elevator Saga.
Elevator Saga challenge be executed repeatedly with the contents of the specified file.

I created it for Elevator Saga benchmark!!

* [Elevator Saga \- the elevator programming game](https://play.elevatorsaga.com/)

## Usage

In the environment where Java is installed, build the application with the following command.

```
gradlew shadowJar
```

Specify the file writing the Elevator Sage program and execute it.

The following is an example of writing a program in a `elevator-saga.js`.

```
java -jar build/libs/elevator-saga-auto-play-all.jar -f elevator-saga.js
```

Chrome will come up and Elevator Saga will run.

When execution is completed, the result (success rate) of Challenge is output.

```
Start auto play.
* Play parallel    : false
* Challenge numbers: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18]
* Number of play   : 10
Finish. Total time: 493 seconds
--------------------------------------------
All         :  85.00 (153/180)
Challenge  1: 100.00 (10/10)
Challenge  2:  70.00 (7/10)
Challenge  3:  90.00 (9/10)
Challenge  4: 100.00 (10/10)
Challenge  5: 100.00 (10/10)
Challenge  6: 100.00 (10/10)
Challenge  7: 100.00 (10/10)
Challenge  8: 100.00 (10/10)
Challenge  9: 100.00 (10/10)
Challenge 10:  70.00 (7/10)
Challenge 11: 100.00 (10/10)
Challenge 12:  80.00 (8/10)
Challenge 13:  50.00 (5/10)
Challenge 14:  70.00 (7/10)
Challenge 15:  80.00 (8/10)
Challenge 16: 100.00 (10/10)
Challenge 17:  90.00 (9/10)
Challenge 18:  30.00 (3/10)
--------------------------------------------
```

## Command description

```
usage: java -jar elevator-saga-auto-play-all.jar [-c <challenges>] -f <file> [-n <numer>] [-p]
```

You can specify the following as an argument.

* `-f <file>`<br>
Program file path.
* `-c <challenges>`<br>
It is a Challenge to execute. Comma separated values are used. Default is execute all Challenge.<br>
`-c 1,2,3` play challenge #1, #2, #3.
* `-n <numer>`<br>
The number of times to repeat each Challenge. Defualt is repeat 10 times.
* `-p`<br>
It is parallel execution. Launch a browser for the number of CPU core and execute in parallel. This shortens the execution time.


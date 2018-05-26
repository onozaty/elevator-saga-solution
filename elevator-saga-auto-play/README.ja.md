# elevator-saga-auto-play

Elevator Sagaを自動実行するツールです。
指定したファイルの内容で、Elevator SagaのChallengeを繰り返し実行することができます。

Elevator Saga のベンチマークを取るために作りました。

* [Elevator Saga \- the elevator programming game](https://play.elevatorsaga.com/)

## 利用方法

Javaがインストールされた環境で、下記コマンドでアプリケーションをビルドします。

```
gradlew shadowJar
```

Elevator Sagaのプログラムを記載したファイルを指定して、Elevator Sagaの自動実行を開始します。
以下は、`elevator-saga.js`というファイルにプログラムを記載した際の例です。

```
java -jar build/libs/elevator-saga-auto-play-all.jar -f elevator-saga.js
```

Chromeが立ち上がり、Elevator Sagaの実行が行われます。
実行が完了すると、Challengeの結果(成功率)が出力されます。

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

## コマンドの説明

```
usage: java -jar elevator-saga-auto-play-all.jar [-c <challenges>] -f <file> [-n <numer>] [-p]
```

引数として下記を指定できます。

* `-f <file>`<br>
実行するプログラムが記載されたファイルのパスです。
* `-c <challenges>`<br>
実行するChallengeです。カンマ区切りで指定します。指定が無かった場合、全てのChallengeを実行します。`-c 1,2,3` とした場合、Challenge #1, #2, #3 が実行されます。
* `-n <numer>`<br>
各Challengeを繰り返す回数です。指定が無かった場合、10回繰り返します。
* `-p`<br>
並列実行です。CPU数分のブラウザを立ち上げて、並列で実行します。これにより実行時間が短縮できます。

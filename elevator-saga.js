{
    init: function(elevators, floors) {

        // 最小/最大フロア番号
        const minFloorNum = Math.min.apply(null, floors.map(floor => floor.floorNum()));
        const maxFloorNum = Math.max.apply(null, floors.map(floor => floor.floorNum()));

        // 各フロアのボタン押下状態(floorNumをキー)
        const floorButton = {};
        floors.forEach(floor => floorButton[floor.floorNum()] = {up: false, down: false});

        // -------------------------------
        // 関数
        // -------------------------------

        // ボタンが押下されているフロアの情報を取得する関数
        const getPushedFloorButtons = () => {
            
            const pushedFloorButtons = [];
            for (let floorNum = minFloorNum; floorNum <= maxFloorNum; floorNum++) {

                if (floorButton[floorNum].up) {
                    pushedFloorButtons.push({
                        floorNum: floorNum,
                        indicator: "up"
                    });
                }

                if (floorButton[floorNum].down) {
                    pushedFloorButtons.push({
                        floorNum: floorNum,
                        indicator: "down"
                    });
                }
            }
            
            return pushedFloorButtons;
        }

        // 他のエレベータが現在向かっているフロアの情報を取得する関数
        const getOtherElevatorGoFloors = (targetElevator) => {
            
            return elevators
                .filter(elevator => elevator != targetElevator)
                .map((otherElevator) => {
                    return {
                        elevatorNumber: otherElevator.number,
                        floorNum: otherElevator.destinationQueue[0],
                        up: otherElevator.goingUpIndicator(),
                        down: otherElevator.goingDownIndicator()
                    };
                });
        }

        // 指定したエレベータをボタンが押下されている他のフロアに移動させる関数
        const goToPressedFloor = (elevator, currentFloorNum) => {

            // いったん移動先をクリア
            elevator.destinationQueue = [];
            elevator.checkDestinationQueue();

            // フロアのボタン押下状況と、各エレベータが現在向かっているフロアの情報を元に
            // 候補となる移動先一覧を作成

            // 他のエレベータが現在向かっているフロアの情報
            const otherElevatorGoFloors = getOtherElevatorGoFloors(elevator);

            // 移動先候補
            const gotoChoices = getPushedFloorButtons().filter((pushedFloorButton) => {
                // 他のエレベータが向かっているフロア＆方向は除外
                return !otherElevatorGoFloors.some((otherElevatorGoFloor) => {
                    return pushedFloorButton.floorNum == otherElevatorGoFloor.floorNum
                            && ((pushedFloorButton.indicator == "up" && otherElevatorGoFloor.up)
                                || (pushedFloorButton.indicator == "down" && otherElevatorGoFloor.down));
                })
            });

            console.log(
                `elevator${elevator.number} [goToPressedFloor]`
                + ` otherElevatorGoFloors:${JSON.stringify(otherElevatorGoFloors)}`
                + ` floorButton:${JSON.stringify(floorButton)}`
                + ` gotoChoices:${JSON.stringify(gotoChoices)}`);

            if (gotoChoices.length == 0) {
                // 移動先候補無し
                return;
            }

            // 現在のフロアに一番近い移動先を探す
            const nearGotoChoice = gotoChoices.sort((gotoChoice1, gotoChoice2) => {

                // 現在フロアからの距離(絶対値)を求める
                const diff = Math.abs(gotoChoice1.floorNum - currentFloorNum)
                                - Math.abs(gotoChoice2.floorNum - currentFloorNum);

                if (diff != 0) {
                    return diff;
                }

                // 現在のフロアからの距離が一緒の場合
                // 進行方向のフロアを優先
                if (elevator.goingUpIndicator()) {
                    // 上へ向かっている場合、大きいフロアを優先
                    return gotoChoice2.floorNum - gotoChoice1.floorNum;
                } else {
                    // 下へ向かっている場合、小さいフロアを優先
                    return gotoChoice1.floorNum - gotoChoice2.floorNum;
                }
            })[0];
            
            console.log(
                `elevator${elevator.number} [goToPressedFloor]`
                + ` nearGotoChoice:${JSON.stringify(nearGotoChoice)}`);

            // 移動
            if (nearGotoChoice.indicator == "up") {
                elevator.goingUpIndicator(true);
                elevator.goingDownIndicator(false);
            } else {
                elevator.goingUpIndicator(false);
                elevator.goingDownIndicator(true);
            }
            elevator.goToFloor(nearGotoChoice.floorNum);
        }
            
        // 一番近くいる停止中のエレベータを取得する関数
        const pickupStoppedNearElevator = (floorNum) => {

            return elevators.filter((elevator) => {
                return elevator.destinationQueue.length == 0;
            })
            .sort((elevator1, elevator2) => {
                return Math.abs(elevator1.currentFloor() - floorNum)
                        - Math.abs(elevator2.currentFloor() - floorNum);
            })[0];
        }

        // エレベータの行き先を再設定する関数
        const resetDestination = (elevator) => {

            const currentFloorNum = elevator.currentFloor();

            console.log(
                `[resetDestination] currentFloorNum:${currentFloorNum}`
                                + ` getPressedFloors:${elevator.getPressedFloors()}`
                                + ` goingUpIndicator:${elevator.goingUpIndicator()}`
                                + ` goingDownIndicator:${elevator.goingDownIndicator()}`);

            // 現在フロアの上下で分けてソート(現在フロアから近い順に)
            const uppers = elevator.getPressedFloors()
                             // 同じ階が押されていることは無いはずだが漏れがないように念のため含めておく
                            .filter(num => num >= currentFloorNum)
                            .sort((num1, num2) => num1 - num2);

            const lowers = elevator.getPressedFloors()
                            .filter(num => num < currentFloorNum)
                            .sort((num1, num2) => num1 - num2)
                            .reverse(); // 大きい階の方が近い階になるので逆順

            // 進行方向に行き先フロア無しの場合、方向を反転
            if (elevator.goingUpIndicator() && uppers.length == 0) {
               elevator.goingUpIndicator(false);
               elevator.goingDownIndicator(true);
            }
            if (elevator.goingDownIndicator() && lowers.length == 0) {
               elevator.goingUpIndicator(true);
               elevator.goingDownIndicator(false);
            }

            // 現在の進行方向にあわせてキューに追加
            if (elevator.goingUpIndicator()) {
                elevator.destinationQueue = uppers.concat(lowers);
            } else {
                elevator.destinationQueue = lowers.concat(uppers);
            }

            console.log(`[resetDestination] destinationQueue:${elevator.destinationQueue}`);

            elevator.checkDestinationQueue();
        }

        // -------------------------------
        // フロアに対するイベント登録
        // -------------------------------
        floors.forEach((floor) => {

            const floorNum = floor.floorNum();

            floor.on("up_button_pressed", () => {
                console.log(`floor${floorNum} [up_button_pressed]`);

                // フロアでボタン押下された情報を保持
                floorButton[floorNum].up = true;

                // 停止中のエレベータがあれば向かわせる
                const stoppedElevator = pickupStoppedNearElevator(floorNum);
                console.log(`floor${floorNum} [up_button_pressed] stoppedElevator`);
                console.log(stoppedElevator);
                if (stoppedElevator) {
                    stoppedElevator.goingUpIndicator(true);
                    stoppedElevator.goingDownIndicator(false);
                    stoppedElevator.goToFloor(floorNum);
                }
            });

            floor.on("down_button_pressed", () => {
                console.log(`floor${floorNum} [down_button_pressed]`);

                // フロアでボタン押下された情報を保持
                floorButton[floorNum].down = true;

                // 停止中のエレベータがあれば向かわせる
                const stoppedElevator = pickupStoppedNearElevator(floorNum);
                console.log(`floor${floorNum} [down_button_pressed] stoppedElevator`);
                console.log(stoppedElevator);
                if (stoppedElevator) {
                    stoppedElevator.goingUpIndicator(false);
                    stoppedElevator.goingDownIndicator(true);
                    stoppedElevator.goToFloor(floorNum);
                }
            });
        });

        // -------------------------------
        // エレベータに対するイベント登録
        // -------------------------------
        elevators.forEach((elevator) => {

            // ログ用にエレベータに番号を付与
            elevator.number = elevators.indexOf(elevator);

            elevator.on("stopped_at_floor", (floorNum) => {
                console.log(
                    `elevator${elevator.number}`
                    + ` [stopped_at_floor] floorNum:${floorNum} destinationQueue:${elevator.destinationQueue}`);

                // 最後の停止フロアの場合
                // 該当のフロアでボタンが押下されているならば、その方向で設定
                if (elevator.destinationQueue.length == 0) {
                    if (floorButton[floorNum].up) {
                        elevator.goingUpIndicator(true);
                        elevator.goingDownIndicator(false);
                    } else if (floorButton[floorNum].down) {
                        elevator.goingUpIndicator(false);
                        elevator.goingDownIndicator(true);
                    }
                }

                // フロアに到着したら、進行方向に応じたボタンをクリア
                if (elevator.goingUpIndicator()) {
                    floorButton[floorNum].up = false;
                }
                if (elevator.goingDownIndicator()) {
                    floorButton[floorNum].down = false;
                }
            })
            
            elevator.on("passing_floor", (floorNum, direction) => {
                console.log(`elevator${elevator.number} [passing_floor] floorNum:${floorNum}`);

                // 最初の乗客を拾いに行く状態の場合、最新の状況による移動先を再設定
                // (移動中に近いフロアで押されている可能性があるため)
                if (elevator.loadFactor() == 0) {
                    goToPressedFloor(elevator, floorNum);
                    return;
                }

                // フロア通過前に
                // ・進行方向と同じボタンが押下されている
                // ・乗車できる余裕がある
                // ・他のエレベータが止まろうとしていない
                // 場合には、フロアに止まって人を乗せる
                const otherElevatorGoFloors = getOtherElevatorGoFloors(elevator);
                const isOtherElevetorStop = otherElevatorGoFloors.some((elevatorGoFloor) => {
                    return elevatorGoFloor.floorNum == floorNum
                        && ((direction == "up" && elevatorGoFloor.up)
                            || (direction == "down" && elevatorGoFloor.down));
                });

                console.log(
                    `elevator${elevator.number} [passing_floor]`
                    + ` isOtherElevetorStop:${isOtherElevetorStop}`
                    + ` loadFactor:${elevator.loadFactor()}`
                    + ` otherElevatorGoFloors:${JSON.stringify(otherElevatorGoFloors)}`
                    + ` floorButton:${JSON.stringify(floorButton)}`);

                if (floorButton[floorNum][direction] && elevator.loadFactor() < 1.0 && !isOtherElevetorStop) {
                    console.log(`elevator${elevator.number} [passing_floor] stop next floor:${floorNum}`);
                    elevator.goToFloor(floorNum, true);
                }
            });

            elevator.on("idle", () => {
                console.log(`elevator${elevator.number} [idle]`);

                // 行き先フロアが無くなったら、ボタンが押下されている他のフロアに移動
                goToPressedFloor(elevator, elevator.currentFloor());
            });
            
            elevator.on("floor_button_pressed", (floorNum) => {
                console.log(`elevator${elevator.number} [floor_button_pressed] floorNum:${floorNum}`);

                // 行き先を再設定
                resetDestination(elevator);
            });
        });
    
    },
    update: function(dt, elevators, floors) {
        // We normally don't need to do anything here
    }
}

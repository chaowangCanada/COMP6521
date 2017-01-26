/**
 * Created by ERIC_LAI on 2017-01-25.
 */

var fs = require('fs');
var random = require('random-number-generator');

// the number of non-repeated random number
var numOfT1 = 5000;
var numOfT2 = 10000;
// the file path
var path1 = './t1.txt';
var path2 = './t2.txt';

createNonRepeatRandomNum(path1, numOfT1);
createNonRepeatRandomNum(path2, numOfT2);

function createNonRepeatRandomNum(path, numOfItem) {
    var list = [];
    fs.open(path, 'w', function (err, fd) {
        for (var j = 0; j < numOfItem; j++) {
            var num = getRandomNum();
            while (checkExistance(num, list)) {
                num = getRandomNum();
            }
            list.push(num);
            fs.write(fd, num);
            if (j != numOfItem - 1) {
                fs.write(fd, '\n');
            }
        }
    });
}

function getRandomNum() {
    return random(9999999, 1000000);
}

function checkExistance(num, list) {
    for (var i = 0; i < list.length; i++) {
        if (num === list[i]) {
            return true;
        }
    }
    return false
}


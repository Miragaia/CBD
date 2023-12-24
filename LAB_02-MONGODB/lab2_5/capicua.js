iscapicua = function () {
    var contador = 0;
    var phones = db.phones.find();
    phones.forEach(function (phone) {
        var display = phone.display;
        var displayReverse = phone.display.split("").reverse().join("");
        if (display == displayReverse) {
            contador++;
        }
    });
    print("Existem " + contador + " capicúas");
}
function startLoading(seq) {
    if (seq) {
        $(`#spinner-overlay${seq}`).fadeIn(300);
    } else {
        $("#spinner-overlay").fadeIn(300);
    }
}

function stopLoading(seq) {
    if (seq) {
        $(`#spinner-overlay${seq}`).fadeOut(300);
    } else {
        $("#spinner-overlay").fadeOut(300);
    }
}

function isDoneOtherSpinners() {
    var result = true;
    var $spinners = $('.spinner-overlay:not(#spinner-overlay)');
    $spinners.each(function (idx) {
        var cssDisplay = $($spinners[idx]).css('display');
        if (cssDisplay == 'block') {
            result = false;
        }
    });

    return result;
}

$(".btn-spinner").on("click", function() {
    startLoading();
})

$(document).ready(() => {

    $('select[name="isPkgSw"]').change(function () {
        var pkgSwInput = $('input[name="pkgSwName"]');
        if (this.value == 'N') {
            pkgSwInput.prop('disabled', 'disabled');
        } else {
            pkgSwInput.prop('disabled', false);
        }
    });
    const convertDateStringToArray = (str) => {
        //2020년 11월 10일 10시 10분
        const array = []
        array.push(Number(str.substring(0,4)));
        array.push(Number(str.substring(6,8))-1);
        array.push(Number(str.substring(10,12)));
        array.push(Number(str.substring(14,16)));
        array.push(Number(str.substring(18,20)));
        return array;
    }

    $('#databaseInfoForm').submit((e) => {
        var operSttus = $("#operSttusDate").val();
        if(operSttus) {
            var operSttusDate = new Date(...convertDateStringToArray(operSttus));
            $("#operSttusDate").val(operSttusDate.toISOString());
        }
        return true;
    })

    let inputs = document.getElementsByClassName("dateTimePicker");
    for (let input of inputs) {
        flatpickr(input, {
            enableTime: true,
            dateFormat: "Y년 m월 d일 H시 i분",
            defaultDate: new Date(input.value),
            locale: {
                weekdays: {
                    shorthand: ["일", "월", "화", "수", "목", "금", "토"],
                    longhand: [
                        "일요일",
                        "월요일",
                        "화요일",
                        "수요일",
                        "목요일",
                        "금요일",
                        "토요일",
                    ],
                },
                months: {
                    shorthand: [
                        "1월",
                        "2월",
                        "3월",
                        "4월",
                        "5월",
                        "6월",
                        "7월",
                        "8월",
                        "9월",
                        "10월",
                        "11월",
                        "12월",
                    ],
                    longhand: [
                        "1월",
                        "2월",
                        "3월",
                        "4월",
                        "5월",
                        "6월",
                        "7월",
                        "8월",
                        "9월",
                        "10월",
                        "11월",
                        "12월",
                    ],
                },
                rangeSeparator: " ~ ",
                weekAbbreviation: "주",
                scrollTitle: "스크롤",
                toggleTitle: "클릭",
                amPM: ["오전", "오후"],
                yearAriaLabel: "년",
                monthAriaLabel: "월",
                hourAriaLabel: "시",
                minuteAriaLabel: "분",
                time_24hr: false,
            }
        });
    }
});
$(document).ready(() => {
    let $indexForm = $("#evaluationIndexForm");
    let $existEvaluationTypeRadio = $("#existEvaluationTypeRadio");
    let $newEvaluationTypeRadio = $("#newEvaluationTypeRadio");

    $indexForm.submit((ev) => {
        ev.preventDefault();
        let serializedDatas = $indexForm.serializeArray();
        const data = {}
        const headers = {}
        let beforeSendFunc;
        for(let obj of serializedDatas) {
            if(obj.name === '_csrf') {
                beforeSendFunc = (xhr) => {
                    xhr.setRequestHeader('X-CSRF-TOKEN', obj.value);
                    xhr.setRequestHeader('accept', '*/*');
                    xhr.setRequestHeader('content-type', 'application/json;charset=UTF-8');
                }
            } else {
                data[obj.name] = obj.value;
            }
        }

        let method = 'post'
        if(data.flexRadioDefault && data.flexRadioDefault === 'exist') {
            method = 'put'
        }
        delete data.flexRadioDefault;

        $.ajax({
            type: method,
            url: $indexForm.attr("action"),
            beforeSend: beforeSendFunc,
            data: JSON.stringify(data),
        })
            .done(function(response) {
                console.log(response);
            })
            .catch(function(err) {
                console.log(err);
            })

        return false;
    })
})
$.ajaxSetup({
    beforeSend: function (xhr){
        xhr.setRequestHeader('X-CSRF-TOKEN', $('meta[name=_csrf]').attr("content"));
    }
});
var SummernoteManager = function(options) {
    var self = this;
    this.contextPath = options.contextPath;
    this.options = _.defaults(options, {
        lineHeights: ['0.2', '0.3', '0.4', '0.5', '0.6', '0.8', '1.0', '1.2', '1.4', '1.5', '2.0', '3.0'],
        fontNames: ['Arial', 'Comic Sans MS',  'sans-serif', 'Noto Sans CJK Kr', '나눔바른고딕', '나눔고딕', '나눔손글씨체', '한돋움체', '나눔명조', '나눔명조에코', '마루부리'],
        fontNamesIgnoreCheck : ['Arial', 'Comic Sans MS',  'sans-serif', 'Noto Sans CJK Kr', '나눔바른고딕', '나눔고딕', '나눔손글씨체', '한돋움체', '나눔명조', '나눔명조에코', '마루부리'],
        toolbar: [
            ['font', ['fontname', 'fontsize', 'fontsizeunit']],
            ['height',['height']],
            ['style', ['style']],
            ['font', ['bold', 'underline', 'clear']],
            ['color', ['color']],
            ['para', ['ul', 'ol', 'paragraph']],
            ['table', ['table']],
            ['insert', ['link', 'picture']],
            ['view', ['fullscreen', 'codeview', 'help']],
        ],
        callbacks: {
            onImageUpload : function(files) {
                self.sendImg(files, $(this));
            }
        },
        lang: 'ko-KR',
        height: 400,
    });
    this.summernote = $('#summernote').summernote(this.options);

    if (this.options.content != 'null') {
        $('#summernote').summernote('code', htmlDecode(this.options.content));
    }
    this.initEvent();
};

SummernoteManager.prototype.initEvent = function () {
    $("#summernote-form").on("submit", function () {
        var markupStr = $('#summernote').summernote('code');
        $("#summernote").val(markupStr);
    });

    $('.note-codable').on('blur', function() {
        if ($('#summernote').summernote('codeview.isActivated')) {
            $('[name="content"]').val($('#summernote').summernote('code'));
        }
    });
}

SummernoteManager.prototype.sendImg = function(files, editor) {
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    var data = new FormData();
    var self = this;
    for (var i=0; i<files.length; i++) {
        data.append("files", files[i]);
    }
    $.ajax({
        data: data,
        beforeSend : function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        type: "POST",
        url: self.contextPath + '/file/upload/summernote',
        enctype: 'multipart/form-data',
        cache: false,
        contentType: false,
        processData: false,
        success: function(data) {
            for (var i=0; i<data.length; i++) {
                editor.summernote('insertImage', self.contextPath + '/upload' + data[i].relativePath);
            }
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log(errorThrown)
        }
    });
}

const htmlDecode = (input) => {
    const e = document.createElement('textarea');
    e.innerHTML = input;
    return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
}
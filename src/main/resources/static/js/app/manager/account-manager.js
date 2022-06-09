var AccountManager = function (options) {
    this.options = _.cloneDeep(options);

    this.contextPath = options.contextPath;

    this.$password = $('#password');
    this.$confirmPassword = $('#confirm-password');
    this.$userId = $('#userId');

    this.token = $("meta[name='_csrf']").attr("content");
    this.header = $("meta[name='_csrf_header']").attr("content");
    this.assignIndexTemplate = Handlebars.getTemplate("account/assign-index");
    this.indicesAssignSection = $(".indices-assign-section");
    this.indicesSelect = $("#indices-select");

    this.initEvent();
};

AccountManager.prototype.initEvent = function () {
    var self = this;

    this.initIndexDeleteEvent();

    // 담당지표 할당 관련 로직
    this.indicesSelect.on("change", function () {
        if (self.indicesAssignSection.find("#index-" + $(this).val()).length < 1) {
            self.indicesAssignSection.append(self.assignIndexTemplate({
                id: $(this).val(),
                text: $(this).find("option:selected").text(),
                name: `indices[${self.indicesAssignSection.children().length}].id`,
                value: $(this).find("option:selected").val(),
            }));

            self.initIndexDeleteEvent();
        }
    })

    $(".account-new-submit").on("click", function (e) {
        e.preventDefault();

        if (self.checkConfirmPasswd() && self.checkDuplicateId()) {
            $("#account-new-form").submit();
        }
    });

    $(".account-edit-submit").on("click", function (e) {
        e.preventDefault();
        if (self.$password.val().length > 0) {
            if (self.checkConfirmPasswd() && self.checkOldPasswd()) {
                $("#account-edit-form").submit();
            }
        } else {
            $("#account-edit-form").submit();
        }
    });

    $('#password, #confirm-password').on('keyup', function (e) {
        e.preventDefault();
        self.checkPassword();
    });
};

AccountManager.prototype.initIndexDeleteEvent = function () {
    $(".assigned-index-delete-btn").on("click", function () {
        $(this).parent().remove();

        var inputs = $(".indices-assign-section").find("input")
        for (var i = 0; i < inputs.length; i++) {
            inputs[i].name = `indices[${i}].id`
        }
    })
}

AccountManager.prototype.checkDuplicateId = function () {
    var self = this;
    var flag = true;

    $.ajax({
        url: self.contextPath + "/admin/account/" + self.$userId.val() + '/checkId',
        type: "POST",
        async: false,
        beforeSend: function (xhr) {
            xhr.setRequestHeader(self.header, self.token);
        },
        success: function (bool) {
            if (bool === false) {
                window.alert("중복된 아이디입니다.");
                flag = false;
            }
        },
        error: function (error) {
            flag = false;
        }
    });

    return flag;
}

AccountManager.prototype.checkOldPasswd = function () {
    var self = this;
    var oldPassword = $('#oldPassword').val();
    var flag = true;

    $.ajax({
        url: self.contextPath + "/admin/account/" + self.$userId.val() + '/checkPw',
        type: "POST",
        async: false,
        data: {
            "oldPassword": oldPassword,
        },
        beforeSend: function (xhr) {
            xhr.setRequestHeader(self.header, self.token);
        },
        success: function (bool) {
            if (bool === false) {
                window.alert("기존 비밀번호를 정확하게 입력해주세요.");
                flag = false;
            }
        },
        error: function (error) {
            flag = false;
        }
    });

    return flag;
}

AccountManager.prototype.checkPassword = function () {
    var self = this;

    if (self.$password.val().length > 0) {
        if (self.$password.val() === self.$confirmPassword.val()) {
            $('#pw-confirm-message').html('패스워드가 일치합니다.').css('color', 'green');
        } else {
            $('#pw-confirm-message').html('패스워드가 일치하지 않습니다.').css('color', 'red');
        }
    }
}

AccountManager.prototype.checkConfirmPasswd = function () {
    var self = this;

    if (self.$password.val().length <= 0 || self.$password.val() !== self.$confirmPassword.val()) {
        $('#pw-message').html('패스워드를 확인해주세요.').css('color', 'red');
        return false;
    } else {
        $('#pw-message').html('');
    }

    return true;
}
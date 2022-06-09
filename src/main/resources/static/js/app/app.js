/* 01. Global Variable
------------------------------------------------ */
var app = {
    font: {},
    color: {}
}


/* 02. Handle Scrollbar
------------------------------------------------ */
var handleSlimScroll = function () {
    "use strict";
    $.when($('[data-scrollbar=true]').each(function () {
        generateSlimScroll($(this));
    })).done(function () {
        $('[data-scrollbar="true"]').mouseover();
    });
};
var generateSlimScroll = function (element) {
    var isMobile = (/Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent));

    if ($(element).attr('data-init') || (isMobile && $(element).attr('data-skip-mobile'))) {
        return;
    }
    var dataHeight = $(element).attr('data-height');
    dataHeight = (!dataHeight) ? $(element).height() : dataHeight;

    var scrollBarOption = {
        height: dataHeight,
        alwaysVisible: false
    };
    if (isMobile) {
        $(element).css('height', dataHeight);
        $(element).css('overflow-x', 'scroll');
    } else {
        $(element).slimScroll(scrollBarOption);
        $(element).closest('.slimScrollDiv').find('.slimScrollBar').hide();
    }
    $(element).attr('data-init', true);
};


/* 03. Handle Sidebar Menu
------------------------------------------------ */
var handleSidebarMenu = function () {
    "use strict";
    $(document).on('click', '.app-sidebar .menu > .menu-item.has-sub > .menu-link', function (e) {
        e.preventDefault();

        var target = $(this).next('.menu-submenu');
        var otherMenu = $('.app-sidebar .menu > .menu-item.has-sub > .menu-submenu').not(target);

        if ($('.app-sidebar-minified').length === 0) {
            $(otherMenu).slideUp(250);
            $(otherMenu).closest('.menu-item').removeClass('expand');

            $(target).slideToggle(250);
            var targetElm = $(target).closest('.menu-item');
            if ($(targetElm).hasClass('expand')) {
                $(targetElm).removeClass('expand');
            } else {
                $(targetElm).addClass('expand');
            }
        }
    });
    $(document).on('click', '.app-sidebar .menu > .menu-item.has-sub .menu-submenu .menu-item.has-sub > .menu-link', function (e) {
        e.preventDefault();

        if ($('.app-sidebar-minified').length === 0) {
            var target = $(this).next('.menu-submenu');
            $(target).slideToggle(250);
        }
    });
};


/* 04. Handle Sidebar Minify
------------------------------------------------ */
var MOBILE_SIDEBAR_TOGGLE_CLASS = 'app-sidebar-mobile-toggled';
var MOBILE_SIDEBAR_CLOSED_CLASS = 'app-sidebar-mobile-closed';
var handleSidebarMinify = function () {
    $('[data-toggle="sidebar-minify"]').on('click', function (e) {
        e.preventDefault();

        var targetElm = '#app';
        var targetClass = 'app-sidebar-minified';

        if ($(targetElm).hasClass(targetClass)) {
            $(targetElm).removeClass(targetClass);
            localStorage.removeItem('appSidebarMinified');
        } else {
            $(targetElm).addClass(targetClass);
            localStorage.setItem('appSidebarMinified', true);
        }
    });

    if (typeof (Storage) !== 'undefined') {
        if (localStorage.appSidebarMinified) {
            $('#app').addClass('app-sidebar-minified');
        }
    }
};
var handleSidebarMobileToggle = function () {
    $(document).on('click', '[data-toggle="sidebar-mobile"]', function (e) {
        e.preventDefault();

        var targetElm = '#app';

        $(targetElm).removeClass(MOBILE_SIDEBAR_CLOSED_CLASS).addClass(MOBILE_SIDEBAR_TOGGLE_CLASS);
    });
};
var handleSidebarMobileDismiss = function () {
    $(document).on('click', '[data-dismiss="sidebar-mobile"]', function (e) {
        e.preventDefault();

        var targetElm = '#app';

        $(targetElm).removeClass(MOBILE_SIDEBAR_TOGGLE_CLASS).addClass(MOBILE_SIDEBAR_CLOSED_CLASS);
        setTimeout(function () {
            $(targetElm).removeClass(MOBILE_SIDEBAR_CLOSED_CLASS);
        }, 250);
    });
};


/* 05. Handle Sidebar Minify Float Menu
------------------------------------------------ */
var floatSubMenuTimeout;
var targetFloatMenu;
var handleMouseoverFloatSubMenu = function (elm) {
    clearTimeout(floatSubMenuTimeout);
};
var handleMouseoutFloatSubMenu = function (elm) {
    floatSubMenuTimeout = setTimeout(function () {
        $('.app-float-submenu').remove();
    }, 250);
};
var handleSidebarMinifyFloatMenu = function () {
    $(document).on('click', '.app-float-submenu .menu-item.has-sub > .menu-link', function (e) {
        e.preventDefault();

        var target = $(this).next('.menu-submenu');
        $(target).slideToggle(250, function () {
            var targetMenu = $('.app-float-submenu');
            var targetHeight = $(targetMenu).height() + 20;
            var targetOffset = $(targetMenu).offset();
            var targetTop = $(targetMenu).attr('data-offset-top');
            var windowHeight = $(window).height();
            if ((windowHeight - targetTop) > targetHeight) {
                $('.app-float-submenu').css({
                    'top': targetTop,
                    'bottom': 'auto',
                    'overflow': 'initial'
                });
            } else {
                $('.app-float-submenu').css({
                    'bottom': 0,
                    'overflow': 'scroll'
                });
            }
        });
    });
    $(document).on('mouseover', '.app-sidebar-minified .app-sidebar .menu .menu-item.has-sub > .menu-link', function () {
        clearTimeout(floatSubMenuTimeout);

        var targetMenu = $(this).closest('.menu-item').find('.menu-submenu').first();
        if (targetFloatMenu == this) {
            return false;
        } else {
            targetFloatMenu = this;
        }
        var targetMenuHtml = $(targetMenu).html();

        if (targetMenuHtml) {
            var targetHeight = $(targetMenu).height() + 20;
            var targetOffset = $(this).offset();
            var targetTop = targetOffset.top - $(window).scrollTop();
            var targetLeft = ($('body').css('direction') != 'rtl') ? $('#sidebar').width() + $('#sidebar').offset().left : 'auto';
            var targetRight = ($('body').css('direction') != 'rtl') ? 'auto' : $('#sidebar').width();
            var windowHeight = $(window).height();
            var submenuHeight = 0;

            if ($('.app-float-submenu').length == 0) {
                targetMenuHtml = '<div class="app-float-submenu" data-offset-top="' + targetTop + '" onmouseover="handleMouseoverFloatSubMenu(this)" onmouseout="handleMouseoutFloatSubMenu(this)">' + targetMenuHtml + '</div>';
                $('body').append(targetMenuHtml);
            } else {
                $('.app-float-submenu').html(targetMenuHtml);
            }
            submenuHeight = $('.app-float-submenu').height();
            if ((windowHeight - targetTop) > targetHeight && ((targetTop + submenuHeight) < windowHeight)) {
                $('.app-float-submenu').css({
                    'top': targetTop,
                    'left': targetLeft,
                    'bottom': 'auto',
                    'right': targetRight
                });
            } else {
                $('.app-float-submenu').css({
                    'bottom': 0,
                    'top': 'auto',
                    'left': targetLeft,
                    'right': targetRight
                });
            }
        } else {
            $('.app-float-submenu').remove();
            targetFloatMenu = '';
        }
    });
    $(document).on('mouseout', '.app-sidebar-minified .app-sidebar .menu > .menu-item.has-sub > .menu-link', function () {
        floatSubMenuTimeout = setTimeout(function () {
            $('.app-float-submenu').remove();
            targetFloatMenu = '';
        }, 250);
    });
}


/* 06. Handle Dropdown Close Option
------------------------------------------------ */
var handleDropdownClose = function () {
    $(document).on('click', '[data-dropdown-close="false"]', function (e) {
        e.stopPropagation();
    });
};


/* 07. Handle Card - Remove / Reload / Collapse / Expand
------------------------------------------------ */
var cardActionRunning = false;
var handleCardAction = function () {
    "use strict";

    if (cardActionRunning) {
        return false;
    }
    cardActionRunning = true;

    // expand
    $(document).on('mouseover', '[data-toggle=card-expand]', function (e) {
        if (!$(this).attr('data-init')) {
            $(this).tooltip({
                title: 'Expand / Compress',
                placement: 'bottom',
                trigger: 'hover',
                container: 'body'
            });
            $(this).tooltip('show');
            $(this).attr('data-init', true);
        }
    });
    $(document).on('click', '[data-toggle=card-expand]', function (e) {
        e.preventDefault();
        var target = $(this).closest('.card');
        var targetBody = $(target).find('.card-body');
        var targetClass = 'card-expand';
        var targetTop = 40;
        if ($(targetBody).length !== 0) {
            var targetOffsetTop = $(target).offset().top;
            var targetBodyOffsetTop = $(targetBody).offset().top;
            targetTop = targetBodyOffsetTop - targetOffsetTop;
        }

        if ($('body').hasClass(targetClass) && $(target).hasClass(targetClass)) {
            $('body, .card').removeClass(targetClass);
            $('.card').removeAttr('style');
            $(targetBody).removeAttr('style');
        } else {
            $('body').addClass(targetClass);
            $(this).closest('.card').addClass(targetClass);
        }
        $(window).trigger('resize');
    });
};


/* 08. Handle Tooltip & Popover Activation
------------------------------------------------ */
var handelTooltipPopoverActivation = function () {
    "use strict";
    if ($('[data-toggle="tooltip"]').length !== 0) {
        $('[data-toggle=tooltip]').tooltip();
    }
    if ($('[data-toggle="popover"]').length !== 0) {
        $('[data-toggle=popover]').popover();
    }
};


/* 09. Handle Scroll to Top Button Activation
------------------------------------------------ */
var handleScrollToTopButton = function () {
    "use strict";
    $(document).on('scroll', function () {
        var totalScroll = $(document).scrollTop();

        if (totalScroll >= 200) {
            $('[data-click=scroll-top]').addClass('show');
        } else {
            $('[data-click=scroll-top]').removeClass('show');
        }
    });
    $('[data-click=scroll-top]').on('click', function (e) {
        e.preventDefault();
        $('html, body, .content').animate({
            scrollTop: $("body").offset().top
        }, 500);
    });
};


/* 10. Handle hexToRgba
------------------------------------------------ */
var hexToRgba = function (hex, transparent = 1) {
    var c;
    if (/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)) {
        c = hex.substring(1).split('');
        if (c.length == 3) {
            c = [c[0], c[0], c[1], c[1], c[2], c[2]];
        }
        c = '0x' + c.join('');
        return 'rgba(' + [(c >> 16) & 255, (c >> 8) & 255, c & 255].join(',') + ',' + transparent + ')';
    }
    throw new Error('Bad Hex');
};


/* 11. Handle Scroll to
------------------------------------------------ */
var handleScrollTo = function () {
    $(document).on('click', '[data-toggle="scroll-to"]', function (e) {
        e.preventDefault();

        var targetElm = ($(this).attr('data-target')) ? $(this).attr('data-target') : $(this).attr('href');
        if (targetElm) {
            $('html, body').animate({
                scrollTop: $(targetElm).offset().top - $('#header').height() - 24
            }, 0);
        }
    });
};


/* 12. Handle Theme Panel Expand
------------------------------------------------ */
var handleThemePanelExpand = function () {
    $(document).on('click', '[data-click="theme-panel-expand"]', function () {
        var targetContainer = '.theme-panel';
        var targetClass = 'active';
        if ($(targetContainer).hasClass(targetClass)) {
            $(targetContainer).removeClass(targetClass);
            Cookies.set('theme-panel', 'collapse');
        } else {
            $(targetContainer).addClass(targetClass);
            Cookies.set('theme-panel', 'expand');
        }
    });

    if (Cookies.get('theme-panel') && Cookies.get('theme-panel') == 'expand') {
        $('[data-click="theme-panel-expand"]').trigger('click');
    }
};


/* 13. Handle Theme Page Control
------------------------------------------------ */
var handleThemePageControl = function () {
    if (typeof Cookies !== 'undefined') {
        $(document).on('click', '.theme-list [data-theme]', function (e) {
            e.preventDefault();
            var targetThemeClass = $(this).attr('data-theme');

            for (var x = 0; x < document.body.classList.length; x++) {
                var targetClass = document.body.classList[x];
                if (targetClass.search('theme-') > -1) {
                    $('body').removeClass(targetClass);
                }
            }

            $('body').addClass(targetThemeClass);
            $('.theme-list [data-theme]').not(this).closest('li').removeClass('active');
            $(this).closest('li').addClass('active');

            if (Cookies) {
                Cookies.set('theme', $(this).attr('data-theme'));
                $(document).trigger('theme-change');
            }
        });

        $(document).on('change', '.theme-panel [name="app-theme-dark-mode"]', function () {
            var targetCookie = '';

            if ($(this).is(':checked')) {
                $('html').addClass('dark-mode');
                targetCookie = 'dark-mode';
            } else {
                $('html').removeClass('dark-mode');
            }

            if (Cookies) {
                App.initVariable();
                Cookies.set('app-theme-dark-mode', targetCookie);
                $(document).trigger('theme-change');
            }
        });

        if (Cookies.get('theme') && $('.theme-list').length !== 0) {
            var targetElm = '.theme-list [data-theme="' + Cookies.get('theme') + '"]';
            $(targetElm).trigger('click');
        }
        if (Cookies.get('app-theme-dark-mode') && $('.theme-panel [name="app-theme-dark-mode"]').length !== 0) {
            $('.theme-panel [name="app-theme-dark-mode"]').prop('checked', true).trigger('change');
        }
    }
};


/* 14. Handle Enable Tooltip & Popover
------------------------------------------------ */
var handleEnableTooltip = function () {
    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    });
};


/* Application Controller
------------------------------------------------ */
var App = function () {
    "use strict";

    return {
        //main function
        init: function () {
            this.initSidebar();
            this.initHeader();
            this.initComponent();
            this.initVariable();
        },
        initSidebar: function () {
            handleSidebarMinifyFloatMenu();
            handleSidebarMenu();
            handleSidebarMinify();
            handleSidebarMobileToggle();
            handleSidebarMobileDismiss();
        },
        initHeader: function () {
        },
        initComponent: function () {
            handleSlimScroll();
            handleCardAction();
            handelTooltipPopoverActivation();
            handleScrollToTopButton();
            handleDropdownClose();
            handleScrollTo();
            handleThemePanelExpand();
            handleThemePageControl();
            handleEnableTooltip();
        },
        scrollTop: function () {
            $('html, body, .content').animate({
                scrollTop: $('body').offset().top
            }, 0);
        },
        getCssVariable: function (variable) {
            return window.getComputedStyle(document.body).getPropertyValue(variable).trim();
        },
        initVariable: function () {
            app.color.theme = this.getCssVariable('--app-theme');
            app.font.family = this.getCssVariable('--bs-body-font-family');
            app.font.size = this.getCssVariable('--bs-body-font-size');
            app.font.weight = this.getCssVariable('--bs-body-font-weight');
            app.color.componentColor = this.getCssVariable('--app-component-color');
            app.color.componentBg = this.getCssVariable('--app-component-bg');
            app.color.dark = this.getCssVariable('--bs-dark');
            app.color.light = this.getCssVariable('--bs-light');
            app.color.blue = this.getCssVariable('--bs-blue');
            app.color.indigo = this.getCssVariable('--bs-indigo');
            app.color.purple = this.getCssVariable('--bs-purple');
            app.color.pink = this.getCssVariable('--bs-pink');
            app.color.red = this.getCssVariable('--bs-red');
            app.color.orange = this.getCssVariable('--bs-orange');
            app.color.yellow = this.getCssVariable('--bs-yellow');
            app.color.green = this.getCssVariable('--bs-green');
            app.color.success = this.getCssVariable('--bs-success');
            app.color.teal = this.getCssVariable('--bs-teal');
            app.color.cyan = this.getCssVariable('--bs-cyan');
            app.color.white = this.getCssVariable('--bs-white');
            app.color.gray = this.getCssVariable('--bs-gray');
            app.color.lime = this.getCssVariable('--bs-lime');
            app.color.gray100 = this.getCssVariable('--bs-gray-100');
            app.color.gray200 = this.getCssVariable('--bs-gray-200');
            app.color.gray300 = this.getCssVariable('--bs-gray-300');
            app.color.gray400 = this.getCssVariable('--bs-gray-400');
            app.color.gray500 = this.getCssVariable('--bs-gray-500');
            app.color.gray600 = this.getCssVariable('--bs-gray-600');
            app.color.gray700 = this.getCssVariable('--bs-gray-700');
            app.color.gray800 = this.getCssVariable('--bs-gray-800');
            app.color.gray900 = this.getCssVariable('--bs-gray-900');
            app.color.black = this.getCssVariable('--bs-black');

            app.color.themeRgb = this.getCssVariable('--app-theme-rgb');
            app.font.familyRgb = this.getCssVariable('--bs-body-font-family-rgb');
            app.font.sizeRgb = this.getCssVariable('--bs-body-font-size-rgb');
            app.font.weightRgb = this.getCssVariable('--bs-body-font-weight-rgb');
            app.color.componentColorRgb = this.getCssVariable('--app-component-color-rgb');
            app.color.componentBgRgb = this.getCssVariable('--app-component-bg-rgb');
            app.color.darkRgb = this.getCssVariable('--bs-dark-rgb');
            app.color.lightRgb = this.getCssVariable('--bs-light-rgb');
            app.color.blueRgb = this.getCssVariable('--bs-blue-rgb');
            app.color.indigoRgb = this.getCssVariable('--bs-indigo-rgb');
            app.color.purpleRgb = this.getCssVariable('--bs-purple-rgb');
            app.color.pinkRgb = this.getCssVariable('--bs-pink-rgb');
            app.color.redRgb = this.getCssVariable('--bs-red-rgb');
            app.color.orangeRgb = this.getCssVariable('--bs-orange-rgb');
            app.color.yellowRgb = this.getCssVariable('--bs-yellow-rgb');
            app.color.greenRgb = this.getCssVariable('--bs-green-rgb');
            app.color.successRgb = this.getCssVariable('--bs-success-rgb');
            app.color.tealRgb = this.getCssVariable('--bs-teal-rgb');
            app.color.cyanRgb = this.getCssVariable('--bs-cyan-rgb');
            app.color.whiteRgb = this.getCssVariable('--bs-white-rgb');
            app.color.grayRgb = this.getCssVariable('--bs-gray-rgb');
            app.color.limeRgb = this.getCssVariable('--bs-lime-rgb');
            app.color.gray100Rgb = this.getCssVariable('--bs-gray-100-rgb');
            app.color.gray200Rgb = this.getCssVariable('--bs-gray-200-rgb');
            app.color.gray300Rgb = this.getCssVariable('--bs-gray-300-rgb');
            app.color.gray400Rgb = this.getCssVariable('--bs-gray-400-rgb');
            app.color.gray500Rgb = this.getCssVariable('--bs-gray-500-rgb');
            app.color.gray600Rgb = this.getCssVariable('--bs-gray-600-rgb');
            app.color.gray700Rgb = this.getCssVariable('--bs-gray-700-rgb');
            app.color.gray800Rgb = this.getCssVariable('--bs-gray-800-rgb');
            app.color.gray900Rgb = this.getCssVariable('--bs-gray-900-rgb');
            app.color.blackRgb = this.getCssVariable('--bs-black-rgb');
        }
    };
}();

$(document).ready(function () {
    App.init();

    // remove-btn 클릭 시 confirm 메세지 출력
    $(".remove-btn").on("click", function () {
        return confirm("정말 삭제하시겠습니까?");
    })
});
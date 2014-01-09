droiuby = {
    history: [],
    pointer: null,
    sendControl: function (command, params, msg) {
        $.get('/control?cmd=' + command + '&' + params, function (data) {
            var result = data['result'];
            if (data['err']) {
                $('#output')
                    .append(
                        "<div class='error'>" + htmlEscape(result) + "</div>");
            } else {
                $('#output')
                    .append(
                        "<div class='result'>" + msg + ".</div>");
            }
            $('#command')
                .val('');
        });
    },
    fileUpload: function (event) {
        event.preventDefault();
        //grab all form data
        $('#reload').attr('src','ajax-loader.gif');
        var formData = new FormData($('form#fileuploader')[0]);
        $.ajax({
            url: '/upload',
            type: 'POST',
            data: formData,
            cache: false,
            contentType: false,
            processData: false,
            success: function (data, textStatus, jqXHR) {
               $('#output').append(
                  "<div class='command'>application uploaded.</div>");
            },
            complete: function (jqXHR, textStatus ) {
              $('#reload').attr('src','reload.png');  
            }
        })
    },

    passCommand: function (command) {
        var cmd_array = command.split(' ');

        if (command == 'clear!') {
            $('#output').empty();
            $('#command').val('');
            droiuby.history = [];
            droiuby.pointer = null;
        } else if (command == 'clear') {
            $('#output').empty();
            $('#command').val('');
        } else if (cmd_array[0] == '!proximity') {
            var option = cmd_array[1];
            droiuby.sendControl('proximity', 'switch=' + cmd_array[1], 'done');
        } else if (cmd_array[0] == '!reload') {
            droiuby.sendControl('reload', '', 'reloaded');
        } else if (cmd_array[0] == '!autostart') {
            droiuby.sendControl('autostart', 'name=' + cmd_array[1], 'reloaded');
        } else if (cmd_array[0] == '!list') {
            $.get('/control?cmd=list', function (data) {
                var list = data['list'].split(',');
                $.each(list, function (index, elem) {
                    $('#output')
                        .append("<div class='result'>" + htmlEscape('- ' + elem) + "</div>");
                });
                $('#command').val('');
            });
        } else if (cmd_array[0] == '!switch') {
            droiuby.sendControl('switch', 'name=' + cmd_array[1], 'switched');
        } else {
            droiuby.history.push(command);
            if (droiuby.history.length > 10) {
                droiuby.history.shift();
            };

            $
                .ajax({
                    type: 'POST',
                    url: '/console',
                    data: {
                        "cmd": command
                    },
                    success: function (
                        data) {
                        var command = data['cmd'];
                        var result = data['result'];
                        droiuby.pointer = null;
                        $('#output')
                            .append(
                                "<div class='command'>" + htmlEscape(command) + "</div>");
                        if (data['err']) {
                            $('#output')
                                .append(
                                    "<div class='error'>" + htmlEscape(result) + "</div>");
                        } else {
                            $('#output')
                                .append(
                                    "<div class='result'>" + htmlEscape(result) + "</div>");
                        }
                        $('#command')
                            .val('');
                    }
                });
        }
    }
};

function htmlEscape(str) {
    return String(str).replace(/&/g, '&amp;').replace(/"/g, '&quot;').replace(
        /'/g, '&#39;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

$(document)
    .ready(
        function () {
            $('#command').focus();
            
            $('#showuploader').click(function() {
              $('#showuploader').hide();
              $('#fileuploader').fadeIn();
            });
            
            $('form #reload').click(droiuby.fileUpload);
            $('form #filename').change( function(event) {
              $('form #filefield').val($('form #filename').val());
              droiuby.fileUpload(event);
            });

            $('#toggle_multiline').live('click',
                function (event) {
                    if ($('#toggle_multiline').hasClass('disabled')) {
                        $('#toggle_multiline').removeClass('disabled');
                        $('#command').removeClass('disabled');
                        $('#multiline_section').addClass('disabled');
                    } else {
                        $('#toggle_multiline').addClass('disabled');
                        $('#command').addClass('disabled');
                        $('#multiline_section').removeClass('disabled');
                    };
                }
            );

            $('#submit').click(function (event) {
                var command = $('#command_multiline').val();
                $('#command_multiline').val('');
                droiuby.passCommand(command);
            });

            $("#command")
                .keydown(
                    function (event) {
                        if (event.keyCode == 38) {
                            event.preventDefault();

                            if (droiuby.pointer == null) {
                                droiuby.pointer = droiuby.history.length - 1;
                            } else {
                                droiuby.pointer -= 1;
                            }

                            if (droiuby.pointer < 0) {
                                droiuby.pointer = null;
                                $('#command').val('');
                            } else {
                                $('#command')
                                    .val(
                                        droiuby.history[droiuby.pointer]);
                            }
                        } else if (event.keyCode == 40) {
                            event.preventDefault();
                            if (droiuby.history.length > 0) {
                                if (droiuby.pointer == null) {
                                    droiuby.pointer = 0;
                                } else {
                                    droiuby.pointer += 1;
                                }

                                if (droiuby.pointer > droiuby.history.length - 1) {
                                    droiuby.pointer = null;
                                    $('#command').val('');
                                } else {
                                    $('#command')
                                        .val(
                                            droiuby.history[droiuby.pointer]);
                                }
                            }
                        } else if (event.keyCode == 13) {
                            event.preventDefault();
                            var command = $('#command').val();
                            droiuby.passCommand(command);
                        }

                        event.returnValue = false;
                        event.cancel = true;
                    });
        });

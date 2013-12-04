droiuby = {
	history : [],
	pointer : null,
	passCommand: function(command) {
		var cmd_array = command.split(' ');
         
        if (command == 'clear!') {
			$('#output').empty();
			$('#command').val('');
			droiuby.history = [];
			droiuby.pointer = null;
		} else if (command=='clear') { 
			$('#output').empty();
			$('#command').val('');
		} else if (cmd_array[0]=='$list') {
            $.get('/control?cmd=list', function(data) {
                 var list = data['list'].split(',');
                 $.each(list, function(index, elem) {
                 $('#output')
					.append("<div class='result'>"
							+ htmlEscape('- '+elem)
							+ "</div>");
                 });
				$('#command').val(''); 
            });
        }  else if (cmd_array[0]=='$switch') {
           $.get('/control?cmd=switch&name='+cmd_array[1], function(data) {
                 var result = data['result'];
                 if (data['err']) {
								$('#output')
										.append(
												"<div class='error'>"
														+ htmlEscape(result)
														+ "</div>");
							} else {
								$('#output')
										.append(
												"<div class='result'>switched.</div>");
							}
							$('#command')
									.val('');
            });
        }  else {
			droiuby.history.push(command);
			if (droiuby.history.length > 10) {
				droiuby.history.shift();
			}
			;

			$
					.ajax({
						type : 'POST',
						url : '/console',
						data : {
							"cmd" : command
						},
						success : function(
								data) {
							var command = data['cmd'];
							var result = data['result'];
							droiuby.pointer = null;
							$('#output')
									.append(
											"<div class='command'>"
													+ htmlEscape(command)
													+ "</div>");
							if (data['err']) {
								$('#output')
										.append(
												"<div class='error'>"
														+ htmlEscape(result)
														+ "</div>");
							} else {
								$('#output')
										.append(
												"<div class='result'>"
														+ htmlEscape(result)
														+ "</div>");
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
				function() {
					$('#command').focus();
					
					$('#toggle_multiline').live('click',
							function(event) {
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
					
					$('#submit').click(function(event) {
						var command = $('#command_multiline').val();
						$('#command_multiline').val('');
						droiuby.passCommand(command);
					});
					
					$("#command")
							.keydown(
									function(event) {
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

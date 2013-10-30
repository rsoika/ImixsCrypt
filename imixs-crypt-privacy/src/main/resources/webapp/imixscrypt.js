/**
 * This Library contains methods to manage the ImixsCrypt UI and interact with
 * the local imixsCrypt servers REST API
 * 
 * 
 * @author rsoika
 * @version 1.0.0
 */

/*
 * This method toggle the current page section. The UI is divided in the
 * following sections:
 * 
 * welcome_id = marketing stuff
 * 
 * start_id = the key generation page
 * 
 * login_id = the login page
 * 
 * workspace_id = the working page in started session
 * 
 */

var pageSections = [ 'welcome_id', 'start_id', 'login_id', 'workspace_id' ];

/*
 * Toggle the current page section
 */
function togglePage(section) {
	$.each(pageSections, function(index, value) {
		if (section == value) {
			if (!$('#' + value).is(":visible"))
				$('#' + value).show();
		} else {
			if ($('#' + value).is(":visible"))
				$('#' + value).hide();
		}
	});

}

/*
 * This Method loads the local public key. If the key is available the method
 * switch into the Login screen. Otherwise the method switch to the start screen
 */
function initImixsCrypt() {

	// get public key....
	$.getJSON("/rest/session/", function(data) {
		console.log("success");
		if (data.key == null) {
			// key is not available so show start screen
			togglePage('welcome_id');
		} else {
			// public key is availalbe so start login
			togglePage('login_id');
		}
	}).done(function() {
		console.log("second success");
	}).fail(function() {
		console.log("error");
	}).always(function() {
		console.log("complete");
	});

}

/*
 * This method verifies the password input and starts a key generation
 */
function createKey() {
	// disable input
	password1 = $("#password_id1").prop('disabled', true);
	password2 = $("#password_id2").prop('disabled', true);
	// get password
	var password1 = $("#password_id1").val();
	var password2 = $("#password_id2").val();
	if (password1 != password2) {
		// enable input
		password1 = $("#password_id1").prop('disabled', false);
		password2 = $("#password_id2").prop('disabled', false);
		alert('Password not correct!');

		password1 = $("#password_id1").val('');
		password2 = $("#password_id2").val('');
	} else {
		// start session Sending password
		var saveData = $.ajax({
			type : 'POST',
			dataType : "text",
			processData : false,
			contentType : 'text/plain',
			url : "http://localhost:4040/rest/session",
			data : password1,
			success : function(resultData) {
				// key generated and session enabled - switch to workspace
				togglePage('workspace_id');
			}
		});
		saveData.error(function() {
			alert("Something went wrong");
		});
	}
}

/*
 * This method posts the password and creates a new session
 */
function login() {
	// disable input
	$("#password_id").prop('disabled', true);
	// get password
	var password = $("#password_id").val();

	// open session and Sending password
	var saveData = $.ajax({
		type : 'POST',
		dataType : "text",
		processData : false,
		contentType : 'text/plain',
		url : "http://localhost:4040/rest/session",
		data : password,
		success : function(resultData) {

			// read notes
			readNotes();

			// Login successful and session started - switch to workspace
			togglePage('workspace_id');
		}
	});
	saveData.error(function() {
		alert("Something went wrong");
	});

}
function createNote() {

	$("#workspace_notes #notes_table").hide();
	$("#workspace_notes #notes_editor").show();

}

function closeNote() {

	$("#workspace_notes #notes_table").show();
	$("#workspace_notes #notes_editor").hide();

}

/*
 * Read all notes from the local data directory
 */
function readNotes() {

	// get public key....
	$.getJSON("/rest/notes/", function(data) {
		console.log("success");
		if (data != null) {
			// build list
			var tableBody = $('#notes_table .table tbody');
			$.each(data, function(index, value) {
				var d = new Date();
				d.setTime(value.modified);
				
				var html = $('<tr><td>' + index + '</td><td>' + value.name + '</td><td>' + d + '</td></tr>');
				tableBody.append(	html);
			});
	

		}
	}).done(function() {
		console.log("second success");
	}).fail(function() {
		console.log("error");
	}).always(function() {
		console.log("complete");
	});

}

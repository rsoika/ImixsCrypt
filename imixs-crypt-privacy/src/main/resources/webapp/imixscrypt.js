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
var publicKey = null;
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
			publicKey = data;
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
	// get email and password
	var email = $("#start_id #email_id").val();
	var password1 = $("#start_id #password_id1").val();
	var password2 = $("#start_id #password_id2").val();

	if (email == null || email == "") {
		alert('Please enter a Emailaddress.');
		return;
	}
	if (password1 != password2) {
		// enable input
		$("#start_id #password_id1").prop('disabled', false);
		$("#start_id #password_id2").prop('disabled', false);

		$("#start_id #password_id1").val('');
		$("#start_id #password_id2").val('');
		alert('Password not correct!');
		return;
	}

	// disable input
	$("#start_id #password_id1").prop('disabled', true);
	$("#start_id #password_id2").prop('disabled', true);
	$("#start_id #email_id").prop('disabled', true);

	// start session Sending password
	var saveData = $.ajax({
		type : 'POST',
		dataType : "text",
		processData : false,
		contentType : 'text/plain',
		url : "http://localhost:4040/rest/session/" + email,
		data : password1,
		success : function(resultData) {
			//publicKey = resultData;
			
			publicKey= jQuery.parseJSON(resultData);
			// key generated and session enabled - switch to workspace
			togglePage('workspace_id');
		}
	});
	saveData.error(function() {
		alert("Something went wrong");
	});

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
		url : "http://localhost:4040/rest/session/"+publicKey.user,
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

/**
 * Updates the worspace profile section
 */
function updateProfile() {
	//alert(publicKey.key); 
	$("#workspace_myprofile #publickey_id").val(publicKey.key);
	$("#workspace_myprofile #email_id").val(publicKey.user);

}
/**
 * Opens the note editor to edit the note. If not name is given a new note is
 * created. If a name is given a ajax request is started to load the text.
 */
function editNote(name) {

	$("#workspace_notes #notes_table").hide();
	$("#workspace_notes #notes_editor").show();

	// load notes per ajax
	if (name != null) {
		// decrypt data
		$.ajax({
			type : 'GET',
			dataType : "text",
			processData : false,
			contentType : 'application/json',
			url : "/rest/notes/decrypt/" + name,
			success : function(data) {
				console.log("success");
				// alert(data);
				if (data != null) {
					// fill editor
					$('#notes_editor textarea.tinymce').html(data);
					$('#notes_editor #notes_title').val(name);

				} else {
					// corrupted data
					alert("Unable to decrypt data '" + name
							+ "'. Please verify your data files!");
					// clear input data
					$('#notes_editor textarea.tinymce').html('');
					$('#notes_editor #notes_title').val('');
					$("#workspace_notes #notes_editor").hide();
					$("#workspace_notes #notes_table").show();
				}
			},
			error : function(data) {
				alert('An error ocurred during decryption!');
				// clear input data
				$('#notes_editor textarea.tinymce').html('');
				$('#notes_editor #notes_title').val('');
				$("#workspace_notes #notes_editor").hide();
				$("#workspace_notes #notes_table").show();
			}
		});

	} else {
		// clear input data
		$('#notes_editor textarea.tinymce').html('');
		$('#notes_editor #notes_title').val('');
	}

}

/**
 * Opens the note editor to edit the note. If not name is given a new note is
 * created. If a name is given a ajax request is started to load the text.
 */
function deleteNote(name) {

	if (!confirm("Delete '" + name + "' ?"))
		return;
	if (!confirm("Are you really sure ?"))
		return;

	// delete per ajax POST request
	if (name != null) {
		// decrypt data
		$.ajax({
			type : 'DELETE',
			dataType : "text",
			processData : false,
			contentType : 'application/json',
			url : "/rest/notes/" + name,
			success : function(data) {
				console.log("success");
				// alert('data delted');
				readNotes();
			},
			error : function(data) {
				alert('An error ocurred during deletion of data!');

			}
		});
	}
}

/**
 * Saves the data of the note editor.
 */
function saveNote() {

	var content = $('#notes_editor textarea.tinymce').tinymce().getContent();
	var name = $('#notes_editor #notes_title').val();

	// load notes per ajax
	if (name != null) {
		// decrypt data
		$.ajax({
			type : 'POST',
			dataType : "text",
			processData : false,
			contentType : 'application/json',
			data : content,
			url : "/rest/notes/encrypt/" + name,
			success : function() {
				console.log("success");
				// alert('Encrypted!');
				$("#workspace_notes #notes_editor").hide();
				$("#workspace_notes #notes_table").show();
				readNotes();
			}
		});

	}

}

function closeNote() {

	$("#workspace_notes #notes_table").show();
	$("#workspace_notes #notes_editor").hide();

}

/*
 * Read all notes from the local data directory
 */
function readNotes() {

	// get note list....
	$.getJSON(
			"/rest/notes/",
			function(data) {
				console.log("success");
				if (data != null) {
					// build list
					var tableBody = $('#notes_table .table tbody');
					tableBody.empty();
					$.each(data, function(index, value) {
						var d = new Date();
						d.setTime(value.modified);

						var aDelete = "<td><a href='#' onclick=\"deleteNote('"
								+ value.name + "');\">Delete</a></td>;";

						var html = $("<tr><td>" + index
								+ "</td><td><a href='#' onclick=\"editNote('"
								+ value.name + "');\">" + value.name
								+ "</a></td><td>" + d + "</td>" + aDelete
								+ "</tr>");
						tableBody.append(html);
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

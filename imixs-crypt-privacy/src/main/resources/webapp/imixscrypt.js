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
var myIdentity = null;
var messageItem = null;
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
	$.getJSON("/rest/identities", function(data) {
		console.log("success");
		if (data.key == null) {
			// key is not available so show start screen
			togglePage('welcome_id');
		} else {
			myIdentity = data;
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

	// reinialize identity
	var jsonData = '{"id":"' + email + '","key":"' + btoa(password1) + '"}';
	// start session Sending password
	var saveData = $.ajax({
		type : 'POST',
		dataType : "json",
		processData : false,
		contentType : 'application/json',
		url : "/rest/identities",
		data : jsonData,
		success : function(resultData) {
			// publicKey = resultData;

			myIdentity = resultData;
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
	var jsonData = '{"key":"' + btoa(password) + '"}';

	// open session and Sending password
	var saveData = $.ajax({
		type : 'POST',
		dataType : "json",
		processData : false,
		contentType : 'application/json',
		url : "/rest/identities",
		data : jsonData,
		success : function(resultData) {
			myIdentity = resultData;
			// read notes
			readNotes();
			// Login successful and session started - switch to workspace
			togglePage('workspace_id');
			publishPublicKey();
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
	// alert(publicKey.key);
	$("#workspace_myprofile #publickey_id").val(publicKey.key);
	$("#workspace_myprofile #email_id").val(publicKey.user);

}

/**
 * Verifies the current public node and updates the chat section
 */
function updateChat() {

	// get public node...
	var saveData = $.ajax({
		type : 'GET',
		dataType : "text",
		processData : false,
		contentType : 'application/json',
		url : "/rest/session/properties/default.public.node",
		success : function(resultData) {
			// alert(resultData);
			if (resultData == null) {
				$("#workspace_chat #no_public_node").show();
				$("#workspace_chat #chat").hide();
			} else {
				$("#workspace_chat #no_public_node").hide();
				$("#workspace_chat #chat").show();
				// alert(resultData);
				// insert server address
				$("#workspace_chat #chat #publicnode_id").append(resultData);
			}
		}
	});
	saveData.error(function() {
		alert("Something went wrong");
	});

}

function savePublicNode(servernode) {
	// get public node...
	var saveData = $.ajax({
		type : 'POST',
		dataType : "text",
		processData : false,
		contentType : 'application/json',
		url : "/rest/session/properties/default.public.node",
		data : servernode,
		success : function(resultData) {

			$("#workspace_chat #no_public_node").hide();

		}
	});
	saveData.error(function() {
		alert("Something went wrong");
	});
}

/**
 * Opens the note editor to edit the note with the given message digest. If not
 * message digest is given a new empty note is created.
 * 
 * If a message digest is given a ajax request is started to load the text.
 */
function editNote(digest) {

	$("#workspace_notes #notes_table").hide();
	$("#workspace_notes #notes_editor").show();

	// load notes per ajax
	if (digest != null) {
		// decrypt data
		$.ajax({
			type : 'GET',
			dataType : "json",
			processData : false,
			contentType : 'application/json',
			url : "/rest/messages/" + digest,
			success : function(result) {
				messageItem = result;
				console.log("success");
				// alert(data);
				if (messageItem != null) {
					// decode base64 message body....
					var encodedMessage = messageItem.message;
					var encodedComment = messageItem.comment;
					var content = atob(encodedMessage);
					// fill editor
					$('#notes_editor textarea.tinymce').html(
							atob(encodedMessage));
					$('#notes_editor #notes_title').val(atob(encodedComment));

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
		messageItem = null;
		$('#notes_editor textarea.tinymce').html('');
		$('#notes_editor #notes_title').val('');
	}

}

/**
 * Opens the note editor to edit the note. If not name is given a new note is
 * created. If a name is given a ajax request is started to load the text.
 */
function deleteLocalMessage(digest, name) {

	if (!confirm("Delete '" + name + "' ?"))
		return;
	if (!confirm("Are you really sure ?"))
		return;

	// delete per ajax POST request
	if (digest != null) {
		// decrypt data
		$.ajax({
			type : 'DELETE',
			dataType : "text",
			processData : false,
			url : "/rest/messages/" + digest,
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
function saveLocalMessage() {

	var content = $('#notes_editor textarea.tinymce').tinymce().getContent();
	var name = $('#notes_editor #notes_title').val();

	// encode base64 content and comment
	// create json data string
	var jsonData = '{"comment":"' + btoa(name) + '","message":"'
			+ btoa(content) + '"}';

	var oldMessageDigest = null;
	if (messageItem != null)
		oldMessageDigest = messageItem.digest;

	// load notes per ajax
	if (name != null) {
		// decrypt data
		$.ajax({
			type : 'POST',
			dataType : "json",
			processData : false,
			contentType : 'application/json',
			data : jsonData,
			url : "/rest/messages",
			success : function(result) {
				console.log("success");
				// alert('Encrypted!');
				$("#workspace_notes #notes_editor").hide();
				$("#workspace_notes #notes_table").show();
				// update last messageItem
				messageItem = result;

				readNotes();
			}
		});

		// now delete deprecated local message
		if (oldMessageDigest != null) {
			$.ajax({
				type : 'DELETE',
				dataType : "text",
				processData : false,
				url : "/rest/messages/" + oldMessageDigest
			});
		}

	}

}

function closeNote() {

	$("#workspace_notes #notes_table").show();
	$("#workspace_notes #notes_editor").hide();

}

/*
 * Fetches all local notes
 */
function readNotes() {

	// get note list....
	$
			.getJSON(
					"/rest/messages",
					function(data) {
						console.log("success");
						if (data != null) {
							// build list
							var tableBody = $('#notes_table .table tbody');
							tableBody.empty();
							$
									.each(
											data,
											function(index, value) {
												var d = new Date();
												d.setTime(value.created);

												// base64 decode comment string
												var comment = atob(value.comment);

												var aDelete = "<td><a href='#' onclick=\"deleteLocalMessage('"
														+ value.digest
														+ "','"
														+ comment
														+ "');\">Delete</a></td>;";

												var html = $("<tr><td>"
														+ index
														+ "</td><td><a href='#' onclick=\"editNote('"
														+ value.digest
														+ "');\">" + comment
														+ "</a></td><td>" + d
														+ "</td>" + aDelete
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

/**
 * sends a message
 */
function sendMessage() {
	var receipient = $('#message_editor #receipient_id').val();
	// alert(receipient);

	var content = $('#message_editor textarea.tinymce').tinymce().getContent();

	// alert(content);

	// var jsonData="{\"user\":\"" + receipient + "\",\"message\":\"" + content
	// + "\"}";
	// alert(jsonData);
	// load notes per ajax
	if (content != null) {
		// decrypt data
		$.ajax({
			type : 'POST',
			dataType : "text",
			processData : false,
			contentType : 'application/json',
			data : content,
			url : "/rest/message/" + receipient,
			success : function() {
				console.log("success");
				alert('Encrypted message send!');

			}
		});

	}

}

/**
 * sends the local public key to the identity service
 */
function publishPublicKey() {
	// post public key
	// alert(JSON.stringify( publicKey));
	var jsonData = JSON.stringify(publicKey);
	$.ajax({
		type : 'POST',
		dataType : "json",
		processData : false,
		contentType : 'application/json',
		data : jsonData,
		url : "/rest/session/publicnode/",
		success : function() {
			console.log("success");
			// alert('Public Key published');

		}
	});

}

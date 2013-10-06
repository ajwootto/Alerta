$( document ).ready(function() {

	var email = new Firebase('https://alerta.firebaseio.com/');

	$('#emailForm').submit(function(e) {
	 	e.preventDefault();
	 	
	 	email.push({
	 		firstname: $('.customer_firstname').val(),
	 		lastname: $('.customer_lastname').val(),
	 		email: $('.customer_email').val(),
	 	});

	 	$('#emailForm')[0].reset()
	})
});

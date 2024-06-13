const toggleSideBar = () => {
	if ($(".sidebar").is(":visible")) {
		//close sidebar
		$(".sidebar").css("display", "none");
		$(".content").css("margin-left", "0%");
		$(".content").css("width", "100%");
	}
	else {
		//open sidebar
		$(".sidebar").css("display", "block");
		$(".content").css("margin-left", "23%");
	}
}

const search = () => {

	let query = $("#search-input").val();

	if (query === '') {
		$(".search-result").hide();
	}
	else {
		let url = `http://localhost:8080/search/${query}`;
		fetch(url).then((response) => response.json())
			.then((data) => {
				let text = `<div class='list-group'>`;
				data.forEach(contact => {
					text += `<a href='/user/${contact.cid}/contact' class='list-group-item list-group-action'>${contact.name}</a>`;
				});
				text += `</div>`;
				$(".search-result").html(text);
				$(".search-result").show();
			});
	}

}

const searchForUser = () => {

	let query = $("#search-input").val();

	if (query === '') {
		$(".search-result").hide();
	}
	else {
		let url = `http://localhost:8080/search-user/${query}`;
		fetch(url).then((response) => response.json())
			.then((data) => {
				let text = `<div class='list-group'>`;
				data.forEach(user => {
					text += `<a href='/admin/user-profile/${user.id}' class='list-group-item list-group-action'>${user.name}</a>`;
				});
				text += `</div>`;
				$(".search-result").html(text);
				$(".search-result").show();
			});
	}

}

const updatePaymentOnServer = (payment_id, order_id, status)=> {
    $.ajax({
		url: '/user/update-order',
		data: JSON.stringify({ payment_id : payment_id, order_id : order_id, status : status }),
		contentType: 'application/json',
		type: 'POST',
		dataType: 'json',
		success: function(response){
			swal("Congrats !!!", "Payment successful...", "success");
		},
		error: function(error){
			swal("Oops !!!", "Payment failed ...", "error");
		}
	});
}

const paymentStart = () => {
	console.log("Payment started ...");
	let amount = $("#payment_field").val();
	console.log(amount);
	if (amount === '' || amount === null) {
		// alert("Amount is required !!!");
		swal("Oops !!!", "Amount is required ...", "error");
		return;
	}
	$.ajax({
		url: '/user/create-order',
		data: JSON.stringify({ amount: amount, info: 'order_request' }),
		contentType: 'application/json',
		type: 'POST',
		dataType: 'json',
		success: function (response) {
			console.log(response);
			if (response.status === 'created') {
				let options = {
					key: 'rzp_test_aioxHHMyOq8spx',
					amount: response.amount,
					currency: 'INR',
					name: 'Smart Contact Manager',
					description: 'Donation',
					image: 'https://i.pinimg.com/564x/15/17/f8/1517f8381d7d16a5ed622a8c0909df97.jpg',
					order_id: response.id,
					handler: function (response) {
						console.log(response.razorpay_payment_id);
						console.log(response.razorpay_order_id);
						console.log(response.razorpay_signature);
						console.log("Payment Successful !!!");
						// alert("Congrats, Payment successful ...");
						updatePaymentOnServer(response.razorpay_payment_id, response.razorpay_order_id, "paid");
						// swal("Congrats !!!", "Payment successful...", "success");
					},
					prefill: {
						name: "",
						email: "",
						contact: ""
					},
					notes: {
						address: "Kolkata, Barrackpore",
					},
					theme: {
						color: "#3399cc"
					}
				};
				let rzp1 = new Razorpay(options);
				rzp1.on('payment.failed', function (response) {
					console.log(response.error.code);
					console.log(response.error.description);
					console.log(response.error.source);
					console.log(response.error.step);
					console.log(response.error.reason);
					console.log(response.error.metadata.order_id);
					console.log(response.error.metadata.payment_id);
					console.log("Payment failed !!!");
					// alert("Payment Failed !!!");
					swal("Oops !!!", "Payment failed ...", "error");
				});
				rzp1.open();
			}
		},
		error: function (error) {
			console.log(error);
			// alert("Something went wrong !!!");
			swal("Oops !!!", "Something went wrong ...", "error");
		}
	});
}
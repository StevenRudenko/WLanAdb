function goBack() {
	window.history.go(-1);
}

function changeContent(newContent) {
	document.getElementById("content").setAttribute('src', newContent);
}

setInterval(function() {
	var today = new Date();
	document.getElementById('time_block').innerHTML = today.toLocaleTimeString().replace(/(.+)(\:\d{2})/, ' $1');
}, 500); 
function goBack() {
	window.history.go(-1);
}

function changeContent(newContent) {
	document.getElementById("content").setAttribute('src', newContent);
}

function onDownload(link) {
	_gaq.push(['_trackEvent', 'Downloads', 'Download desktop client', 'Link <' + text($(link).html()) + '>']);
}

setInterval(function() {
	var today = new Date();
	document.getElementById('time_block').innerHTML = today.toLocaleTimeString().replace(/(.+)(\:\d{2})/, ' $1');
}, 500); 
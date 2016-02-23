var ajaxCounter={};

function changeButtonsAvailability(targetDiv, available) {

	var commandButtons = $('div[id*='+targetDiv+'] input');
	if(ajaxCounter[targetDiv] == undefined){
		ajaxCounter[targetDiv] = 0;
	}

	if(available){
		ajaxCounter[targetDiv]--;
		if(ajaxCounter[targetDiv] == 0 ){
			commandButtons.removeAttr('disabled');
		}
	}else{
		ajaxCounter[targetDiv]++;
		commandButtons.attr('disabled','disabled');
	}

}

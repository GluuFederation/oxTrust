var visible = false;

function slideUpAndDown() {
	$(document).ready(function() {
		var element = document.getElementById("personForm:attrs");
		if (element.style.display == "none") {
			sh();
		} else {
			hi();

		}
	});
};
/*
Effect.BlindRight = function(element) {
	element = $(element);
	var elementDimensions = element.getDimensions();
	return new Effect.Scale(element, 100, Object.extend({
		scaleContent : true,
		scaleY : false,
		scaleFrom : 0,
		scaleMode : {
			originalHeight : elementDimensions.height,
			originalWidth : elementDimensions.width
		},
		restoreAfterFinish : true,
		afterSetup : function(effect) {
			effect.element.makeClipping().setStyle({
				width : '0px',
				height : effect.dims[0] + 'px'
			}).show();
		},
		afterFinishInternal : function(effect) {
			effect.element.undoClipping();
		}
	}, arguments[1] || {}));
};

Effect.BlindLeft = function(element) {
	element = $(element);
	element.makeClipping();
	return new Effect.Scale(element, 0, Object.extend({
		scaleContent : false,
		scaleY : false,
		scaleMode : 'box',
		scaleContent : true,
		restoreAfterFinish : true,
		afterSetup : function(effect) {
			effect.element.makeClipping().setStyle({
				height : effect.dims[0] + 'px'
			}).show();
		},
		afterFinishInternal : function(effect) {
			effect.element.hide().undoClipping();
		}
	}, arguments[1] || {}));
};*/
deployJava.writeAppletTag = function (attributes, parameters) {    
        var pokenApplet = document.createElement('applet');
		if (attributes != 'undefined' && attributes != null) {
            for (var attribute in attributes) {
				pokenApplet.setAttribute(attribute, attributes[attribute]);
            }
        }
		pokenApplet.setAttribute("id","pokenApplet");
		var applets = document.getElementById("applets");
		applets.appendChild(pokenApplet);
}
var lastStatus;
var lastCode;
var sysLocation;
function start(location){
	sysLocation = location;
	document.getElementById("detect").onclick=function(event){
    var attributes = {code:'org.gluu.site.applet.GluuPokenSync.class',
                      archive:sysLocation + '/GluuApplet.jar',
					  mayscript:true,
					  width:1,
					  height:1
					  } ;
    var version = '1.6' ;
    deployJava.runApplet(attributes, null, version);
	}
}

function sendStatus(status,code){	
	if (lastStatus === status) {
            return
        }
	if (status == "connected") {
            if (code) {
				show(status,code)
                lastCode = code;
            }
    }
	if (status == "disconnected") {
        show(status,lastCode);
    }
       lastStatus = status;
}

function show(status,code){
	var codeDisplay = document.createElement('div');
	codeDisplay.setAttribute('id',status);
	reply = document.createTextNode(code);
	
	if (status == 'connected') {
		textNode = document.createTextNode("Poken Detected")
		codeDisplay.innerHTML = '<img src="'+sysLocation+'/img/green.png" alt="Device dconnected"  width="30" height="30"/>';
	}
	if (status == 'disconnected') {
		textNode = document.createTextNode("Poken Not Connected")
		codeDisplay.innerHTML = '<img src="'+sysLocation+'/img/red.png" alt="Device disconnected"  width="30" height="30"/>';
	}
	
	
	var statusDiv = document.getElementById("codeDisplay");
	while (statusDiv.hasChildNodes()){
		statusDiv.removeChild(statusDiv.lastChild);
	}

	statusDiv.appendChild(textNode);
	statusDiv.appendChild(codeDisplay);
	updateString(code);
	//codeDisplay.appendChild(reply);
}
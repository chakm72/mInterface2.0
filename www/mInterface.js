window.minterface = function(task,args,success,error) {
	cordova.exec(success, error, "mInterface", task, args);
};

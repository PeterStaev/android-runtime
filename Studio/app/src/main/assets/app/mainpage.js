__enableVerboseLogging();

var Base2 = com.tns.tests.BaseClass.extend({
    sum: function() {
        __enableVerboseLogging();
        __log("com.tns.tests.BaseClass.sum() called");
        __disableVerboseLogging();
    }
});



//var baseClass = new BaseClass2();



//require("./tests/testWeakRef");
//require("./tests/tests");
//require("./tests/testMethodResolution");
//require("./tests/testArrays");
//require("./tests/testsForRuntimeBindingGenerator");
//require("./tests/testPrimitiveTypeConversion");
//require("./tests/numericConversionTests");
//require("./tests/inheritanceChainResolutionTest");
//require("./tests/exceptionHandlingTests");
//require("./tests/dispatchCallbacksOnUiThreadTests");
//require("./tests/stringConversionTests");
//require("./tests/testsForTypescript");
//require("./tests/testGC");
//require("./tests/testsMemoryManagement");
//require("./tests/testIfAbleToRunExternalFile");
//require("./tests/testFieldGetSet");
//require("./tests/extendedClassesTests");
//require("./tests/extendClassNameTests");
//require("./tests/testJniReferenceLeak");

var MainActivity = {
    onCreate: function (bundle) {

        var packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
        var code = packageInfo.versionCode;
        var updateTime = packageInfo.lastUpdateTime;
        var thumb = java.lang.String.valueOf(updateTime) + "-" + java.lang.String.valueOf(code);



    	var packageName = this.getPackageName();
    	__log("The package name is " + packageName);
    	//var baseClass = java.lang.Class.forName("com.tns.tests.BaseClass_fmainpage_l3_c37__");
    	var baseClass = java.lang.Class.forName(Base2.class.getName());

        __log("The class name is " + baseClass.getName());

        __log("Creating instance " + baseClass.getName());
        var instance = baseClass.newInstance();

        instance.sum();








    	__log("-----------> onCreate from js");
    	var k = this.toString();
    	__log("this.toString " + k);
    	this.super.onCreate(bundle);

    	require("./tests/testsWithContext").run(this);
    	
    	execute(); //run jasmine
    	
    	var layout = new android.widget.LinearLayout(this);
    	layout.setOrientation(1);
    	this.setContentView(layout);
    	
    	var textView = new android.widget.TextView(this);
    	textView.setText("Hit that sucker");
    	layout.addView(textView);
    	
    	var button = new android.widget.Button(this);
    	button.setText("Hit me");
    	layout.addView(button);
    	var counter = 0;
    	button.setOnClickListener(new android.view.View.OnClickListener("AppClickListener", {
    		onClick:  function() {
    			__log("onClick called");  
    			button.setText("Hit that sucker one more time " + ++counter);
    		}}));
    }
}; 

app.init({
	
	getActivity: function(activity) {
		var intent = activity.getIntent();
		__log("intent=" + intent)
		var action = intent.getAction();
		__log("action=" + action)
		return MainActivity;
	},
	
	
	onCreate: function() {
		__log("Application on create called");
	},

	onLiveSync: function() {
    	__log("LiveSync called");
    }
});
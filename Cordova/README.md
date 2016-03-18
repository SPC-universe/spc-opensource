# Cordova SPCFit Plugin

Código opensource para la pulsera SPC Fitness

## Using

Clone the plugin

    $ git clone https://github.com/SPC-universe/spc-opensource.git

Create a new Cordova Project

    $ cordova create spcfit-cordova-example com.spc.spcfitexample SPCFitExample

Install the plugin

    $ cd spcfit-cordova-example
    $ cordova plugin add ../spc-opensource/Cordova

Edit `www/js/index.js` and add the following code inside `onDeviceReady`

```js
    var success = function(message) {
        alert(message);
    }

    var failure = function() {
        alert("Error calling SPCFit Plugin");
    }

    SPCFit.test("World", success, failure);
```

Or you can also use our example www.zip instead of the default www directory

    $ unzip ../spc-opensource/Cordova/www.zip

Install iOS or Android platform

    cordova platform add ios
    cordova build ios
    Open the Xcode project inside your app (platforms/iOS/SPCFitExample.xcodeproj)

    cordova platform add android
    cordova build android
    cordova run

## More Info

For more information on setting up Cordova see [the documentation](http://cordova.apache.org/docs/en/4.0.0/guide_cli_index.md.html#The%20Command-Line%20Interface)

For more info on plugins see the [Plugin Development Guide](http://cordova.apache.org/docs/en/4.0.0/guide_hybrid_plugins_index.md.html#Plugin%20Development%20Guide)

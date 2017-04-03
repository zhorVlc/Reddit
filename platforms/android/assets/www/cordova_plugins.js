cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "id": "cordova-plugin-device.device",
        "file": "plugins/cordova-plugin-device/www/device.js",
        "pluginId": "cordova-plugin-device",
        "clobbers": [
            "device"
        ]
    },
    {
        "id": "cordova-plugin-geolocation.geolocation",
        "file": "plugins/cordova-plugin-geolocation/www/android/geolocation.js",
        "pluginId": "cordova-plugin-geolocation",
        "clobbers": [
            "navigator.geolocation"
        ]
    },
    {
        "id": "cordova-plugin-geolocation.PositionError",
        "file": "plugins/cordova-plugin-geolocation/www/PositionError.js",
        "pluginId": "cordova-plugin-geolocation",
        "runs": true
    },
    {
        "id": "cordova-plugin-googlemaps.cordova-plugin-googlemaps",
        "file": "plugins/cordova-plugin-googlemaps/www/googlemaps-cdv-plugin.js",
        "pluginId": "cordova-plugin-googlemaps",
        "clobbers": [
            "cordova-plugin-googlemaps"
        ]
    },
    {
        "id": "cordova-plugin-health.health",
        "file": "plugins/cordova-plugin-health/www/android/health.js",
        "pluginId": "cordova-plugin-health",
        "clobbers": [
            "navigator.health"
        ]
    },
    {
        "id": "cordova-plugin-splashscreen.SplashScreen",
        "file": "plugins/cordova-plugin-splashscreen/www/splashscreen.js",
        "pluginId": "cordova-plugin-splashscreen",
        "clobbers": [
            "navigator.splashscreen"
        ]
    },
    {
        "id": "cordova-plugin-statusbar.statusbar",
        "file": "plugins/cordova-plugin-statusbar/www/statusbar.js",
        "pluginId": "cordova-plugin-statusbar",
        "clobbers": [
            "window.StatusBar"
        ]
    },
    {
        "id": "ionic-plugin-keyboard.keyboard",
        "file": "plugins/ionic-plugin-keyboard/www/android/keyboard.js",
        "pluginId": "ionic-plugin-keyboard",
        "clobbers": [
            "cordova.plugins.Keyboard"
        ],
        "runs": true
    },
    {
        "id": "cordova-plugin-datepicker.DatePicker",
        "file": "plugins/cordova-plugin-datepicker/www/android/DatePicker.js",
        "pluginId": "cordova-plugin-datepicker",
        "clobbers": [
            "datePicker"
        ]
    },
    {
        "id": "cordova-plugin-mapbox.Mapbox",
        "file": "plugins/cordova-plugin-mapbox/www/Mapbox.js",
        "pluginId": "cordova-plugin-mapbox",
        "clobbers": [
            "Mapbox"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "cordova-plugin-console": "1.0.5",
    "cordova-plugin-device": "1.1.4",
    "cordova-plugin-compat": "1.1.0",
    "cordova-plugin-geolocation": "2.4.2",
    "cordova-plugin-googlemaps": "1.4.0",
    "cordova-plugin-health": "0.9.0",
    "cordova-plugin-splashscreen": "4.0.2",
    "cordova-plugin-statusbar": "2.2.1",
    "cordova-plugin-whitelist": "1.3.1",
    "ionic-plugin-keyboard": "2.2.1",
    "cordova-plugin-datepicker": "0.9.3",
    "cordova-plugin-mapbox": "1.2.3"
};
// BOTTOM OF METADATA
});
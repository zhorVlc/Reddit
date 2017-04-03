package org.apache.cordova.health;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataTypeCreateRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataTypeResult;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Health plugin Android code.
 * MIT licensed.
 */
public class HealthPlugin extends CordovaPlugin {
    //logger tag
    private static final String TAG = "cordova-plugin-health";

    //calling activity
    private CordovaInterface cordova;

    //actual Google API client
    private GoogleApiClient mClient;

    private static final int REQUEST_OAUTH = 1;
    private CallbackContext authReqCallbackCtx;
    private boolean authAutoresolve = false;
    private static final LinkedList<String> dynPerms = new LinkedList<String>();
    private static final int REQUEST_DYN_PERMS = 2;

    //Scope for read/write access to activity-related data types in Google Fit.
    //These include activity type, calories consumed and expended, step counts, and others.
    public static Map<String, DataType> activitydatatypes = new HashMap<String, DataType>();
    static {
        activitydatatypes.put("steps", DataType.TYPE_STEP_COUNT_DELTA);
        activitydatatypes.put("calories", DataType.TYPE_CALORIES_EXPENDED);
        activitydatatypes.put("calories.basal", DataType.TYPE_BASAL_METABOLIC_RATE);
        activitydatatypes.put("activity", DataType.TYPE_ACTIVITY_SEGMENT);
    }

    //Scope for read/write access to biometric data types in Google Fit. These include heart rate, height, and weight.
    public static Map<String, DataType> bodydatatypes = new HashMap<String, DataType>();
    static {
        bodydatatypes.put("height", DataType.TYPE_HEIGHT);
        bodydatatypes.put("weight", DataType.TYPE_WEIGHT);
        bodydatatypes.put("heart_rate", DataType.TYPE_HEART_RATE_BPM);
        bodydatatypes.put("fat_percentage", DataType.TYPE_BODY_FAT_PERCENTAGE);
    }

    //Scope for read/write access to location-related data types in Google Fit. These include location, distance, and speed.
    public static Map<String, DataType> locationdatatypes = new HashMap<String, DataType>();
    static {
        locationdatatypes.put("distance", DataType.TYPE_DISTANCE_DELTA);
    }

    private static class NutrientFieldInfo {
        public String field;
        public String unit;

        public NutrientFieldInfo(String field, String unit) {
            this.field = field;
            this.unit = unit;
        }
    }

    //Lookup for nutrition fields and units
    public static Map<String, NutrientFieldInfo> nutrientFields = new HashMap<String, NutrientFieldInfo>();

    static {
        nutrientFields.put("nutrition.calories", new NutrientFieldInfo(Field.NUTRIENT_CALORIES, "kcal"));
        nutrientFields.put("nutrition.fat.total", new NutrientFieldInfo(Field.NUTRIENT_TOTAL_FAT, "g"));
        nutrientFields.put("nutrition.fat.saturated", new NutrientFieldInfo(Field.NUTRIENT_SATURATED_FAT, "g"));
        nutrientFields.put("nutrition.fat.unsaturated", new NutrientFieldInfo(Field.NUTRIENT_UNSATURATED_FAT, "g"));
        nutrientFields.put("nutrition.fat.polyunsaturated", new NutrientFieldInfo(Field.NUTRIENT_POLYUNSATURATED_FAT, "g"));
        nutrientFields.put("nutrition.fat.monounsaturated", new NutrientFieldInfo(Field.NUTRIENT_MONOUNSATURATED_FAT, "g"));
        nutrientFields.put("nutrition.fat.trans", new NutrientFieldInfo(Field.NUTRIENT_TRANS_FAT, "g"));
        nutrientFields.put("nutrition.cholesterol", new NutrientFieldInfo(Field.NUTRIENT_CHOLESTEROL, "mg"));
        nutrientFields.put("nutrition.sodium", new NutrientFieldInfo(Field.NUTRIENT_SODIUM, "mg"));
        nutrientFields.put("nutrition.potassium", new NutrientFieldInfo(Field.NUTRIENT_POTASSIUM, "mg"));
        nutrientFields.put("nutrition.carbs.total", new NutrientFieldInfo(Field.NUTRIENT_TOTAL_CARBS, "g"));
        nutrientFields.put("nutrition.dietary_fiber", new NutrientFieldInfo(Field.NUTRIENT_DIETARY_FIBER, "g"));
        nutrientFields.put("nutrition.sugar", new NutrientFieldInfo(Field.NUTRIENT_SUGAR, "g"));
        nutrientFields.put("nutrition.protein", new NutrientFieldInfo(Field.NUTRIENT_PROTEIN, "g"));
        nutrientFields.put("nutrition.vitamin_a", new NutrientFieldInfo(Field.NUTRIENT_VITAMIN_A, "IU"));
        nutrientFields.put("nutrition.vitamin_c", new NutrientFieldInfo(Field.NUTRIENT_VITAMIN_C, "mg"));
        nutrientFields.put("nutrition.calcium", new NutrientFieldInfo(Field.NUTRIENT_CALCIUM, "mg"));
        nutrientFields.put("nutrition.iron", new NutrientFieldInfo(Field.NUTRIENT_IRON, "mg"));
    }

    //Scope for read/write access to nutrition data types in Google Fit.
    public static Map<String, DataType> nutritiondatatypes = new HashMap<String, DataType>();

    static {
        nutritiondatatypes.put("nutrition", DataType.TYPE_NUTRITION);
        nutritiondatatypes.put("nutrition.water", DataType.TYPE_HYDRATION);
        for (String dataType : nutrientFields.keySet()) {
            nutritiondatatypes.put(dataType, DataType.TYPE_NUTRITION);
        }
    }

    public static Map<String, DataType> customdatatypes = new HashMap<String, DataType>();


    public HealthPlugin() {
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.cordova = cordova;
    }


    private void authReqSuccess() {
        //Create custom data types
        cordova.getThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                try {
                    String packageName = cordova.getActivity().getApplicationContext().getPackageName();
                    DataTypeCreateRequest request = new DataTypeCreateRequest.Builder()
                            .setName(packageName + ".gender")
                            .addField("gender", Field.FORMAT_STRING)
                            .build();
                    PendingResult<DataTypeResult> pendingResult = Fitness.ConfigApi.createCustomDataType(mClient, request);
                    DataTypeResult dataTypeResult = pendingResult.await();
                    if (!dataTypeResult.getStatus().isSuccess()) {
                        authReqCallbackCtx.error(dataTypeResult.getStatus().getStatusMessage());
                        return;
                    }
                    customdatatypes.put("gender", dataTypeResult.getDataType());

                    request = new DataTypeCreateRequest.Builder()
                            .setName(packageName + ".date_of_birth")
                            .addField("day", Field.FORMAT_INT32)
                            .addField("month", Field.FORMAT_INT32)
                            .addField("year", Field.FORMAT_INT32)
                            .build();
                    pendingResult = Fitness.ConfigApi.createCustomDataType(mClient, request);
                    dataTypeResult = pendingResult.await();
                    if (!dataTypeResult.getStatus().isSuccess()) {
                        authReqCallbackCtx.error(dataTypeResult.getStatus().getStatusMessage());
                        return;
                    }
                    customdatatypes.put("date_of_birth", dataTypeResult.getDataType());

                    Log.i(TAG, "All custom data types created");
                    requestDynamicPermissions();
                } catch (Exception ex) {
                    authReqCallbackCtx.error(ex.getMessage());
                }
            }
        });
    }

    private void requestDynamicPermissions() {
        if (dynPerms.isEmpty()) {
            // nothing to be done
            authReqCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
        } else {
            LinkedList<String> perms = new LinkedList<String>();
            for (String p : dynPerms) {
                if (!cordova.hasPermission(p)) {
                    perms.add(p);
                }
            }
            if (perms.isEmpty()) {
                // nothing to be done
                authReqCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
            } else {
                if (authAutoresolve) {
                    cordova.requestPermissions(this, REQUEST_DYN_PERMS, perms.toArray(new String[perms.size()]));
                } else {
                    authReqCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                }
            }
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        if (requestCode == REQUEST_DYN_PERMS) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    String errmsg = "Permission denied ";
                    for (String perm : permissions) {
                        errmsg += " " + perm;
                    }
                    authReqCallbackCtx.error("Permission denied: " + permissions[i]);
                    return;
                }
            }
            //all accepted!
            authReqCallbackCtx.success();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_OAUTH) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Got authorisation from Google Fit");
                if (!mClient.isConnected() && !mClient.isConnecting()) {
                    Log.d(TAG, "Re-trying connection with Fit");
                    mClient.connect();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user cancelled the login dialog before selecting any action.
                authReqCallbackCtx.error("User cancelled the dialog");
            } else authReqCallbackCtx.error("Authorisation failed, result code " + resultCode);
        }
    }

    /**
     * The "execute" method that Cordova calls whenever the plugin is used from the JavaScript
     *
     * @param action          The action to execute.
     * @param args            The exec() arguments.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return
     * @throws JSONException
     */
    @Override
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {

        if ("isAvailable".equals(action)) {
            isAvailable(callbackContext);
            return true;
        } else if ("promptInstallFit".equals(action)) {
            promptInstall(callbackContext);
            return true;
        } else if ("requestAuthorization".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkAuthorization(args, callbackContext, true); // with autoresolve
                    } catch (Exception ex) {
                        callbackContext.error(ex.getMessage());
                    }
                }
            });
            return true;
        } else if ("checkAuthorization".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkAuthorization(args, callbackContext, false); // without autoresolve
                    } catch (Exception ex) {
                        callbackContext.error(ex.getMessage());
                    }
                }
            });
            return true;
        } else if ("isAuthorized".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkAuthorization(args, callbackContext, false);
                    } catch (Exception ex) {
                        callbackContext.error(ex.getMessage());
                    }
                }
            });
            return true;
        } else if ("query".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        query(args, callbackContext);
                    } catch (Exception ex) {
                        callbackContext.error(ex.getMessage());
                    }
                }
            });
            return true;
        } else if ("queryAggregated".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        queryAggregated(args, callbackContext);
                    } catch (Exception ex) {
                        callbackContext.error(ex.getMessage());
                    }
                }
            });
            return true;
        } else if ("store".equals(action)) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        store(args, callbackContext);
                    } catch (Exception ex) {
                        callbackContext.error(ex.getMessage());
                    }
                }
            });
            return true;
        } else if("delete".equals(action)){
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        delete(args, callbackContext);
                    } catch (Exception ex) {
                        callbackContext.error(ex.getMessage());
                    }
                }
            });
            return true;
        }

        return false;
    }

    private void isAvailable(final CallbackContext callbackContext) {
        // first check that the Google APIs are available
        GoogleApiAvailability gapi = GoogleApiAvailability.getInstance();
        int apiresult = gapi.isGooglePlayServicesAvailable(this.cordova.getActivity());
        if (apiresult == ConnectionResult.SUCCESS) {
            // then check that Google Fit is actually installed
            PackageManager pm = cordova.getActivity().getApplicationContext().getPackageManager();
            try {
                pm.getPackageInfo("com.google.android.apps.fitness", PackageManager.GET_ACTIVITIES);
                // Success return object
                PluginResult result;
                result = new PluginResult(PluginResult.Status.OK, true);
                callbackContext.sendPluginResult(result);
            } catch (PackageManager.NameNotFoundException e) {
                Log.d(TAG, "Google Fit not installed");
            }
        }
        PluginResult result;
        result = new PluginResult(PluginResult.Status.OK, false);
        callbackContext.sendPluginResult(result);
    }

    private void promptInstall(final CallbackContext callbackContext) {
        GoogleApiAvailability gapi = GoogleApiAvailability.getInstance();
        int apiresult = gapi.isGooglePlayServicesAvailable(this.cordova.getActivity());
        if (apiresult != ConnectionResult.SUCCESS) {
            if (gapi.isUserResolvableError(apiresult)) {
                // show the dialog, but no action is performed afterwards
                gapi.showErrorDialogFragment(this.cordova.getActivity(), apiresult, 1000);
            }
        } else {
            // check that Google Fit is actually installed
            PackageManager pm = cordova.getActivity().getApplicationContext().getPackageManager();
            try {
                pm.getPackageInfo("com.google.android.apps.fitness", PackageManager.GET_ACTIVITIES);
            } catch (PackageManager.NameNotFoundException e) {
                //show popup for downloading app
                //code from http://stackoverflow.com/questions/11753000/how-to-open-the-google-play-store-directly-from-my-android-application
                try {
                    cordova.getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.fitness")));
                } catch (android.content.ActivityNotFoundException anfe) {
                    cordova.getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.fitness")));
                }
            }
        }
        callbackContext.success();
    }

    private void checkAuthorization(final JSONArray args, final CallbackContext callbackContext, final boolean autoresolve) throws JSONException {
        this.cordova.setActivityResultCallback(this);
        authReqCallbackCtx = callbackContext;
        authAutoresolve = autoresolve;

        //reset scopes
        boolean bodyscope = false;
        boolean activityscope = false;
        boolean locationscope = false;
        boolean nutritionscope = false;

        for (int i = 0; i < args.length(); i++) {
            String type = args.getString(i);
            if (bodydatatypes.get(type) != null)
                bodyscope = true;
            if (activitydatatypes.get(type) != null)
                activityscope = true;
            if (locationdatatypes.get(type) != null)
                locationscope = true;
            if (nutritiondatatypes.get(type) != null)
                nutritionscope = true;
        }
        dynPerms.clear();
        if (locationscope) dynPerms.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (bodyscope) dynPerms.add(Manifest.permission.BODY_SENSORS);

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this.cordova.getActivity());
        builder.addApi(Fitness.HISTORY_API);
        builder.addApi(Fitness.CONFIG_API);
        builder.addApi(Fitness.SESSIONS_API);
        //scopes: https://developers.google.com/android/reference/com/google/android/gms/common/Scopes.html
        if (bodyscope) builder.addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE));
        if (activityscope) builder.addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE));
        if (locationscope) builder.addScope(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE));
        if (nutritionscope) builder.addScope(new Scope(Scopes.FITNESS_NUTRITION_READ_WRITE));

        builder.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                mClient.unregisterConnectionCallbacks(this);
                Log.i(TAG, "Google Fit connected");
                authReqSuccess();
            }

            @Override
            public void onConnectionSuspended(int i) {
                String message = "";
                if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                    message = "connection lost, network lost";
                } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                    message = "connection lost, service disconnected";
                } else message = "connection lost, code: " + i;
                Log.e(TAG, message);
                authReqCallbackCtx.error(message);
            }
        });

        builder.addOnConnectionFailedListener(
                new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.i(TAG, "Connection to Fit failed, cause: " + result.getErrorMessage());
                        if (!result.hasResolution()) {
                            Log.e(TAG, "Connection failure has no resolution: " + result.getErrorMessage());
                            authReqCallbackCtx.error(result.getErrorMessage());
                            return;
                        } else {
                            if (authAutoresolve) {
                                // The failure has a resolution. Resolve it.
                                // Called typically when the app is not yet authorized, and an
                                // authorization dialog is displayed to the user.
                                try {
                                    Log.i(TAG, "Attempting to resolve failed connection");
                                    result.startResolutionForResult(cordova.getActivity(), REQUEST_OAUTH);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.e(TAG, "Exception while starting resolution activity", e);
                                    authReqCallbackCtx.error(result.getErrorMessage());
                                    return;
                                }
                            } else {
                                // probably not authorized, send false
                                Log.d(TAG, "Connection to Fit failed, probably because of authorization, giving up now");
                                authReqCallbackCtx.sendPluginResult(new PluginResult(PluginResult.Status.OK, false));
                                return;
                            }
                        }
                    }
                }
        );
        mClient = builder.build();
        mClient.connect();
    }


    private boolean lightConnect() {
        this.cordova.setActivityResultCallback(this);

        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this.cordova.getActivity().getApplicationContext());
        builder.addApi(Fitness.HISTORY_API);
        builder.addApi(Fitness.CONFIG_API);
        builder.addApi(Fitness.SESSIONS_API);

        mClient = builder.build();
        mClient.blockingConnect();
        if (mClient.isConnected()) {
            Log.i(TAG, "Google Fit connected (light)");
            return true;
        } else {
            return false;
        }
    }

    private void query(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (!args.getJSONObject(0).has("startDate")) {
            callbackContext.error("Missing argument startDate");
            return;
        }
        long st = args.getJSONObject(0).getLong("startDate");
        if (!args.getJSONObject(0).has("endDate")) {
            callbackContext.error("Missing argument endDate");
            return;
        }
        long et = args.getJSONObject(0).getLong("endDate");
        if (!args.getJSONObject(0).has("dataType")) {
            callbackContext.error("Missing argument dataType");
            return;
        }
        String datatype = args.getJSONObject(0).getString("dataType");
        DataType dt = null;

        if (bodydatatypes.get(datatype) != null)
            dt = bodydatatypes.get(datatype);
        if (activitydatatypes.get(datatype) != null)
            dt = activitydatatypes.get(datatype);
        if (locationdatatypes.get(datatype) != null)
            dt = locationdatatypes.get(datatype);
        if (nutritiondatatypes.get(datatype) != null)
            dt = nutritiondatatypes.get(datatype);
        if (customdatatypes.get(datatype) != null)
            dt = customdatatypes.get(datatype);
        if (dt == null) {
            callbackContext.error("Datatype " + datatype + " not supported");
            return;
        }
        final DataType DT = dt;

        if ((mClient == null) || (!mClient.isConnected())) {
            if (!lightConnect()) {
                callbackContext.error("Cannot connect to Google Fit");
                return;
            }
        }

        DataReadRequest readRequest = null;
        if (DT.equals(DataType.TYPE_STEP_COUNT_DELTA) && args.getJSONObject(0).has("filtered") && args.getJSONObject(0).getBoolean("filtered")) {
            // exceptional case for filtered steps
            DataSource filteredStepsSource = new DataSource.Builder()
                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .setType(DataSource.TYPE_DERIVED)
                    .setStreamName("estimated_steps")
                    .setAppPackageName("com.google.android.gms")
                    .build();

            readRequest = new DataReadRequest.Builder()
                    .setTimeRange(st, et, TimeUnit.MILLISECONDS)
                    .read(filteredStepsSource)
                    .build();
        } else {
            readRequest = new DataReadRequest.Builder()
                    .setTimeRange(st, et, TimeUnit.MILLISECONDS)
                    .read(dt)
                    .build();
        }


        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest).await();

        if (dataReadResult.getStatus().isSuccess()) {
            JSONArray resultset = new JSONArray();
            List<DataSet> datasets = dataReadResult.getDataSets();
            for (DataSet dataset : datasets) {
                for (DataPoint datapoint : dataset.getDataPoints()) {
                    JSONObject obj = new JSONObject();
                    obj.put("startDate", datapoint.getStartTime(TimeUnit.MILLISECONDS));
                    obj.put("endDate", datapoint.getEndTime(TimeUnit.MILLISECONDS));
                    DataSource dataSource = datapoint.getOriginalDataSource();
                    if (dataSource != null) {
                        String sourceName = dataSource.getName();
                        if (sourceName != null) obj.put("sourceName", sourceName);
                        String sourceBundleId = dataSource.getAppPackageName();
                        if (sourceBundleId != null) obj.put("sourceBundleId", sourceBundleId);
                    }

                    //reference for fields: https://developers.google.com/android/reference/com/google/android/gms/fitness/data/Field.html
                    if (DT.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
                        int steps = datapoint.getValue(Field.FIELD_STEPS).asInt();
                        obj.put("value", steps);
                        obj.put("unit", "count");
                    } else if (DT.equals(DataType.TYPE_DISTANCE_DELTA)) {
                        float distance = datapoint.getValue(Field.FIELD_DISTANCE).asFloat();
                        obj.put("value", distance);
                        obj.put("unit", "m");
                    } else if (DT.equals(DataType.TYPE_HYDRATION)) {
                        float distance = datapoint.getValue(Field.FIELD_VOLUME).asFloat();
                        obj.put("value", distance);
                        obj.put("unit", "ml");// documentation says it's litres, but from experiments I get ml
                    } else if (DT.equals(DataType.TYPE_NUTRITION)) {
                        if (datatype.equalsIgnoreCase("nutrition")) {
                            JSONObject dob = new JSONObject();
                            if (datapoint.getValue(Field.FIELD_FOOD_ITEM) != null) {
                                dob.put("item", datapoint.getValue(Field.FIELD_FOOD_ITEM).asString());
                            }
                            if (datapoint.getValue(Field.FIELD_MEAL_TYPE) != null) {
                                int mealt = datapoint.getValue(Field.FIELD_MEAL_TYPE).asInt();
                                if (mealt == Field.MEAL_TYPE_BREAKFAST)
                                    dob.put("meal_type", "breakfast");
                                else if (mealt == Field.MEAL_TYPE_DINNER)
                                    dob.put("meal_type", "dinner");
                                else if (mealt == Field.MEAL_TYPE_LUNCH)
                                    dob.put("meal_type", "lunch");
                                else if (mealt == Field.MEAL_TYPE_SNACK)
                                    dob.put("meal_type", "snack");
                                else dob.put("meal_type", "unknown");
                            }
                            if (datapoint.getValue(Field.FIELD_NUTRIENTS) != null) {
                                Value v = datapoint.getValue(Field.FIELD_NUTRIENTS);
                                dob.put("nutrients", getNutrients(v, null));
                            }
                            obj.put("value", dob);
                            obj.put("unit", "nutrition");
                        } else {
                            Value nutrients = datapoint.getValue(Field.FIELD_NUTRIENTS);
                            NutrientFieldInfo fieldInfo = nutrientFields.get(datatype);
                            if (fieldInfo != null) {
                                obj.put("value", (float) nutrients.getKeyValue(fieldInfo.field));
                                obj.put("unit", fieldInfo.unit);
                            }
                        }
                    } else if (DT.equals(DataType.TYPE_CALORIES_EXPENDED)) {
                        float calories = datapoint.getValue(Field.FIELD_CALORIES).asFloat();
                        obj.put("value", calories);
                        obj.put("unit", "kcal");
                    } else if (DT.equals(DataType.TYPE_BASAL_METABOLIC_RATE)) {
                        float calories = datapoint.getValue(Field.FIELD_CALORIES).asFloat();
                        obj.put("value", calories);
                        obj.put("unit", "kcal");
                    } else if (DT.equals(DataType.TYPE_HEIGHT)) {
                        float height = datapoint.getValue(Field.FIELD_HEIGHT).asFloat();
                        obj.put("value", height);
                        obj.put("unit", "m");
                    } else if (DT.equals(DataType.TYPE_WEIGHT)) {
                        float weight = datapoint.getValue(Field.FIELD_WEIGHT).asFloat();
                        obj.put("value", weight);
                        obj.put("unit", "kg");
                    } else if (DT.equals(DataType.TYPE_HEART_RATE_BPM)) {
                        float weight = datapoint.getValue(Field.FIELD_BPM).asFloat();
                        obj.put("value", weight);
                        obj.put("unit", "bpm");
                    } else if (DT.equals(DataType.TYPE_BODY_FAT_PERCENTAGE)) {
                        float weight = datapoint.getValue(Field.FIELD_PERCENTAGE).asFloat();
                        obj.put("value", weight);
                        obj.put("unit", "percent");
                    } else if (DT.equals(DataType.TYPE_ACTIVITY_SEGMENT)) {
                        String activity = datapoint.getValue(Field.FIELD_ACTIVITY).asActivity();
                        obj.put("value", activity);
                        obj.put("unit", "activityType");
                    } else if (DT.equals(customdatatypes.get("gender"))) {
                        for (Field f : customdatatypes.get("gender").getFields()) {
                            //there should be only one field named gender
                            String gender = datapoint.getValue(f).asString();
                            obj.put("value", gender);
                        }
                    } else if (DT.equals(customdatatypes.get("date_of_birth"))) {
                        JSONObject dob = new JSONObject();
                        for (Field f : customdatatypes.get("date_of_birth").getFields()) {
                            //all fields are integers
                            int fieldvalue = datapoint.getValue(f).asInt();
                            dob.put(f.getName(), fieldvalue);
                        }
                        obj.put("value", dob);
                    }

                    resultset.put(obj);
                }
            }
            callbackContext.success(resultset);
        } else {
            callbackContext.error(dataReadResult.getStatus().getStatusMessage());
        }
    }

    private JSONObject getNutrients(Value nutrientsMap, JSONObject mergewith) throws JSONException {
        JSONObject nutrients;
        if (mergewith != null) {
            nutrients = mergewith;
        } else {
            nutrients = new JSONObject();
        }
        mergeNutrient(Field.NUTRIENT_CALORIES, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_TOTAL_FAT, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_SATURATED_FAT, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_UNSATURATED_FAT, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_POLYUNSATURATED_FAT, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_MONOUNSATURATED_FAT, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_TRANS_FAT, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_CHOLESTEROL, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_SODIUM, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_POTASSIUM, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_TOTAL_CARBS, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_DIETARY_FIBER, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_SUGAR, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_PROTEIN, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_VITAMIN_A, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_VITAMIN_C, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_CALCIUM, nutrientsMap, nutrients);
        mergeNutrient(Field.NUTRIENT_IRON, nutrientsMap, nutrients);

        return nutrients;
    }

    private void mergeNutrient(String f, Value nutrientsMap, JSONObject nutrients) throws JSONException {
        if (nutrientsMap.getKeyValue(f) != null) {
            String n = null;
            for (String name : nutrientFields.keySet()) {
                if (nutrientFields.get(name).field.equalsIgnoreCase(f)) {
                    n = name;
                    break;
                }
            }
            if (n != null) {
                float val = nutrientsMap.getKeyValue(f);
                if (nutrients.has(n)) {
                    val += nutrients.getDouble(n);
                }
                nutrients.put(n, val);
            }
        }
    }

    private void queryAggregated(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (!args.getJSONObject(0).has("startDate")) {
            callbackContext.error("Missing argument startDate");
            return;
        }
        long st = args.getJSONObject(0).getLong("startDate");
        if (!args.getJSONObject(0).has("endDate")) {
            callbackContext.error("Missing argument endDate");
            return;
        }
        long et = args.getJSONObject(0).getLong("endDate");
        long _et = et; // keep track of the original end time, needed for basal calories
        if (!args.getJSONObject(0).has("dataType")) {
            callbackContext.error("Missing argument dataType");
            return;
        }
        String datatype = args.getJSONObject(0).getString("dataType");

        boolean hasbucket = args.getJSONObject(0).has("bucket");
        boolean customBuckets = false;
        String bucketType = "";
        if (hasbucket) {
            bucketType = args.getJSONObject(0).getString("bucket");
            if (!bucketType.equalsIgnoreCase("hour") && !bucketType.equalsIgnoreCase("day")) {
                customBuckets = true;
                if (!bucketType.equalsIgnoreCase("week") && !bucketType.equalsIgnoreCase("month") && !bucketType.equalsIgnoreCase("year")) {
                    // error
                    callbackContext.error("Bucket type " + bucketType + " not recognised");
                    return;
                }
            }
            // Google fit bucketing is different and start and end must be quantised
            Calendar c = Calendar.getInstance();

            c.setTimeInMillis(st);
            c.clear(Calendar.MINUTE);
            c.clear(Calendar.SECOND);
            c.clear(Calendar.MILLISECOND);
            if (!bucketType.equalsIgnoreCase("hour")) {
                c.set(Calendar.HOUR_OF_DAY, 0);
                if (bucketType.equalsIgnoreCase("week")) {
                    c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek());
                } else if (bucketType.equalsIgnoreCase("month")) {
                    c.set(Calendar.DAY_OF_MONTH, 1);
                } else if (bucketType.equalsIgnoreCase("year")) {
                    c.set(Calendar.DAY_OF_YEAR, 1);
                }
            }
            st = c.getTimeInMillis();

            c.setTimeInMillis(et);
            c.clear(Calendar.MINUTE);
            c.clear(Calendar.SECOND);
            c.clear(Calendar.MILLISECOND);
            if (bucketType.equalsIgnoreCase("hour")) {
                c.add(Calendar.HOUR_OF_DAY, 1);
            } else {
                c.set(Calendar.HOUR_OF_DAY, 0);
                if (bucketType.equalsIgnoreCase("day")) {
                    c.add(Calendar.DAY_OF_YEAR, 1);
                } else if (bucketType.equalsIgnoreCase("week")) {
                    c.add(Calendar.DAY_OF_YEAR, 7);
                } else if (bucketType.equalsIgnoreCase("month")) {
                    c.add(Calendar.MONTH, 1);
                } else if (bucketType.equalsIgnoreCase("year")) {
                    c.add(Calendar.YEAR, 1);
                }
            }
            et = c.getTimeInMillis();
        }

        if ((mClient == null) || (!mClient.isConnected())) {
            if (!lightConnect()) {
                callbackContext.error("Cannot connect to Google Fit");
                return;
            }
        }

        // basal metabolic rate is treated in a different way
        // we need to query per day and not all days may have a sample
        // so we query over a week then we take the average
        float basalAVG = 0;
        if (datatype.equalsIgnoreCase("calories.basal")) {
            try {
                basalAVG = getBasalAVG(_et);
            } catch (Exception ex) {
                callbackContext.error(ex.getMessage());
                return;
            }
        }


        DataReadRequest.Builder builder = new DataReadRequest.Builder();
        builder.setTimeRange(st, et, TimeUnit.MILLISECONDS);
        int allms = (int) (et - st);

        if (datatype.equalsIgnoreCase("steps")) {
            if (args.getJSONObject(0).has("filtered") && args.getJSONObject(0).getBoolean("filtered")) {
                // exceptional case for filtered steps
                DataSource filteredStepsSource = new DataSource.Builder()
                        .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .setType(DataSource.TYPE_DERIVED)
                        .setStreamName("estimated_steps")
                        .setAppPackageName("com.google.android.gms")
                        .build();
                builder.aggregate(filteredStepsSource, DataType.AGGREGATE_STEP_COUNT_DELTA);
            } else {
                builder.aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA);
            }
        } else if (datatype.equalsIgnoreCase("distance")) {
            builder.aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA);
        } else if (datatype.equalsIgnoreCase("calories")) {
            builder.aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED);
        } else if (datatype.equalsIgnoreCase("calories.basal")) {
            builder.aggregate(DataType.TYPE_BASAL_METABOLIC_RATE, DataType.AGGREGATE_BASAL_METABOLIC_RATE_SUMMARY);
        } else if (datatype.equalsIgnoreCase("activity")) {
            if(hasbucket) {
                builder.aggregate(DataType.TYPE_ACTIVITY_SEGMENT, DataType.AGGREGATE_ACTIVITY_SUMMARY);
            }  else {
                builder.aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED);
                //here we could also get the distance: builder.aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA);
            }
        } else if (datatype.equalsIgnoreCase("nutrition.water")) {
            builder.aggregate(DataType.TYPE_HYDRATION, DataType.AGGREGATE_HYDRATION);
        } else if (nutritiondatatypes.get(datatype) != null) {
            builder.aggregate(DataType.TYPE_NUTRITION, DataType.AGGREGATE_NUTRITION_SUMMARY);
        } else {
            callbackContext.error("Datatype " + datatype + " not supported");
            return;
        }

        if (hasbucket) {
            if (bucketType.equalsIgnoreCase("hour")) {
                builder.bucketByTime(1, TimeUnit.HOURS);
            } else if (bucketType.equalsIgnoreCase("day")) {
                builder.bucketByTime(1, TimeUnit.DAYS);
            } else {
                // use days, then will need to aggregate manually
                builder.bucketByTime(1, TimeUnit.DAYS);
            }
        } else {
            if (datatype.equalsIgnoreCase("activity")) {
                builder.bucketByActivityType(1, TimeUnit.MILLISECONDS);
            } else {
                builder.bucketByTime(allms, TimeUnit.MILLISECONDS);
            }
        }

        DataReadRequest readRequest = builder.build();
        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest).await();

        if (dataReadResult.getStatus().isSuccess()) {
            JSONObject retBucket = null;
            JSONArray retBucketsArr = new JSONArray();
            if (hasbucket) {
                if (customBuckets) {
                    // create custom buckets, as these are not supported by Google Fit
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(st);
                    while (cal.getTimeInMillis() < et) {
                        JSONObject customBuck = new JSONObject();
                        customBuck.put("startDate", cal.getTimeInMillis());
                        if (bucketType.equalsIgnoreCase("week")) {
                            cal.add(Calendar.DAY_OF_YEAR, 7);
                        } else if (bucketType.equalsIgnoreCase("month")) {
                            cal.add(Calendar.MONTH, 1);
                        } else {
                            cal.add(Calendar.YEAR, 1);
                        }
                        customBuck.put("endDate", cal.getTimeInMillis());
                        retBucketsArr.put(customBuck);
                    }
                }
            } else {
                //there will be only one bucket spanning all the period
                retBucket = new JSONObject();
                retBucket.put("startDate", st);
                retBucket.put("endDate", et);
                retBucket.put("value", 0);
                if (datatype.equalsIgnoreCase("steps")) {
                    retBucket.put("unit", "count");
                } else if (datatype.equalsIgnoreCase("distance")) {
                    retBucket.put("unit", "m");
                } else if (datatype.equalsIgnoreCase("calories")) {
                    retBucket.put("unit", "kcal");
                } else if (datatype.equalsIgnoreCase("activity")) {
                    retBucket.put("value", new JSONObject());
                    retBucket.put("unit", "activitySummary");
                } else if (datatype.equalsIgnoreCase("nutrition.water")) {
                    retBucket.put("unit", "ml");
                } else if (datatype.equalsIgnoreCase("nutrition")) {
                    retBucket.put("value", new JSONObject());
                    retBucket.put("unit", "nutrition");
                } else if (nutritiondatatypes.get(datatype) != null) {
                    retBucket.put("unit", nutrientFields.get(datatype).unit);
                }
            }

            for (Bucket bucket : dataReadResult.getBuckets()) {

                // special case of the activity without time buckets
                // here the buckets contain activities and the datapoints contain calories
                if(datatype.equalsIgnoreCase("activity") && !hasbucket){
                    String activity = bucket.getActivity();
                    float calories = 0;
                    int duration = (int) (bucket.getEndTime(TimeUnit.MILLISECONDS) - bucket.getStartTime(TimeUnit.MILLISECONDS));
                    for (DataSet dataset : bucket.getDataSets()) {
                        for (DataPoint datapoint : dataset.getDataPoints()) {
                            calories += datapoint.getValue(Field.FIELD_CALORIES).asFloat();
                        }
                    }
                    JSONObject actobj = retBucket.getJSONObject("value");
                    JSONObject summary = new JSONObject();
                    summary.put("duration", duration);
                    summary.put("calories", calories);
                    actobj.put(activity, summary);
                    retBucket.put("value", actobj);
                    // jump to the next iteration
                    continue;
                }

                if (hasbucket) {
                    if (customBuckets) {
                        //find the bucket among customs
                        for (int i = 0; i < retBucketsArr.length(); i++) {
                            retBucket = retBucketsArr.getJSONObject(i);
                            long bst = retBucket.getLong("startDate");
                            long bet = retBucket.getLong("endDate");
                            if (bucket.getStartTime(TimeUnit.MILLISECONDS) >= bst
                                    && bucket.getEndTime(TimeUnit.MILLISECONDS) <= bet) {
                                break;
                            }
                        }
                    } else {
                        //pick the current
                        retBucket = new JSONObject();
                        retBucket.put("startDate", bucket.getStartTime(TimeUnit.MILLISECONDS));
                        retBucket.put("endDate", bucket.getEndTime(TimeUnit.MILLISECONDS));
                        retBucketsArr.put(retBucket);
                    }
                    if (!retBucket.has("value")) {
                        retBucket.put("value", 0);
                        if (datatype.equalsIgnoreCase("steps")) {
                            retBucket.put("unit", "count");
                        } else if (datatype.equalsIgnoreCase("distance")) {
                            retBucket.put("unit", "m");
                        } else if (datatype.equalsIgnoreCase("calories")) {
                            retBucket.put("unit", "kcal");
                        } else if (datatype.equalsIgnoreCase("activity")) {
                            retBucket.put("value", new JSONObject());
                            retBucket.put("unit", "activitySummary");
                        } else if (datatype.equalsIgnoreCase("nutrition.water")) {
                            retBucket.put("unit", "ml");
                        } else if (datatype.equalsIgnoreCase("nutrition")) {
                            retBucket.put("value", new JSONObject());
                            retBucket.put("unit", "nutrition");
                        } else if (nutritiondatatypes.get(datatype) != null) {
                            NutrientFieldInfo fieldInfo = nutrientFields.get(datatype);
                            if (fieldInfo != null) {
                                retBucket.put("unit", fieldInfo.unit);
                            }
                        }
                    }
                }

                // aggregate data points over the bucket
                boolean atleastone = false;
                for (DataSet dataset : bucket.getDataSets()) {
                    for (DataPoint datapoint : dataset.getDataPoints()) {
                        atleastone = true;
                        if (datatype.equalsIgnoreCase("steps")) {
                            int nsteps = datapoint.getValue(Field.FIELD_STEPS).asInt();
                            int osteps = retBucket.getInt("value");
                            retBucket.put("value", osteps + nsteps);
                        } else if (datatype.equalsIgnoreCase("distance")) {
                            float ndist = datapoint.getValue(Field.FIELD_DISTANCE).asFloat();
                            double odist = retBucket.getDouble("value");
                            retBucket.put("value", odist + ndist);
                        } else if (datatype.equalsIgnoreCase("calories")) {
                            float ncal = datapoint.getValue(Field.FIELD_CALORIES).asFloat();
                            double ocal = retBucket.getDouble("value");
                            retBucket.put("value", ocal + ncal);
                        } else if (datatype.equalsIgnoreCase("calories.basal")) {
                            float ncal = datapoint.getValue(Field.FIELD_AVERAGE).asFloat();
                            double ocal = retBucket.getDouble("value");
                            retBucket.put("value", ocal + ncal);
                        } else if (datatype.equalsIgnoreCase("nutrition.water")) {
                            float nwat = datapoint.getValue(Field.FIELD_VOLUME).asFloat();
                            double owat = retBucket.getDouble("value");
                            retBucket.put("value", owat + nwat);
                        } else if (datatype.equalsIgnoreCase("nutrition")) {
                            JSONObject nutrsob = retBucket.getJSONObject("value");
                            if (datapoint.getValue(Field.FIELD_NUTRIENTS) != null) {
                                nutrsob = getNutrients(datapoint.getValue(Field.FIELD_NUTRIENTS), nutrsob);
                            }
                            retBucket.put("value", nutrsob);
                        } else if (nutritiondatatypes.get(datatype) != null) {
                            Value nutrients = datapoint.getValue(Field.FIELD_NUTRIENTS);
                            NutrientFieldInfo fieldInfo = nutrientFields.get(datatype);
                            if (fieldInfo != null) {
                                float value = nutrients.getKeyValue(fieldInfo.field);
                                double total = retBucket.getDouble("value");
                                retBucket.put("value", total + value);
                            }
                        } else if (datatype.equalsIgnoreCase("activity")) {
                            String activity = datapoint.getValue(Field.FIELD_ACTIVITY).asActivity();
                            int ndur = datapoint.getValue(Field.FIELD_DURATION).asInt();
                            JSONObject actobj = retBucket.getJSONObject("value");
                            JSONObject summary;
                            if (actobj.has(activity)) {
                                summary = actobj.getJSONObject(activity);
                                int odur = summary.getInt("duration");
                                summary.put("duration", odur + ndur);
                            } else {
                                summary = new JSONObject();
                                summary.put("duration", ndur);
                            }
                            actobj.put(activity, summary);
                            retBucket.put("value", actobj);
                        }
                    }
                } //end of data set loop
                if (datatype.equalsIgnoreCase("calories.basal")) {
                    double basals = retBucket.getDouble("value");
                    if (!atleastone) {
                        //when no basal is available, use the daily average
                        basals += basalAVG;
                        retBucket.put("value", basals);
                    }
                    // if the bucket is not daily, it needs to be normalised
                    if (!hasbucket || bucketType.equalsIgnoreCase("hour")) {
                        long sst = retBucket.getLong("startDate");
                        long eet = retBucket.getLong("endDate");
                        basals = (basals / (24 * 60 * 60 * 1000)) * (eet - sst);
                        retBucket.put("value", basals);
                    }
                }
            } // end of buckets loop
            if (hasbucket) callbackContext.success(retBucketsArr);
            else callbackContext.success(retBucket);
        } else {
            callbackContext.error(dataReadResult.getStatus().getStatusMessage());
        }
    }

    private float getBasalAVG(long _et) throws Exception {
        float basalAVG = 0;
        Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(new Date(_et));
        //set start time to a week before end time
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long nst = cal.getTimeInMillis();

        DataReadRequest.Builder builder = new DataReadRequest.Builder();
        builder.aggregate(DataType.TYPE_BASAL_METABOLIC_RATE, DataType.AGGREGATE_BASAL_METABOLIC_RATE_SUMMARY);
        builder.bucketByTime(1, TimeUnit.DAYS);
        builder.setTimeRange(nst, _et, TimeUnit.MILLISECONDS);
        DataReadRequest readRequest = builder.build();

        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mClient, readRequest).await();

        if (dataReadResult.getStatus().isSuccess()) {
            JSONObject obj = new JSONObject();
            int avgsN = 0;
            for (Bucket bucket : dataReadResult.getBuckets()) {
                // in the com.google.bmr.summary data type, each data point represents
                // the average, maximum and minimum basal metabolic rate, in kcal per day, over the time interval of the data point.
                DataSet ds = bucket.getDataSet(DataType.AGGREGATE_BASAL_METABOLIC_RATE_SUMMARY);
                for (DataPoint dp : ds.getDataPoints()) {
                    float avg = dp.getValue(Field.FIELD_AVERAGE).asFloat();
                    basalAVG += avg;
                    avgsN++;
                }
            }
            // do the average of the averages
            if (avgsN != 0) basalAVG /= avgsN; // this a daily average
            return basalAVG;
        } else throw new Exception(dataReadResult.getStatus().getStatusMessage());
    }


    private void store(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (!args.getJSONObject(0).has("startDate")) {
            callbackContext.error("Missing argument startDate");
            return;
        }
        long st = args.getJSONObject(0).getLong("startDate");
        if (!args.getJSONObject(0).has("endDate")) {
            callbackContext.error("Missing argument endDate");
            return;
        }
        long et = args.getJSONObject(0).getLong("endDate");
        if (!args.getJSONObject(0).has("dataType")) {
            callbackContext.error("Missing argument dataType");
            return;
        }
        String datatype = args.getJSONObject(0).getString("dataType");
        if (!args.getJSONObject(0).has("value")) {
            callbackContext.error("Missing argument value");
            return;
        }
        if (!args.getJSONObject(0).has("sourceName")) {
            callbackContext.error("Missing argument sourceName");
            return;
        }
        String sourceName = args.getJSONObject(0).getString("sourceName");

        String sourceBundleId = cordova.getActivity().getApplicationContext().getPackageName();
        if (args.getJSONObject(0).has("sourceBundleId")) {
            sourceBundleId = args.getJSONObject(0).getString("sourceBundleId");
        }

        DataType dt = null;
        if (bodydatatypes.get(datatype) != null)
            dt = bodydatatypes.get(datatype);
        if (activitydatatypes.get(datatype) != null)
            dt = activitydatatypes.get(datatype);
        if (locationdatatypes.get(datatype) != null)
            dt = locationdatatypes.get(datatype);
        if (nutritiondatatypes.get(datatype) != null)
            dt = nutritiondatatypes.get(datatype);
        if (customdatatypes.get(datatype) != null)
            dt = customdatatypes.get(datatype);
        if (dt == null) {
            callbackContext.error("Datatype " + datatype + " not supported");
            return;
        }

        if ((mClient == null) || (!mClient.isConnected())) {
            if (!lightConnect()) {
                callbackContext.error("Cannot connect to Google Fit");
                return;
            }
        }

        DataSource datasrc = new DataSource.Builder()
                .setAppPackageName(sourceBundleId)
                .setName(sourceName)
                .setDataType(dt)
                .setType(DataSource.TYPE_RAW)
                .build();

        DataSet dataSet = DataSet.create(datasrc);
        DataPoint datapoint = DataPoint.create(datasrc);
        datapoint.setTimeInterval(st, et, TimeUnit.MILLISECONDS);
        if (dt.equals(DataType.TYPE_STEP_COUNT_DELTA)) {
            String value = args.getJSONObject(0).getString("value");
            int steps = Integer.parseInt(value);
            datapoint.getValue(Field.FIELD_STEPS).setInt(steps);
        } else if (dt.equals(DataType.TYPE_DISTANCE_DELTA)) {
            String value = args.getJSONObject(0).getString("value");
            float dist = Float.parseFloat(value);
            datapoint.getValue(Field.FIELD_DISTANCE).setFloat(dist);
        } else if (dt.equals(DataType.TYPE_CALORIES_EXPENDED)) {
            String value = args.getJSONObject(0).getString("value");
            float cals = Float.parseFloat(value);
            datapoint.getValue(Field.FIELD_CALORIES).setFloat(cals);
        } else if (dt.equals(DataType.TYPE_HEIGHT)) {
            String value = args.getJSONObject(0).getString("value");
            float height = Float.parseFloat(value);
            datapoint.getValue(Field.FIELD_HEIGHT).setFloat(height);
        } else if (dt.equals(DataType.TYPE_WEIGHT)) {
            String value = args.getJSONObject(0).getString("value");
            float weight = Float.parseFloat(value);
            datapoint.getValue(Field.FIELD_WEIGHT).setFloat(weight);
        } else if (dt.equals(DataType.TYPE_HEART_RATE_BPM)) {
            String value = args.getJSONObject(0).getString("value");
            float hr = Float.parseFloat(value);
            datapoint.getValue(Field.FIELD_BPM).setFloat(hr);
        } else if (dt.equals(DataType.TYPE_BODY_FAT_PERCENTAGE)) {
            String value = args.getJSONObject(0).getString("value");
            float perc = Float.parseFloat(value);
            datapoint.getValue(Field.FIELD_PERCENTAGE).setFloat(perc);
        } else if (dt.equals(DataType.TYPE_ACTIVITY_SEGMENT)) {
            String value = args.getJSONObject(0).getString("value");
            datapoint.getValue(Field.FIELD_ACTIVITY).setActivity(value);
        } else if (dt.equals(customdatatypes.get("gender"))) {
            String value = args.getJSONObject(0).getString("value");
            for (Field f : customdatatypes.get("gender").getFields()) {
                //we expect only one field named gender
                datapoint.getValue(f).setString(value);
            }
        } else if (dt.equals(customdatatypes.get("date_of_birth"))) {
            JSONObject dob = args.getJSONObject(0).getJSONObject("value");
            int year = dob.getInt("year");
            int month = dob.getInt("month");
            int day = dob.getInt("day");

            for (Field f : customdatatypes.get("date_of_birth").getFields()) {
                if (f.getName().equalsIgnoreCase("day"))
                    datapoint.getValue(f).setInt(day);
                if (f.getName().equalsIgnoreCase("month"))
                    datapoint.getValue(f).setInt(month);
                if (f.getName().equalsIgnoreCase("year"))
                    datapoint.getValue(f).setInt(year);
            }
        }
        dataSet.add(datapoint);


        Status insertStatus = Fitness.HistoryApi.insertData(mClient, dataSet)
                .await(1, TimeUnit.MINUTES);

        if (!insertStatus.isSuccess()) {
            callbackContext.error(insertStatus.getStatusMessage());
        } else {
            callbackContext.success();
        }
    }

    private void delete(final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (!args.getJSONObject(0).has("startDate")) {
            callbackContext.error("Missing argument startDate");
            return;
        }
        final long st = args.getJSONObject(0).getLong("startDate");
        if (!args.getJSONObject(0).has("endDate")) {
            callbackContext.error("Missing argument endDate");
            return;
        }
        final long et = args.getJSONObject(0).getLong("endDate");
        if (!args.getJSONObject(0).has("dataType")) {
            callbackContext.error("Missing argument dataType");
            return;
        }
        final String datatype = args.getJSONObject(0).getString("dataType");

        DataType dt = null;
        if (bodydatatypes.get(datatype) != null)
            dt = bodydatatypes.get(datatype);
        if (activitydatatypes.get(datatype) != null)
            dt = activitydatatypes.get(datatype);
        if (locationdatatypes.get(datatype) != null)
            dt = locationdatatypes.get(datatype);
        if (nutritiondatatypes.get(datatype) != null)
            dt = nutritiondatatypes.get(datatype);
        if (customdatatypes.get(datatype) != null)
            dt = customdatatypes.get(datatype);
        if (dt == null) {
            callbackContext.error("Datatype " + datatype + " not supported");
            return;
        }

        if ((mClient == null) || (!mClient.isConnected())) {
            if (!lightConnect()) {
                callbackContext.error("Cannot connect to Google Fit");
                return;
            }
        }

        DataDeleteRequest request = new DataDeleteRequest.Builder()
                .setTimeInterval(st, et, TimeUnit.MILLISECONDS)
                .addDataType(dt)
                .build();

        Fitness.HistoryApi.deleteData(mClient, request)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            callbackContext.success();
                        } else {
                            Log.e(TAG, "Cannot delete samples of " + datatype + ", status code "
                                    + status.getStatusCode() + ", message " + status.getStatusMessage());
                            callbackContext.error(status.getStatusMessage());
                        }
                    }
                });
    }
}

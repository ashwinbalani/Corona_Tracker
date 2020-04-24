package com.fitech.coronatracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fitech.coronatracker.web.App;
import com.fitech.coronatracker.web.Constants;
import com.fitech.coronatracker.web.CustomRequest;
import com.fitech.coronatracker.web.VolleyMultipartRequest;
import com.github.clans.fab.FloatingActionButton;
import com.thebrownarrow.permissionhelper.ActivityManagePermission;
import com.thebrownarrow.permissionhelper.PermissionResult;
import com.thebrownarrow.permissionhelper.PermissionUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import me.echodev.resizer.Resizer;

public class MainActivity extends ActivityManagePermission {

    private static final String TAG = "MainActivity";
    public String[] permissionsList = new String[]{PermissionUtils.Manifest_ACCESS_COARSE_LOCATION, PermissionUtils.Manifest_ACCESS_FINE_LOCATION, PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_EXTERNAL_STORAGE, PermissionUtils.Manifest_READ_PHONE_STATE};

    public MainActivity context = null;
    public ListView lstNewsItems;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap imageBitmap;
    private boolean imageCaptured = false;
    private String currentPhotoPath;
    private Location currentLocation;
    private String currentLocationStr;

    private ProgressBar prgMain;
    private OneReportDisplayListAdapter reportAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);

        context = this;

        lstNewsItems = (ListView)findViewById(R.id.lstNearbyReports);
        reportAdapter = new OneReportDisplayListAdapter(context,context,R.id.itemImg,new ArrayList<OneReport>());
        lstNewsItems.setAdapter(reportAdapter);
        prgMain = (ProgressBar)findViewById(R.id.prgMain);
        prgMain.setVisibility(View.GONE);

        boolean isPermissionGranted = isPermissionsGranted(MainActivity.this, permissionsList);
        if (isPermissionGranted == false) {
            AlertDialog.Builder abuild = new AlertDialog.Builder(context);
            abuild.setTitle("Please allow permissions");
            abuild.setMessage("We require the following permissions\n\nLocation Permission: To add location tagging on your captured photos\n\nFile Read/Write permission: To store captured images on your phone, and read them when needed\n\nManage Calls permission: To get your device IMEI number in order to identify each user\n\nWe will use these permissions only for the above mentioned reasons\n\nThank you for saving humanity");
            abuild.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    askForPermissions();
                }
            });
            abuild.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            abuild.create().show();
        } else {

            final TextView lblMainDetails = (TextView) findViewById(R.id.lblMainDetails);
            final FloatingActionButton fabAdd = (FloatingActionButton) findViewById(R.id.fabAddNewImage);
            final FloatingActionButton fabRefresh = (FloatingActionButton) findViewById(R.id.fabRefresh);

            fabAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    makeNewPost();
                }
            });
            fabRefresh.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    findLocation(true);
                }
            });
            findLocation(false);
            //makeNewPost();
        }
    }

    public void makeNewPost() {
        takePhoto();
        AsyncTask<String, String, Void> asyncTask = new AsyncTask<String, String, Void>() {
            String address;
            String city;
            String state;
            String country;
            String postalCode;
            String knownName;

            ProgressDialog pd;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                currentLocation = null;
                findLocation(false);
                pd = ProgressDialog.show(context,"Please wait","Wait while fetching location\nPlease turn ON location and data services if they are turned OFF");
            }

            @Override
            protected Void doInBackground(String... strings) {
                try {
                    while (currentLocation == null) {
                        Thread.sleep(5000);
                    }

                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(context, Locale.getDefault());
                    addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    city = addresses.get(0).getLocality();
                    state = addresses.get(0).getAdminArea();
                    country = addresses.get(0).getCountryName();
                    postalCode = addresses.get(0).getPostalCode();
                    knownName = addresses.get(0).getFeatureName();
                } catch (Exception ex) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pd.dismiss();

                if(imageCaptured == false)
                    return;

                final Dialog dlgMain = new Dialog(context);
                dlgMain.setTitle("Review Report");
                dlgMain.setContentView(R.layout.post_dialog);
                ((ImageView) dlgMain.findViewById(R.id.imgCapture)).setImageBitmap(imageBitmap);

                String locationStr = "";
                if (address != null)
                    locationStr = locationStr + address;
                else if (city != null)
                    locationStr = locationStr + "," + city;
                else if (postalCode != null)
                    locationStr = locationStr + "," + postalCode;

                currentLocationStr = locationStr;
                ((TextView) dlgMain.findViewById(R.id.lblLocation)).setText(locationStr);
                final CheckBox chkTerms = (CheckBox) dlgMain.findViewById(R.id.chkConditions);
                chkTerms.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        if (b) {
                            AlertDialog.Builder abuild = new AlertDialog.Builder(context);
                            abuild.setTitle("Terms of reporting");
                            abuild.setMessage("1. Your report is solely your responsiblity\n2. This report might/might not go to the concerned authorities\n3. Your post doesn't contain any copyrighted material, nudity, etc. and only consist of people breaking curfew laws\n4. If you voilate point 3, then a legal action might be taken on you\n5. Corona Reporter is in no way connected to you, and has not endorsed this post");
                            abuild.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                            abuild.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                    chkTerms.setChecked(false);
                                }
                            });
                            abuild.setCancelable(false);
                            abuild.create().show();
                        }
                    }
                });
                final Button btnPost = (Button) dlgMain.findViewById(R.id.btnPost);
                btnPost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (chkTerms.isChecked()) {
                            DisplayHelper.showSnackbar(context, "Reporting! Please wait.");
                            VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, Constants.SERVER_OPERATIONS,
                                    new Response.Listener<NetworkResponse>() {
                                        @Override
                                        public void onResponse(NetworkResponse response) {
                                            String resp = new String(response.data);
                                            try {
                                                JSONObject obj = new JSONObject(new String(resp));
                                                if (!obj.getBoolean("error")) {
                                                    DisplayHelper.showSnackbar(context, obj.getString("message"));
                                                    int postId = Integer.parseInt(obj.getString("postId"));
                                                    findLocation(true);
                                                }
                                            } catch (JSONException e) {
                                                DisplayHelper.showSnackbar(context, e.getMessage());
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                            Log.e("GotError", "" + error.getMessage());
                                        }
                                    }) {


                                @Override
                                protected Map<String, DataPart> getByteData() {
                                    Map<String, DataPart> params = new HashMap<>();
                                    long imagename = System.currentTimeMillis();
                                    params.put("image", new DataPart(imagename + ".png", getFileDataFromDrawable(imageBitmap)));

                                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                                        return null;
                                    }
                                    String imei = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                                    String date = ((new Date()).getTime()) + "";

                                    try {
                                        String path = Environment.getExternalStorageDirectory() + "/info.txt";
                                        BufferedWriter bw = new BufferedWriter(new FileWriter(path));
                                        bw.write(imei + ";" + date + ";" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + ";" + currentLocationStr);
                                        bw.flush();
                                        bw.close();
                                        params.put("report_details",new DataPart("info.txt",getFileData(new File(path))));
                                    } catch (Exception ex) {

                                    }
                                    return params;
                                }
                            };
                            App.getInstance().addToRequestQueue(volleyMultipartRequest);
                            reportAdapter.add(new OneReport(-1,currentPhotoPath,currentLocationStr,"Just Now","0"));

                        } else {
                            DisplayHelper.showSnackbar(context,"Post Cancelled");
                        }
                        dlgMain.cancel();
                    }
                });
                dlgMain.show();
            }
        };
        asyncTask.execute("");
    }

    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public byte[] getFileData(File file) {
        try {
            byte[] bytesArray = new byte[(int) file.length()];

            FileInputStream fis = new FileInputStream(file);
            fis.read(bytesArray); //read file into bytes[]
            fis.close();

            return bytesArray;
        } catch (Exception ex) {
            return null;
        }
    }

    public void askForPermissions() {

        askCompactPermissions(permissionsList, new PermissionResult() {
            @Override
            public void permissionGranted() {
                //permission granted
                //replace with your action
            }

            @Override
            public void permissionDenied() {
                //permission denied
                //replace with your action
                askForPermissions();
            }

            @Override
            public void permissionForeverDenied() {
                // user has check 'never ask again'
                // you need to open setting manually
                boolean isPermissionGranted = isPermissionsGranted(MainActivity.this, permissionsList);
                if (isPermissionGranted == false) {
                    DisplayHelper.showSnackbar(MainActivity.this, "Please select Permissions and enable all options");
                    openSettingsApp(MainActivity.this);
                }
            }
        });
    }

    public void takePhoto() {
        try {
            imageCaptured = false;
            File photoFile = createImageFile();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile);
            } else {
                File file = new File(photoFile.getPath());
                Uri photoUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".provider", file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (intent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        } catch (Exception ex) {

        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        //currentPhotoPath = "file:" + image.getAbsolutePath();
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        imageCaptured = false;
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                imageCaptured = true;
                currentPhotoPath = getRightAngleImage(currentPhotoPath);

                imageBitmap = new Resizer(context)
                        .setTargetLength(350)
                        .setSourceImage(new File(currentPhotoPath))
                        .getResizedBitmap();
            } catch (Exception e) {
                DisplayHelper.showToast(context,e.getMessage(),Toast.LENGTH_SHORT);
            }
        }
    }

    LocationListener locationListener = null;
    boolean isLocationRequested = false;

    public void findLocation(boolean showFetchingMessage) {
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                lm.removeUpdates(locationListener);
                fetchReports();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, locationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,10,locationListener);

        if(showFetchingMessage){
            DisplayHelper.showSnackbar(context,"Fetching new records");
        }

        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            DisplayHelper.showToast(context,"Please enable location services for reporting",Toast.LENGTH_SHORT);
            isLocationRequested = true;
            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isLocationRequested) {
            isLocationRequested = false;
            findLocation(false);
        }
    }

    public void fetchReports() {
        prgMain.setVisibility(View.VISIBLE);

        CustomRequest jsonReq = new CustomRequest(Request.Method.POST, Constants.SERVER_OPERATIONS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        prgMain.setVisibility(View.GONE);

                        try {
                            if (!response.getBoolean("error")) {
                                if(response.has("items")) {
                                    JSONArray itemsArray = response.getJSONArray("items");

                                    final int arrayLength = itemsArray.length();

                                    reportAdapter.clear();
                                    if (arrayLength > 0) {
                                        for (int i = 0; i < itemsArray.length(); i++) {
                                            JSONObject itemObj = (JSONObject) itemsArray.get(i);
                                            int id = itemObj.getInt("id");
                                            String distance = itemObj.getString("distance");
                                            String image = Constants.BASE_URL + itemObj.getString("img_path");
                                            long date_val = Long.parseLong(itemObj.getString("date_time")) * 1000;
                                            String location_str = itemObj.getString("location_str");
                                            Date date = new Date(date_val);

                                            PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
                                            String date_str =  prettyTime.format(date);

                                            reportAdapter.add(new OneReport(id,image,location_str,date_str,distance));
                                        }
                                    }
                                }
                            }

                        } catch (JSONException e) {

                            e.printStackTrace();

                        } finally {

                        }

                        DisplayHelper.showSnackbar(context,"Records updated!");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                prgMain.setVisibility(View.GONE);
                DisplayHelper.showSnackbar(context,error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("location",currentLocation.getLatitude() + "," + currentLocation.getLongitude());
                params.put("operation", Constants.METHOD_FETCH_REPORTS);

                return params;
            }
        };
        App.getInstance().addToRequestQueue(jsonReq);
    }

    private String getRightAngleImage(String photoPath) {

        try {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int degree = 0;

            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    degree = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                case ExifInterface.ORIENTATION_UNDEFINED:
                    degree = 0;
                    break;
                default:
                    degree = 90;
            }

            return rotateImage(degree,photoPath);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return photoPath;
    }

    private String rotateImage(int degree, String imagePath){

        if(degree<=0){
            return imagePath;
        }
        try{
            Bitmap b= BitmapFactory.decodeFile(imagePath);

            Matrix matrix = new Matrix();
            if(b.getWidth()>b.getHeight()){
                matrix.setRotate(degree);
                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(),
                        matrix, true);
            }

            FileOutputStream fOut = new FileOutputStream(imagePath);
            String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            String imageType = imageName.substring(imageName.lastIndexOf(".") + 1);

            FileOutputStream out = new FileOutputStream(imagePath);
            if (imageType.equalsIgnoreCase("png")) {
                b.compress(Bitmap.CompressFormat.PNG, 100, out);
            }else if (imageType.equalsIgnoreCase("jpeg")|| imageType.equalsIgnoreCase("jpg")) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            fOut.flush();
            fOut.close();

            b.recycle();
        }catch (Exception e){
            e.printStackTrace();
        }
        return imagePath;
    }
}

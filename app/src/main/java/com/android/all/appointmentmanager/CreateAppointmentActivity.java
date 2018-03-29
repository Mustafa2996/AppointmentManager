package com.android.all.appointmentmanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.all.appointmentmanager.Database.AppointmentRepository;
import com.android.all.appointmentmanager.Local.AppointmentDataSource;
import com.android.all.appointmentmanager.Local.AppointmentDatabase;
import com.android.all.appointmentmanager.Model.Appointment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by yuriyallakhverdov on 26.03.2018.
 */


public class CreateAppointmentActivity extends AppCompatActivity {

    private static final String TAG = "CreateAppointment";

    private static final String THESAURUS_ENDPOINT =
            "http://thesaurus.altervista.org/thesaurus/v1";

    EditText appointmentTitle;
    EditText appointmentTime;
    EditText appointmentDetails;
    Button btnThesaurusTyping;
    Button btnThesaurusHighlighting;
    TextView thesaurusDetails;
    Button saveAppointment;
    Intent initIntent;

    //Database
    private CompositeDisposable compositeDisposable;
    private AppointmentRepository appointmentRepository;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_appointment);
        initIntent = getIntent();

        //Database
        AppointmentDatabase appointmentDatabase = AppointmentDatabase.getInstance(this);
        appointmentRepository = AppointmentRepository.getInstance(AppointmentDataSource.getInstance(
                appointmentDatabase.appointmentDAO()));

        compositeDisposable = new CompositeDisposable();

        appointmentTitle = (EditText) findViewById(R.id.appointmentTitle);
        appointmentTime = (EditText) findViewById(R.id.appointmentTime);
        appointmentDetails = (EditText) findViewById(R.id.appointmentDetails);
        thesaurusDetails = (TextView) findViewById(R.id.thesaurusDetails);
        saveAppointment = (Button) findViewById(R.id.btnSaveAppointment);
        saveAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "in onClick appointmentDate = " +
                        initIntent.getStringExtra("Date") + ", appointmentTitle = " +
                        appointmentTitle.getText().toString());
                validateTitle(initIntent.getStringExtra("Date"),
                        appointmentTitle.getText().toString());
            }
        });

        btnThesaurusTyping = (Button) findViewById(R.id.btnThesaurusTyping);
        btnThesaurusTyping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findSynonyms(appointmentDetails.getText().toString());
            }
        });

        btnThesaurusHighlighting = (Button) findViewById(R.id.btnThesaurusHighlighting);
        btnThesaurusHighlighting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findSynonyms(getSelectedText());
            }
        });
    }

    private String getSelectedText() {
        int startSelection=appointmentDetails.getSelectionStart();
        int endSelection=appointmentDetails.getSelectionEnd();

        String selectedText =
                appointmentDetails.getText().toString().substring(startSelection, endSelection);
        return selectedText;
    }

    private void findSynonyms(final String word) {
        new AsyncTask<URL, Void, String>() {
            @Override
            protected String doInBackground(URL... urls) {
                // Create URL object
                URL url = null;
                try {
                    url = new URL(THESAURUS_ENDPOINT + "?word="+
                            URLEncoder.encode(word, "UTF-8") +
                            "&language=en_US&key=nxiY61lctTFLWJ6MEcFZ&output=json");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                // Perform HTTP request to the URL and receive a JSON response back
                String jsonResponse = "";
                try {
                    jsonResponse = makeHttpRequest(url);
                } catch (IOException e) {
                    // Handle the IOException
                    Log.e(TAG, "IOException is occurred", e);
                }

                // Extract relevant fields from the JSON response and create a String synonyms object
                String synonyms = extractSynonymsFromJson(jsonResponse);

                return synonyms;
            }

            @Override
            protected void onPostExecute(String synonyms) {
                if (synonyms == null) {
                    return;
                }

                updateUi(synonyms);
            }
        }.execute();
    }

    private void updateUi(String synonyms) {
        thesaurusDetails.setText(synonyms);
    }

    private String extractSynonymsFromJson(String jsonResponse) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            JSONObject baseJsonResponse = new JSONObject(jsonResponse);
            JSONArray synonymsArray = baseJsonResponse.getJSONArray("response");

            // If there are results in the features array
            if (synonymsArray.length() > 0) {

                for (int i=0; i < synonymsArray.length(); i++) {
                    JSONObject list = (JSONObject) ((JSONObject)synonymsArray.get(i)).get("list");
                    stringBuilder.append(list.get("category")+":"+list.get("synonyms"))
                    .append("\n");
                }
                return stringBuilder.toString();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Problem parsing the Thesaurus JSON results", e);
        }
        return null;
    }

    private String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        if (url != null) {
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                }

            } catch (IOException e) {
                // Handle the exception
                Log.e(TAG, "Status code " + urlConnection.getResponseCode(), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
        }
        return jsonResponse;
    }

    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    private void createPromptDialog() {
        new AlertDialog.Builder(CreateAppointmentActivity.this)
                .setMessage("Appointment " +
                        appointmentTitle.getText().toString() +
                        " already exists, please choose a different date or event title")
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(CreateAppointmentActivity.this,
                                        CalendarActivity.class);
                                startActivity(intent);

                            }
                        }).create().show();
    }

    private void validateTitle(final String date, final String title) {

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean isTitleValid = true;
                List<Appointment> appointmentList =
                        appointmentRepository.getAppointmentsByDate(date);
                for (Appointment appointment : appointmentList) {

                    if (appointment.getTitle().equals(title)) {
                        isTitleValid = false;
                        Log.d(TAG, "in onPostExecute appointmentDate = " +
                                appointment.getDate() + ", appointmentTitle = " +
                                appointment.getTitle() + " isTitleValid[0] = " +
                                isTitleValid);
                        break;
                    }
                }
                return isTitleValid;
            }

            @Override
            protected void onPostExecute(Boolean isTitleValid) {
                if (isTitleValid) {
                    addAppointment();
                } else {
                    createPromptDialog();
                }
            }
        }.execute();
    }

    private void addAppointment() {
        //Add new Appointment
        Disposable disposable = Observable.create(
                new ObservableOnSubscribe<Object>() {

                    @Override
                    public void subscribe(ObservableEmitter<Object> e) throws Exception {
                        Appointment appointment =
                                new Appointment(initIntent.getStringExtra("Date"),
                                        appointmentTime.getText().toString(),
                                        appointmentTitle.getText().toString(),
                                        appointmentDetails.getText().toString());

                        appointmentRepository.insertAppointment(appointment);
                        e.onComplete();
                    }
                }
        )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer() {
                               @Override
                               public void accept(Object o) throws Exception {
                                   Toast.makeText(CreateAppointmentActivity.this,
                                           "Appointment is added!", Toast.LENGTH_SHORT)
                                           .show();
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) throws Exception {
                                   Toast.makeText(CreateAppointmentActivity.this,
                                           "" + throwable.getMessage(), Toast.LENGTH_SHORT)
                                           .show();
                               }
                           }, new Action() {
                               @Override
                               public void run() throws Exception {
                                   Intent intent = new Intent(CreateAppointmentActivity.this,
                                           ListActivity.class);
                                   startActivity(intent);
                               }
                           }

                );
        compositeDisposable.add(disposable);
    }


}


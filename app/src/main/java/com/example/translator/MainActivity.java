package com.example.translator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //Widgets
    ImageView imageView;
    Button chooseBtn, readBtn, trnslBTN;
    TextView textView;
    EditText inputText;
    Spinner fromSpinner, toSpinner;
    LinearLayout edtxContainer;

    //nothing wll change

    //Variables
    InputImage inputImage;
    TextRecognizer recognizer;
    TextToSpeech textToSpeech;

    public Bitmap imgBitmap;

    //Spinner items
    String[] fromLanguage = {
            "from", "English", "Bengali", "Hindi", "Urdu", "German", "Afrikaans", "Arabic", "Catalan", "Belarusian"
    };

    String[] toLanguage = {
            "from", "English", "Bengali", "Hindi", "Urdu", "German", "Afrikaans", "Arabic", "Catalan", "Belarusian"
    };

    private static final int PICK_IMAGE = 1;
    String languageCode, fromLangCode, toLangCode;
    private static final int REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        imageView = findViewById(R.id.mainImg);
        inputText = findViewById(R.id.inputText);
        chooseBtn = findViewById(R.id.chooseBtn);
        readBtn = findViewById(R.id.readBtn);
        textView = findViewById(R.id.transl_text);

        fromSpinner = findViewById(R.id.fromSpinner);
        toSpinner = findViewById(R.id.toSpinner);

        edtxContainer = findViewById(R.id.edtxContainer);



        imageView.setVisibility(View.GONE);
        chooseBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

                   OpenGallery();
           }
       });

       textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
           @Override
           public void onInit(int status) {
               if (status != TextToSpeech.ERROR){

                   textToSpeech.setLanguage(Locale.US);

               }
           }
       });

       readBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               textToSpeech.speak(textView.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
           }
       });


       //Spinner Settings
       fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               fromLangCode = GetLanguage(fromLanguage[position]);

//               TranslateLanguages(fromLangCode, toLangCode, "Tarnslating Here");

           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });

        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);



        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLangCode = GetLanguage(toLanguage[position]);

                TranslateLanguages(fromLangCode, toLangCode, inputText.getText().toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);


//        trnslBTN = findViewById(R.id.trnslBTN);
//
//        trnslBTN.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (inputText.getText().toString().isEmpty()) {
//                    Toast.makeText(MainActivity.this, "Please enter your text", Toast.LENGTH_SHORT).show();
//                }
//                else if (fromLangCode.isEmpty()){
//                    Toast.makeText(MainActivity.this, "Please Select source language", Toast.LENGTH_SHORT).show();
//                }
//                else if (toLangCode.isEmpty()){
//                    Toast.makeText(MainActivity.this, "Please select Target Language", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    TranslateLanguages(fromLangCode, toLangCode, inputText.getText().toString());
//                }
//            }
//        });

        inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                TranslateLanguages(fromLangCode, toLangCode, inputText.getText().toString());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




    }

    private void TranslateLanguages(String fromLangCode, String toLangCode, String sourceText) {

        textView.setText("Downloading Language Model");

        try {
            TranslatorOptions options = new TranslatorOptions.Builder()
                    .setSourceLanguage(fromLangCode)
                    .setTargetLanguage(toLangCode)
                    .build();


            Translator translator  = Translation.getClient(options);

            DownloadConditions conditions = new DownloadConditions.Builder().build();

            translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                    textView.setText("Translating...");

                    translator.translate(sourceText).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            textView.setText(s);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Failed to Translate", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Failed to Download", Toast.LENGTH_SHORT).show();
                }
            });


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void OpenGallery() {

        edtxContainer.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);

        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooseImg = Intent.createChooser(getIntent, "Choose Image");
        chooseImg.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});


        startActivityForResult(chooseImg, PICK_IMAGE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE){

            if (data != null){

                Byte[] arrayByte = new Byte[0];
                String filePath = null;

                try {
                    inputImage = InputImage.fromFilePath(this, data.getData());
                    Bitmap imgUri = inputImage.getBitmapInternal();

                    Glide.with(MainActivity.this)
                            .load(imgUri)
                            .into(imageView);



                    Task<Text> result = recognizer.process(inputImage).addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(Text text) {
                            ProcessTexts(text);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Recognize Failed", Toast.LENGTH_SHORT).show();
                        }
                    });


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }




    }

//  Process Language
    private void ProcessTexts(Text text) {

        StringBuilder resultText = new StringBuilder();

        for (Text.TextBlock block : text.getTextBlocks()){

            String blockText = block.getText();
            Point[] blockCorner = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            textView.append("\n");



            for (Text.Line line : block.getLines()){

                String lineText = line.getText();
                Point[] lineCorner = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                textView.append("\n");



                for (Text.Element element : line.getElements()){

                    textView.append(" ");
                    String elementText = element.getText();
                    Point[] elmntText = element.getCornerPoints();
                    Rect elmnt = element.getBoundingBox();

                    resultText.append(element.getText()).append(" ");
                }

                inputText.setText(resultText.toString());


            }

        }

    }


    //Get Language
    private String GetLanguage(String language) {

        String languageCodes;

        switch (language){
            case "English":
                languageCodes = TranslateLanguage.ENGLISH;
                break;
            case "Bengali":
                languageCodes = TranslateLanguage.BENGALI;
                break;
            case "Hindi":
                languageCodes = TranslateLanguage.HINDI;
                break;
            case "Urdu":
                languageCodes = TranslateLanguage.URDU;
                break;
            case "German":
                languageCodes = TranslateLanguage.GERMAN;
                break;
            case "Afrikaans":
                languageCodes = TranslateLanguage.GERMAN;
                break;
            case "Arabic":
                languageCodes = TranslateLanguage.ARABIC;
                break;
            case "Catalan":
                languageCodes = TranslateLanguage.CATALAN;
                break;
            case "Belarusian":
                languageCodes = TranslateLanguage.BELARUSIAN;
                break;
            default:
                languageCodes = " ";
        }

        return languageCodes;
    }
}
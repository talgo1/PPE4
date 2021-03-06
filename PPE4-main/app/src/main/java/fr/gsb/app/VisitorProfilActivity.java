package fr.gsb.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class VisitorProfilActivity extends AppCompatActivity {

    private Button btn_dcnx;
    private Button btn_praticien;
    private Button btn_rdv;
    private Button btn_frais;
    private Button btn_profil;
    private TextView tv_ident;

    private FirebaseUser userFB;

    private EditText et_name;
    private EditText et_firstName;
    private EditText et_city;
    private EditText et_postalCode;
    private EditText et_phone;
    private EditText et_currentPassword;
    private EditText et_newPassword;
    private Button btn_submit;
    private Button btn_update_password;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visitor_profil);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        btn_dcnx = findViewById(R.id.deconnexion);
        btn_praticien = findViewById(R.id.praticien);
        btn_rdv = findViewById(R.id.rdv);
        btn_frais = findViewById(R.id.frais);
        btn_profil = findViewById(R.id.profil);
        tv_ident = findViewById(R.id.tv_ident);

        Intent i_recu = getIntent();
        User currentUser = (User) i_recu.getSerializableExtra("currentUser");
        tv_ident.setText(currentUser.getName() + " " + currentUser.getFirstName());

        // attribution des EditText
        et_name = findViewById(R.id.et_name);
        et_firstName = findViewById(R.id.et_firstName);
        et_city = findViewById(R.id.et_city);
        et_postalCode = findViewById(R.id.et_postalCode);
        et_phone = findViewById(R.id.et_phone);
        et_currentPassword = findViewById(R.id.et_currentPassword);
        et_newPassword = findViewById(R.id.et_newPassword);
        btn_submit = findViewById(R.id.btn_submit);
        btn_update_password = findViewById(R.id.btn_update_password);

        // Set des EditText
        et_name.setText(currentUser.getName());
        et_firstName.setText(currentUser.getFirstName());
        et_city.setText(currentUser.getCity());
        et_postalCode.setText(currentUser.getPostalCode());
        et_phone.setText(currentUser.getPhone());


        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Si les champs ont bien tous ??t?? renseign??s (sauf les champs mot de passe)
                if (et_name.getText().length()!=0 && et_firstName.getText().length()!=0 && et_city.getText().length()!=0 &&
                        et_postalCode.getText().length()!=0 && et_phone.getText().length()!=0){
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("name", et_name.getText().toString());
                    userMap.put("firstName", et_firstName.getText().toString());
                    userMap.put("city", et_city.getText().toString());
                    userMap.put("postalCode", et_postalCode.getText().toString());
                    userMap.put("phone", et_phone.getText().toString());
                    userMap.put("role", currentUser.getRole());



                    // update de User dans FireCloud
                    db.collection("users")
                            .document(currentUser.getId())
                            .set(userMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("FIREC", "DocumentSnapshot successfully written!");
                                    Toast.makeText(VisitorProfilActivity.this, "Changement(s) effectu??(s)",
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("FIREC", "Error writing document", e);
                                    Toast.makeText(VisitorProfilActivity.this, "Il y a une erreur quelque part, veuillez r??essayer.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        btn_update_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Si les champs mot de passe sont remplis
                if (et_currentPassword.getText().length()!=0 && et_newPassword.getText().length()>=5){
                    userFB = FirebaseAuth.getInstance().getCurrentUser();
                    final String email = userFB.getEmail();

                    String old_password = et_currentPassword.getText().toString();
                    String new_password = et_newPassword.getText().toString();

                    // initialisation connexion au compte
                    AuthCredential credential = EmailAuthProvider.getCredential(email,old_password);

                    // update du mot de passe
                    userFB.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                userFB.updatePassword(new_password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()){
                                            Toast.makeText(VisitorProfilActivity.this, "Il y a une erreur quelque part, veuillez r??essayer.",
                                                    Toast.LENGTH_SHORT).show();
                                        }else {
                                            Toast.makeText(VisitorProfilActivity.this, "Mot de passe mis ?? jour.",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    });

                }
            }
        });

        btn_dcnx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent connexion = new Intent(VisitorProfilActivity.this, MainActivity.class);
                connexion.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(connexion);
            }
        });
        btn_praticien.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent praticien = new Intent(VisitorProfilActivity.this, VisitorPractitionersActivity.class);
                praticien.putExtra("currentUser", currentUser);
                startActivity(praticien);
            }
        });

        btn_rdv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent rdv = new Intent(VisitorProfilActivity.this, VisitorCalendarActivity.class);
                rdv.putExtra("currentUser", currentUser);
                startActivity(rdv);
            }
        });

        btn_frais.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent frais = new Intent(VisitorProfilActivity.this, VisitorExpenseFormActivity.class);
                frais.putExtra("currentUser", currentUser);
                startActivity(frais);
            }
        });
    }
}
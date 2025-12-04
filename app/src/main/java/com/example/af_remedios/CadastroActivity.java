package com.example.af_remedios;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class CadastroActivity extends AppCompatActivity {

    EditText edtNome, edtDesc, edtHora;
    Button btnSalvar;
    DatabaseReference dbRef;
    String idAtual = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        dbRef = FirebaseDatabase.getInstance().getReference("Medicamentos");

        edtNome = findViewById(R.id.edtNome);
        edtDesc = findViewById(R.id.edtDesc);
        edtHora = findViewById(R.id.edtHora);
        btnSalvar = findViewById(R.id.btnSalvar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            idAtual = extras.getString("id");
            edtNome.setText(extras.getString("nome"));
            edtDesc.setText(extras.getString("desc"));
            edtHora.setText(extras.getString("hora"));
        }

        edtHora.setOnClickListener(v -> abrirRelogio());

        btnSalvar.setOnClickListener(v -> salvar());
    }

    private void abrirRelogio() {
        Calendar calendario = Calendar.getInstance();
        int horaAtual = calendario.get(Calendar.HOUR_OF_DAY);
        int minutoAtual = calendario.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String horaFormatada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    edtHora.setText(horaFormatada);
                },
                horaAtual, minutoAtual, true);

        timePickerDialog.show();
    }

    private void salvar() {
        String nome = edtNome.getText().toString();
        String desc = edtDesc.getText().toString();
        String hora = edtHora.getText().toString();

        if (nome.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Preencha nome e horário!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (idAtual == null) {
            idAtual = dbRef.push().getKey();
        }

        Medicamento med = new Medicamento(idAtual, nome, desc, hora, false);

        dbRef.child(idAtual).setValue(med)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CadastroActivity.this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show();
                    agendarAlarme(nome, hora);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CadastroActivity.this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void agendarAlarme(String nome, String horario) {
        try {
            String[] split = horario.split(":");
            int h = Integer.parseInt(split[0]);
            int m = Integer.parseInt(split[1]);

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, h);
            cal.set(java.util.Calendar.MINUTE, m);
            cal.set(java.util.Calendar.SECOND, 0);

            if (cal.getTimeInMillis() < System.currentTimeMillis()) {
                cal.add(java.util.Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("msg", "Tomar " + nome);


            int uniqueId = nome.hashCode();

            PendingIntent pi = PendingIntent.getBroadcast(this, uniqueId, intent, PendingIntent.FLAG_IMMUTABLE);
            android.app.AlarmManager am = (android.app.AlarmManager) getSystemService(Context.ALARM_SERVICE);

            if (am != null) {
                try {

                    am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                } catch (SecurityException e) {
                    am.setExact(android.app.AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
                }
            }

            Toast.makeText(this, "Lembrete agendado para " + horario, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao agendar.", Toast.LENGTH_SHORT).show();
        }
    }

}
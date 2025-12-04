package com.example.af_remedios;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    DatabaseReference dbRef;
    MedicamentoAdapter adapter;
    List<Medicamento> lista;
    TextView txtInfoGremio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtInfoGremio = findViewById(R.id.txtInfoGremio);
        carregarInfoGremio();

        dbRef = FirebaseDatabase.getInstance().getReference("Medicamentos");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lista = new ArrayList<>();
        adapter = new MedicamentoAdapter(this, lista, dbRef);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> startActivity(new Intent(this, CadastroActivity.class)));

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lista.clear();
                for (DataSnapshot s : snapshot.getChildren()) {
                    try {
                        Medicamento m = s.getValue(Medicamento.class);
                        if (m != null) {
                            lista.add(m);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Erro ao ler banco: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void carregarInfoGremio() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://pt.wikipedia.org/api/rest_v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WikiApi api = retrofit.create(WikiApi.class);
        api.getGremioInfo().enqueue(new Callback<WikiResponse>() {
            @Override
            public void onResponse(Call<WikiResponse> call, Response<WikiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String textoCompleto = response.body().getExtract();
                    String resumo = textoCompleto.length() > 150 ? textoCompleto.substring(0, 150) + "..." : textoCompleto;
                    txtInfoGremio.setText("Você sabia? " + resumo);
                } else {
                    txtInfoGremio.setText("Erro na API: Código " + response.code());
                    Log.e("API_ERRO", "Erro: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<WikiResponse> call, Throwable t) {
                txtInfoGremio.setText("Erro ao carregar infos do Grêmio.");
            }
        });
    }
}
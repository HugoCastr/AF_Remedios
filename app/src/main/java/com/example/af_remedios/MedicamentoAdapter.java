package com.example.af_remedios;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import java.util.List;

public class MedicamentoAdapter extends RecyclerView.Adapter<MedicamentoAdapter.ViewHolder> {

    private Context context;
    private List<Medicamento> lista;
    private DatabaseReference dbRef;

    public MedicamentoAdapter(Context context, List<Medicamento> lista, DatabaseReference dbRef) {
        this.context = context;
        this.lista = lista;
        this.dbRef = dbRef;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medicamento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicamento med = lista.get(position);
        holder.txtNome.setText(med.getNome());
        holder.txtHorario.setText(med.getHorario());

        holder.chkTomado.setOnCheckedChangeListener(null);
        holder.chkTomado.setChecked(med.isTomado());
        holder.itemView.setBackgroundColor(med.isTomado() ? Color.parseColor("#C8E6C9") : Color.WHITE);

        holder.chkTomado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            med.setTomado(isChecked);
            dbRef.child(med.getId()).setValue(med);
        });

        // Clique Curto: Editar
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CadastroActivity.class);
            intent.putExtra("id", med.getId());
            intent.putExtra("nome", med.getNome());
            intent.putExtra("desc", med.getDescricao());
            intent.putExtra("hora", med.getHorario());
            context.startActivity(intent);
        });

        // Clique Longo: Excluir
        holder.itemView.setOnLongClickListener(v -> {
            dbRef.child(med.getId()).removeValue();
            return true;
        });
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtHorario;
        CheckBox chkTomado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.itemNome);
            txtHorario = itemView.findViewById(R.id.itemHorario);
            chkTomado = itemView.findViewById(R.id.chkTomado);
        }
    }
}
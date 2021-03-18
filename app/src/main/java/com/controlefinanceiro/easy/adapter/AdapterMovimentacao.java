package com.controlefinanceiro.easy.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.controlefinanceiro.easy.R;
import com.controlefinanceiro.easy.config.ConfiguracaoFirebase;
import com.controlefinanceiro.easy.helper.DateCustom;
import com.controlefinanceiro.easy.model.Movimentacao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;


public class AdapterMovimentacao extends RecyclerView.Adapter<AdapterMovimentacao.MyViewHolder> {

    List<Movimentacao> movimentacoes;
    Context context;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuario;
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();

    private Double despesaTotal;
    private Double receitaTotal;
    private Double resumoUsuario;




    public AdapterMovimentacao(List<Movimentacao> movimentacoes, Context context) {
        this.movimentacoes = movimentacoes;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_movimentacao, parent, false);
        return new MyViewHolder(itemLista);
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Movimentacao movimentacao = movimentacoes.get(position);

        holder.titulo.setText(movimentacao.getDescricao());
        holder.titulo.setTextColor(context.getResources().getColor(R.color.colorBlack));

        holder.valor.setText(String.valueOf(movimentacao.getValor()));
        holder.valor.setTextColor(context.getResources().getColor(R.color.colorAccentReceita));

        holder.categoria.setText(movimentacao.getCategoria());
        holder.categoria.setTextColor(context.getResources().getColor(R.color.colorBlack));

        holder.data.setText(movimentacao.getData());
        holder.data.setTextColor(context.getResources().getColor(R.color.colorBlack));

        String pegaDataAtual = DateCustom.mesAnoDataEscolhida(DateCustom.dataAtual());
        int dataAtual = Integer.parseInt(pegaDataAtual);
        String pegadataDoisDigitos = DateCustom.mesAnoDataEscolhida(movimentacao.getData());
        int pegaData = Integer.parseInt(pegadataDoisDigitos);

        //data atual cehga assim 032021
        //int dataAtual = 42021;
        Log.i("DATA", "Data atual" + dataAtual);
        Log.i("DATA", "Data atual" + pegaData);

        if (movimentacao.getTipo() == "d" || movimentacao.getTipo().equals("d") && pegaData <= dataAtual) {
            holder.valor.setTextColor(context.getResources().getColor(R.color.colorAccent));
            holder.valor.setText("-" + movimentacao.getValor());

        }

        if (pegaData > dataAtual){

            holder.valor.setTextColor(context.getResources().getColor(R.color.colorGray));
            holder.data.setTextColor(context.getResources().getColor(R.color.colorGray));
            holder.titulo.setTextColor(context.getResources().getColor(R.color.colorGray));
            holder.categoria.setTextColor(context.getResources().getColor(R.color.colorGray));
            //  holder.valor.setText("-" + movimentacao.getValor());

            if (movimentacao.getTipo() == "d" || movimentacao.getTipo().equals("d") && pegaData > dataAtual) {
                holder.valor.setText("-" + movimentacao.getValor());


            }

        }

    }


    @Override
    public int getItemCount() {
        return movimentacoes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titulo, valor, categoria, data;

        public MyViewHolder(View itemView) {
            super(itemView);

            titulo = itemView.findViewById(R.id.textAdapterTitulo);
            valor = itemView.findViewById(R.id.textAdapterValor);
            categoria = itemView.findViewById(R.id.textAdapterCategoria);
            data = itemView.findViewById(R.id.textAdapterData);

            Log.i("F","Funcionaou aqui");
        }

    }

}
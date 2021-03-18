package com.controlefinanceiro.easy.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.controlefinanceiro.easy.R;
import com.controlefinanceiro.easy.adapter.AdapterMovimentacao;
import com.controlefinanceiro.easy.config.ConfiguracaoFirebase;
import com.controlefinanceiro.easy.helper.Base64Custon;
import com.controlefinanceiro.easy.helper.DateCustom;
import com.controlefinanceiro.easy.model.Movimentacao;
import com.controlefinanceiro.easy.model.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PrincipalActivity extends AppCompatActivity {
    private FirebaseAuth autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
    private MaterialCalendarView calendarView;
    private TextView campoSaldo, campoSaldacao;
    private DatabaseReference firebaseRef = ConfiguracaoFirebase.getFirebaseDatabase();
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerUsuario;
    private ValueEventListener valueEventListenerMovimentacoes;
    private AdapterMovimentacao adapterMovimentacao;
    private List<Movimentacao> movimentacoes = new ArrayList<>();
    private Movimentacao movimentacao;
    private Usuario usuario;

    private RecyclerView recyclerView;

    private Double despesaTotal = 0.0;
    private Double receitaTotal = 0.0;
    private Double resumoUsuario = 0.0;
    private Double valorReceita = 0.0;
    private Double valorDespesa = 0.0;
    private Double vReceita = 0.0;
    private String key;
    private Double somaReceita = 0.0;
    private Double somaDespesa = 0.0;
    private String flag;



     private DatabaseReference movimentacaoRef;
    private String mesAnoSelecionado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("EASY");
        setSupportActionBar(toolbar);

        campoSaldacao = findViewById(R.id.textSaudacao);
        campoSaldo = findViewById(R.id.textSaldo);
        calendarView = findViewById(R.id.calendarView);
        recyclerView = findViewById(R.id.recyclerMovimentos);
        configuraCalendaryView();
        swipe();

        //configurar o adapter
        adapterMovimentacao = new AdapterMovimentacao(movimentacoes, this);

        //configurar o recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter( adapterMovimentacao );


    }

    public void swipe() {
        ItemTouchHelper.Callback itemTouch = new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

                int dragflags = ItemTouchHelper.ACTION_STATE_IDLE;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragflags, swipeFlags);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                excluirMovimentacao(viewHolder);
            }

        };
        new ItemTouchHelper(itemTouch).attachToRecyclerView(recyclerView);
    }

    public void pegarValorMov(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custon.codificarBase64(emailUsuario);
        movimentacaoRef = firebaseRef.child("movimentacao").child( idUsuario).child( mesAnoSelecionado );

        valueEventListenerMovimentacoes = movimentacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                  movimentacoes.clear();
                   for (DataSnapshot dados: snapshot.getChildren() ){
                       Movimentacao movimentacao = dados.getValue(Movimentacao.class);
                       movimentacao.setKey(dados.getKey());
                       movimentacoes.add(movimentacao);

                       //retornando o nó principal de cada movimentacao
                        key = dados.getKey();
                        if (movimentacao.getTipo().equals("r") && movimentacao.getFlag().equals("noDebitado")){

                               //movimentacaoRef.child(key).child("flag").setValue("debitado");
                               valorReceita = movimentacao.getValor();//recupera 100
                               Log.i("VALOR", "Valor movimentacao receita " + valorReceita);

                           }


                          if (movimentacao.getTipo().equals("d") && movimentacao.getFlag().equals("noDebitado")){


                           Log.i("VALOR", "Valor movimentacao despesa " + movimentacao.getValor());

                           valorDespesa = movimentacao.getValor();//recupera 200

                           //movimentacaoRef.child(key).child("flag").setValue("debitado");
                           somaDespesa = somaDespesa + valorDespesa;
                           Log.i("VALOR", "Valor despesa somado = -" + somaDespesa);
                           Log.i("VALOR", "flag  " + movimentacao.getFlag());
                         }


                   }
                //somaValoresReceita(somaReceita);



                adapterMovimentacao.notifyDataSetChanged();

               }



            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
    public void somaValoresReceita(Double valor){



}

    public void atualizarDespesa( Double despesa){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custon.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);
       // despesa = despesa + usuario.getDespesaTotal();
        usuario = new Usuario();
       /* Toast.makeText(PrincipalActivity.this,
                "despesa do usuario recuperada = -" + usuario.getDespesaTotal(),
                Toast.LENGTH_SHORT).show();*/

       // usuarioRef.child("despesaTotal").setValue(despesa);
    }

    public void atualizarReceita( Double receita){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custon.codificarBase64(emailUsuario);
        DatabaseReference usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

       valorReceita = receita;


        valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                valorReceita = valorReceita + usuario.getReceitaTotal();
                usuarioRef.child("receitaTotal").setValue(valorReceita);


             /*  DecimalFormat decimalFormat = new DecimalFormat("0.##");
                String resultadoformatado = decimalFormat.format(resumoUsuario);
                campoSaldacao.setText("Olá, " + usuario.getNome());
                campoSaldo.setText("R$ " + resultadoformatado);*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void excluirMovimentacao(RecyclerView.ViewHolder viewHolder) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Excluir movimentação da conta");
        alertDialog.setMessage("Voce tem certeza que deseja realmente excluir essa movimentação?");
        alertDialog.setCancelable(false);

        alertDialog.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int position = viewHolder.getAdapterPosition();
                movimentacao = movimentacoes.get(position);

                String emailUsuario = autenticacao.getCurrentUser().getEmail();
                String idUsuario = Base64Custon.codificarBase64(emailUsuario);
                movimentacaoRef = firebaseRef.child("movimentacao")
                        .child(idUsuario)
                        .child(mesAnoSelecionado);

                movimentacaoRef.child(movimentacao.getKey()).removeValue();
                adapterMovimentacao.notifyItemRemoved(position);

                String pegaDataAtual = DateCustom.mesAnoDataEscolhida(DateCustom.dataAtual());
                int dataAtual = Integer.parseInt(pegaDataAtual);
                String pegadataDoisDigitos = DateCustom.mesAnoDataEscolhida(movimentacao.getData());
                int pegaData = Integer.parseInt(pegadataDoisDigitos);


                if (pegaData <= dataAtual) {
                    atualizarSaldo();
                }
            }
        });

        alertDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PrincipalActivity.this, "Cancelado", Toast.LENGTH_SHORT).show();
                adapterMovimentacao.notifyDataSetChanged();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    //cria o menu superior sair
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //programa as acoes dos itens de menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSair:
                autenticacao.signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;

            case R.id.menuLancarFuturo:

                startActivity(new Intent(this, LancamentoFuturoActivity.class));
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }



    public void atualizarSaldo() {

        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custon.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        if (movimentacao.getTipo().equals("r")) {
            receitaTotal = receitaTotal - movimentacao.getValor();
            usuarioRef.child("receitaTotal").setValue(receitaTotal);
        }

        if (movimentacao.getTipo().equals("d")) {
            despesaTotal = despesaTotal - movimentacao.getValor();
            usuarioRef.child("despesaTotal").setValue(despesaTotal);
        }
    }

    public void recuperarMovimentacao(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custon.codificarBase64(emailUsuario);
        movimentacaoRef = firebaseRef.child("movimentacao")
                                     .child( idUsuario)
                                     .child( mesAnoSelecionado );
            Log.i("Ms", "Mes ano selecionado " + mesAnoSelecionado);

        valueEventListenerMovimentacoes = movimentacaoRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                movimentacoes.clear();
                for (DataSnapshot dados: snapshot.getChildren() ){
                    Movimentacao movimentacao = dados.getValue(Movimentacao.class);
                    movimentacao.setKey(dados.getKey());
                    movimentacoes.add(movimentacao);
                }

                adapterMovimentacao.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    public void recuperaResumo(){
        String emailUsuario = autenticacao.getCurrentUser().getEmail();
        String idUsuario = Base64Custon.codificarBase64(emailUsuario);
        usuarioRef = firebaseRef.child("usuarios").child(idUsuario);

        Log.i("Evento", "Evento foi adicionado!");
        valueEventListenerUsuario = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                    despesaTotal = usuario.getDespesaTotal();
                    receitaTotal = usuario.getReceitaTotal();
                    resumoUsuario = receitaTotal - despesaTotal;


                DecimalFormat decimalFormat = new DecimalFormat("0.##");
                String resultadoformatado = decimalFormat.format(resumoUsuario);
                campoSaldacao.setText("Olá, " + usuario.getNome());
                campoSaldo.setText("R$ " + resultadoformatado);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void adicionarDespesas(View view){
        startActivity(new Intent(this, DespesasActivity.class));
    }

    public void adicionarReceita(View view){
        startActivity(new Intent(this, ReceitasActivity.class));
    }

    public void configuraCalendaryView(){
        CharSequence meses[] = {"JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO", "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO"};
        calendarView.setTitleMonths(meses);

        CalendarDay dataAtual = calendarView.getCurrentDate();

        String mesSelecionado = String.format("%02d", (dataAtual.getMonth() + 1));
        mesAnoSelecionado = String.valueOf( mesSelecionado + "" + dataAtual.getYear());

        calendarView.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {
                String mesSelecionado = String.format("%02d", (date.getMonth() + 1));
                mesAnoSelecionado = String.valueOf (mesSelecionado + "" + date.getYear());
                movimentacaoRef.removeEventListener(valueEventListenerMovimentacoes);
                recuperarMovimentacao();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperaResumo();
        recuperarMovimentacao();
        pegarValorMov();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Evento", "Evento foi removido!");
        usuarioRef.removeEventListener( valueEventListenerUsuario );
        movimentacaoRef.removeEventListener(valueEventListenerMovimentacoes);
    }
}